package com.cinema.management.view.management;

import com.cinema.management.controller.CustomerController;
import com.cinema.management.model.entity.Customer;
import com.cinema.management.util.IdGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class CustomerManagementPanel extends JPanel {

    private final CustomerController customerController = new CustomerController();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);

    private final String[] COLUMNS = { "Mã KH", "Họ Tên", "Số Điện Thoại", "Điểm", "Hạng", "Chi tiêu" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    private final JTextField txtId = new JTextField();
    private final JTextField txtFullName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtPoints = new JTextField();
    private final JTextField txtTier = new JTextField();

    private final JButton btnAdd = new JButton("Thêm mới");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnClear = new JButton("Clear Form");
    private final JButton btnRefresh = new JButton("Làm mới");

    private Customer selectedCustomer;

    public CustomerManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        // Header đã được dời ra ngoài CustomerCRMPanel
        add(buildCenter(), BorderLayout.CENTER);

        loadTable();
        configureTableSelection();
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(15, 0));
        center.setOpaque(false);

        // Sidebar Form
        center.add(buildFormPanel(), BorderLayout.EAST);
        // Table
        center.add(buildTableArea(), BorderLayout.CENTER);

        return center;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        JTextField txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã KH, Tên, Số điện thoại...");
        txtSearch.setPreferredSize(new Dimension(300, 36));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);
        top.add(new JLabel("🔍 Tìm kiếm:  "));
        top.add(txtSearch);

        btnRefresh.setBackground(new Color(100, 116, 139));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(100, 36));
        btnRefresh.setFocusPainted(false);
        top.add(btnRefresh);

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadTable();
        });

        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setOpaque(false);
        filterWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        filterWrapper.add(top, BorderLayout.CENTER);
        panel.add(filterWrapper, BorderLayout.NORTH);

        styleTable(table);
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

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(CARD);
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " THÔNG TIN KHÁCH HÀNG ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));
        txtPoints.setEditable(false);
        txtPoints.setBackground(new Color(241, 245, 249));
        txtTier.setEditable(false);
        txtTier.setBackground(new Color(241, 245, 249));

        // Add components
        int r = 0;
        addField(fields, "Mã KH:", txtId, gc, r++);
        addField(fields, "Họ Tên:", txtFullName, gc, r++);
        addField(fields, "SĐT:", txtPhone, gc, r++);
        addField(fields, "Điểm:", txtPoints, gc, r++);
        addField(fields, "Hạng:", txtTier, gc, r++);

        panel.add(fields, BorderLayout.NORTH);

        JPanel bot = new JPanel(new GridLayout(1, 3, 10, 0));
        bot.setOpaque(false);
        btnAdd.setBackground(SUCCESS);
        btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(PRIMARY);
        btnUpdate.setForeground(Color.WHITE);

        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnClear.addActionListener(e -> clearForm());

        bot.add(btnAdd);
        bot.add(btnUpdate);
        bot.add(btnClear);
        panel.add(bot, BorderLayout.SOUTH);

        clearForm();
        return panel;
    }

    private void addField(JPanel p, String lbl, JTextField tf, GridBagConstraints gc, int r) {
        gc.gridx = 0;
        gc.gridy = r;
        gc.weightx = 0;
        p.add(new JLabel(lbl), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        tf.setPreferredSize(new Dimension(150, 36));
        p.add(tf, gc);
    }

    private void styleTable(JTable tbl) {
        tbl.setRowHeight(38);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tbl.setSelectionBackground(new Color(224, 242, 254));
        tbl.setSelectionForeground(new Color(15, 23, 42));
        tbl.setGridColor(new Color(241, 245, 249));
        tbl.setShowVerticalLines(false);
    }

    private void onAdd() {
        String name = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được để trống!");
            return;
        }
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống!");
            return;
        }
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10-11 chữ số!");
            return;
        }
        Customer c = new Customer();
        c.setCustomerId(IdGenerator.generateId("CS", Customer.class, "customerId"));
        c.setFullName(txtFullName.getText().trim());
        c.setPhone(txtPhone.getText().trim());
        c.setRewardPoints(0);
        c.setMemberTier("Basic");
        c.setTotalSpent(java.math.BigDecimal.ZERO);

        try {
            customerController.createCustomer(c);
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công.");
            loadTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedCustomer == null)
            return;

        String name = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được để trống!");
            return;
        }
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống!");
            return;
        }
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10-11 chữ số!");
            return;
        }

        selectedCustomer.setFullName(name);
        selectedCustomer.setPhone(phone);

        try {
            customerController.updateCustomer(selectedCustomer);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công.");
            loadTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<Customer> list = customerController.getAllCustomers();
        for (Customer c : list) {
            tableModel.addRow(new Object[] {
                    c.getCustomerId(), c.getFullName(), c.getPhone(), c.getRewardPoints(), c.getMemberTier(),
                    c.getTotalSpent()
            });
        }
    }

    private void configureTableSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            String cid = (String) table.getValueAt(row, 0);
            selectedCustomer = customerController.getAllCustomers().stream()
                    .filter(c -> c.getCustomerId().equals(cid))
                    .findFirst().orElse(null);

            if (selectedCustomer != null) {
                txtId.setText(selectedCustomer.getCustomerId());
                txtFullName.setText(selectedCustomer.getFullName());
                txtPhone.setText(selectedCustomer.getPhone());
                txtPoints.setText(String.valueOf(selectedCustomer.getRewardPoints()));
                txtTier.setText(selectedCustomer.getMemberTier());
                btnAdd.setEnabled(false);
                btnUpdate.setEnabled(true);
            }
        });
    }

    private void clearForm() {
        selectedCustomer = null;
        txtId.setText(IdGenerator.generateId("CS", Customer.class, "customerId"));
        txtFullName.setText("");
        txtPhone.setText("");
        txtPoints.setText("0");
        txtTier.setText("Basic");
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
    }
}
