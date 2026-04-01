package com.cinema.management.view.dialog;

import com.cinema.management.controller.UserController;
import com.cinema.management.model.entity.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StaffSelectionDialog extends JDialog {

    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color BG = new Color(245, 247, 250);

    private final UserController userController = new UserController();
    private final Consumer<User> onSelect;
    private final List<User> allStaff = new ArrayList<>();

    private JTextField txtSearch;
    private JTable tblStaff;
    private DefaultTableModel tableModel;

    public StaffSelectionDialog(Window owner, Consumer<User> onSelect) {
        super(owner, "Chon nhan vien", ModalityType.APPLICATION_MODAL);
        this.onSelect = onSelect;

        setSize(760, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BG);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("Tim nhan vien:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlSearch.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhap ma NV, ho ten, so dien thoai...");
        txtSearch.setPreferredSize(new Dimension(0, 36));
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        add(pnlSearch, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterData();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterData();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterData();
            }
        });

        String[] cols = {"Ma NV", "Ho ten", "So dien thoai", "Vai tro", "Tai khoan", "Trang thai"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblStaff = new JTable(tableModel);
        tblStaff.setRowHeight(32);
        tblStaff.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblStaff.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblStaff.setSelectionBackground(new Color(224, 242, 254));
        tblStaff.setSelectionForeground(new Color(15, 23, 42));

        JScrollPane scrollPane = new JScrollPane(tblStaff);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scrollPane, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        JButton btnSelect = new JButton("Chon nhan vien");
        btnSelect.setBackground(PRIMARY);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton btnCancel = new JButton("Huy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSelect);
        add(pnlButtons, BorderLayout.SOUTH);

        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());
        tblStaff.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmSelection();
                }
            }
        });
    }

    private void loadData() {
        allStaff.clear();
        List<User> users = userController.getAllUsers();
        if (users != null) {
            allStaff.addAll(users.stream()
                    .filter(Objects::nonNull)
                    .sorted((a, b) -> safe(a.getFullName()).compareToIgnoreCase(safe(b.getFullName())))
                    .toList());
        }
        loadToTable(allStaff);
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadToTable(allStaff);
            return;
        }

        List<User> filtered = allStaff.stream()
                .filter(u -> safe(u.getUserId()).toLowerCase().contains(kw)
                        || safe(u.getFullName()).toLowerCase().contains(kw)
                        || safe(u.getPhone()).toLowerCase().contains(kw)
                        || safe(u.getUsername()).toLowerCase().contains(kw))
                .toList();
        loadToTable(filtered);
    }

    private void loadToTable(List<User> data) {
        tableModel.setRowCount(0);
        for (User u : data) {
            String roleName = u.getRole() != null ? safe(u.getRole().getRoleName()) : "";
            String account = safe(u.getUsername());
            String status = Boolean.TRUE.equals(u.getIsActive()) ? "Hoat dong" : "Bi khoa";
            tableModel.addRow(new Object[]{
                    safe(u.getUserId()),
                    safe(u.getFullName()),
                    safe(u.getPhone()),
                    roleName,
                    account,
                    status
            });
        }
    }

    private void confirmSelection() {
        int row = tblStaff.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot nhan vien!", "Luu y", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = String.valueOf(tableModel.getValueAt(row, 0));
        User selected = allStaff.stream()
                .filter(u -> safe(u.getUserId()).equals(userId))
                .findFirst()
                .orElse(null);
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Khong tim thay thong tin nhan vien.", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (onSelect != null) {
            onSelect.accept(selected);
        }
        dispose();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
