package com.cinema.management.view.booking;

import com.cinema.management.controller.BookingController;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Product;
import com.cinema.management.model.entity.ShowTime;
import com.formdev.flatlaf.extras.FlatSVGIcon; // Thêm thư viện SVG Icon

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BookingPanel extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Bảng màu chuẩn hệ thống ───────────────────────────────────────────────
    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59); // Dark Slate
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    // ── Controller & Session ──────────────────────────────────────────────────
    private final BookingController bookingController;
    private final String currentUserId;

    // ── Components: Sơ đồ ghế ─────────────────────────────────────────────────
    private final JPanel seatMapContainer = new JPanel(new BorderLayout());
    private SeatMapPanel seatMapPanel;

    // ── Components: Ma trận Suất chiếu (Showtime Matrix) ──────────────────────
    private final JPanel pnlDates = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final JPanel pnlMovies = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final JPanel pnlTimes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    private List<ShowTime> allShowTimes;
    private LocalDate selectedDate;
    private String selectedMovieId;
    private String currentShowTimeId;

    // ── Components: Tóm tắt đơn (EAST Panel) ──────────────────────────────────
    private final DefaultListModel<String> seatListModel = new DefaultListModel<>();
    private final JList<String> seatList = new JList<>(seatListModel);
    private final JLabel lblSeatTotal = new JLabel("0 VNĐ");

    private final Map<String, int[]> fbqMap = new LinkedHashMap<>();
    private final DefaultListModel<String> fbListModel = new DefaultListModel<>();
    private final JList<String> fbList = new JList<>(fbListModel);
    private final JLabel lblFbTotal = new JLabel("0 VNĐ");
    private final JLabel lblGrandTotal = new JLabel("0 VNĐ");

    private final JButton btnCheckout = new JButton(" Thanh toán  (F5)");
    private final JButton btnReset = new JButton(" Hủy đơn");

    private Runnable onProceedToCheckout;

    public BookingPanel(String currentUserId) {
        this.currentUserId = currentUserId;
        this.bookingController = new BookingController();

        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);

        loadMatrixData();
        bindHotkeys();
    }

    public void setOnProceedToCheckout(Runnable callback) {
        this.onProceedToCheckout = callback;
    }

    public void refreshShowTimeList() {
        loadMatrixData();
    }

    public String getCurrentShowTimeId() {
        return currentShowTimeId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public List<SeatStatusDto> getCurrentSelectedSeats() {
        return seatMapPanel != null ? seatMapPanel.getSelectedSeats() : Collections.emptyList();
    }

    public Map<String, Integer> getCurrentFbItems() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> e : fbqMap.entrySet()) {
            result.put(e.getKey(), e.getValue()[0]);
        }
        return result;
    }

    public BigDecimal getCurrentFbTotal() {
        BigDecimal fbSum = BigDecimal.ZERO;
        List<Product> allProducts = bookingController.getAllProducts();
        for (Map.Entry<String, int[]> entry : fbqMap.entrySet()) {
            String pid = entry.getKey();
            int qty = entry.getValue()[0];
            Product p = allProducts.stream()
                    .filter(x -> x.getProductId().equals(pid))
                    .findFirst()
                    .orElse(null);
            if (p != null) {
                fbSum = fbSum.add(p.getCurrentPrice().multiply(BigDecimal.valueOf(qty)));
            }
        }
        return fbSum;
    }

    // ── Hàm tiện ích tải SVG Icon ─────────────────────────────────────────────
    private Icon createIcon(String path, int size, Color color) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon(path, size, size);
            // Đổi màu icon sang màu trắng để nổi bật trên nền nút xanh/đỏ
            if (color != null) {
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
            }
            return icon;
        } catch (Exception e) {
            return null; // Fallback an toàn nếu chưa copy file SVG
        }
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HEADER);
        h.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("BÁN VÉ – POS (KIOSK MODE)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setIcon(createIcon("icons/wallet.svg", 24, Color.WHITE)); // Thêm icon cho Title
        h.add(title, BorderLayout.WEST);

        JButton btnRefreshMatrix = new JButton(" Làm mới lịch chiếu");
        btnRefreshMatrix.setIcon(createIcon("icons/refresh.svg", 16, Color.WHITE)); // Icon Refresh
        styleBtn(btnRefreshMatrix, PRIMARY, Color.WHITE);
        btnRefreshMatrix.addActionListener(e -> loadMatrixData());
        h.add(btnRefreshMatrix, BorderLayout.EAST);

        return h;
    }

    private JPanel buildMainArea() {
        JPanel area = new JPanel(new BorderLayout(15, 15));
        area.setBorder(new EmptyBorder(15, 15, 15, 15));
        area.setBackground(BG);

        // Khối bên trái: Matrix (Trên) + Sơ đồ ghế (Dưới)
        JPanel leftSide = new JPanel(new BorderLayout(0, 15));
        leftSide.setOpaque(false);
        leftSide.add(buildShowtimeMatrix(), BorderLayout.NORTH);

        seatMapContainer.setBackground(CARD);
        seatMapContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JLabel hint = new JLabel("← Chọn suất chiếu ở trên để hiển thị sơ đồ ghế", SwingConstants.CENTER);
        hint.setForeground(new Color(148, 163, 184));
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        seatMapContainer.add(hint, BorderLayout.CENTER);

        leftSide.add(seatMapContainer, BorderLayout.CENTER);

        area.add(leftSide, BorderLayout.CENTER);
        area.add(buildOrderPanel(), BorderLayout.EAST);

        return area;
    }

    private JPanel buildShowtimeMatrix() {
        JPanel matrix = new JPanel(new GridLayout(3, 1, 0, 10));
        matrix.setBackground(CARD);
        matrix.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(15, 15, 15, 15)
        ));

        pnlDates.setOpaque(false);
        pnlMovies.setOpaque(false);
        pnlTimes.setOpaque(false);

        matrix.add(buildMatrixRow("Ngày:", pnlDates));
        matrix.add(buildMatrixRow("Phim:", pnlMovies));
        matrix.add(buildMatrixRow("Suất:", pnlTimes));

        return matrix;
    }

    private JPanel buildMatrixRow(String title, JPanel buttonFlowPanel) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(HEADER);
        lblTitle.setPreferredSize(new Dimension(60, 36));

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(buttonFlowPanel, BorderLayout.WEST);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);

        row.add(lblTitle, BorderLayout.WEST);
        row.add(scroll, BorderLayout.CENTER);

        return row;
    }

    private JPanel buildOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG);
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ── Ghế đã chọn ──
        JPanel seatSection = new JPanel(new BorderLayout(0, 4));
        seatSection.setBackground(CARD);
        seatSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createTitledBorder(null, "  Ghế đang chọn  ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), PRIMARY)
        ));
        seatList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        seatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane seatScroll = new JScrollPane(seatList);
        seatScroll.setPreferredSize(new Dimension(380, 150));
        seatScroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        seatSection.add(seatScroll, BorderLayout.CENTER);

        JPanel seatFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        seatFooter.setOpaque(false);
        seatFooter.add(new JLabel("Tiền ghế:"));
        lblSeatTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSeatTotal.setForeground(PRIMARY);
        seatFooter.add(lblSeatTotal);
        seatSection.add(seatFooter, BorderLayout.SOUTH);

        // ── F&B ──
        JPanel fbSection = new JPanel(new BorderLayout(0, 4));
        fbSection.setBackground(CARD);
        fbSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createTitledBorder(null, "  F&B đã chọn  ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), WARNING)
        ));
        fbList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane fbScroll = new JScrollPane(fbList);
        fbScroll.setPreferredSize(new Dimension(380, 120));
        fbScroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        fbSection.add(fbScroll, BorderLayout.CENTER);

        JButton btnAddFb = new JButton(" Thêm bắp/nước");
        btnAddFb.setIcon(createIcon("icons/plus.svg", 16, Color.WHITE)); // Icon Plus
        styleBtn(btnAddFb, WARNING, Color.WHITE);
        btnAddFb.setPreferredSize(new Dimension(170, 34));
        btnAddFb.addActionListener(e -> showAddFbDialog());

        JPanel fbFooter = new JPanel(new BorderLayout());
        fbFooter.setBorder(new EmptyBorder(5, 5, 5, 5));
        fbFooter.setOpaque(false);
        fbFooter.add(btnAddFb, BorderLayout.WEST);

        JPanel fbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fbRight.setOpaque(false);
        fbRight.add(new JLabel("Tiền F&B:"));
        lblFbTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFbTotal.setForeground(WARNING);
        fbRight.add(lblFbTotal);
        fbFooter.add(fbRight, BorderLayout.EAST);
        fbSection.add(fbFooter, BorderLayout.SOUTH);

        // ── Tổng + nút ──
        JPanel totalPanel = new JPanel(new GridBagLayout());
        totalPanel.setBackground(CARD);
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        gc.gridx = 0;
        gc.gridy = 0;
        JLabel lblTotalTitle = new JLabel("TỔNG CỘNG");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalPanel.add(lblTotalTitle, gc);

        gc.gridx = 1;
        gc.anchor = GridBagConstraints.EAST;
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGrandTotal.setForeground(DANGER);
        lblGrandTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        totalPanel.add(lblGrandTotal, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 2;
        gc.insets = new Insets(15, 0, 8, 0);
        btnCheckout.setIcon(createIcon("icons/wallet.svg", 20, Color.WHITE)); // Icon Thanh Toán
        styleBtn(btnCheckout, SUCCESS, Color.WHITE);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCheckout.setPreferredSize(new Dimension(0, 50));
        btnCheckout.addActionListener(e -> proceedToCheckout());
        totalPanel.add(btnCheckout, gc);

        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 0, 0);
        btnReset.setIcon(createIcon("icons/trash.svg", 16, Color.WHITE)); // Icon Hủy (Thùng rác)
        styleBtn(btnReset, new Color(100, 116, 139), Color.WHITE);
        btnReset.setPreferredSize(new Dimension(0, 38));
        btnReset.addActionListener(e -> resetOrder());
        totalPanel.add(btnReset, gc);

        JPanel sections = new JPanel();
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
        sections.setOpaque(false);
        sections.add(seatSection);
        sections.add(Box.createVerticalStrut(15));
        sections.add(fbSection);

        panel.add(sections, BorderLayout.CENTER);
        panel.add(totalPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ── Logic Data Matrix & Vẽ Nút Custom ─────────────────────────────────────

    private void loadMatrixData() {
        LocalDateTime now = LocalDateTime.now();
        allShowTimes = bookingController.getAllShowTimes().stream()
                .filter(st -> {
                    // Thời hạn chót để mua vé = Giờ bắt đầu + 30 phút
                    java.time.LocalDateTime cutoffTime = st.getStartTime().plusMinutes(30);
                    // Chỉ giữ lại những suất chiếu chưa vượt quá thời hạn chót
                    return !cutoffTime.isBefore(now);
                })
                .collect(Collectors.toList());
        selectedDate = null;
        selectedMovieId = null;
        currentShowTimeId = null;
        renderDateRow();
    }

    private void renderDateRow() {
        pnlDates.removeAll();
        if (allShowTimes == null || allShowTimes.isEmpty()) {
            pnlMovies.removeAll();
            pnlMovies.revalidate();
            pnlMovies.repaint();
            pnlTimes.removeAll();
            pnlTimes.revalidate();
            pnlTimes.repaint();
            return;
        }

        List<LocalDate> dates = allShowTimes.stream()
                .map(st -> st.getStartTime().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        ButtonGroup bg = new ButtonGroup();
        for (LocalDate date : dates) {
            String label = (date.equals(LocalDate.now())) ? "Hôm nay, " + date.format(DateTimeFormatter.ofPattern("dd/MM"))
                    : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            JToggleButton btn = createMatrixButton(label);
            bg.add(btn);
            btn.addActionListener(e -> {
                selectedDate = date;
                renderMovieRow();
            });
            pnlDates.add(btn);
        }

        if (!dates.isEmpty()) {
            JToggleButton first = (JToggleButton) pnlDates.getComponent(0);
            first.setSelected(true);
            selectedDate = dates.get(0);
            renderMovieRow();
        }
        pnlDates.revalidate();
        pnlDates.repaint();
    }

    private void renderMovieRow() {
        pnlMovies.removeAll();
        if (selectedDate == null) return;

        List<ShowTime> showsOnDate = allShowTimes.stream()
                .filter(st -> st.getStartTime().toLocalDate().equals(selectedDate))
                .toList();

        List<Movie> movies = showsOnDate.stream()
                .map(ShowTime::getMovie)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Movie::getMovieId, m -> m, (m1, m2) -> m1))
                .values().stream().toList();

        ButtonGroup bg = new ButtonGroup();
        for (Movie m : movies) {
            JToggleButton btn = createMatrixButton(m.getTitle());
            bg.add(btn);
            btn.addActionListener(e -> {
                selectedMovieId = m.getMovieId();
                renderTimeRow();
            });
            pnlMovies.add(btn);
        }

        if (!movies.isEmpty()) {
            JToggleButton first = (JToggleButton) pnlMovies.getComponent(0);
            first.setSelected(true);
            selectedMovieId = movies.get(0).getMovieId();
            renderTimeRow();
        } else {
            pnlTimes.removeAll();
            pnlTimes.revalidate();
            pnlTimes.repaint();
        }
        pnlMovies.revalidate();
        pnlMovies.repaint();
    }

    private void renderTimeRow() {
        pnlTimes.removeAll();
        if (selectedDate == null || selectedMovieId == null) return;

        List<ShowTime> shows = allShowTimes.stream()
                .filter(st -> st.getStartTime().toLocalDate().equals(selectedDate))
                .filter(st -> st.getMovie() != null && st.getMovie().getMovieId().equals(selectedMovieId))
                .sorted(Comparator.comparing(ShowTime::getStartTime))
                .toList();

        ButtonGroup bg = new ButtonGroup();
        for (ShowTime st : shows) {
            String timeStr = st.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            String roomStr = st.getRoom() != null ? st.getRoom().getRoomName() : "R?";
            JToggleButton btn = createMatrixButton(timeStr + " - " + roomStr);
            bg.add(btn);
            btn.addActionListener(e -> {
                currentShowTimeId = st.getShowTimeId();
                loadSeatMap();
            });
            pnlTimes.add(btn);
        }
        pnlTimes.revalidate();
        pnlTimes.repaint();
    }

    // 🔥 Code fix lỗi chữ Trắng nền Trắng & Tự vẽ UI Bo góc đẹp mắt
    private JToggleButton createMatrixButton(String text) {
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected()) {
                    g2.setColor(PRIMARY);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(224, 242, 254));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setFont(getFont());
                if (isSelected()) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(71, 85, 105));
                }
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(PRIMARY.darker());
                } else {
                    g2.setColor(new Color(203, 213, 225));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);

        FontMetrics fm = btn.getFontMetrics(btn.getFont());
        int textWidth = fm.stringWidth(text);
        btn.setPreferredSize(new Dimension(textWidth + 32, 36));

        return btn;
    }

    private void loadSeatMap() {
        if (currentShowTimeId == null) return;
        if (seatMapPanel != null) seatMapPanel.dispose();

        seatMapPanel = new SeatMapPanel(bookingController, currentUserId);
        seatMapPanel.setOnSelectionChanged(this::onSeatSelectionChanged);
        seatMapPanel.loadShowTime(currentShowTimeId);

        seatMapContainer.removeAll();
        seatMapContainer.add(seatMapPanel, BorderLayout.CENTER);
        seatMapContainer.revalidate();
        seatMapContainer.repaint();

        fbqMap.clear();
        refreshFbList();
        refreshTotals(Collections.emptyList());
    }

    private void onSeatSelectionChanged(List<SeatStatusDto> selected) {
        seatListModel.clear();
        BigDecimal seatTotal = BigDecimal.ZERO;
        for (SeatStatusDto s : selected) {
            String price = String.format("%,.0f", s.getBasePrice());
            seatListModel.addElement(String.format(" %-6s [%-10s]  %s VNĐ", s.getLabel(), s.getSeatTypeName(), price));
            seatTotal = seatTotal.add(s.getBasePrice());
        }
        lblSeatTotal.setText(String.format("%,.0f VNĐ", seatTotal));
        refreshTotals(selected);
    }

    // ── F&B Dialog ────────────────────────────────────────────────────────────

    private void showAddFbDialog() {
        List<Product> products = bookingController.getAllProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có sản phẩm F&B nào.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn Bắp/Nước", true);
        dlg.setLayout(new BorderLayout(15, 15));
        dlg.setSize(450, 350);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG);
        ((JPanel) dlg.getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        DefaultListModel<String> model = new DefaultListModel<>();
        for (Product p : products) {
            String price = String.format("%,.0f", p.getCurrentPrice());
            model.addElement(" " + p.getProductName() + "  –  " + price + " VNĐ");
        }
        JList<String> productList = new JList<>(model);
        productList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.setSelectionBackground(new Color(224, 242, 254));

        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        dlg.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottom.setOpaque(false);

        JLabel lblQty = new JLabel("Số lượng:");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bottom.add(lblQty);

        SpinnerNumberModel qtyModel = new SpinnerNumberModel(1, 1, 20, 1);
        JSpinner spinQty = new JSpinner(qtyModel);
        spinQty.setPreferredSize(new Dimension(80, 36));
        spinQty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bottom.add(spinQty);

        JButton btnAdd = new JButton(" Thêm vào đơn");
        btnAdd.setIcon(createIcon("icons/check.svg", 16, Color.WHITE)); // Icon Check
        styleBtn(btnAdd, SUCCESS, Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(160, 36));
        btnAdd.addActionListener(e -> {
            int idx = productList.getSelectedIndex();
            if (idx < 0) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn sản phẩm.");
                return;
            }
            Product p = products.get(idx);
            int qty = (int) spinQty.getValue();
            fbqMap.merge(p.getProductId(), new int[]{qty}, (a, b) -> new int[]{a[0] + b[0]});
            refreshFbListWithProducts(products);
            dlg.dispose();
        });
        bottom.add(btnAdd);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void refreshFbListWithProducts(List<Product> allProducts) {
        fbListModel.clear();
        BigDecimal fbTotal = BigDecimal.ZERO;
        for (Map.Entry<String, int[]> entry : fbqMap.entrySet()) {
            String pid = entry.getKey();
            int qty = entry.getValue()[0];
            Product p = allProducts.stream()
                    .filter(x -> x.getProductId().equals(pid))
                    .findFirst().orElse(null);
            if (p == null) continue;
            BigDecimal lineTotal = p.getCurrentPrice().multiply(BigDecimal.valueOf(qty));
            fbListModel.addElement("  " + p.getProductName() + " x" + qty + "  =  " + String.format("%,.0f", lineTotal) + " VNĐ");
            fbTotal = fbTotal.add(lineTotal);
        }
        lblFbTotal.setText(String.format("%,.0f VNĐ", fbTotal));
        refreshTotals(seatMapPanel != null ? seatMapPanel.getSelectedSeats() : Collections.emptyList());
    }

    private void refreshFbList() {
        fbListModel.clear();
        lblFbTotal.setText("0 VNĐ");
    }

    // ── Totals & Actions ──────────────────────────────────────────────────────

    private void refreshTotals(List<SeatStatusDto> selectedSeats) {
        BigDecimal seatSum = selectedSeats.stream()
                .map(SeatStatusDto::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fbSum = BigDecimal.ZERO;
        List<Product> allProducts = bookingController.getAllProducts();
        for (Map.Entry<String, int[]> entry : fbqMap.entrySet()) {
            String pid = entry.getKey();
            int qty = entry.getValue()[0];
            Product p = allProducts.stream().filter(x -> x.getProductId().equals(pid)).findFirst().orElse(null);
            if (p != null) {
                fbSum = fbSum.add(p.getCurrentPrice().multiply(BigDecimal.valueOf(qty)));
            }
        }
        BigDecimal grand = seatSum.add(fbSum);
        lblGrandTotal.setText(String.format("%,.0f VNĐ", grand));
    }

    private void proceedToCheckout() {
        if (seatMapPanel == null || seatMapPanel.getSelectedSeats().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn ít nhất 1 ghế trước khi thanh toán.",
                    "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onProceedToCheckout != null) {
            onProceedToCheckout.run();
        }
    }

    private void resetOrder() {
        if (seatMapPanel != null && currentShowTimeId != null) {
            bookingController.unlockAllSeats(currentShowTimeId, currentUserId);
            seatMapPanel.refreshSeatMap();
        }
        seatListModel.clear();
        fbqMap.clear();
        refreshFbList();
        lblSeatTotal.setText("0 VNĐ");
        lblGrandTotal.setText("0 VNĐ");
    }

    private void bindHotkeys() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "checkout");
        getActionMap().put("checkout", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                proceedToCheckout();
            }
        });
    }

    private void styleBtn(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
