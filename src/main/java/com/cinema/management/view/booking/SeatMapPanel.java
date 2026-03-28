package com.cinema.management.view.booking;

import com.cinema.management.controller.BookingController;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.SeatStatusDto.Status;
import com.cinema.management.view.component.SeatButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel hiển thị Sơ đồ ghế theo GridLayout (FR-ST-01).
 *
 * Chức năng:
 *  - Vẽ lại sơ đồ mỗi khi showTime thay đổi hoặc refresh.
 *  - Click ghế AVAILABLE → lockSeat() → chuyển màu SELECTED.
 *  - Click ghế SELECTED  → unlockSeat() → chuyển màu AVAILABLE.
 *  - Timer 1 giây refresh đếm ngược thời gian lock (BR-03, BR-04).
 *  - Gọi callback onSelectionChanged khi danh sách ghế chọn thay đổi.
 */
public class SeatMapPanel extends JPanel {

    private final BookingController bookingController;
    private final String            currentUserId;

    private String showTimeId;

    /** Map seatId → SeatButton để cập nhật nhanh không cần vẽ lại toàn bộ. */
    private final Map<String, SeatButton> buttonMap = new LinkedHashMap<>();

    // ── UI components ────────────────────────────────────────────────────────
    private final JPanel     gridPanel    = new JPanel();
    private final JLabel     lblTimer     = new JLabel("⏱ --:--");
    private final JLabel     lblSelected  = new JLabel("Đang chọn: 0 ghế");
    private final JLabel     lblTotal     = new JLabel("Tổng ghế: 0");
    private final JButton    btnRefresh   = new JButton("🔄 Làm mới");
    private final JButton    btnCancelAll = new JButton("✖ Hủy tất cả");

    /** Callback về BookingPanel khi danh sách ghế đang chọn thay đổi. */
    private Consumer<List<SeatStatusDto>> onSelectionChanged;

    /** Swing Timer để refresh đếm ngược 15 phút. */
    private javax.swing.Timer countdownTimer;

    // ── Legend colors ────────────────────────────────────────────────────────
    private static final Color COLOR_AVAILABLE = new Color(39, 174, 96);
    private static final Color COLOR_SELECTED  = new Color(41, 128, 185);
    private static final Color COLOR_LOCKED    = new Color(230, 126, 34);
    private static final Color COLOR_BOOKED    = new Color(149, 165, 166);

    public SeatMapPanel(BookingController bookingController, String currentUserId) {
        this.bookingController = bookingController;
        this.currentUserId     = currentUserId;

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildScrollGrid(),BorderLayout.CENTER);
        add(buildLegend(),    BorderLayout.SOUTH);

        startCountdownTimer();
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public void setOnSelectionChanged(Consumer<List<SeatStatusDto>> callback) {
        this.onSelectionChanged = callback;
    }

    /** Tải sơ đồ ghế cho suất chiếu mới. */
    public void loadShowTime(String showTimeId) {
        this.showTimeId = showTimeId;
        refreshSeatMap();
    }

    /** Refresh toàn bộ sơ đồ (sau timeout hoặc khi cần sync). */
    public void refreshSeatMap() {
        if (showTimeId == null) return;
        List<SeatStatusDto> statuses = bookingController.getSeatStatuses(showTimeId, currentUserId);
        renderGrid(statuses);
        updateStatusBar(statuses);
    }

    /** Trả về danh sách ghế đang ở trạng thái SELECTED. */
    public List<SeatStatusDto> getSelectedSeats() {
        List<SeatStatusDto> selected = new ArrayList<>();
        for (SeatButton btn : buttonMap.values()) {
            if (btn.getStatus() == Status.SELECTED) {
                selected.add(btn.getSeatStatus());
            }
        }
        return selected;
    }

    // ── Build UI ─────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(Color.WHITE);

        // Trái: thông tin
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        left.setBackground(Color.WHITE);
        lblSelected.setFont(new Font("Arial", Font.BOLD, 13));
        lblSelected.setForeground(new Color(41, 128, 185));
        lblTotal.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTotal.setForeground(Color.GRAY);
        left.add(lblSelected);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(lblTotal);
        bar.add(left, BorderLayout.WEST);

        // Phải: timer + nút
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        right.setBackground(Color.WHITE);
        lblTimer.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTimer.setForeground(new Color(192, 57, 43));
        lblTimer.setToolTipText("Thời gian còn lại trước khi ghế tự động nhả");

        styleSmallButton(btnRefresh,   new Color(52, 152, 219));
        styleSmallButton(btnCancelAll, new Color(192, 57, 43));
        btnRefresh.addActionListener(e   -> refreshSeatMap());
        btnCancelAll.addActionListener(e -> cancelAllSeats());

        right.add(lblTimer);
        right.add(btnRefresh);
        right.add(btnCancelAll);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildScrollGrid() {
        gridPanel.setBackground(new Color(30, 30, 30));
        gridPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94), 2));
        scroll.getViewport().setBackground(new Color(30, 30, 30));
        return scroll;
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        legend.setBackground(new Color(245, 245, 245));
        legend.setBorder(new TitledBorder("Chú thích"));
        legend.add(buildLegendItem("Trống",        COLOR_AVAILABLE));
        legend.add(buildLegendItem("Đang chọn",    COLOR_SELECTED));
        legend.add(buildLegendItem("Người khác giữ", COLOR_LOCKED));
        legend.add(buildLegendItem("Đã bán",       COLOR_BOOKED));
        return legend;
    }

    private JPanel buildLegendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setBackground(new Color(245, 245, 245));
        JPanel box = new JPanel();
        box.setBackground(color);
        box.setPreferredSize(new Dimension(20, 16));
        box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        item.add(box);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        item.add(lbl);
        return item;
    }

    // ── Render grid ──────────────────────────────────────────────────────────

    private void renderGrid(List<SeatStatusDto> statuses) {
        buttonMap.clear();
        gridPanel.removeAll();

        if (statuses.isEmpty()) {
            gridPanel.setLayout(new BorderLayout());
            JLabel empty = new JLabel("Không có ghế nào trong phòng này.", SwingConstants.CENTER);
            empty.setForeground(Color.LIGHT_GRAY);
            empty.setFont(new Font("Arial", Font.ITALIC, 14));
            gridPanel.add(empty, BorderLayout.CENTER);
            gridPanel.revalidate();
            gridPanel.repaint();
            return;
        }

        // Nhóm ghế theo hàng để tính số cột
        Map<String, List<SeatStatusDto>> byRow = new LinkedHashMap<>();
        for (SeatStatusDto s : statuses) {
            byRow.computeIfAbsent(s.getRowChar(), k -> new ArrayList<>()).add(s);
        }
        int maxCols = byRow.values().stream().mapToInt(List::size).max().orElse(1);
        // +1 cho cột nhãn hàng
        gridPanel.setLayout(new GridLayout(byRow.size(), maxCols + 1, 4, 4));

        // Màn hình chiếu ở trên
        addScreenLabel(maxCols + 1);

        for (Map.Entry<String, List<SeatStatusDto>> entry : byRow.entrySet()) {
            // Nhãn hàng (A, B, C...)
            JLabel rowLabel = new JLabel(entry.getKey(), SwingConstants.CENTER);
            rowLabel.setForeground(Color.LIGHT_GRAY);
            rowLabel.setFont(new Font("Arial", Font.BOLD, 12));
            gridPanel.add(rowLabel);

            for (SeatStatusDto dto : entry.getValue()) {
                SeatButton btn = new SeatButton(dto);
                btn.addActionListener(e -> onSeatClick(btn));
                buttonMap.put(dto.getSeatId(), btn);
                gridPanel.add(btn);
            }
            // Điền ô trống nếu hàng có ít ghế hơn maxCols
            int fill = maxCols - entry.getValue().size();
            for (int i = 0; i < fill; i++) {
                JPanel empty = new JPanel();
                empty.setBackground(new Color(30, 30, 30));
                gridPanel.add(empty);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    /** Dải màn hình chiếu trên cùng sơ đồ. */
    private void addScreenLabel(int cols) {
        // Thêm ô trống để căn hàng đầu
        for (int i = 0; i < cols; i++) {
            if (i == cols / 2) {
                JLabel screen = new JLabel("══════ MÀN HÌNH ══════", SwingConstants.CENTER);
                screen.setForeground(new Color(241, 196, 15));
                screen.setFont(new Font("Arial", Font.BOLD, 11));
                gridPanel.add(screen);
            } else {
                JPanel empty = new JPanel();
                empty.setBackground(new Color(30, 30, 30));
                gridPanel.add(empty);
            }
        }
    }

    // ── Event: click ghế ────────────────────────────────────────────────────

    private void onSeatClick(SeatButton btn) {
        if (showTimeId == null) return;
        String seatId = btn.getSeatId();
        Status currentStatus = btn.getStatus();

        try {
            if (currentStatus == Status.AVAILABLE) {
                // Khóa ghế → chuyển SELECTED
                bookingController.lockSeat(showTimeId, seatId, currentUserId);
            } else if (currentStatus == Status.SELECTED) {
                // Nhả ghế → chuyển AVAILABLE
                bookingController.unlockSeat(showTimeId, seatId, currentUserId);
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Ghế không khả dụng", JOptionPane.WARNING_MESSAGE);
        }

        // Refresh chỉ nút vừa click để tránh vẽ lại toàn bộ grid
        refreshSingleButton(seatId);
        notifySelectionChanged();
    }

    /** Cập nhật 1 button dựa trên trạng thái mới từ service. */
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
        int c = JOptionPane.showConfirmDialog(this,
                "Hủy tất cả ghế đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        bookingController.unlockAllSeats(showTimeId, currentUserId);
        refreshSeatMap();
        notifySelectionChanged();
    }

    // ── Countdown timer ───────────────────────────────────────────────────────

    private void startCountdownTimer() {
        countdownTimer = new javax.swing.Timer(1000, e -> {
            if (showTimeId == null) { lblTimer.setText("⏱ --:--"); return; }
            long secs = bookingController.getSecondsUntilExpiry(showTimeId, currentUserId);
            if (secs < 0) {
                lblTimer.setText("⏱ --:--");
                lblTimer.setForeground(new Color(149, 165, 166));
            } else {
                long min = secs / 60;
                long sec = secs % 60;
                lblTimer.setText(String.format("⏱ %02d:%02d", min, sec));
                // Đỏ cảnh báo khi còn < 3 phút
                lblTimer.setForeground(secs < 180 ? new Color(192, 57, 43) : new Color(39, 174, 96));
                // Hết hạn → refresh để nhả ghế
                if (secs <= 0) {
                    refreshSeatMap();
                    notifySelectionChanged();
                    JOptionPane.showMessageDialog(this,
                            "Thời gian giữ ghế đã hết!\nCác ghế đã được tự động nhả.",
                            "Hết thời gian", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        countdownTimer.start();
    }

    /** Gọi khi panel bị destroy để dừng timer tránh memory leak. */
    public void dispose() {
        if (countdownTimer != null) countdownTimer.stop();
    }

    // ── Status bar update ─────────────────────────────────────────────────────

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

    // ── Utility ──────────────────────────────────────────────────────────────

    private void styleSmallButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 28));
    }
}