package com.cinema.management.view.management;

import com.cinema.management.controller.UserController;
import com.cinema.management.model.entity.Role;
import com.cinema.management.model.entity.User;
import com.cinema.management.repository.RoleRepository;
import com.cinema.management.view.dialog.StaffSelectionDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserController userController = new UserController();
    private final RoleRepository roleRepository = new RoleRepository();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);

    private final String[] COLUMNS = { "Mã NV", "Họ Tên", "Tên Đăng Nhập", "Quyền", "Trạng thái" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    private final JTextField txtId = new JTextField(12);
    private final JTextField txtFullName = new JTextField(16);
    private final JTextField txtUsername = new JTextField(16);
    private final JPasswordField txtPassword = new JPasswordField(16);
    private final JComboBox<RoleItem> cbRole = new JComboBox<>();

    private final JButton btnSearchStaff = new JButton("🔍");
    private final JButton btnUpdateStatus = new JButton("Khóa / Mở Acc");
    private final JButton btnConfirm = new JButton("Xác nhận");
    private final JButton btnClear = new JButton("Clear Form");

    private User selectedUser;

    public UserManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadRoles();
        loadTable();
        configureTableSelection();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Cấp quyền hệ thống cho nhân viên");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);
        centerContainer.add(buildTableArea(), BorderLayout.CENTER);
        centerContainer.add(buildFormPanel(), BorderLayout.EAST);
        return centerContainer;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setOpaque(false);
        JLabel lblSearch = new JLabel(" Tìm Nhanh:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterBar.add(lblSearch);

        // JTextField txtLiveSearch = new JTextField();
        // txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã NV
        // hoặc Username...");
        // txtLiveSearch.setPreferredSize(new Dimension(350, 36));
        // filterBar.add(txtLiveSearch);
        JTextField txtLiveSearch = new JTextField(20);
        txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã NV hoặc Username...");

        // Sử dụng setMaximumSize hoặc setColumns thay vì chỉ dùng PreferredSize
        // để tránh bị Layout Manager "bóp" méo
        txtLiveSearch.setPreferredSize(new Dimension(350, 35));
        txtLiveSearch.setMinimumSize(new Dimension(200, 35));

        // Đảm bảo filterBar không bị co giãn quá mức
        filterBar.add(lblSearch);
        filterBar.add(txtLiveSearch);

        // Thêm một khoảng trống linh hoạt phía sau để đẩy thanh tìm kiếm về bên trái
        // (nếu cần)
        filterBar.add(Box.createHorizontalGlue());

        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setOpaque(false);
        filterWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        filterWrapper.add(filterBar, BorderLayout.CENTER);
        panel.add(filterWrapper, BorderLayout.NORTH);

        styleTable(table);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        txtLiveSearch.getDocument().addDocumentListener(new DocumentListener() {
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
                String text = txtLiveSearch.getText().trim();
                if (text.isEmpty())
                    rowSorter.setRowFilter(null);
                else
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(CARD);
        panel.setPreferredSize(new Dimension(450, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Thông tin tài khoản ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(250, 36);
        txtFullName.setPreferredSize(fieldSize);
        txtUsername.setPreferredSize(fieldSize);
        txtPassword.setPreferredSize(fieldSize);
        cbRole.setPreferredSize(fieldSize);

        txtId.setEditable(false);
        txtFullName.setEditable(false);
        btnSearchStaff.setToolTipText("Chọn Nhân Viên");
        btnSearchStaff.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            new StaffSelectionDialog(parent, this::fillStaffInfo).setVisible(true);
        });

        JPanel pnlStaffId = new JPanel(new BorderLayout(5, 0));
        pnlStaffId.setOpaque(false);
        pnlStaffId.add(txtId, BorderLayout.CENTER);
        pnlStaffId.add(btnSearchStaff, BorderLayout.EAST);

        int row = 0;
        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Chọn NV:"), gc);
        gc.gridx = 1;
        fields.add(pnlStaffId, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Họ tên:"), gc);
        gc.gridx = 1;
        fields.add(txtFullName, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        fields.add(txtUsername, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Mật khẩu:"), gc);
        gc.gridx = 1;
        fields.add(txtPassword, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Vai trò:"), gc);
        gc.gridx = 1;
        fields.add(cbRole, gc);

        panel.add(fields, BorderLayout.NORTH);

        JPanel pnlActionContent = new JPanel(new BorderLayout(0, 10));
        pnlActionContent.setOpaque(false);

        JLabel lblConfirmDetail = new JLabel(
                "<html><i>*Chọn nhân viên, nhập Username + Mật khẩu<br>rồi nhấn <b>Xác nhận</b> để lưu.</i></html>");
        lblConfirmDetail.setForeground(Color.GRAY);
        pnlActionContent.add(lblConfirmDetail, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);
        styleActionButtons();

        JButton btnLocalRefresh = new JButton("Làm mới");
        btnLocalRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLocalRefresh.setBackground(new Color(148, 163, 184));
        btnLocalRefresh.setForeground(Color.WHITE);
        btnLocalRefresh.addActionListener(e -> {
            loadRoles();
            loadTable();
            showSuccess("Đã cập nhật danh sách tài khoản và quyền.");
        });

        btnUpdateStatus.addActionListener(e -> onToggleStatus());
        btnConfirm.addActionListener(e -> onSaveAccount());
        btnClear.addActionListener(e -> clearForm());

        btnUpdateStatus.setEnabled(false);
        btnConfirm.setEnabled(false);

        buttons.add(btnConfirm);
        buttons.add(btnUpdateStatus);
        buttons.add(btnLocalRefresh);
        buttons.add(btnClear);

        pnlActionContent.add(buttons, BorderLayout.CENTER);
        panel.add(pnlActionContent, BorderLayout.SOUTH);

        return panel;
    }

    private void styleActionButtons() {
        btnConfirm.setBackground(SUCCESS);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdateStatus.setBackground(DANGER);
        btnUpdateStatus.setForeground(Color.WHITE);
        btnUpdateStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void styleTable(JTable tbl) {
        tbl.setRowHeight(38);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tbl.setSelectionBackground(new Color(224, 242, 254));
        tbl.setSelectionForeground(new Color(15, 23, 42));
        tbl.setShowVerticalLines(false);
    }

    private void loadRoles() {
        cbRole.removeAllItems();
        List<Role> roles = roleRepository.findAll();
        for (Role r : roles) {
            // Bỏ qua vai trò "Thu ngân" và "Quản lý rạp"
            String name = r.getRoleName();
            if (name != null && (name.equalsIgnoreCase("Thu ngân")
                    || name.equalsIgnoreCase("Quản lý rạp"))) {
                continue;
            }
            cbRole.addItem(new RoleItem(r.getRoleId(), r.getRoleName()));
        }
    }

    private void fillStaffInfo(User user) {
        selectedUser = user;
        txtId.setText(user.getUserId());
        txtFullName.setText(user.getFullName());
        txtUsername.setText(user.getUsername() != null ? user.getUsername() : "");
        txtPassword.setText("");

        if (user.getRole() != null) {
            for (int i = 0; i < cbRole.getItemCount(); i++) {
                if (cbRole.getItemAt(i).roleId.equals(user.getRole().getRoleId())) {
                    cbRole.setSelectedIndex(i);
                    break;
                }
            }
        }

        boolean hasAccount = user.getUsername() != null && !user.getUsername().isEmpty();
        btnConfirm.setEnabled(true);
        btnUpdateStatus.setEnabled(hasAccount);
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<User> users = userController.getAllUsers();
        for (User u : users) {
            // Chỉ show nhan vien co account (username != null) + Role khac null
            if (u.getUsername() != null && !u.getUsername().isEmpty()) {
                String roleName = (u.getRole() != null) ? u.getRole().getRoleName() : "Không gán";
                String status = (u.getIsActive() != null && u.getIsActive()) ? "Hoạt động" : "Bị khóa";
                tableModel.addRow(new Object[] { u.getUserId(), u.getFullName(), u.getUsername(), roleName, status });
            }
        }
    }

    private void configureTableSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                clearForm();
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            String userId = (String) tableModel.getValueAt(modelRow, 0);

            User u = userController.getAllUsers().stream().filter(x -> x.getUserId().equals(userId)).findFirst()
                    .orElse(null);
            if (u != null) {
                fillStaffInfo(u);
            }
        });
    }

    private void onSaveAccount() {
        if (selectedUser == null) {
            showError("Vui lòng chọn nhân viên!");
            return;
        }

        String newUsername = txtUsername.getText().trim();
        if (newUsername.isEmpty()) {
            showError("Username không được để trống!");
            return;
        }

        String newPass = new String(txtPassword.getPassword()).trim();
        boolean hasExistingAccount = selectedUser.getUsername() != null && !selectedUser.getUsername().isEmpty();
        if (!hasExistingAccount && newPass.isEmpty()) {
            showError("Mật khẩu không được để trống khi tạo tài khoản mới!");
            return;
        }

        selectedUser.setUsername(newUsername);
        if (!newPass.isEmpty()) {
            selectedUser.setPassword(newPass);
        }

        RoleItem selectedRole = (RoleItem) cbRole.getSelectedItem();
        if (selectedRole != null) {
            Role role = new Role();
            role.setRoleId(selectedRole.roleId);
            selectedUser.setRole(role);
        }

        try {
            // Lưu userId trước vì loadTable() sẽ trigger clearForm() → selectedUser = null
            String savedUserId = selectedUser.getUserId();
            userController.updateUser(selectedUser);
            showSuccess(hasExistingAccount ? "Cập nhật tài khoản thành công." : "Tạo tài khoản thành công.");
            loadTable();
            // Re-fetch user từ DB để đảm bảo role được load đầy đủ (có roleName)
            User refreshed = userController.getAllUsers().stream()
                    .filter(u -> u.getUserId().equals(savedUserId))
                    .findFirst().orElse(null);
            if (refreshed != null) {
                fillStaffInfo(refreshed);
            }
        } catch (Exception ex) {
            showError("Lỗi lưu tài khoản: " + ex.getMessage());
        }
    }

    private void onToggleStatus() {
        if (selectedUser == null)
            return;
        boolean current = selectedUser.getIsActive() != null && selectedUser.getIsActive();
        selectedUser.setIsActive(!current);

        try {
            userController.updateUser(selectedUser);
            showSuccess(current ? "Đã khóa tài khoản" : "Đã mở khóa tài khoản");
            loadTable();
        } catch (Exception ex) {
            showError("Lỗi khóa/Mở Acc: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedUser = null;
        txtId.setText("");
        txtFullName.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        if (cbRole.getItemCount() > 0)
            cbRole.setSelectedIndex(0);

        btnConfirm.setEnabled(false);
        btnUpdateStatus.setEnabled(false);
        table.clearSelection();
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // DTO for Combobox Role
    private static class RoleItem {
        String roleId;
        String name;

        public RoleItem(String r, String n) {
            this.roleId = r;
            this.name = n;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
