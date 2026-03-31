package com.cinema.management.view.booking;

import com.cinema.management.controller.InvoiceController;
import com.cinema.management.model.dto.InvoiceDto;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.TicketDto;
import com.cinema.management.model.entity.Customer;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CheckoutPanel extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final InvoiceController invoiceController = new InvoiceController();

    private final String showTimeId;
    private final String staffUserId;
    private final List<SeatStatusDto> selectedSeats;
    private final Map<String, Integer> fbItems;

    // ── Customer section ──────────────────────────────────────────────────────
    private final JTextField txtPhone = new JTextField(14);
    private final JButton btnLookup = new JButton("Tra cứu");
    private final JLabel lblCustomerInfo = new JLabel("Chưa nhập SĐT thành viên");
    private Customer foundCustomer = null;

    // ── Promo section ─────────────────────────────────────────────────────────
    private final JTextField txtPromoCode = new JTextField(12);
    private final JButton btnValidate = new JButton("Kiểm tra");
    private final JLabel lblPromoResult = new JLabel(" ");

    // ── Points section ────────────────────────────────────────────────────────
    private final JSpinner spinPoints = new JSpinner(new SpinnerNumberModel(0, 0, 0, 100));
    private final JLabel lblPointsAvail = new JLabel("Điểm khả dụng: 0");
    private final JLabel lblPointDisc = new JLabel("Giảm: 0 VNĐ");

    // ── Payment method ────────────────────────────────────────────────────────
    private final JRadioButton rdoCash = new JRadioButton("Tiền mặt", true);
    private final JRadioButton rdoCard = new JRadioButton("Thẻ ngân hàng");
    private final JRadioButton rdoTransfer = new JRadioButton("Chuyển khoản");

    // ── Summary labels ────────────────────────────────────────────────────────
    private final JLabel lblSeatTotal = new JLabel("0 VNĐ");
    private final JLabel lblFbTotal = new JLabel("0 VNĐ");
    private final JLabel lblPromoDisc = new JLabel("0 VNĐ");
    private final JLabel lblPointDiscSum = new JLabel("0 VNĐ");
    private final JLabel lblGrandTotal = new JLabel("0 VNĐ");

    // ── Action buttons ────────────────────────────────────────────────────────
    private final JButton btnConfirm = new JButton(" Xác nhận thanh toán (F5)");
    private final JButton btnBack = new JButton("← Quay lại");

    private Runnable onBack;

    private final BigDecimal seatTotal;
    private final BigDecimal fbTotal;

    public CheckoutPanel(String showTimeId, String staffUserId,
                         List<SeatStatusDto> selectedSeats,
                         Map<String, Integer> fbItems,
                         BigDecimal seatTotal, BigDecimal fbTotal) {
        this.showTimeId = showTimeId;
        this.staffUserId = staffUserId;
        this.selectedSeats = selectedSeats != null ? selectedSeats : Collections.emptyList();
        this.fbItems = fbItems != null ? fbItems : Collections.emptyMap();
        this.seatTotal = seatTotal != null ? seatTotal : BigDecimal.ZERO;
        this.fbTotal = fbTotal != null ? fbTotal : BigDecimal.ZERO;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        txtPhone.putClientProperty("JTextField.placeholderText", "Nhập SĐT khách hàng...");
        txtPromoCode.putClientProperty("JTextField.placeholderText", "Nhập mã KM...");

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        refreshSummary(BigDecimal.ZERO, BigDecimal.ZERO);
        bindHotkeys();
    }

    public void setOnBack(Runnable callback) {
        this.onBack = callback;
    }

    private Icon createIcon(String path, int size, Color color) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon(path, size, size);
            if (color != null) icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HEADER);
        h.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("THANH TOÁN & XUẤT VÉ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setIcon(createIcon("icons/wallet.svg", 24, Color.WHITE));
        h.add(title, BorderLayout.WEST);

        styleBtn(btnBack, new Color(71, 85, 105), Color.WHITE);
        btnBack.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });
        h.add(btnBack, BorderLayout.EAST);
        return h;
    }

    private JPanel buildCenter() {
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        centerContainer.add(buildSummaryPanel(), BorderLayout.CENTER);
        centerContainer.add(buildFormPanel(), BorderLayout.EAST);

        return centerContainer;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Seat list
        JPanel seatSection = new JPanel(new BorderLayout(0, 4));
        seatSection.setBackground(CARD);
        seatSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createTitledBorder(null, "  Ghế đã chọn  ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), PRIMARY)
        ));

        DefaultListModel<String> seatModel = new DefaultListModel<>();
        for (SeatStatusDto s : selectedSeats) {
            seatModel.addElement(String.format(" %-6s [%-10s]  %s VNĐ",
                    s.getLabel(), s.getSeatTypeName(),
                    String.format("%,.0f", s.getBasePrice())));
        }
        JList<String> seatList = new JList<>(seatModel);
        seatList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane seatScroll = new JScrollPane(seatList);
        seatScroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        seatSection.add(seatScroll, BorderLayout.CENTER);
        panel.add(seatSection, BorderLayout.NORTH);

        // F&B list
        JPanel fbSection = new JPanel(new BorderLayout(0, 4));
        fbSection.setBackground(CARD);
        fbSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createTitledBorder(null, "  F&B  ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), WARNING)
        ));

        DefaultListModel<String> fbModel = new DefaultListModel<>();
        if (fbItems.isEmpty()) {
            fbModel.addElement(" (Không có F&B)");
        } else {
            fbItems.forEach((pid, qty) -> fbModel.addElement("  " + pid + " × " + qty));
        }
        JList<String> fbList = new JList<>(fbModel);
        fbList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane fbScroll = new JScrollPane(fbList);
        fbScroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        fbSection.add(fbScroll, BorderLayout.CENTER);
        panel.add(fbSection, BorderLayout.CENTER);

        // Total breakdown
        panel.add(buildTotalBreakdown(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTotalBreakdown() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(15, 20, 15, 20)));

        GridBagConstraints gl = new GridBagConstraints();
        gl.anchor = GridBagConstraints.WEST;
        gl.insets = new Insets(5, 5, 5, 5);
        GridBagConstraints gr = new GridBagConstraints();
        gr.anchor = GridBagConstraints.EAST;
        gr.weightx = 1;
        gr.fill = GridBagConstraints.HORIZONTAL;
        gr.insets = new Insets(5, 5, 5, 5);

        addBreakdownRow(p, gl, gr, 0, "Tiền ghế:", lblSeatTotal, new Color(15, 23, 42));
        addBreakdownRow(p, gl, gr, 1, "Tiền F&B:", lblFbTotal, new Color(15, 23, 42));
        addBreakdownRow(p, gl, gr, 2, "Giảm giá (khuyến mãi):", lblPromoDisc, SUCCESS);
        addBreakdownRow(p, gl, gr, 3, "Giảm giá (điểm):", lblPointDiscSum, SUCCESS);

        GridBagConstraints sep = new GridBagConstraints();
        sep.gridx = 0;
        sep.gridy = 4;
        sep.gridwidth = 2;
        sep.fill = GridBagConstraints.HORIZONTAL;
        sep.insets = new Insets(10, 0, 10, 0);
        p.add(new JSeparator(), sep);

        addBreakdownRow(p, gl, gr, 5, "TỔNG PHẢI TRẢ:", lblGrandTotal, DANGER);
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        return p;
    }

    private void addBreakdownRow(JPanel p, GridBagConstraints gl, GridBagConstraints gr,
                                 int row, String labelText, JLabel valueLabel, Color valueColor) {
        gl.gridx = 0;
        gl.gridy = row;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(lbl, gl);
        gr.gridx = 1;
        gr.gridy = row;
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(valueLabel, gr);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD);
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(15, 20, 15, 20)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 0, 8, 10);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;

        addSectionTitle(form, gc, row++, "Khách hàng thành viên");
        gc.gridx = 0;
        gc.gridy = row;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        form.add(new JLabel("SĐT:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JPanel phoneRow = new JPanel(new BorderLayout(5, 0));
        phoneRow.setOpaque(false);
        txtPhone.setPreferredSize(new Dimension(0, 36));
        styleBtn(btnLookup, PRIMARY, Color.WHITE);
        phoneRow.add(txtPhone, BorderLayout.CENTER);
        phoneRow.add(btnLookup, BorderLayout.EAST);
        form.add(phoneRow, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        lblCustomerInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCustomerInfo.setForeground(new Color(100, 116, 139));
        form.add(lblCustomerInfo, gc);
        row++;
        gc.gridwidth = 1;

        addSectionTitle(form, gc, row++, "Mã khuyến mãi");
        gc.gridx = 0;
        gc.gridy = row;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        form.add(new JLabel("Mã:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JPanel promoRow = new JPanel(new BorderLayout(5, 0));
        promoRow.setOpaque(false);
        txtPromoCode.setPreferredSize(new Dimension(0, 36));
        styleBtn(btnValidate, WARNING, Color.WHITE);
        promoRow.add(txtPromoCode, BorderLayout.CENTER);
        promoRow.add(btnValidate, BorderLayout.EAST);
        form.add(promoRow, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        lblPromoResult.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        form.add(lblPromoResult, gc);
        row++;
        gc.gridwidth = 1;

        addSectionTitle(form, gc, row++, "Điểm thưởng");
        gc.gridx = 0;
        gc.gridy = row;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        form.add(new JLabel("Dùng điểm:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JPanel pointsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pointsRow.setOpaque(false);
        spinPoints.setPreferredSize(new Dimension(100, 36));
        ((JSpinner.DefaultEditor) spinPoints.getEditor()).getTextField().setEditable(true);
        spinPoints.addChangeListener(e -> onPointsChanged());
        pointsRow.add(spinPoints);
        lblPointsAvail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pointsRow.add(lblPointsAvail);
        form.add(pointsRow, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        lblPointDisc.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPointDisc.setForeground(SUCCESS);
        form.add(lblPointDisc, gc);
        row++;
        gc.gridwidth = 1;

        addSectionTitle(form, gc, row++, "Phương thức thanh toán");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoCash);
        bg.add(rdoCard);
        bg.add(rdoTransfer);
        rdoCash.setOpaque(false);
        rdoCash.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rdoCard.setOpaque(false);
        rdoCard.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rdoTransfer.setOpaque(false);
        rdoTransfer.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        JPanel radioRow = new JPanel(new GridLayout(1, 3, 5, 0));
        radioRow.setOpaque(false);
        radioRow.add(rdoCash);
        radioRow.add(rdoCard);
        radioRow.add(rdoTransfer);
        form.add(radioRow, gc);

        panel.add(form, BorderLayout.NORTH);
        return panel;
    }

    private void addSectionTitle(JPanel panel, GridBagConstraints gc, int row, String title) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(15, 0, 10, 0);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(HEADER);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        panel.add(lbl, gc);
        gc.gridwidth = 1;
        gc.weightx = 0;
        gc.insets = new Insets(5, 0, 5, 10);
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottom.setOpaque(false);

        btnConfirm.setIcon(createIcon("icons/check.svg", 20, Color.WHITE));
        styleBtn(btnConfirm, SUCCESS, Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnConfirm.setPreferredSize(new Dimension(300, 46));
        btnConfirm.addActionListener(e -> confirmCheckout());

        bottom.add(btnConfirm);
        return bottom;
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    private void lookupCustomer() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            showError("Vui lòng nhập số điện thoại.");
            return;
        }
        Optional<Customer> opt = invoiceController.findCustomerByPhone(phone);
        if (opt.isPresent()) {
            foundCustomer = opt.get();
            int pts = foundCustomer.getRewardPoints();
            lblCustomerInfo.setText("✅ " + foundCustomer.getFullName()
                    + "  |  Hạng: " + foundCustomer.getMemberTier()
                    + "  |  Điểm: " + pts);
            lblCustomerInfo.setForeground(SUCCESS);
            SpinnerNumberModel m = (SpinnerNumberModel) spinPoints.getModel();
            m.setMaximum(pts);
            lblPointsAvail.setText("Khả dụng: " + pts + " điểm");
        } else {
            foundCustomer = null;
            lblCustomerInfo.setText("❌ Không tìm thấy thành viên với SĐT: " + phone);
            lblCustomerInfo.setForeground(DANGER);
            SpinnerNumberModel m = (SpinnerNumberModel) spinPoints.getModel();
            m.setMaximum(0);
            m.setValue(0);
            lblPointsAvail.setText("Khả dụng: 0 điểm");
        }
        onPointsChanged();
    }

    private void validatePromo() {
        String code = txtPromoCode.getText().trim();
        if (code.isEmpty()) {
            lblPromoResult.setText("  ");
            refreshSummary(BigDecimal.ZERO, calculatePointDiscount());
            return;
        }
        try {
            com.cinema.management.model.entity.ShowTime st = new com.cinema.management.model.entity.ShowTime();
            st.setShowTimeId(showTimeId);
            com.cinema.management.model.entity.Promotion promo = invoiceController.validatePromoCode(code.toUpperCase(), st);

            String discount = promo.getDiscountPercent() != null
                    ? promo.getDiscountPercent().toPlainString() + "%" : "?";
            lblPromoResult.setText("✅ Hợp lệ – giảm " + discount + (promo.getIsExclusive() ? "  ⚠ Độc quyền" : ""));
            lblPromoResult.setForeground(SUCCESS);
            refreshSummary(BigDecimal.ZERO, calculatePointDiscount());
        } catch (IllegalArgumentException ex) {
            lblPromoResult.setText("❌ " + ex.getMessage());
            lblPromoResult.setForeground(DANGER);
            refreshSummary(BigDecimal.ZERO, calculatePointDiscount());
        }
    }

    private void onPointsChanged() {
        BigDecimal pd = calculatePointDiscount();
        lblPointDisc.setText("Giảm: " + String.format("%,.0f", pd) + " VNĐ");
        refreshSummary(BigDecimal.ZERO, pd);
    }

    private void confirmCheckout() {
        if (selectedSeats.isEmpty()) {
            showError("Không có ghế nào được chọn.");
            return;
        }

        String paymentMethod = rdoCard.isSelected() ? "CARD" : rdoTransfer.isSelected() ? "TRANSFER" : "CASH";
        String paymentMethodDisplay = rdoCard.isSelected() ? "Thẻ ngân hàng"
                : rdoTransfer.isSelected() ? "Chuyển khoản" : "Tiền mặt";
        String customerId = (foundCustomer != null) ? foundCustomer.getCustomerId() : null;
        String promoCode = txtPromoCode.getText().trim().isEmpty() ? null : txtPromoCode.getText().trim().toUpperCase();
        int usedPoints = (int) spinPoints.getValue();

        BigDecimal grand = computeGrandTotal(promoCode, usedPoints);
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Xác nhận thanh toán <b>" + String.format("%,.0f", grand)
                        + " VNĐ</b><br>Phương thức: <b>" + paymentMethodDisplay + "</b>?</html>",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        try {
            InvoiceDto invoice = invoiceController.checkout(
                    showTimeId, staffUserId, customerId,
                    selectedSeats, fbItems, promoCode, usedPoints, paymentMethod);

            showInvoiceResult(invoice);

        } catch (Exception ex) {
            showError("Thanh toán thất bại:\n" + ex.getMessage());
            btnConfirm.setEnabled(true);
            btnConfirm.setText(" Xác nhận thanh toán (F5)");
        }
    }

    // ── XỬ LÝ IN ẤN (HÓA ĐƠN + VÉ RỜI) ──────────────────────────────────────────

    private void showInvoiceResult(InvoiceDto invoice) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hoá đơn – " + invoice.getInvoiceId(), true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setSize(560, 680);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG);

        // Nơi hiển thị bản Preview hóa đơn
        JTextArea ta = new JTextArea(buildMultiPartInvoiceText(invoice));
        ta.setFont(new Font("Monospaced", Font.PLAIN, 14));
        ta.setEditable(false);
        ta.setMargin(new Insets(15, 20, 15, 20));

        JScrollPane scrollPane = new JScrollPane(ta);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        dlg.add(scrollPane, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btns.setOpaque(false);
        JButton btnPrint = new JButton(" In hoá đơn & Vé");
        btnPrint.setIcon(createIcon("icons/refresh.svg", 18, Color.WHITE)); // Thay bằng Icon Máy in nếu có
        JButton btnClose = new JButton("Đóng & Về màn hình chính");

        styleBtn(btnPrint, PRIMARY, Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(180, 40));
        styleBtn(btnClose, new Color(100, 116, 139), Color.WHITE);
        btnClose.setPreferredSize(new Dimension(220, 40));

        btnPrint.addActionListener(e -> printInvoice(invoice, ta.getText()));
        btnClose.addActionListener(e -> {
            dlg.dispose();
            if (onBack != null) onBack.run();
        });
        btns.add(btnPrint);
        btns.add(btnClose);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    /**
     * Thuật toán tạo 1 Hóa đơn tổng và N Vé xem phim rời.
     * Cắt phân trang bằng nét đứt (Mô phỏng máy in cắt giấy tự động)
     */
    private String buildMultiPartInvoiceText(InvoiceDto inv) {
        StringBuilder sb = new StringBuilder();
        String eqLine = "================================================\n";
        String dashLine = "------------------------------------------------\n";
        String cutLine = "\n               - - - ✂ CẮT ✂ - - -               \n\n";

        // ================== PHẦN 1: HÓA ĐƠN TỔNG (RECEIPT) ==================
        sb.append(eqLine);
        sb.append("            HỆ THỐNG QUẢN LÝ RẠP CHIẾU PHIM\n");
        sb.append("                 HOÁ ĐƠN BÁN VÉ\n");
        sb.append(eqLine);
        sb.append(String.format("Số HĐ   : %s\n", inv.getInvoiceId()));
        sb.append(String.format("Ngày    : %s\n", inv.getCreatedAt().format(DT_FMT)));
        sb.append(String.format("Thu ngân: %s\n", inv.getStaffName()));
        if (inv.getCustomerName() != null && !inv.getCustomerName().isEmpty()) {
            sb.append(String.format("Khách   : %s (%s)\n", inv.getCustomerName(), inv.getCustomerPhone()));
        }
        sb.append(dashLine);

        // Gộp chung tiền vé xem phim (Tóm tắt)
        sb.append(String.format("Vé xem phim (Số lượng: %d)\n", inv.getTickets().size()));
        sb.append(String.format("%48s\n", fmt(inv.getSeatTotal())));

        // Chi tiết F&B
        if (!inv.getFbLines().isEmpty()) {
            sb.append("Bắp Nước (F&B):\n");
            for (String line : inv.getFbLines()) {
                sb.append("  ").append(line).append("\n");
            }
        }
        sb.append(dashLine);

        // Phần tổng kết tiền
        sb.append(String.format("%-30s %17s\n", "Tạm tính:", fmt(inv.getSeatTotal().add(inv.getFbTotal()))));
        if (inv.getPromotionDiscount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s %17s\n", "Giảm giá (Khuyến mãi):", "-" + fmt(inv.getPromotionDiscount())));
        }
        if (inv.getPointDiscount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s %17s\n", "Giảm giá (Điểm):", "-" + fmt(inv.getPointDiscount())));
        }
        sb.append(eqLine);
        sb.append(String.format("%-28s %19s\n", "TỔNG PHẢI TRẢ:", fmt(inv.getGrandTotal())));
        sb.append(String.format("%-30s %17s\n", "Hình thức TT:", toPaymentMethodLabel(inv.getPaymentMethod())));
        if (inv.getEarnedPoints() > 0) {
            sb.append(String.format("%-30s %17s\n", "Điểm tích lũy:", "+" + inv.getEarnedPoints()));
        }
        sb.append("\n         Cảm ơn quý khách đã sử dụng dịch vụ!\n");

        // ================== PHẦN 2: CÁC VÉ RỜI (TICKETS) ==================
        for (TicketDto t : inv.getTickets()) {
            sb.append(cutLine);
            sb.append(eqLine);
            sb.append("                  VÉ XEM PHIM\n");
            sb.append("            HỆ THỐNG QUẢN LÝ RẠP CHIẾU PHIM\n");
            sb.append(eqLine);
            sb.append(String.format("Mã HĐ      : %s\n", inv.getInvoiceId()));
            sb.append(String.format("Ngày chiếu : %s\n", t.getShowTime().format(DT_FMT)));
            sb.append(String.format("Loại ghế   : %s\n", t.getSeatTypeName()));
            sb.append(dashLine);
            sb.append(String.format("GHẾ NGỒI   : %-20s\n", t.getSeatLabel()));

            // Theo đúng nguyên tắc: In đúng giá Snapshot tại thời điểm mua
            sb.append(String.format("GIÁ VÉ     : %s\n", fmt(t.getPrice())));
            sb.append(eqLine);
            sb.append("      Vui lòng xuất trình vé trước khi vào rạp\n");
        }

        return sb.toString();
    }

    private String fmt(BigDecimal val) {
        return String.format("%,.0f VNĐ", val);
    }

    private String toPaymentMethodLabel(String paymentMethod) {
        if ("CARD".equalsIgnoreCase(paymentMethod)) return "Thẻ ngân hàng";
        if ("TRANSFER".equalsIgnoreCase(paymentMethod)) return "Chuyển khoản";
        return "Tiền mặt";
    }

    private void printInvoice(InvoiceDto invoice, String text) {
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        job.setJobName("Hoá đơn " + invoice.getInvoiceId());
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            // Máy in cuộn (Thermal Printer) sẽ cuộn giấy ra liên tục theo trục Y
            // Vì text đã tự động Format xuống dòng \n và đường Cắt, ta in ra 1 trang siêu dài là đủ.
            if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
            graphics.setFont(new Font("Monospaced", Font.PLAIN, 10));
            FontMetrics fm = graphics.getFontMetrics();
            int lineH = fm.getHeight();
            int x = (int) pageFormat.getImageableX() + 10;
            int y = (int) pageFormat.getImageableY() + lineH;
            for (String line : text.split("\n")) {
                graphics.drawString(line, x, y);
                y += lineH;
            }
            return java.awt.print.Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (java.awt.print.PrinterException ex) {
                showError("Lỗi in: " + ex.getMessage());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BigDecimal calculatePointDiscount() {
        if (foundCustomer == null) return BigDecimal.ZERO;
        int pts = (int) spinPoints.getValue();
        if (pts <= 0) return BigDecimal.ZERO;
        BigDecimal sub = seatTotal.add(fbTotal);
        BigDecimal maxAllowed = sub.multiply(new BigDecimal("0.50"));
        return BigDecimal.valueOf(pts).min(maxAllowed);
    }

    private BigDecimal computeGrandTotal(String promoCode, int usedPoints) {
        BigDecimal sub = seatTotal.add(fbTotal);
        BigDecimal pd = calculatePointDiscount();
        return sub.subtract(pd).max(BigDecimal.ZERO);
    }

    private void refreshSummary(BigDecimal promoDiscount, BigDecimal pointDiscount) {
        lblSeatTotal.setText(fmt(seatTotal));
        lblFbTotal.setText(fmt(fbTotal));
        lblPromoDisc.setText("- " + fmt(promoDiscount));
        lblPointDiscSum.setText("- " + fmt(pointDiscount));
        BigDecimal grand = seatTotal.add(fbTotal).subtract(promoDiscount).subtract(pointDiscount);
        if (grand.compareTo(BigDecimal.ZERO) < 0) grand = BigDecimal.ZERO;
        lblGrandTotal.setText(fmt(grand));
    }

    private void bindHotkeys() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "confirm");
        getActionMap().put("confirm", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmCheckout();
            }
        });
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void styleBtn(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
