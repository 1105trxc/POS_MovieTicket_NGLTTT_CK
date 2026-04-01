package com.cinema.management.view.management;

import com.cinema.management.controller.AuditLogController;
import com.cinema.management.model.entity.AuditLog;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.event.DocumentListener;

public class AuditLogPanel extends JPanel {

    private final AuditLogController auditLogController = new AuditLogController();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);

    private final String[] COLUMNS = { "Thời gian", "Người thực hiện", "Bảng dữ liệu", "Trường thay đổi", "Dữ liệu cũ",
            "Dữ liệu mới" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;
    private final JTextField txtSearch = new JTextField();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public AuditLogPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadTableData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("NHẬT KÝ HOẠT ĐỘNG (AUDIT LOG)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Theo dõi các thay đổi dữ liệu được thực hiện bởi người dùng hệ thống");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        header.add(left, BorderLayout.WEST);

        JButton btnRefresh = new JButton(" Làm mới");
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/refresh.svg", 16, 16);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            btnRefresh.setIcon(icon);
        } catch (Exception ignored) {
        }
        btnRefresh.setBackground(PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadTableData());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        right.setOpaque(false);
        right.add(btnRefresh);

        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel buildCenter() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        txtSearch.putClientProperty("JTextField.placeholderText",
                "Tìm theo Nhân viên, Bảng, Tên trường hoặc Dữ liệu...");
        txtSearch.setPreferredSize(new Dimension(350, 36));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Date filter
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setPreferredSize(new Dimension(150, 36));

        dateSpinner.addChangeListener(e -> {
            java.util.Date d = (java.util.Date) dateSpinner.getValue();
            java.time.LocalDate localDate = d.toInstant().atZone(java.util.TimeZone.getDefault().toZoneId())
                    .toLocalDate();
            List<AuditLog> logs = auditLogController.getLogsByDate(localDate);
            populateTable(logs);
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        top.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchBox.setOpaque(false);
        searchBox.add(new JLabel("🔍 Tìm kiếm:"));
        searchBox.add(txtSearch);

        JPanel dateBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dateBox.setOpaque(false);
        dateBox.add(new JLabel("📅 Lọc theo ngày:"));
        dateBox.add(dateSpinner);

        top.add(searchBox);
        top.add(dateBox);
        panel.add(top, BorderLayout.NORTH);

        styleTable();
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String keyword = txtSearch.getText().trim();
                List<AuditLog> logs = auditLogController.searchLogs(keyword);
                populateTable(logs);
            }
        });

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(140); // Thời gian
        table.getColumnModel().getColumn(1).setPreferredWidth(160); // Người thực hiện
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Bảng dữ liệu
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Tên trường
        table.getColumnModel().getColumn(4).setPreferredWidth(250); // Dữ liệu cũ
        table.getColumnModel().getColumn(5).setPreferredWidth(250); // Dữ liệu mới

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add a bottom warning label
        JLabel lblInfo = new JLabel("(*) Hiển thị tối đa 200 thao tác thay đổi dữ liệu gần nhất.");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(new Color(100, 116, 139));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(lblInfo);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void styleTable() {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(new Color(15, 23, 42));
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
    }

    private void loadTableData() {
        txtSearch.setText("");
        List<AuditLog> logs = auditLogController.getAllLogs();
        populateTable(logs);
    }

    private void populateTable(List<AuditLog> logs) {
        tableModel.setRowCount(0);
        for (AuditLog log : logs) {
            String timeStr = log.getChangedAt() != null ? log.getChangedAt().format(formatter) : "";
            String userStr = log.getChangedBy() != null
                    ? log.getChangedBy().getFullName() + " (" + log.getChangedBy().getUserId() + ")"
                    : "Unknown";

            tableModel.addRow(new Object[] {
                    timeStr,
                    userStr,
                    log.getTableName(),
                    log.getFieldName(),
                    log.getOldValue(),
                    log.getNewValue()
            });
        }
    }
}
