package com.cinema.management.view.management;

import com.cinema.management.controller.ShiftReportController;
import com.cinema.management.model.dto.ShiftReportSummaryDto;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.util.UserSessionContext;
import com.cinema.management.view.auth.LoginFrame;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShiftReportPanel extends JPanel {

    private static final Color BG_APP = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ShiftReportController shiftReportController = new ShiftReportController();
    private final String staffId;

    private ShiftReportSummaryDto currentSummary;

    private final JLabel lblStaff = new JLabel();
    private final JLabel lblShiftStart = new JLabel();
    private final JLabel lblShiftEnd = new JLabel();

    private final JLabel lblOpeningCash = new JLabel("0 VND");
    private final JLabel lblCashRevenue = new JLabel("0 VND");
    private final JLabel lblTransferRevenue = new JLabel("0 VND");
    private final JLabel lblCardRevenue = new JLabel("0 VND");
    private final JLabel lblTotalRevenue = new JLabel("0 VND");
    private final JLabel lblExpectedCash = new JLabel("0 VND");

    private final JTextField txtActualCash = new JTextField();
    private final JLabel lblDiscrepancy = new JLabel("0 VND");

    private final JButton btnRefresh = new JButton("Lam moi so lieu");
    private final JButton btnCloseShift = new JButton("In bao cao & Chot ca");

    public ShiftReportPanel(String staffId) {
        this.staffId = staffId;
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_APP);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        txtActualCash.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calculateDiscrepancy();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calculateDiscrepancy();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calculateDiscrepancy();
            }
        });

        btnRefresh.addActionListener(e -> reloadSummary());
        btnCloseShift.addActionListener(e -> closeShift());
        reloadSummary();
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
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("BAO CAO CHOT CA (Z-REPORT)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(HEADER);
        title.setIcon(createIcon("icons/monitor.svg", 28, HEADER));

        JPanel rightInfo = new JPanel(new GridLayout(3, 1));
        rightInfo.setOpaque(false);

        lblStaff.setHorizontalAlignment(SwingConstants.RIGHT);
        lblStaff.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblShiftStart.setHorizontalAlignment(SwingConstants.RIGHT);
        lblShiftStart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblShiftStart.setForeground(new Color(100, 116, 139));
        lblShiftEnd.setHorizontalAlignment(SwingConstants.RIGHT);
        lblShiftEnd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblShiftEnd.setForeground(new Color(100, 116, 139));

        rightInfo.add(lblStaff);
        rightInfo.add(lblShiftStart);
        rightInfo.add(lblShiftEnd);

        header.add(title, BorderLayout.WEST);
        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setOpaque(false);
        content.add(buildSystemDataPanel());
        content.add(buildActualCountingPanel());
        return content;
    }

    private JPanel buildSystemDataPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createTitledBorder(null, " 1. HE THONG GHI NHAN ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), PRIMARY)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(10, 15, 10, 15);

        int row = 0;
        addRow(panel, gc, row++, "Tien dau ca:", lblOpeningCash, new Color(100, 116, 139), 14);
        addRow(panel, gc, row++, "Thu bang tien mat:", lblCashRevenue, new Color(15, 23, 42), 14);
        addRow(panel, gc, row++, "Thu bang QR/Chuyen khoan:", lblTransferRevenue, SUCCESS, 14);
        addRow(panel, gc, row++, "Thu bang the:", lblCardRevenue, PRIMARY, 14);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        panel.add(new JSeparator(), gc);
        gc.gridwidth = 1;

        addRow(panel, gc, row++, "Tong doanh thu trong ca:", lblTotalRevenue, new Color(15, 23, 42), 18);
        addRow(panel, gc, row, "Tien mat phai co tai quay:", lblExpectedCash, DANGER, 16);
        return panel;
    }

    private JPanel buildActualCountingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createTitledBorder(null, " 2. KIEM DEM THUC TE ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), DANGER)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(12, 15, 12, 15);

        int row = 0;
        gc.gridx = 0;
        gc.gridy = row;
        JLabel lblInput = new JLabel("Tien mat thuc te dem duoc:");
        lblInput.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(lblInput, gc);

        gc.gridx = 1;
        txtActualCash.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtActualCash.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(txtActualCash, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        JLabel lblDiff = new JLabel("Do lech (thua/thieu):");
        lblDiff.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(lblDiff, gc);

        gc.gridx = 1;
        lblDiscrepancy.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDiscrepancy.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblDiscrepancy, gc);

        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        styleButton(btnRefresh, new Color(100, 116, 139), Color.WHITE);
        styleButton(btnCloseShift, HEADER, Color.WHITE);
        btnCloseShift.setIcon(createIcon("icons/check.svg", 18, Color.WHITE));

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
        lbl.setFont(new Font("Segoe UI", fontSize > 14 ? Font.BOLD : Font.PLAIN, fontSize));
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
            BigDecimal openingCash = UserSessionContext.getOpeningCash() != null
                    ? UserSessionContext.getOpeningCash() : BigDecimal.ZERO;

            currentSummary = shiftReportController.getCurrentShiftSummary(staffId, shiftStart, openingCash);
            renderSummary(currentSummary);
            txtActualCash.setText(currentSummary.getExpectedCash().toPlainString());
            calculateDiscrepancy();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Khong the tai du lieu chot ca:\n" + ex.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderSummary(ShiftReportSummaryDto summary) {
        lblStaff.setText("Thu ngan: " + summary.getStaffId() + " - " + safe(summary.getStaffName()));
        lblShiftStart.setText("Bat dau ca: " + format(summary.getShiftStart()));
        lblShiftEnd.setText("Thoi gian hien tai: " + format(summary.getShiftEnd()));

        lblOpeningCash.setText(fmt(summary.getOpeningCash()));
        lblCashRevenue.setText(fmt(summary.getCashRevenue()));
        lblTransferRevenue.setText(fmt(summary.getTransferRevenue()));
        lblCardRevenue.setText(fmt(summary.getCardRevenue()));
        lblTotalRevenue.setText(fmt(summary.getTotalRevenue()));
        lblExpectedCash.setText(fmt(summary.getExpectedCash()));
    }

    private void calculateDiscrepancy() {
        if (currentSummary == null) {
            return;
        }
        try {
            BigDecimal actualCash = parseMoney(txtActualCash.getText());
            BigDecimal diff = actualCash.subtract(currentSummary.getExpectedCash());

            if (diff.compareTo(BigDecimal.ZERO) == 0) {
                lblDiscrepancy.setForeground(SUCCESS);
                lblDiscrepancy.setText("Khop (0 VND)");
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                lblDiscrepancy.setForeground(DANGER);
                lblDiscrepancy.setText("Lech thieu: " + fmt(diff));
            } else {
                lblDiscrepancy.setForeground(PRIMARY);
                lblDiscrepancy.setText("Lech du: +" + fmt(diff));
            }
            btnCloseShift.setEnabled(true);
        } catch (Exception e) {
            lblDiscrepancy.setForeground(DANGER);
            lblDiscrepancy.setText("Sai dinh dang so");
            btnCloseShift.setEnabled(false);
        }
    }

    private void closeShift() {
        if (currentSummary == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xac nhan in bao cao va chot ca?\nSau khi chot ca he thong se dang xuat.",
                "Xac nhan chot ca",
                JOptionPane.YES_NO_OPTION
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
            JOptionPane.showMessageDialog(this, "Da luu va in bao cao chot ca thanh cong.",
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);

            UserSessionContext.logout();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            new LoginFrame().setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Chot ca that bai:\n" + ex.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildShiftReportPrintText(ShiftReport r) {
        String line = "============================================\n";
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        sb.append("BAO CAO CHOT CA (Z-REPORT)\n");
        sb.append(line);
        sb.append("Ma bien ban : ").append(r.getShiftReportId()).append("\n");
        sb.append("Thu ngan    : ").append(r.getUser() != null ? r.getUser().getUserId() : "").append("\n");
        sb.append("Bat dau ca  : ").append(format(r.getShiftStart())).append("\n");
        sb.append("Ket thuc ca : ").append(format(r.getShiftEnd())).append("\n");
        sb.append("--------------------------------------------\n");
        sb.append("Tien dau ca           : ").append(fmt(r.getOpeningCash())).append("\n");
        sb.append("Thu tien mat          : ").append(fmt(r.getCashRevenue())).append("\n");
        sb.append("Thu QR/Chuyen khoan   : ").append(fmt(r.getTransferRevenue())).append("\n");
        sb.append("Thu the               : ").append(fmt(r.getCardRevenue())).append("\n");
        sb.append("Tong doanh thu        : ").append(fmt(r.getTotalRevenue())).append("\n");
        sb.append("Tien mat phai co      : ").append(fmt(r.getExpectedCash())).append("\n");
        sb.append("Tien mat thuc te dem  : ").append(fmt(r.getActualCash())).append("\n");
        sb.append("Do lech               : ").append(fmt(r.getDiscrepancy())).append("\n");
        sb.append(line);
        sb.append("Ky ten thu ngan: ________________________\n");
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
                JOptionPane.showMessageDialog(this, "In bao cao that bai: " + ex.getMessage(),
                        "Canh bao", JOptionPane.WARNING_MESSAGE);
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
        btn.setPreferredSize(new Dimension(220, 42));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String fmt(BigDecimal value) {
        BigDecimal safe = value != null ? value : BigDecimal.ZERO;
        return String.format("%,.0f VND", safe);
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
