package com.cinema.management.view.dialog;

import com.cinema.management.controller.CustomerController;
import com.cinema.management.model.entity.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CustomerSelectionDialog extends JDialog {

    private final CustomerController customerController;
    private List<Customer> customers;
    private Customer selectedCustomer = null;

    private JTextField txtSearch;
    private JTable tblCustomer;
    private DefaultTableModel tableModel;

    // Add new customer form
    private JTextField txtNewFullName;
    private JTextField txtNewPhone;
    private JTextField txtNewEmail;
    private JButton btnAddCustomer;

    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color BG = new Color(245, 247, 250);

    public CustomerSelectionDialog(Window owner, String initialSearchValue) {
        super(owner, "Tìm kiếm & Thêm thành viên", ModalityType.APPLICATION_MODAL);
        this.customerController = new CustomerController();
        this.customers = customerController.getAllCustomers();

        setSize(800, 550);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BG);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        buildUI();
        loadDataToTable(customers);

        if (initialSearchValue != null && !initialSearchValue.isEmpty()) {
            txtSearch.setText(initialSearchValue);
            filterData();
            txtNewPhone.setText(initialSearchValue);
        }
    }

    private void buildUI() {
        // --- Left: Search & Table ---
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
        pnlLeft.setOpaque(false);

        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("🔍 Tìm SĐT/Tên:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlSearch.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập SĐT hoặc Tên...");
        txtSearch.setPreferredSize(new Dimension(0, 36));
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlLeft.add(pnlSearch, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterData(); updateNewPhone(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterData(); updateNewPhone(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); updateNewPhone(); }
        });

        String[] cols = {"Mã KH", "Họ tên", "Số ĐT", "Hạng", "Điểm"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCustomer = new JTable(tableModel);
        tblCustomer.setRowHeight(30);
        tblCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCustomer.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblCustomer.setSelectionBackground(new Color(224, 242, 254));

        tblCustomer.getColumnModel().getColumn(0).setMinWidth(0);
        tblCustomer.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(tblCustomer);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        pnlLeft.add(scrollPane, BorderLayout.CENTER);

        // --- Right: Add new Form ---
        JPanel pnlRight = new JPanel(new BorderLayout(0, 10));
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setPreferredSize(new Dimension(280, 0));
        pnlRight.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblAddTitle = new JLabel("THÊM MỚI KHÁCH HÀNG");
        lblAddTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAddTitle.setForeground(PRIMARY);
        pnlRight.add(lblAddTitle, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridLayout(6, 1, 0, 5));
        pnlForm.setOpaque(false);
        
        pnlForm.add(new JLabel("Họ và Tên:"));
        txtNewFullName = new JTextField();
        pnlForm.add(txtNewFullName);

        pnlForm.add(new JLabel("Số điện thoại:"));
        txtNewPhone = new JTextField();
        pnlForm.add(txtNewPhone);

        pnlForm.add(new JLabel("Email (Tùy chọn):"));
        txtNewEmail = new JTextField();
        pnlForm.add(txtNewEmail);

        pnlRight.add(pnlForm, BorderLayout.CENTER);

        btnAddCustomer = new JButton("Thêm Mới KH Của Rạp");
        btnAddCustomer.setBackground(SUCCESS);
        btnAddCustomer.setForeground(Color.WHITE);
        btnAddCustomer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddCustomer.setPreferredSize(new Dimension(80, 40));
        btnAddCustomer.addActionListener(e -> addNewCustomer());
        pnlRight.add(btnAddCustomer, BorderLayout.SOUTH);

        // --- Container split ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlLeft, pnlRight);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(10);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);

        // --- Bottom Buttons ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnSelect = new JButton("Chọn khách hàng");
        btnSelect.setBackground(PRIMARY);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSelect);
        add(pnlButtons, BorderLayout.SOUTH);

        // --- Events ---
        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());
        tblCustomer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Customer> list) {
        tableModel.setRowCount(0);
        for (Customer c : list) {
            tableModel.addRow(new Object[] {
                    c.getCustomerId(),
                    c.getFullName(),
                    c.getPhone(),
                    c.getMemberTier(),
                    c.getRewardPoints()
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadDataToTable(customers);
            return;
        }
        List<Customer> filtered = customers.stream()
                .filter(c -> (c.getFullName() != null && c.getFullName().toLowerCase().contains(kw)) ||
                             (c.getPhone() != null && c.getPhone().contains(kw)))
                .toList();
        loadDataToTable(filtered);
    }
    
    private void updateNewPhone() {
        String kw = txtSearch.getText().trim();
        if (kw.matches("\\d+")) {
            txtNewPhone.setText(kw);
        }
    }

    private void confirmSelection() {
        int row = tblCustomer.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            selectedCustomer = customers.stream().filter(c -> c.getCustomerId().equals(id)).findFirst().orElse(null);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trong danh sách!", "Lưu ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addNewCustomer() {
        String name = txtNewFullName.getText().trim();
        String phone = txtNewPhone.getText().trim();
        String email = txtNewEmail.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên và Số điện thoại không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Customer existing = customerController.findByPhone(phone);
        if (existing != null) {
            JOptionPane.showMessageDialog(this, "Số điện thoại này đã tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Customer c = new Customer();
            c.setCustomerId("CUS" + System.currentTimeMillis() % 1000000);
            c.setFullName(name);
            c.setPhone(phone);
            c.setEmail(email);

            customerController.createCustomer(c);
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            // reload list & select the new one
            customers = customerController.getAllCustomers();
            filterData();
            
            // Select the newly added record
            for (int i = 0; i < tblCustomer.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(c.getCustomerId())) {
                    tblCustomer.setRowSelectionInterval(i, i);
                    break;
                }
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể thêm khách hàng:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }
}
