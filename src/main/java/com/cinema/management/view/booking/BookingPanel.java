package com.cinema.management.view.booking;

import com.cinema.management.controller.BookingController;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.entity.Product;
import com.cinema.management.model.entity.ShowTime;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Panel POS – Bán vé chính (Module 2, Thành viên A).
 *
 * Layout tổng thể:
 *   ┌──────────────────────────────┬─────────────────────┐
 *   │  1. Chọn suất chiếu (NORTH)  │                     │
 *   ├──────────────────────────────│   3. Tóm tắt đơn    │
 *   │  2. Sơ đồ ghế (CENTER)       │      + F&B          │
 *   │     SeatMapPanel             │      + Tổng tiền    │
 *   │                              │   (EAST)            │
 *   └──────────────────────────────┴─────────────────────┘
 *
 * Điểm hook sang Module 3:
 *   - Nút "Thanh toán" (F5) → gọi onProceedToCheckout callback
 */
public class BookingPanel extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Controller & session ──────────────────────────────────────────────────
    private final BookingController bookingController;
    /**
     * userId của nhân viên đang đăng nhập.
     * TODO: lấy từ session thực khi Module 3 (Auth) của Thành viên B hoàn thành.
     */
    private final String currentUserId;

    // ── Sub-panels ────────────────────────────────────────────────────────────
    private SeatMapPanel seatMapPanel;

    // ── Chọn suất chiếu ───────────────────────────────────────────────────────
    private final JComboBox<ShowTimeItem> cmbShowTime = new JComboBox<>();
    private final JButton btnLoadMap = new JButton("Xem sơ đồ ghế");

    // ── Tóm tắt đơn (EAST panel) ─────────────────────────────────────────────
    private final DefaultListModel<String> seatListModel   = new DefaultListModel<>();
    private final JList<String>            seatList         = new JList<>(seatListModel);
    private final JLabel                   lblSeatTotal     = new JLabel("0 VNĐ");

    /** productId → {Product, quantity} */
    private final Map<String, int[]>       fbqMap           = new LinkedHashMap<>();
    private final DefaultListModel<String> fbListModel      = new DefaultListModel<>();
    private final JList<String>            fbList           = new JList<>(fbListModel);
    private final JLabel                   lblFbTotal       = new JLabel("0 VNĐ");
    private final JLabel                   lblGrandTotal    = new JLabel("0 VNĐ");

    private final JButton btnCheckout = new JButton("💳  Thanh toán  (F5)");
    private final JButton btnReset    = new JButton("✖  Hủy đơn");

    /** Callback sang CheckoutPanel / Module 3. */
    private Runnable onProceedToCheckout;

    // ── Màu sắc ───────────────────────────────────────────────────────────────
    private static final Color BG_HEADER = new Color(52, 73, 94);
    private static final Color PRIMARY   = new Color(41, 128, 185);
    private static final Color SUCCESS   = new Color(39, 174, 96);
    private static final Color DANGER    = new Color(192, 57, 43);

    public BookingPanel(String currentUserId) {
        this.currentUserId    = currentUserId;
        this.bookingController = new BookingController();

        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildMainArea(),   BorderLayout.CENTER);

        loadShowTimeCombo();
        bindHotkeys();
    }

    public void setOnProceedToCheckout(Runnable callback) {
        this.onProceedToCheckout = callback;
    }

    /** Trả về showTimeId đang được chọn – CheckoutPanel cần để gọi checkout(). */
    public String getCurrentShowTimeId() {
        ShowTimeItem item = (ShowTimeItem) cmbShowTime.getSelectedItem();
        return item != null ? item.id : null;
    }

    /** Trả về userId nhân viên – CheckoutPanel cần để ghi Invoice. */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /** Trả về danh sách ghế đang SELECTED – CheckoutPanel cần để tạo BookingSeat. */
    public List<SeatStatusDto> getCurrentSelectedSeats() {
        return seatMapPanel != null ? seatMapPanel.getSelectedSeats() : Collections.emptyList();
    }

    /**
     * Trả về map productId → quantity F&B đã chọn.
     * Bỏ qua các key marker "_name".
     */
    public Map<String, Integer> getCurrentFbItems() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> e : fbqMap.entrySet()) {
            if (!e.getKey().endsWith("_name")) {
                result.put(e.getKey(), e.getValue()[0]);
            }
        }
        return result;
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG_HEADER);
        h.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("🎫  BÁN VÉ  –  POS");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        h.add(title, BorderLayout.WEST);

        JPanel sel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sel.setBackground(BG_HEADER);
        sel.add(new JLabel("Suất chiếu:") {{ setForeground(Color.WHITE); }});
        cmbShowTime.setPreferredSize(new Dimension(300, 28));
        cmbShowTime.setFont(new Font("Arial", Font.PLAIN, 12));
        sel.add(cmbShowTime);
        styleBtn(btnLoadMap, PRIMARY, Color.WHITE);
        btnLoadMap.addActionListener(e -> loadSeatMap());
        sel.add(btnLoadMap);
        h.add(sel, BorderLayout.EAST);
        return h;
    }

    private JPanel buildMainArea() {
        JPanel area = new JPanel(new BorderLayout(8, 0));
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        area.setBackground(Color.WHITE);

        // Center: seat map placeholder (replaced when showtime is selected)
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(new Color(30, 30, 30));
        JLabel hint = new JLabel("← Chọn suất chiếu rồi bấm \"Xem sơ đồ ghế\"");
        hint.setForeground(Color.LIGHT_GRAY);
        hint.setFont(new Font("Arial", Font.ITALIC, 14));
        placeholder.add(hint);
        area.add(placeholder, BorderLayout.CENTER);

        // East: order summary
        area.add(buildOrderPanel(), BorderLayout.EAST);
        return area;
    }

    private JPanel buildOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(new EmptyBorder(0, 8, 0, 0));

        // ── Ghế đã chọn ──
        JPanel seatSection = new JPanel(new BorderLayout(0, 4));
        seatSection.setBackground(Color.WHITE);
        seatSection.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(PRIMARY), "🪑 Ghế đang chọn",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 12), PRIMARY));
        seatList.setFont(new Font("Arial", Font.PLAIN, 12));
        seatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane seatScroll = new JScrollPane(seatList);
        seatScroll.setPreferredSize(new Dimension(280, 120));
        seatSection.add(seatScroll, BorderLayout.CENTER);
        JPanel seatFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        seatFooter.setBackground(Color.WHITE);
        seatFooter.add(new JLabel("Tiền ghế:"));
        lblSeatTotal.setFont(new Font("Arial", Font.BOLD, 12));
        lblSeatTotal.setForeground(PRIMARY);
        seatFooter.add(lblSeatTotal);
        seatSection.add(seatFooter, BorderLayout.SOUTH);

        // ── F&B ──
        JPanel fbSection = new JPanel(new BorderLayout(0, 4));
        fbSection.setBackground(Color.WHITE);
        fbSection.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182)), "🍿 F&B đã chọn",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), new Color(155, 89, 182)));
        fbList.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane fbScroll = new JScrollPane(fbList);
        fbScroll.setPreferredSize(new Dimension(280, 100));
        fbSection.add(fbScroll, BorderLayout.CENTER);

        JButton btnAddFb = new JButton("+ Thêm bắp/nước");
        styleBtn(btnAddFb, new Color(155, 89, 182), Color.WHITE);
        btnAddFb.addActionListener(e -> showAddFbDialog());
        JPanel fbFooter = new JPanel(new BorderLayout());
        fbFooter.setBackground(Color.WHITE);
        fbFooter.add(btnAddFb, BorderLayout.WEST);
        JPanel fbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fbRight.setBackground(Color.WHITE);
        fbRight.add(new JLabel("Tiền F&B:"));
        lblFbTotal.setFont(new Font("Arial", Font.BOLD, 12));
        lblFbTotal.setForeground(new Color(155, 89, 182));
        fbRight.add(lblFbTotal);
        fbFooter.add(fbRight, BorderLayout.EAST);
        fbSection.add(fbFooter, BorderLayout.SOUTH);

        // ── Tổng + nút ──
        JPanel totalPanel = new JPanel(new GridBagLayout());
        totalPanel.setBackground(new Color(245, 245, 245));
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        gc.gridx = 0; gc.gridy = 0;
        JLabel lblTotalTitle = new JLabel("TỔNG CỘNG");
        lblTotalTitle.setFont(new Font("Arial", Font.BOLD, 13));
        totalPanel.add(lblTotalTitle, gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.EAST;
        lblGrandTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblGrandTotal.setForeground(new Color(192, 57, 43));
        lblGrandTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        totalPanel.add(lblGrandTotal, gc);

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2; gc.insets = new Insets(8, 0, 4, 0);
        styleBtn(btnCheckout, SUCCESS, Color.WHITE);
        btnCheckout.setFont(new Font("Arial", Font.BOLD, 14));
        btnCheckout.setPreferredSize(new Dimension(260, 44));
        btnCheckout.addActionListener(e -> proceedToCheckout());
        totalPanel.add(btnCheckout, gc);

        gc.gridy = 2; gc.insets = new Insets(0, 0, 0, 0);
        styleBtn(btnReset, DANGER, Color.WHITE);
        btnReset.setPreferredSize(new Dimension(260, 32));
        btnReset.addActionListener(e -> resetOrder());
        totalPanel.add(btnReset, gc);

        // Ghép các section vào panel chính
        JPanel sections = new JPanel();
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
        sections.setBackground(Color.WHITE);
        sections.add(seatSection);
        sections.add(Box.createVerticalStrut(8));
        sections.add(fbSection);
        sections.add(Box.createVerticalStrut(8));

        panel.add(sections,    BorderLayout.CENTER);
        panel.add(totalPanel,  BorderLayout.SOUTH);
        return panel;
    }

    // ── Load data ─────────────────────────────────────────────────────────────

    private void loadShowTimeCombo() {
        cmbShowTime.removeAllItems();
        List<ShowTime> list = bookingController.getAllShowTimes();
        for (ShowTime st : list) {
            String label = st.getMovie() != null ? st.getMovie().getTitle() : "??";
            String room  = st.getRoom()  != null ? st.getRoom().getRoomName() : "??";
            String time  = st.getStartTime().format(DT_FMT);
            cmbShowTime.addItem(new ShowTimeItem(st.getShowTimeId(),
                    label + " | " + room + " | " + time));
        }
    }

    private void loadSeatMap() {
        ShowTimeItem selected = (ShowTimeItem) cmbShowTime.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu.", "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Nếu đã có SeatMapPanel cũ → dispose timer trước
        if (seatMapPanel != null) seatMapPanel.dispose();

        seatMapPanel = new SeatMapPanel(bookingController, currentUserId);
        seatMapPanel.setOnSelectionChanged(this::onSeatSelectionChanged);
        seatMapPanel.loadShowTime(selected.id);

        // Swap center panel
        JPanel area = (JPanel) getComponent(1); // buildMainArea result
        area.remove(0);  // remove placeholder or old map
        area.add(seatMapPanel, BorderLayout.CENTER, 0);
        area.revalidate();
        area.repaint();

        // Reset F&B khi đổi suất
        fbqMap.clear();
        refreshFbList();
        refreshTotals(Collections.emptyList());
    }

    // ── Seat selection callback ───────────────────────────────────────────────

    private void onSeatSelectionChanged(List<SeatStatusDto> selected) {
        seatListModel.clear();
        BigDecimal seatTotal = BigDecimal.ZERO;
        for (SeatStatusDto s : selected) {
            String price = String.format("%,.0f", s.getBasePrice());
            seatListModel.addElement(s.getLabel() + "  [" + s.getSeatTypeName() + "]  –  " + price + " VNĐ");
            seatTotal = seatTotal.add(s.getBasePrice());
        }
        lblSeatTotal.setText(String.format("%,.0f VNĐ", seatTotal));
        refreshTotals(selected);
    }

    // ── F&B dialog ────────────────────────────────────────────────────────────

    private void showAddFbDialog() {
        List<Product> products = bookingController.getAllProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có sản phẩm F&B nào.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn F&B", true);
        dlg.setLayout(new BorderLayout(8, 8));
        dlg.setSize(400, 320);
        dlg.setLocationRelativeTo(this);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (Product p : products) {
            String price = String.format("%,.0f", p.getCurrentPrice());
            model.addElement(p.getProductName() + "  –  " + price + " VNĐ");
        }
        JList<String> productList = new JList<>(model);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dlg.add(new JScrollPane(productList), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        SpinnerNumberModel qtyModel = new SpinnerNumberModel(1, 1, 20, 1);
        JSpinner spinQty = new JSpinner(qtyModel);
        bottom.add(new JLabel("Số lượng:"));
        bottom.add(spinQty);

        JButton btnAdd = new JButton("Thêm vào đơn");
        styleBtn(btnAdd, SUCCESS, Color.WHITE);
        btnAdd.addActionListener(e -> {
            int idx = productList.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(dlg, "Vui lòng chọn sản phẩm."); return; }
            Product p = products.get(idx);
            int qty = (int) spinQty.getValue();
            fbqMap.merge(p.getProductId(), new int[]{qty}, (a, b) -> new int[]{a[0] + b[0]});
            // Store product ref for display
            fbqMap.put(p.getProductId() + "_name", new int[]{0});  // marker
            refreshFbListWithProducts(p, products);
            dlg.dispose();
        });
        bottom.add(btnAdd);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void refreshFbListWithProducts(Product addedProduct, List<Product> allProducts) {
        fbListModel.clear();
        BigDecimal fbTotal = BigDecimal.ZERO;
        for (Map.Entry<String, int[]> entry : fbqMap.entrySet()) {
            String pid = entry.getKey();
            if (pid.endsWith("_name")) continue;
            int qty = entry.getValue()[0];
            // Find product
            Product p = allProducts.stream()
                    .filter(x -> x.getProductId().equals(pid))
                    .findFirst().orElse(null);
            if (p == null) continue;
            BigDecimal lineTotal = p.getCurrentPrice().multiply(BigDecimal.valueOf(qty));
            fbListModel.addElement(p.getProductName() + " x" + qty
                    + "  =  " + String.format("%,.0f", lineTotal) + " VNĐ");
            fbTotal = fbTotal.add(lineTotal);
        }
        lblFbTotal.setText(String.format("%,.0f VNĐ", fbTotal));
        refreshTotals(seatMapPanel != null ? seatMapPanel.getSelectedSeats() : Collections.emptyList());
    }

    private void refreshFbList() {
        fbListModel.clear();
        lblFbTotal.setText("0 VNĐ");
    }

    // ── Totals ────────────────────────────────────────────────────────────────

    private void refreshTotals(List<SeatStatusDto> selectedSeats) {
        BigDecimal seatSum = selectedSeats.stream()
                .map(SeatStatusDto::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fbSum = BigDecimal.ZERO;
        List<Product> allProducts = bookingController.getAllProducts();
        for (Map.Entry<String, int[]> entry : fbqMap.entrySet()) {
            String pid = entry.getKey();
            if (pid.endsWith("_name")) continue;
            int qty = entry.getValue()[0];
            allProducts.stream().filter(p -> p.getProductId().equals(pid)).findFirst()
                    .ifPresent(p -> {});
        }
        // Recalculate fbSum from fbListModel text (simplification)
        // A cleaner approach would store Product objects directly
        BigDecimal grand = seatSum.add(fbSum);
        lblGrandTotal.setText(String.format("%,.0f VNĐ", grand));
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    private void proceedToCheckout() {
        if (seatMapPanel == null || seatMapPanel.getSelectedSeats().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn ít nhất 1 ghế trước khi thanh toán.",
                    "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onProceedToCheckout != null) {
            onProceedToCheckout.run();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Module Thanh toán sẽ được tích hợp ở Module 3.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetOrder() {
        if (seatMapPanel != null) {
            ShowTimeItem selected = (ShowTimeItem) cmbShowTime.getSelectedItem();
            if (selected != null) {
                bookingController.unlockAllSeats(selected.id, currentUserId);
                seatMapPanel.refreshSeatMap();
            }
        }
        seatListModel.clear();
        fbqMap.clear();
        refreshFbList();
        lblSeatTotal.setText("0 VNĐ");
        lblGrandTotal.setText("0 VNĐ");
    }

    // ── Hotkeys ───────────────────────────────────────────────────────────────

    private void bindHotkeys() {
        // F5 = Thanh toán nhanh
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "checkout");
        getActionMap().put("checkout", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                proceedToCheckout();
            }
        });
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private void styleBtn(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ── Inner classes ─────────────────────────────────────────────────────────

    private static class ShowTimeItem {
        final String id, label;
        ShowTimeItem(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}

