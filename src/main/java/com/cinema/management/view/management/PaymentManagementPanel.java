package com.cinema.management.view.management;

import com.cinema.management.controller.PaymentController;
import com.cinema.management.model.dto.PaymentDashboardDto;
import com.cinema.management.model.dto.PaymentManagementRowDto;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.model.entity.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaymentManagementPanel extends JPanel {

    // Modern color palette (Tailwind CSS inspired)
    private static final Color BG_COLOR = new Color(248, 250, 252); // slate-50
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_MAIN = new Color(15, 23, 42); // slate-900
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // slate-500
    private static final Color BORDER_COLOR = new Color(226, 232, 240); // slate-200

    private static final Color PRIMARY_COLOR = new Color(14, 165, 233); // sky-500
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94); // green-500
    private static final Color WARNING_COLOR = new Color(245, 158, 11); // amber-500

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final PaymentController paymentController = new PaymentController();
    private final List<PaymentManagementRowDto> currentRows = new ArrayList<>();

    // UI Components
    private final JSpinner spFromDate = createDateSpinner();
    private final JSpinner spToDate = createDateSpinner();
    private final JComboBox<ShiftItem> cbShift = new JComboBox<>();
    private final JComboBox<StaffItem> cbStaff = new JComboBox<>();
    private final JComboBox<String> cbMethod = new JComboBox<>(new String[] { "ALL", "CASH", "QR", "CARD" });

    // Dashboard Cards UI References
    private final JLabel lblTotalAmount = new JLabel("0 ₫");
    private final JLabel lblTotalCount = new JLabel("0 giao dịch");
    private final JLabel lblCashAmount = new JLabel("0 ₫");
    private final JLabel lblCashCount = new JLabel("0 giao dịch");
    private final JLabel lblQrAmount = new JLabel("0 ₫");
    private final JLabel lblQrCount = new JLabel("0 giao dịch");
    private final JLabel lblCardAmount = new JLabel("0 ₫");
    private final JLabel lblCardCount = new JLabel("0 giao dịch");

    private final PieChartPanel pieChartPanel = new PieChartPanel();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "ID", "Mã Hóa Đơn", "Thời gian", "Khách hàng", "Số tiền", "Phương thức", "Thu ngân", "Mã GD",
                    "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public PaymentManagementPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        // Styling FlatLaf properties explicitly for this panel context
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);

        loadFilterData();
        refreshData();
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("ĐỐI SOÁT & DOANH THU");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Quản lý dòng tiền, theo dõi trạng thái thanh toán và xuất báo cáo chi tiết.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        headerPanel.add(left, BorderLayout.WEST);

        // Action Buttons Top Right
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);

        JButton btnExport = new JButton("  Xuất Báo Cáo  ");
        styleButton(btnExport, SUCCESS_COLOR, Color.WHITE);
        btnExport.addActionListener(e -> exportExcel());
        actionPanel.add(btnExport);

        headerPanel.add(actionPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel buildMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setOpaque(false);

        mainContent.add(buildTopDashboardArea(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTablePanel(),
                buildRightAnalyticsPanel());
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(15);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        // FlatLaf splitpane transparency
        splitPane.setBackground(BG_COLOR);

        mainContent.add(splitPane, BorderLayout.CENTER);

        return mainContent;
    }

    private JPanel buildTopDashboardArea() {
        JPanel area = new JPanel(new BorderLayout(0, 20));
        area.setOpaque(false);

        area.add(buildFilterPanel(), BorderLayout.NORTH);
        area.add(buildSummaryCards(), BorderLayout.CENTER);

        return area;
    }

    private JPanel buildFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(CARD_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 15, 10, 15)));

        filterPanel.add(createFilterGroup("Từ ngày:", spFromDate));
        filterPanel.add(createFilterGroup("Đến ngày:", spToDate));
        filterPanel.add(createFilterGroup("Ca làm việc:", cbShift));
        filterPanel.add(createFilterGroup("Thu ngân:", cbStaff));
        filterPanel.add(createFilterGroup("Phương thức:", cbMethod));

        JButton btnApply = new JButton("  Lọc Dữ Liệu  ");
        styleButton(btnApply, PRIMARY_COLOR, Color.WHITE);
        btnApply.addActionListener(e -> refreshData());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(new EmptyBorder(22, 10, 0, 0));
        btnWrapper.add(btnApply);
        filterPanel.add(btnWrapper);

        return filterPanel;
    }

    private JPanel createFilterGroup(String labelText, JComponent inputComp) {
        JPanel group = new JPanel(new BorderLayout(0, 5));
        group.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);

        inputComp.setPreferredSize(new Dimension(inputComp instanceof JComboBox ? 140 : 120, 36));
        inputComp.setFont(FONT_REGULAR);

        group.add(lbl, BorderLayout.NORTH);
        group.add(inputComp, BorderLayout.CENTER);
        return group;
    }

    private JPanel buildSummaryCards() {
        JPanel cardsContainer = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsContainer.setOpaque(false);

        cardsContainer.add(createMetricCard("Tổng Doanh Thu", lblTotalAmount, lblTotalCount, PRIMARY_COLOR, "💰"));
        cardsContainer.add(createMetricCard("Tiền Mặt (CASH)", lblCashAmount, lblCashCount, TEXT_MAIN, "💵"));
        cardsContainer.add(createMetricCard("Chuyển Khoản (QR)", lblQrAmount, lblQrCount, SUCCESS_COLOR, "📱"));
        cardsContainer.add(createMetricCard("Thẻ Tín Dụng (CARD)", lblCardAmount, lblCardCount, WARNING_COLOR, "💳"));

        return cardsContainer;
    }

    private JPanel createMetricCard(String title, JLabel lblVal, JLabel lblCount, Color accentColor,
            String iconSymbol) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        // Add a subtle accent top border visually
        JPanel topBorder = new JPanel();
        topBorder.setBackground(accentColor);
        topBorder.setPreferredSize(new Dimension(0, 4));
        card.add(topBorder, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 5));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setForeground(TEXT_MUTED);

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(accentColor);

        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCount.setForeground(TEXT_MUTED);

        JPanel centerInfo = new JPanel(new GridLayout(2, 1, 0, 4));
        centerInfo.setOpaque(false);
        centerInfo.add(lblVal);
        centerInfo.add(lblCount);

        content.add(lblTitle, BorderLayout.NORTH);
        content.add(centerInfo, BorderLayout.CENTER);

        JLabel lblIcon = new JLabel(iconSymbol);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setForeground(accentColor);

        card.add(content, BorderLayout.CENTER);
        card.add(lblIcon, BorderLayout.EAST);

        return card;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        // Styling the table
        table.setRowHeight(44);
        table.setFont(FONT_REGULAR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(241, 245, 249)); // slate-100
        table.setSelectionForeground(TEXT_MAIN);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));

        // Center alignment for specific columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);

        // FlatLaf smooth scrolling
        scrollPane.putClientProperty("JScrollPane.smoothScrolling", true);

        // Sub-header for table area
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("Lịch Sử Giao Dịch Gần Đây");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);
        tableHeader.add(title, BorderLayout.WEST);

        panel.add(tableHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRightAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(25, 25, 25, 25)));

        JLabel title = new JLabel("Cơ Cấu Phương Thức");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(pieChartPanel, BorderLayout.CENTER);

        // Setup chart legend
        JPanel legend = new JPanel(new GridLayout(3, 1, 0, 10));
        legend.setOpaque(false);
        legend.add(createLegendItem("Tiền Mặt (CASH)", TEXT_MAIN));
        legend.add(createLegendItem("Chuyển Khoản (QR)", SUCCESS_COLOR));
        legend.add(createLegendItem("Thẻ (CARD)", WARNING_COLOR));

        panel.add(legend, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLegendItem(String text, Color c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel();
        dot.setBackground(c);
        dot.setPreferredSize(new Dimension(14, 14));
        // simple rounding approximation with flatlaf
        dot.putClientProperty("FlatLaf.style", "arc: 999");

        JLabel l = new JLabel(text);
        l.setFont(FONT_REGULAR);
        l.setForeground(TEXT_MAIN);

        p.add(dot);
        p.add(l);
        return p;
    }

    private void loadFilterData() {
        cbShift.removeAllItems();
        cbShift.addItem(new ShiftItem("ALL", "Tất cả các ca"));
        for (ShiftReport s : paymentController.getAllShifts()) {
            String name = String.format("%s | %s - %s",
                    s.getUser() != null ? s.getUser().getUserId() : "",
                    s.getShiftStart() != null ? s.getShiftStart().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                            : "",
                    s.getShiftEnd() != null ? s.getShiftEnd().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "");
            cbShift.addItem(new ShiftItem(s.getShiftReportId(), name));
        }

        cbStaff.removeAllItems();
        cbStaff.addItem(new StaffItem("ALL", "Tất cả thu ngân"));
        for (User u : paymentController.getAllCashiers()) {
            String label = (u.getUserId() != null ? u.getUserId() : "") + " - " +
                    (u.getFullName() != null ? u.getFullName() : "");
            cbStaff.addItem(new StaffItem(u.getUserId(), label));
        }
    }

    private void refreshData() {
        try {
            LocalDate from = asLocalDate((Date) spFromDate.getValue());
            LocalDate to = asLocalDate((Date) spToDate.getValue());
            if (from.isAfter(to)) {
                JOptionPane.showMessageDialog(this, "Từ ngày không được lớn hơn đến ngày.",
                        "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ShiftItem shift = (ShiftItem) cbShift.getSelectedItem();
            StaffItem staff = (StaffItem) cbStaff.getSelectedItem();
            String method = (String) cbMethod.getSelectedItem();

            String shiftId = shift != null ? shift.id : "ALL";
            String staffId = staff != null ? staff.id : "ALL";
            if ("ALL".equals(staffId)) {
                staffId = null;
            }

            List<PaymentManagementRowDto> rows = paymentController.searchPayments(from, to, shiftId, staffId, method);
            currentRows.clear();
            currentRows.addAll(rows);

            renderTable(rows);
            renderDashboard(paymentController.summarize(rows));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu thanh toán:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void renderTable(List<PaymentManagementRowDto> rows) {
        tableModel.setRowCount(0);
        for (PaymentManagementRowDto r : rows) {
            // Null check handler
            String status = r.getStatus() != null ? r.getStatus().toString() : "PAID";
            tableModel.addRow(new Object[] {
                    r.getPaymentId(),
                    r.getInvoiceId(),
                    r.getPaidAt() != null ? r.getPaidAt().format(DT_FMT) : "",
                    r.getCustomerName(),
                    fmt(r.getAmount()),
                    r.getPaymentMethod(),
                    r.getCashierId() + " - " + r.getCashierName(),
                    r.getTransactionCode() != null ? r.getTransactionCode() : "-",
                    status
            });
        }
    }

    private void renderDashboard(PaymentDashboardDto d) {
        lblTotalAmount.setText(fmt(d.getTotalAmount()));
        lblTotalCount.setText(d.getTotalCount() + " giao dịch");

        lblCashAmount.setText(fmt(d.getCashAmount()));
        lblCashCount.setText(d.getCashCount() + " giao dịch");

        lblQrAmount.setText(fmt(d.getQrAmount()));
        lblQrCount.setText(d.getQrCount() + " giao dịch");

        lblCardAmount.setText(fmt(d.getCardAmount()));
        lblCardCount.setText(d.getCardCount() + " giao dịch");

        pieChartPanel.setValues(d.getCashAmount(), d.getQrAmount(), d.getCardAmount());
    }

    private void exportExcel() {
        if (currentRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn nơi lưu báo cáo Excel");
        chooser.setSelectedFile(new java.io.File("BaoCaoDoanhThuThanhToan.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new java.io.File(file.getAbsolutePath() + ".xlsx");
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
                FileOutputStream fos = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("Chi Tiết Giao Dịch");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font f = workbook.createFont();
            f.setBold(true);
            headerStyle.setFont(f);

            String[] headers = { "Mã Giao Dịch", "Mã Hóa Đơn", "Thời gian", "Khách hàng", "Số tiền (VND)",
                    "Phương thức", "Thu ngân ID", "Tên Thu Ngân", "Mã Tham Chiếu", "Trạng thái" };

            Row head = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (PaymentManagementRowDto r : currentRows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(nonNull(r.getPaymentId()));
                row.createCell(1).setCellValue(nonNull(r.getInvoiceId()));
                row.createCell(2).setCellValue(r.getPaidAt() != null ? r.getPaidAt().format(DT_FMT) : "");
                row.createCell(3).setCellValue(nonNull(r.getCustomerName()));
                row.createCell(4).setCellValue(r.getAmount() != null ? r.getAmount().doubleValue() : 0);
                row.createCell(5).setCellValue(nonNull(r.getPaymentMethod()));
                row.createCell(6).setCellValue(nonNull(r.getCashierId()));
                row.createCell(7).setCellValue(nonNull(r.getCashierName()));
                row.createCell(8).setCellValue(nonNull(r.getTransactionCode()));
                row.createCell(9).setCellValue(nonNull(String.valueOf(r.getStatus()))); // use String.valueOf to avoid
                                                                                        // null string representation
                                                                                        // exception in nonNull
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(fos);
            JOptionPane.showMessageDialog(this, "Đã xuất Excel thành công tại:\n" + file.getAbsolutePath(),
                    "Hoàn Tất", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất báo cáo:\n" + ex.getMessage(),
                    "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fmt(BigDecimal value) {
        return String.format("%,.0f ₫", value != null ? value : BigDecimal.ZERO);
    }

    private String nonNull(String s) {
        if (s == null || "null".equals(s))
            return "";
        return s;
    }

    private LocalDate asLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 38));
    }

    // -- Custom Renderers & Components --

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);

            String status = value == null ? "" : value.toString();
            if (!isSelected) {
                if ("PAID".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)
                        || "SUCCESS".equalsIgnoreCase(status)) {
                    label.setForeground(new Color(21, 128, 61)); // green-700
                    label.setBackground(new Color(220, 252, 231)); // green-100
                } else if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                    label.setForeground(new Color(185, 28, 28)); // red-700
                    label.setBackground(new Color(254, 226, 226)); // red-100
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    label.setForeground(new Color(180, 83, 9)); // amber-700
                    label.setBackground(new Color(254, 243, 199)); // amber-100
                } else {
                    label.setForeground(TEXT_MAIN);
                    label.setBackground(CARD_BG);
                }
            } else {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }
            // Add a bit of padding to the sides visually inside the cell
            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            return label;
        }
    }

    private static class ShiftItem {
        private final String id;
        private final String label;

        private ShiftItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class StaffItem {
        private final String id;
        private final String label;

        private StaffItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class PieChartPanel extends JPanel {
        private BigDecimal cash = BigDecimal.ZERO;
        private BigDecimal qr = BigDecimal.ZERO;
        private BigDecimal card = BigDecimal.ZERO;

        private PieChartPanel() {
            setPreferredSize(new Dimension(280, 280));
            setOpaque(false);
        }

        private void setValues(BigDecimal cash, BigDecimal qr, BigDecimal card) {
            this.cash = cash != null ? cash : BigDecimal.ZERO;
            this.qr = qr != null ? qr : BigDecimal.ZERO;
            this.card = card != null ? card : BigDecimal.ZERO;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // High quality rendering
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = Math.min(getWidth(), getHeight()) - 40;
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - w) / 2;

            BigDecimal total = cash.add(qr).add(card);
            if (total.compareTo(BigDecimal.ZERO) <= 0) {
                g2.setColor(BORDER_COLOR);
                g2.fillOval(x, y, w, w);
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String text = "Chưa có dữ liệu";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + w / 2 + fm.getAscent() / 2);
                g2.dispose();
                return;
            }

            double totalVal = total.doubleValue();
            int cashArc = (int) Math.round((cash.doubleValue() / totalVal) * 360);
            int qrArc = (int) Math.round((qr.doubleValue() / totalVal) * 360);
            int cardArc = 360 - cashArc - qrArc;

            int start = 90;

            double cashPct = (cash.doubleValue() / totalVal) * 100;
            double qrPct = (qr.doubleValue() / totalVal) * 100;
            double cardPct = 100 - cashPct - qrPct;

            // Draw Pie Segments
            g2.setColor(TEXT_MAIN);
            g2.fillArc(x, y, w, w, start, -cashArc);
            start -= cashArc;

            g2.setColor(SUCCESS_COLOR);
            g2.fillArc(x, y, w, w, start, -qrArc);
            start -= qrArc;

            g2.setColor(WARNING_COLOR);
            g2.fillArc(x, y, w, w, start, -cardArc);

            // Create Donut Hole (make it a donut chart for modern look)
            g2.setColor(CARD_BG);
            int inner = (int) (w * 0.65); // 65% hole
            g2.fillOval(x + (w - inner) / 2, y + (w - inner) / 2, inner, inner);

            // Draw percentages on slices
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            int cx = x + w / 2;
            int cy = y + w / 2;
            int textRadius = (int) (w / 2 * 0.82);

            start = 90;
            drawPercentage(g2, cashPct, start, cashArc, cx, cy, textRadius);
            start -= cashArc;
            drawPercentage(g2, qrPct, start, qrArc, cx, cy, textRadius);
            start -= qrArc;
            drawPercentage(g2, cardPct, start, cardArc, cx, cy, textRadius);

            // Draw center text
            g2.setColor(TEXT_MAIN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            String center1 = String.format("%,.0f đ", totalVal);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(center1, x + (w - fm.stringWidth(center1)) / 2, y + w / 2 - 5);

            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String center2 = "TỔNG TIỀN";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(center2, x + (w - fm2.stringWidth(center2)) / 2, y + w / 2 + 15);

            g2.dispose();
        }

        private void drawPercentage(Graphics2D g2, double pct, int startAngle, int arcAngle, int cx, int cy,
                int radius) {
            if (pct < 5.0)
                return; // Hide very small slices text
            double angle = Math.toRadians(startAngle - arcAngle / 2.0);
            int tx = cx + (int) (radius * Math.cos(angle));
            int ty = cy - (int) (radius * Math.sin(angle));
            String text = String.format("%.1f%%", pct);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(text, tx - fm.stringWidth(text) / 2, ty + fm.getAscent() / 2 - 2);
        }
    }
}
