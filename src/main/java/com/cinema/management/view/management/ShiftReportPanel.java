package com.cinema.management.view.management;

import com.cinema.management.controller.ShiftReportController;
import com.cinema.management.model.dto.ShiftReportSummaryDto;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.util.UserSessionContext;
import com.cinema.management.view.auth.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShiftReportPanel extends JPanel {

    private static final Color BG_APP = new Color(248, 250, 252); // slate-50
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(15, 23, 42); // slate-900
    private static final Color PRIMARY = new Color(14, 165, 233); // sky-500
    private static final Color SUCCESS = new Color(34, 197, 94); // green-500
    private static final Color DANGER = new Color(239, 68, 68); // red-500
    private static final Color BORDER_COLOR = new Color(226, 232, 240); // slate-200
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // slate-500

    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final ShiftReportController shiftReportController = new ShiftReportController();
    private final String staffId;

    private ShiftReportSummaryDto currentSummary;

    private final JLabel lblStaff = new JLabel();
    private final JLabel lblShiftStart = new JLabel();
    private final JLabel lblShiftEnd = new JLabel();

    private final JLabel lblOpeningCash = new JLabel("0 ₫");
    private final JLabel lblCashRevenue = new JLabel("0 ₫");
    private final JLabel lblTransferRevenue = new JLabel("0 ₫");
    private final JLabel lblCardRevenue = new JLabel("0 ₫");
    private final JLabel lblTotalRevenue = new JLabel("0 ₫");
    private final JLabel lblExpectedCash = new JLabel("0 ₫");
    private final JLabel lblTargetOpening = new JLabel("0 ₫");

    private final JTextField txtActualCash = new JTextField();
    private final JLabel lblDiscrepancy = new JLabel("0 ₫");
    private final JLabel lblRemittedCash = new JLabel("0 ₫");
    private final JLabel lblCarryOverCash = new JLabel("0 ₫");

    private final JButton btnRefresh = new JButton("Làm Mới Số Liệu");
    private final JButton btnCloseShift = new JButton("Chốt Ca & In Báo Cáo");

    public ShiftReportPanel(String staffId) {
        this.staffId = staffId;
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_APP);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        txtActualCash.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { calculateDiscrepancy(); }
            @Override
            public void removeUpdate(DocumentEvent e) { calculateDiscrepancy(); }
            @Override
            public void changedUpdate(DocumentEvent e) { calculateDiscrepancy(); }
        });

        btnRefresh.addActionListener(e -> reloadSummary());
        btnCloseShift.addActionListener(e -> closeShift());
        reloadSummary();
    }



    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("BÁO CÁO CHỐT CA (Z-REPORT)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Thống kê chi tiết doanh thu, đối soát tiền mặt và in hóa đơn chốt ca");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        JPanel rightInfo = new JPanel(new GridLayout(3, 1, 0, 4));
        rightInfo.setOpaque(false);

        lblStaff.setHorizontalAlignment(SwingConstants.RIGHT);
        lblStaff.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblStaff.setForeground(new Color(14, 165, 233));
        
        lblShiftStart.setHorizontalAlignment(SwingConstants.RIGHT);
        lblShiftStart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblShiftStart.setForeground(new Color(226, 232, 240));
        
        lblShiftEnd.setHorizontalAlignment(SwingConstants.RIGHT);
        lblShiftEnd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblShiftEnd.setForeground(new Color(226, 232, 240));

        rightInfo.add(lblStaff);
        rightInfo.add(lblShiftStart);
        rightInfo.add(lblShiftEnd);

        header.add(left, BorderLayout.WEST);
        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new GridLayout(1, 2, 30, 0));
        content.setOpaque(false);
        content.add(buildSystemDataPanel());
        content.add(buildActualCountingPanel());
        return content;
    }

    private JPanel buildSystemDataPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        header.setBackground(new Color(241, 245, 249)); // slate-100
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        JLabel title = new JLabel("1. DỮ LIỆU HỆ THỐNG GHI NHẬN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(HEADER);
        header.add(title);
        panel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(15, 20, 15, 20);

        int row = 0;
        addRow(content, gc, row++, "Tiền tồn đầu ca:", lblOpeningCash, TEXT_MUTED, 15);
        addRow(content, gc, row++, "Doanh thu tiền mặt:", lblCashRevenue, HEADER, 15);
        addRow(content, gc, row++, "Thu qua QR/Chuyển khoản:", lblTransferRevenue, SUCCESS, 15);
        addRow(content, gc, row++, "Thu qua quẹt thẻ:", lblCardRevenue, PRIMARY, 15);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        content.add(new JSeparator(), gc);
        gc.gridwidth = 1;

        addRow(content, gc, row++, "TỔNG DOANH THU CA:", lblTotalRevenue, HEADER, 20);
        addRow(content, gc, row, "Tiền mặt máy tính dự kiến:", lblExpectedCash, DANGER, 18);
        addRow(content, gc, row + 1, "Mức tiền giữ lại tiêu chuẩn (vốn):", lblTargetOpening, SUCCESS, 15);
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActualCountingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        header.setBackground(new Color(254, 242, 242)); // red-50
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(254, 202, 202))); // red-200
        JLabel title = new JLabel("2. KIỂM ĐẾM & BÀN GIAO THỰC TẾ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(DANGER);
        header.add(title);
        panel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(15, 20, 15, 20);

        int row = 0;
        gc.gridx = 0;
        gc.gridy = row;
        JLabel lblInput = new JLabel("Nhập tiền mặt thực tế đếm được:");
        lblInput.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInput.setForeground(HEADER);
        content.add(lblInput, gc);

        gc.gridx = 1;
        txtActualCash.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtActualCash.setHorizontalAlignment(SwingConstants.RIGHT);
        txtActualCash.setPreferredSize(new Dimension(200, 40));
        txtActualCash.setForeground(DANGER);
        content.add(txtActualCash, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        JLabel lblDiff = new JLabel("Độ lệch đối soát (Thừa/Thiếu):");
        lblDiff.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDiff.setForeground(TEXT_MUTED);
        content.add(lblDiff, gc);

        gc.gridx = 1;
        lblDiscrepancy.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDiscrepancy.setHorizontalAlignment(SwingConstants.RIGHT);
        content.add(lblDiscrepancy, gc);
        row++;
        
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        content.add(new JSeparator(), gc);
        gc.gridwidth = 1;
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        JLabel lblRemit = new JLabel("Số tiền xuất quỹ NỘP VỀ CÔNG TY:");
        lblRemit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        content.add(lblRemit, gc);

        gc.gridx = 1;
        lblRemittedCash.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblRemittedCash.setForeground(HEADER);
        lblRemittedCash.setHorizontalAlignment(SwingConstants.RIGHT);
        content.add(lblRemittedCash, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        JLabel lblCarry = new JLabel("Số tiền giữ lại tại quầy (Ca sau):");
        lblCarry.setFont(new Font("Segoe UI", Font.BOLD, 16));
        content.add(lblCarry, gc);

        gc.gridx = 1;
        lblCarryOverCash.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCarryOverCash.setForeground(SUCCESS);
        lblCarryOverCash.setHorizontalAlignment(SwingConstants.RIGHT);
        content.add(lblCarryOverCash, gc);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);

        styleButton(btnRefresh, Color.WHITE, TEXT_MUTED);
        btnRefresh.setBorderPainted(true);
        btnRefresh.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        
        styleButton(btnCloseShift, HEADER, Color.WHITE);

        footer.add(btnRefresh);
        footer.add(btnCloseShift);
        return footer;
    }

    private void addRow(JPanel panel,
                        GridBagConstraints gc,
                        int row,
                        String label,
                        JLabel valueLabel,
                        Color valueColor,
                        int fontSize) {
        gc.gridx = 0;
        gc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", fontSize > 15 ? Font.BOLD : Font.PLAIN, fontSize));
        lbl.setForeground(fontSize > 15 ? HEADER : TEXT_MUTED);
        panel.add(lbl, gc);

        gc.gridx = 1;
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(valueLabel, gc);
    }

    private void reloadSummary() {
        try {
            LocalDateTime shiftStart = UserSessionContext.getShiftStartedAt();
            if (shiftStart == null) {
                shiftStart = LocalDateTime.now();
            }
            BigDecimal openingCash = UserSessionContext.getStandardOpeningCash();
            UserSessionContext.setOpeningCash(openingCash);

            currentSummary = shiftReportController.getCurrentShiftSummary(staffId, shiftStart, openingCash);
            renderSummary(currentSummary);
            txtActualCash.setText(currentSummary.getExpectedCash().setScale(0, java.math.RoundingMode.HALF_UP).toPlainString());
            calculateDiscrepancy();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu chốt ca:\n" + ex.getMessage(),
                    "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderSummary(ShiftReportSummaryDto summary) {
        lblStaff.setText("Thu ngân: " + summary.getStaffId() + " - " + safe(summary.getStaffName()));
        lblShiftStart.setText("Bắt đầu ca: " + format(summary.getShiftStart()));
        lblShiftEnd.setText("Thời gian hiện tại: " + format(summary.getShiftEnd()));

        lblOpeningCash.setText(fmt(summary.getOpeningCash()));
        lblCashRevenue.setText(fmt(summary.getCashRevenue()));
        lblTransferRevenue.setText(fmt(summary.getTransferRevenue()));
        lblCardRevenue.setText(fmt(summary.getCardRevenue()));
        lblTotalRevenue.setText(fmt(summary.getTotalRevenue()));
        lblExpectedCash.setText(fmt(summary.getExpectedCash()));
        lblTargetOpening.setText(fmt(summary.getOpeningCash()));
    }

    private void calculateDiscrepancy() {
        if (currentSummary == null) {
            return;
        }
        try {
            BigDecimal actualCash = parseMoney(txtActualCash.getText());
            BigDecimal diff = actualCash.subtract(currentSummary.getExpectedCash());
            BigDecimal targetOpening = currentSummary.getOpeningCash() != null ? currentSummary.getOpeningCash() : BigDecimal.ZERO;
            
            BigDecimal remitted = actualCash.subtract(targetOpening);
            if (remitted.compareTo(BigDecimal.ZERO) < 0) {
                remitted = BigDecimal.ZERO;
            }
            BigDecimal carryOver = actualCash.subtract(remitted);

            if (diff.compareTo(BigDecimal.ZERO) == 0) {
                lblDiscrepancy.setForeground(SUCCESS);
                lblDiscrepancy.setText("Khớp (0 ₫)");
                txtActualCash.setForeground(SUCCESS);
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                lblDiscrepancy.setForeground(DANGER);
                lblDiscrepancy.setText("Lệch thiếu: " + fmt(diff.abs()));
                txtActualCash.setForeground(DANGER);
            } else {
                lblDiscrepancy.setForeground(PRIMARY);
                lblDiscrepancy.setText("Lệch thừa: +" + fmt(diff));
                txtActualCash.setForeground(PRIMARY);
            }

            lblRemittedCash.setText(fmt(remitted));
            lblCarryOverCash.setText(fmt(carryOver));
            
            if (carryOver.compareTo(targetOpening) < 0) {
                lblCarryOverCash.setForeground(DANGER);
            } else {
                lblCarryOverCash.setForeground(SUCCESS);
            }
            btnCloseShift.setEnabled(true);
        } catch (Exception e) {
            lblDiscrepancy.setForeground(DANGER);
            lblDiscrepancy.setText("Sai định dạng số");
            txtActualCash.setForeground(DANGER);
            lblRemittedCash.setText("0 ₫");
            lblCarryOverCash.setText("0 ₫");
            lblCarryOverCash.setForeground(DANGER);
            btnCloseShift.setEnabled(false);
        }
    }

    private void closeShift() {
        if (currentSummary == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận in báo cáo và chốt ca?\nHệ thống sẽ tính số tiền nộp về để duy trì quỹ tiền mặt tại quầy ở mức mặc định.",
                "Cảnh Báo Chốt Ca",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            BigDecimal actualCash = parseMoney(txtActualCash.getText());
            ShiftReport report = shiftReportController.closeShift(
                    staffId,
                    currentSummary.getShiftStart(),
                    currentSummary.getOpeningCash(),
                    actualCash
            );

            String printText = buildShiftReportPrintText(report);
            printShiftReport(printText);
            JOptionPane.showMessageDialog(this, "Đã lưu và xuất báo cáo chốt ca thành công. Phiên làm việc đã kết thúc.",
                    "Hoàn Tất", JOptionPane.INFORMATION_MESSAGE);

            UserSessionContext.logout();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            new LoginFrame().setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi chốt ca:\n" + ex.getMessage(),
                    "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String buildShiftReportPrintText(ShiftReport r) {
        String line = "============================================\n";
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        sb.append("         BAO CAO CHOT CA (Z-REPORT)         \n");
        sb.append(line);
        sb.append("Ma bien ban : ").append(r.getShiftReportId()).append("\n");
        sb.append("Thu ngan    : ").append(r.getUser() != null ? r.getUser().getUserId() : "").append("\n");
        sb.append("Bat dau ca  : ").append(format(r.getShiftStart())).append("\n");
        sb.append("Ket thuc ca : ").append(format(r.getShiftEnd())).append("\n");
        sb.append("--------------------------------------------\n");
        sb.append("Tien doi ca           : ").append(fmt(r.getOpeningCash())).append("\n");
        sb.append("Doanh thu tien mat    : ").append(fmt(r.getCashRevenue())).append("\n");
        sb.append("Doanh thu QR/CK       : ").append(fmt(r.getTransferRevenue())).append("\n");
        sb.append("Doanh thu the         : ").append(fmt(r.getCardRevenue())).append("\n");
        sb.append("TONG DOANH THU        : ").append(fmt(r.getTotalRevenue())).append("\n");
        sb.append("Tien mat du kien      : ").append(fmt(r.getExpectedCash())).append("\n");
        sb.append("Tien mat thuc te      : ").append(fmt(r.getActualCash())).append("\n");
        sb.append("Do lech hien tai      : ").append(fmt(r.getDiscrepancy())).append("\n");
        sb.append("Tien nop ve cong ty   : ").append(fmt(r.getRemittedCash())).append("\n");
        sb.append("Tien giu lai quay     : ").append(fmt(r.getCarryOverCash())).append("\n");
        sb.append(line);
        sb.append("\nKy ten thu ngan: ________________________\n");
        return sb.toString();
    }

    private void printShiftReport(String text) {
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        job.setJobName("ShiftReport-" + staffId);
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
            try {
                job.print();
            } catch (java.awt.print.PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Hủy in báo cáo. Đã lưu dữ liệu vào hệ thống.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 46));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String fmt(BigDecimal value) {
        BigDecimal safe = value != null ? value : BigDecimal.ZERO;
        return String.format("%,.0f ₫", safe);
    }

    private String format(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_TIME) : "";
    }

    private BigDecimal parseMoney(String text) {
        String normalized = text == null ? "" : text.replaceAll("[^0-9-]", "");
        if (normalized.isBlank() || "-".equals(normalized)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(normalized);
    }
}
