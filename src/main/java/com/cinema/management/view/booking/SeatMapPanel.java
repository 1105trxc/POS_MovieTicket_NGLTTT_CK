package com.cinema.management.view.booking;

import com.cinema.management.controller.BookingController;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.SeatStatusDto.Status;
import com.cinema.management.view.component.SeatButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SeatMapPanel extends JPanel {

    private final BookingController bookingController;
    private final String currentUserId;
    private String showTimeId;
    private final Map<String, SeatButton> buttonMap = new LinkedHashMap<>();

    // ── UI components ──
    private final JPanel mapWrapperPanel = new JPanel(new BorderLayout(0, 20));
    private final JPanel gridPanel = new JPanel();
    private final JLabel lblTimer = new JLabel("⏱ --:--");
    private final JLabel lblSelected = new JLabel("Đang chọn: 0 ghế");
    private final JLabel lblTotal = new JLabel("Tổng ghế: 0");
    private final JButton btnRefresh = new JButton("🔄 Làm mới");
    private final JButton btnCancelAll = new JButton("✖ Hủy tất cả");

    private Consumer<List<SeatStatusDto>> onSelectionChanged;
    private Consumer<SeatStatusDto> onProcessingSeatClicked;
    private javax.swing.Timer countdownTimer;

    // Bảng màu giống với SeatButton
    private static final Color COLOR_AVAILABLE = new Color(255, 255, 255);
    private static final Color COLOR_SELECTED = new Color(14, 165, 233);
    private static final Color COLOR_LOCKED = new Color(245, 158, 11);
    private static final Color COLOR_BOOKED = new Color(148, 163, 184);

    public SeatMapPanel(BookingController bookingController, String currentUserId) {
        this.bookingController = bookingController;
        this.currentUserId = currentUserId;

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildScrollGrid(), BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);

        startCountdownTimer();
    }

    public void setOnSelectionChanged(Consumer<List<SeatStatusDto>> callback) {
        this.onSelectionChanged = callback;
    }

    public void setOnProcessingSeatClicked(Consumer<SeatStatusDto> callback) {
        this.onProcessingSeatClicked = callback;
    }

    public void loadShowTime(String showTimeId) {
        this.showTimeId = showTimeId;
        refreshSeatMap();
    }

    public void refreshSeatMap() {
        if (showTimeId == null) return;
        List<SeatStatusDto> statuses = bookingController.getSeatStatuses(showTimeId, currentUserId);
        renderGrid(statuses);
        updateStatusBar(statuses);
    }

    public List<SeatStatusDto> getSelectedSeats() {
        return buttonMap.values().stream()
                .filter(btn -> btn.getStatus() == Status.SELECTED)
                .map(SeatButton::getSeatStatus)
                .collect(Collectors.toList());
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(Color.WHITE);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        left.setBackground(Color.WHITE);
        lblSelected.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelected.setForeground(COLOR_SELECTED);
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotal.setForeground(Color.GRAY);
        left.add(lblSelected);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(lblTotal);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        right.setBackground(Color.WHITE);
        lblTimer.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTimer.setForeground(new Color(239, 68, 68)); // Đỏ

        styleSmallButton(btnRefresh, new Color(100, 116, 139));
        styleSmallButton(btnCancelAll, new Color(239, 68, 68));
        btnRefresh.addActionListener(e -> refreshSeatMap());
        btnCancelAll.addActionListener(e -> cancelAllSeats());

        right.add(lblTimer);
        right.add(Box.createHorizontalStrut(10));
        right.add(btnRefresh);
        right.add(btnCancelAll);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildScrollGrid() {
        mapWrapperPanel.setBackground(new Color(241, 245, 249));
        mapWrapperPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Vẽ màn hình
        JLabel screenLabel = new JLabel("═════════════ MÀN HÌNH ═════════════", SwingConstants.CENTER);
        screenLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        screenLabel.setForeground(new Color(148, 163, 184));
        mapWrapperPanel.add(screenLabel, BorderLayout.NORTH);

        gridPanel.setBackground(new Color(241, 245, 249));

        // Wrap gridPanel vào FlowLayout để nó tự căn giữa
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setBackground(new Color(241, 245, 249));
        centerWrapper.add(gridPanel);

        mapWrapperPanel.add(centerWrapper, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(mapWrapperPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scroll.getViewport().setBackground(new Color(241, 245, 249));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildLegend() {
        // Tăng khoảng cách và cấu hình lại Wrap layout để chứa nhiều chú thích hơn
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        legend.setBackground(Color.WHITE);
        legend.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(5, 0, 0, 0)));

        // Chú thích Loại ghế (Trạng thái Available)
        legend.add(buildLegendItem("Thường", COLOR_AVAILABLE, true));
        legend.add(buildLegendItem("VIP", new Color(254, 205, 211), false));
        legend.add(buildLegendItem("Couple/Sofa", new Color(233, 213, 255), false));

        // Dấu gạch đứng phân cách
        legend.add(new JLabel(" | "));

        // Chú thích Trạng thái
        legend.add(buildLegendItem("Đang chọn", COLOR_SELECTED, false));
        legend.add(buildLegendItem("Đang xử lý", COLOR_LOCKED, false));
        legend.add(buildLegendItem("Đã bán", COLOR_BOOKED, false));

        return legend;
    }

    private JPanel buildLegendItem(String label, Color color, boolean hasBorder) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setBackground(Color.WHITE);
        JPanel box = new JPanel();
        box.setBackground(color);
        box.setPreferredSize(new Dimension(20, 20));
        if (hasBorder) {
            box.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 2));
        } else {
            box.setBorder(BorderFactory.createLineBorder(color.darker(), 2));
        }
        item.add(box);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.add(lbl);
        return item;
    }

    // ── THUẬT TOÁN VẼ LƯỚI TỰ ĐỘNG CĂN CHỈNH LỐI ĐI VÀ GHẾ SOFA ──
    private void renderGrid(List<SeatStatusDto> statuses) {
        buttonMap.clear();
        gridPanel.removeAll();

        if (statuses.isEmpty()) {
            gridPanel.setLayout(new BorderLayout());
            JLabel empty = new JLabel("Sơ đồ phòng chiếu chưa được thiết lập.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            gridPanel.add(empty, BorderLayout.CENTER);
            revalidateAndRepaint();
            return;
        }

        int maxSeatNum = statuses.stream().mapToInt(SeatStatusDto::getSeatNumber).max().orElse(1);

        Map<String, List<SeatStatusDto>> byRow = new LinkedHashMap<>();
        for (SeatStatusDto s : statuses) {
            byRow.computeIfAbsent(s.getRowChar(), k -> new ArrayList<>()).add(s);
        }

        // Chuyển sang GridBagLayout để hỗ trợ nối ô (Visual Spanning)
        gridPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4); // Khoảng cách 8px (4+4) giữa các ghế
        gc.fill = GridBagConstraints.BOTH;

        int rowY = 0;
        for (Map.Entry<String, List<SeatStatusDto>> entry : byRow.entrySet()) {
            // Cột 0: Label hàng ghế (A, B, C...)
            gc.gridy = rowY;
            gc.gridx = 0;
            gc.gridwidth = 1;
            JLabel rowLabel = new JLabel(entry.getKey(), SwingConstants.CENTER);
            rowLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            rowLabel.setForeground(new Color(71, 85, 105));
            rowLabel.setPreferredSize(new Dimension(30, 45));
            gridPanel.add(rowLabel, gc);

            Map<Integer, SeatStatusDto> seatByNum = entry.getValue().stream()
                    .collect(Collectors.toMap(SeatStatusDto::getSeatNumber, s -> s));

            // Duyệt từ 1 đến maxSeatNum để đặt ghế
            for (int colX = 1; colX <= maxSeatNum; colX++) {
                gc.gridx = colX;

                if (seatByNum.containsKey(colX)) {
                    SeatStatusDto s = seatByNum.get(colX);
                    String type = s.getSeatTypeName().toLowerCase();
                    boolean isCouple = type.contains("couple") || type.contains("sofa") || type.contains("sweetbox");

                    if (isCouple) {
                        // NẾU LÀ GHẾ ĐÔI -> Cấp cho nó 2 cột (gridwidth = 2)
                        gc.gridwidth = 2;
                        SeatButton btn = new SeatButton(s, true); // true = Sofa mode
                        btn.addActionListener(e -> onSeatClick(btn));
                        buttonMap.put(btn.getSeatId(), btn);
                        gridPanel.add(btn, gc);

                        // Mẹo quan trọng: Nhảy qua cột tiếp theo vì ghế sofa đã chiếm 2 ô không gian
                        colX++;
                    } else {
                        // NẾU LÀ GHẾ THƯỜNG -> Cấp 1 cột (gridwidth = 1)
                        gc.gridwidth = 1;
                        SeatButton btn = new SeatButton(s, false); // false = Normal mode
                        btn.addActionListener(e -> onSeatClick(btn));
                        buttonMap.put(btn.getSeatId(), btn);
                        gridPanel.add(btn, gc);
                    }
                } else {
                    // Ô LỐI ĐI (Trống) -> Vẽ một khối tàng hình để đẩy khoảng cách
                    gc.gridwidth = 1;
                    JPanel emptySpace = new JPanel();
                    emptySpace.setOpaque(false);
                    emptySpace.setPreferredSize(new Dimension(45, 45));
                    gridPanel.add(emptySpace, gc);
                }
            }
            rowY++;
        }

        revalidateAndRepaint();
    }

    private void revalidateAndRepaint() {
        gridPanel.revalidate();
        gridPanel.repaint();
        mapWrapperPanel.revalidate();
        mapWrapperPanel.repaint();
    }

    private void onSeatClick(SeatButton btn) {
        if (showTimeId == null) return;
        String seatId = btn.getSeatId();
        Status currentStatus = btn.getStatus();

        try {
            if (currentStatus == Status.AVAILABLE) {
                bookingController.lockSeat(showTimeId, seatId, currentUserId);
            } else if (currentStatus == Status.SELECTED) {
                bookingController.unlockSeat(showTimeId, seatId, currentUserId);
            } else if (currentStatus == Status.PROCESSING) {
                if (onProcessingSeatClicked != null) {
                    onProcessingSeatClicked.accept(btn.getSeatStatus());
                }
                return;
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }

        refreshSingleButton(seatId);
        notifySelectionChanged();
    }

    private void refreshSingleButton(String seatId) {
        List<SeatStatusDto> all = bookingController.getSeatStatuses(showTimeId, currentUserId);
        all.stream()
                .filter(s -> s.getSeatId().equals(seatId))
                .findFirst()
                .ifPresent(dto -> {
                    SeatButton btn = buttonMap.get(seatId);
                    if (btn != null) btn.updateStatus(dto);
                });
        updateStatusBar(all);
    }

    private void cancelAllSeats() {
        if (showTimeId == null) return;
        if (getSelectedSeats().isEmpty()) return;

        int c = JOptionPane.showConfirmDialog(this,
                "Hủy tất cả ghế đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;

        bookingController.unlockAllSeats(showTimeId, currentUserId);
        refreshSeatMap();
        notifySelectionChanged();
    }

    private void startCountdownTimer() {
        countdownTimer = new javax.swing.Timer(1000, e -> {
            if (showTimeId == null) {
                lblTimer.setText("⏱ --:--");
                return;
            }
            long secs = bookingController.getSecondsUntilExpiry(showTimeId, currentUserId);

            if (secs < 0) {
                lblTimer.setText("⏱ --:--");
                lblTimer.setForeground(new Color(148, 163, 184)); // Xám
            } else {
                long min = secs / 60;
                long sec = secs % 60;
                lblTimer.setText(String.format("⏱ %02d:%02d", min, sec));

                // Đỏ nhấp nháy khi còn dưới 2 phút
                if (secs <= 120 && secs % 2 == 0) {
                    lblTimer.setForeground(new Color(239, 68, 68));
                } else {
                    lblTimer.setForeground(new Color(15, 23, 42)); // Đen
                }

                // Hết hạn
                if (secs == 0) {
                    refreshSeatMap();
                    notifySelectionChanged();
                    JOptionPane.showMessageDialog(this,
                            "Thời gian giữ ghế đã hết!\nCác ghế của bạn đã được tự động nhả.",
                            "Hết thời gian", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        countdownTimer.start();
    }

    public void dispose() {
        if (countdownTimer != null) countdownTimer.stop();
    }

    private void updateStatusBar(List<SeatStatusDto> statuses) {
        long selectedCount = statuses.stream().filter(s -> s.getStatus() == Status.SELECTED).count();
        lblSelected.setText("Đang chọn: " + selectedCount + " ghế");
        lblTotal.setText("Tổng ghế: " + statuses.size());
    }

    private void notifySelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(getSelectedSeats());
        }
    }

    private void styleSmallButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 32));
    }
}
