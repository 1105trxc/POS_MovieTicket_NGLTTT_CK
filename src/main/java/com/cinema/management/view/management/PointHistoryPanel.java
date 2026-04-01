package com.cinema.management.view.management;

import com.cinema.management.model.entity.PointHistory;
import com.cinema.management.service.IPointService;
import com.cinema.management.service.impl.PointServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PointHistoryPanel extends JPanel {

    private final IPointService pointService = new PointServiceImpl();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);

    private final String[] COLUMNS = { "Mã GD", "Khách hàng", "Số điện thoại", "Loại", "Số điểm", "Nội dung",
            "Thời gian" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PointHistoryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Tìm kiếm giao dịch:  ");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Tên khách hàng, Số điện thoại...");
        txtSearch.setPreferredSize(new Dimension(400, 36));
        panel.add(txtSearch);

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
                String text = txtSearch.getText().trim();
                if (text.isEmpty())
                    rowSorter.setRowFilter(null);
                else
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        return panel;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(5, 5, 5, 5)));

        styleTable();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
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

        // Custom renderer for Point column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    int points = (int) value;
                    if (points > 0) {
                        c.setForeground(SUCCESS);
                        setText("+" + points);
                    } else if (points < 0) {
                        c.setForeground(DANGER);
                        setText(String.valueOf(points));
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                }
                setFont(getFont().deriveFont(Font.BOLD));
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        // Center align "Loại" column
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if ("EARN".equals(value)) {
                    setText("Tích điểm");
                    c.setForeground(SUCCESS);
                } else if ("REDEEM".equals(value)) {
                    setText("Đổi điểm");
                    c.setForeground(DANGER);
                }
                return c;
            }
        });
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<PointHistory> list = pointService.getAllPointHistories();
        for (PointHistory ph : list) {
            String customerName = ph.getCustomer() != null ? ph.getCustomer().getFullName() : "N/A";
            String customerPhone = ph.getCustomer() != null ? ph.getCustomer().getPhone() : "N/A";
            tableModel.addRow(new Object[] {
                    ph.getHistoryId(),
                    customerName,
                    customerPhone,
                    ph.getTransactionType(),
                    ph.getPointAmount(),
                    ph.getDescription(),
                    ph.getCreatedAt().format(dtf)
            });
        }
    }
}
