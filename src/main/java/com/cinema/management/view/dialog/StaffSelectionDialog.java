package com.cinema.management.view.dialog;

import com.cinema.management.controller.UserController;
import com.cinema.management.model.entity.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class StaffSelectionDialog extends JDialog {

    private final String[] COLUMNS = { "Mã NV", "Họ Tên", "SĐT", "CCCD" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;
    private final JTextField txtSearch = new JTextField(20);

    private final UserController userController = new UserController();
    private Consumer<User> onStaffSelectedCallback;

    public StaffSelectionDialog(Window owner, Consumer<User> onSelectedCallback) {
        super(owner, "Chọn Nhân Viên (Nháy đúp để chọn)", ModalityType.APPLICATION_MODAL);
        this.onStaffSelectedCallback = onSelectedCallback;

        setSize(550, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlNorth.setBorder(new EmptyBorder(10, 10, 0, 10));
        pnlNorth.add(new JLabel("🔍 Tìm NV (Tên/SĐT/CCCD):"));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        pnlNorth.add(txtSearch);
        add(pnlNorth, BorderLayout.NORTH);

        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 10, 10, 10),
                BorderFactory.createLineBorder(Color.GRAY)));
        add(scrollPane, BorderLayout.CENTER);

        // Events
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty())
                    rowSorter.setRowFilter(null);
                else
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectRowAndClose();
                }
            }
        });

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<User> list = userController.getAllUsers();
        for (User u : list) {
            // Chỉ hiển thị staff chưa có username (chưa được cấp tk)
            // hoặc hiển thị tất cả tuỳ logic, rules bảo cấp tk cho staff đã có
            tableModel.addRow(new Object[] { u.getUserId(), u.getFullName(), u.getPhone(), u.getCccd() });
        }
    }

    private void selectRowAndClose() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0)
            return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        String userId = (String) tableModel.getValueAt(modelRow, 0);

        User selectedUser = userController.getAllUsers().stream()
                .filter(u -> u.getUserId().equals(userId)).findFirst().orElse(null);

        if (selectedUser != null && onStaffSelectedCallback != null) {
            onStaffSelectedCallback.accept(selectedUser);
            dispose();
        }
    }
}
