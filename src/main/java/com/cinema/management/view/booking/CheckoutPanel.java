package com.cinema.management.view.booking;

import com.cinema.management.controller.InvoiceController;
import com.cinema.management.model.dto.InvoiceDto;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.TicketDto;
import com.cinema.management.model.entity.Customer;

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

/**
 * Panel Thanh toán & Xuất vé (Module 3, FR-ST-04).
 *
 * Layout:
 *   NORTH  – tiêu đề
 *   CENTER – JSplitPane:
 *              Trái:  Tóm tắt đơn (ghế + F&B)
 *              Phải:  Form: tra cứu KH, mã promo, điểm, phương thức TT
 *   SOUTH  – Nút "Xác nhận thanh toán (F5)"
 *
 * Sau khi checkout thành công → hiển thị InvoiceResultPanel (dialog).
 *
 * Nhận dữ liệu từ BookingPanel qua constructor (Dependency Injection đơn giản).
 */
public class CheckoutPanel extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG_HEADER = new Color(39, 174, 96);
    private static final Color PRIMARY   = new Color(41, 128, 185);
    private static final Color DANGER    = new Color(192, 57, 43);
    private static final Color SUCCESS   = new Color(39, 174, 96);

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final InvoiceController invoiceController = new InvoiceController();

    /** Dữ liệu được truyền vào từ BookingPanel khi bấm "Thanh toán". */
    private final String               showTimeId;
    private final String               staffUserId;
    private final List<SeatStatusDto>  selectedSeats;
    private final Map<String, Integer> fbItems;

    // ── Customer section ──────────────────────────────────────────────────────
    private final JTextField txtPhone       = new JTextField(14);
    private final JButton    btnLookup      = new JButton("Tra cứu");
    private final JLabel     lblCustomerInfo= new JLabel("Chưa nhập SĐT thành viên");
    private Customer         foundCustomer  = null;

    // ── Promo section ─────────────────────────────────────────────────────────
    private final JTextField txtPromoCode   = new JTextField(12);
    private final JButton    btnValidate    = new JButton("Kiểm tra");
    private final JLabel     lblPromoResult = new JLabel(" ");

    // ── Points section ────────────────────────────────────────────────────────
    private final JSpinner   spinPoints     = new JSpinner(new SpinnerNumberModel(0, 0, 0, 100));
    private final JLabel     lblPointsAvail = new JLabel("Điểm khả dụng: 0");
    private final JLabel     lblPointDisc   = new JLabel("Giảm: 0 VNĐ");

    // ── Payment method ────────────────────────────────────────────────────────
    private final JRadioButton rdoCash     = new JRadioButton("Tiền mặt", true);
    private final JRadioButton rdoCard     = new JRadioButton("Thẻ ngân hàng");
    private final JRadioButton rdoTransfer = new JRadioButton("Chuyển khoản");

    // ── Summary labels ────────────────────────────────────────────────────────
    private final JLabel lblSeatTotal    = new JLabel("0 VNĐ");
    private final JLabel lblFbTotal      = new JLabel("0 VNĐ");
    private final JLabel lblPromoDisc    = new JLabel("0 VNĐ");
    private final JLabel lblPointDiscSum = new JLabel("0 VNĐ");
    private final JLabel lblGrandTotal   = new JLabel("0 VNĐ");

    // ── Action buttons ────────────────────────────────────────────────────────
    private final JButton btnConfirm = new JButton("✅  Xác nhận thanh toán  (F5)");
    private final JButton btnBack    = new JButton("← Quay lại");

    /** Callback để quay về BookingPanel. */
    private Runnable onBack;

    // ── Pre-calculated totals ─────────────────────────────────────────────────
    private final BigDecimal seatTotal;
    private final BigDecimal fbTotal;

    public CheckoutPanel(String showTimeId, String staffUserId,
                          List<SeatStatusDto> selectedSeats,
                          Map<String, Integer> fbItems,
                          BigDecimal seatTotal, BigDecimal fbTotal) {
        this.showTimeId    = showTimeId;
        this.staffUserId   = staffUserId;
        this.selectedSeats = selectedSeats != null ? selectedSeats : Collections.emptyList();
        this.fbItems       = fbItems       != null ? fbItems       : Collections.emptyMap();
        this.seatTotal     = seatTotal     != null ? seatTotal     : BigDecimal.ZERO;
        this.fbTotal       = fbTotal       != null ? fbTotal       : BigDecimal.ZERO;

        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildBottom(),  BorderLayout.SOUTH);

        refreshSummary(BigDecimal.ZERO, BigDecimal.ZERO);
        bindHotkeys();
    }

    public void setOnBack(Runnable callback) { this.onBack = callback; }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG_HEADER);
        h.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("💳  THANH TOÁN & XUẤT VÉ");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        h.add(title, BorderLayout.WEST);

        styleBtn(btnBack, new Color(52, 73, 94), Color.WHITE);
        btnBack.addActionListener(e -> { if (onBack != null) onBack.run(); });
        h.add(btnBack, BorderLayout.EAST);
        return h;
    }

    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildSummaryPanel(), buildFormPanel());
        split.setDividerLocation(480);
        split.setDividerSize(6);
        split.setBorder(new EmptyBorder(10, 10, 6, 10));
        return split;
    }

    // ── Left: Order Summary ───────────────────────────────────────────────────

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);

        // Seat list
        JPanel seatSection = new JPanel(new BorderLayout(0, 4));
        seatSection.setBackground(Color.WHITE);
        seatSection.setBorder(new TitledBorder(BorderFactory.createLineBorder(PRIMARY),
                "🪑 Ghế đã chọn", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), PRIMARY));

        DefaultListModel<String> seatModel = new DefaultListModel<>();
        for (SeatStatusDto s : selectedSeats) {
            seatModel.addElement(String.format("%-6s [%-10s]  %s VNĐ",
                    s.getLabel(), s.getSeatTypeName(),
                    String.format("%,.0f", s.getBasePrice())));
        }
        JList<String> seatList = new JList<>(seatModel);
        seatList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane seatScroll = new JScrollPane(seatList);
        seatScroll.setPreferredSize(new Dimension(460, 140));
        seatSection.add(seatScroll, BorderLayout.CENTER);
        panel.add(seatSection, BorderLayout.NORTH);

        // F&B list
        JPanel fbSection = new JPanel(new BorderLayout(0, 4));
        fbSection.setBackground(Color.WHITE);
        fbSection.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182)),
                "🍿 F&B", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), new Color(155, 89, 182)));

        DefaultListModel<String> fbModel = new DefaultListModel<>();
        if (fbItems.isEmpty()) {
            fbModel.addElement("(Không có F&B)");
        } else {
            fbItems.forEach((pid, qty) ->
                    fbModel.addElement("  " + pid + " × " + qty));
        }
        JList<String> fbList = new JList<>(fbModel);
        fbList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        fbSection.add(new JScrollPane(fbList), BorderLayout.CENTER);
        panel.add(fbSection, BorderLayout.CENTER);

        // Total breakdown
        panel.add(buildTotalBreakdown(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTotalBreakdown() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(248, 249, 250));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 14, 10, 14)));

        GridBagConstraints gl = new GridBagConstraints();
        gl.anchor = GridBagConstraints.WEST; gl.insets = new Insets(3, 4, 3, 4);
        GridBagConstraints gr = new GridBagConstraints();
        gr.anchor = GridBagConstraints.EAST; gr.weightx = 1;
        gr.fill = GridBagConstraints.HORIZONTAL; gr.insets = new Insets(3, 4, 3, 4);

        addBreakdownRow(p, gl, gr, 0, "Tiền ghế:",          lblSeatTotal,    Color.BLACK);
        addBreakdownRow(p, gl, gr, 1, "Tiền F&B:",          lblFbTotal,      Color.BLACK);
        addBreakdownRow(p, gl, gr, 2, "Giảm giá (promo):",  lblPromoDisc,    new Color(39, 174, 96));
        addBreakdownRow(p, gl, gr, 3, "Giảm giá (điểm):",   lblPointDiscSum, new Color(39, 174, 96));

        // Separator
        GridBagConstraints sep = new GridBagConstraints();
        sep.gridx = 0; sep.gridy = 4; sep.gridwidth = 2;
        sep.fill = GridBagConstraints.HORIZONTAL; sep.insets = new Insets(4, 0, 4, 0);
        JSeparator line = new JSeparator();
        p.add(line, sep);

        addBreakdownRow(p, gl, gr, 5, "TỔNG PHẢI TRẢ:", lblGrandTotal, DANGER);
        lblGrandTotal.setFont(new Font("Arial", Font.BOLD, 18));
        return p;
    }

    private void addBreakdownRow(JPanel p, GridBagConstraints gl, GridBagConstraints gr,
                                  int row, String labelText, JLabel valueLabel, Color valueColor) {
        gl.gridx = 0; gl.gridy = row;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        p.add(lbl, gl);
        gr.gridx = 1; gr.gridy = row;
        valueLabel.setFont(new Font("Arial", Font.BOLD, 13));
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(valueLabel, gr);
    }

    // ── Right: Form ───────────────────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 8, 6);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ── Khách hàng thành viên ──
        addSectionTitle(form, gc, row++, "👤 Khách hàng thành viên");

        gc.gridx = 0; gc.gridy = row; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(new JLabel("Số điện thoại:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        JPanel phoneRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        phoneRow.setBackground(Color.WHITE);
        txtPhone.setPreferredSize(new Dimension(130, 28));
        txtPhone.setToolTipText("Nhập SĐT để tra cứu tích điểm");
        styleBtn(btnLookup, PRIMARY, Color.WHITE);
        btnLookup.setPreferredSize(new Dimension(90, 28));
        btnLookup.addActionListener(e -> lookupCustomer());
        phoneRow.add(txtPhone); phoneRow.add(btnLookup);
        form.add(phoneRow, gc); row++;

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        lblCustomerInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblCustomerInfo.setForeground(Color.GRAY);
        form.add(lblCustomerInfo, gc); row++;
        gc.gridwidth = 1;

        // ── Mã khuyến mãi ──
        addSectionTitle(form, gc, row++, "🎟 Mã khuyến mãi");

        gc.gridx = 0; gc.gridy = row; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(new JLabel("Mã promo:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        JPanel promoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        promoRow.setBackground(Color.WHITE);
        txtPromoCode.setPreferredSize(new Dimension(120, 28));
        txtPromoCode.setToolTipText("Nhập mã khuyến mãi (không bắt buộc)");
        styleBtn(btnValidate, new Color(155, 89, 182), Color.WHITE);
        btnValidate.setPreferredSize(new Dimension(90, 28));
        btnValidate.addActionListener(e -> validatePromo());
        promoRow.add(txtPromoCode); promoRow.add(btnValidate);
        form.add(promoRow, gc); row++;

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        lblPromoResult.setFont(new Font("Arial", Font.ITALIC, 11));
        form.add(lblPromoResult, gc); row++;
        gc.gridwidth = 1;

        // ── Điểm thưởng ──
        addSectionTitle(form, gc, row++, "⭐ Điểm thưởng");

        gc.gridx = 0; gc.gridy = row; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(new JLabel("Dùng điểm:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        JPanel pointsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pointsRow.setBackground(Color.WHITE);
        spinPoints.setPreferredSize(new Dimension(100, 28));
        ((JSpinner.DefaultEditor) spinPoints.getEditor()).getTextField().setEditable(true);
        spinPoints.addChangeListener(e -> onPointsChanged());
        pointsRow.add(spinPoints);
        pointsRow.add(lblPointsAvail);
        form.add(pointsRow, gc); row++;

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        lblPointDisc.setFont(new Font("Arial", Font.ITALIC, 11));
        lblPointDisc.setForeground(new Color(39, 174, 96));
        form.add(lblPointDisc, gc); row++;
        gc.gridwidth = 1;

        // ── Phương thức thanh toán ──
        addSectionTitle(form, gc, row++, "💰 Phương thức thanh toán");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoCash); bg.add(rdoCard); bg.add(rdoTransfer);
        rdoCash.setBackground(Color.WHITE);
        rdoCard.setBackground(Color.WHITE);
        rdoTransfer.setBackground(Color.WHITE);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        radioRow.setBackground(Color.WHITE);
        radioRow.add(rdoCash); radioRow.add(rdoCard); radioRow.add(rdoTransfer);
        form.add(radioRow, gc);

        panel.add(form, BorderLayout.NORTH);
        return panel;
    }

    private void addSectionTitle(JPanel panel, GridBagConstraints gc, int row, String title) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(new Color(52, 73, 94));
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        panel.add(lbl, gc);
        gc.gridwidth = 1; gc.weightx = 0;
    }

    // ── Bottom confirm button ──────────────────────────────────────────────────

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(new Color(245, 245, 245));
        bottom.setBorder(new EmptyBorder(10, 16, 14, 16));

        styleBtn(btnConfirm, SUCCESS, Color.WHITE);
        btnConfirm.setFont(new Font("Arial", Font.BOLD, 15));
        btnConfirm.setPreferredSize(new Dimension(300, 46));
        btnConfirm.addActionListener(e -> confirmCheckout());
        bottom.add(btnConfirm, BorderLayout.CENTER);
        return bottom;
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    private void lookupCustomer() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) { showError("Vui lòng nhập số điện thoại."); return; }
        Optional<Customer> opt = invoiceController.findCustomerByPhone(phone);
        if (opt.isPresent()) {
            foundCustomer = opt.get();
            int pts = foundCustomer.getRewardPoints();
            lblCustomerInfo.setText("✅ " + foundCustomer.getFullName()
                    + "  |  Hạng: " + foundCustomer.getMemberTier()
                    + "  |  Điểm: " + pts);
            lblCustomerInfo.setForeground(new Color(39, 174, 96));
            // Cập nhật spinner điểm tối đa
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
            // Cần ShowTime để validate điều kiện phim/ngày – lấy qua controller
            // Dùng placeholder ShowTime với showTimeId; InvoiceServiceImpl sẽ load đầy đủ
            com.cinema.management.model.entity.ShowTime st =
                    new com.cinema.management.model.entity.ShowTime();
            st.setShowTimeId(showTimeId);
            com.cinema.management.model.entity.Promotion promo =
                    invoiceController.validatePromoCode(code.toUpperCase(), st);

            String discount = promo.getDiscountPercent() != null
                    ? promo.getDiscountPercent().toPlainString() + "%" : "?";
            lblPromoResult.setText("✅ Hợp lệ – giảm " + discount
                    + (promo.getIsExclusive() ? "  ⚠ Độc quyền" : ""));
            lblPromoResult.setForeground(new Color(39, 174, 96));
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
            showError("Không có ghế nào được chọn."); return;
        }

        String paymentMethod = rdoCard.isSelected()     ? "CARD"
                             : rdoTransfer.isSelected() ? "TRANSFER"
                             : "CASH";

        String customerId = (foundCustomer != null) ? foundCustomer.getCustomerId() : null;
        String promoCode  = txtPromoCode.getText().trim().isEmpty() ? null
                          : txtPromoCode.getText().trim().toUpperCase();
        int usedPoints = (int) spinPoints.getValue();

        // Xác nhận lần cuối
        BigDecimal grand = computeGrandTotal(promoCode, usedPoints);
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Xác nhận thanh toán <b>" + String.format("%,.0f", grand)
                + " VNĐ</b><br>Phương thức: <b>" + paymentMethod + "</b>?</html>",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Disable nút tránh double-click
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
            btnConfirm.setText("✅  Xác nhận thanh toán  (F5)");
        }
    }

    // ── Invoice result dialog ─────────────────────────────────────────────────

    private void showInvoiceResult(InvoiceDto invoice) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Hoá đơn – " + invoice.getInvoiceId(), true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setSize(560, 640);
        dlg.setLocationRelativeTo(this);

        JTextArea ta = new JTextArea(buildInvoiceText(invoice));
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setMargin(new Insets(12, 14, 12, 14));
        dlg.add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnPrint = new JButton("🖨 In hoá đơn");
        JButton btnClose = new JButton("Đóng");
        styleBtn(btnPrint, PRIMARY, Color.WHITE);
        styleBtn(btnClose, new Color(149, 165, 166), Color.WHITE);
        btnPrint.addActionListener(e -> printInvoice(invoice, ta.getText()));
        btnClose.addActionListener(e -> {
            dlg.dispose();
            if (onBack != null) onBack.run();  // Quay về BookingPanel
        });
        btns.add(btnPrint); btns.add(btnClose);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private String buildInvoiceText(InvoiceDto inv) {
        StringBuilder sb = new StringBuilder();
        String sep = "─".repeat(50) + "\n";
        sb.append("          🎬  CINEMA MANAGEMENT SYSTEM\n");
        sb.append("               HOÁ ĐƠN BÁN VÉ\n");
        sb.append(sep);
        sb.append(String.format("Số HĐ  : %s\n", inv.getInvoiceId()));
        sb.append(String.format("Ngày   : %s\n", inv.getCreatedAt().format(DT_FMT)));
        sb.append(String.format("Thu ngân: %s\n", inv.getStaffName()));
        sb.append(String.format("Khách  : %s  %s\n", inv.getCustomerName(), inv.getCustomerPhone()));
        sb.append(sep);
        sb.append("VÉ XEM PHIM:\n");
        for (TicketDto t : inv.getTickets()) {
            sb.append(String.format("  %-8s %-14s %s  %s VNĐ\n",
                    t.getSeatLabel(), "[" + t.getSeatTypeName() + "]",
                    t.getShowTime().format(DT_FMT),
                    String.format("%,.0f", t.getPrice())));
        }
        if (!inv.getFbLines().isEmpty()) {
            sb.append("\nBẮP NƯỚC / F&B:\n");
            for (String line : inv.getFbLines()) sb.append("  ").append(line).append("\n");
        }
        sb.append(sep);
        sb.append(String.format("%-30s %15s\n", "Tiền ghế:",  fmt(inv.getSeatTotal())));
        sb.append(String.format("%-30s %15s\n", "Tiền F&B:",  fmt(inv.getFbTotal())));
        if (inv.getPromotionDiscount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s %15s\n",
                    "Giảm giá (promo " + inv.getPromoCode() + "):",
                    "-" + fmt(inv.getPromotionDiscount())));
        }
        if (inv.getPointDiscount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s %15s\n", "Giảm giá (điểm):", "-" + fmt(inv.getPointDiscount())));
        }
        sb.append(sep);
        sb.append(String.format("%-30s %15s\n", "TỔNG PHẢI TRẢ:", fmt(inv.getGrandTotal())));
        sb.append(String.format("%-30s %15s\n", "Phương thức TT:", inv.getPaymentMethod()));
        if (inv.getEarnedPoints() > 0) {
            sb.append(String.format("%-30s %15s\n", "Điểm tích lũy:", "+" + inv.getEarnedPoints()));
        }
        sb.append(sep);
        sb.append("     Cảm ơn quý khách! Chúc xem phim vui vẻ 🎉\n");
        return sb.toString();
    }

    private String fmt(BigDecimal val) {
        return String.format("%,.0f VNĐ", val);
    }

    private void printInvoice(InvoiceDto invoice, String text) {
        // Dùng Java PrinterJob – basic text print
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        job.setJobName("Hoá đơn " + invoice.getInvoiceId());
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
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
            try { job.print(); }
            catch (java.awt.print.PrinterException ex) {
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
        // Giảm giá promo: không tính ở đây – để service tính chính xác
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
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmCheckout();
            }
        });
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void styleBtn(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}

