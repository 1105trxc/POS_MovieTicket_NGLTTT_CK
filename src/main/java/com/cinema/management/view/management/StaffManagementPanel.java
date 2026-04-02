package com.cinema.management.view.management;

import com.cinema.management.controller.UserController;
import com.cinema.management.model.entity.User;
import com.cinema.management.util.IdGenerator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class StaffManagementPanel extends JPanel {

    private final UserController userController = new UserController();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);

    private final String[] COLUMNS = { "Mã NV", "Họ Tên", "SĐT", "Ngày Sinh", "Giới Tính", "CCCD", "Email",
            "Trạng thái" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    private final JTextField txtId = new JTextField(16);
    private final JTextField txtFullName = new JTextField(16);
    private final JTextField txtPhone = new JTextField(16);
    private final JTextField txtBirthDate = new JTextField(16); // dd/MM/yyyy
    private final JComboBox<String> cbGender = new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" });
    private final JTextField txtCCCD = new JTextField(16);
    private final JTextField txtEmail = new JTextField(16);
    private final JCheckBox chkInactive = new JCheckBox("Đã nghỉ làm");

    private final JButton btnAdd = new JButton("Thêm mới");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnClear = new JButton("Clear Form");

    private User selectedUser;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StaffManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        // Khóa mã không cho nhập tay
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));

        txtBirthDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadTable();
        configureTableSelection();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Tạo hồ sơ nhân sự vật lý");
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

        JLabel lblSearch = new JLabel(" Tìm nhanh:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterBar.add(lblSearch);

        JTextField txtLiveSearch = new JTextField();
        txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã NV, Tên, CCCD...");
        txtLiveSearch.setPreferredSize(new Dimension(350, 36));
        filterBar.add(txtLiveSearch);

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
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Thông tin chi tiết ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(250, 36);
        txtId.setPreferredSize(fieldSize);
        txtFullName.setPreferredSize(fieldSize);
        txtPhone.setPreferredSize(fieldSize);
        txtBirthDate.setPreferredSize(fieldSize);
        cbGender.setPreferredSize(fieldSize);
        txtCCCD.setPreferredSize(fieldSize);
        txtEmail.setPreferredSize(fieldSize);

        // Add components
        int row = 0;
        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Mã NV:"), gc);
        gc.gridx = 1;
        fields.add(txtId, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Họ tên:"), gc);
        gc.gridx = 1;
        fields.add(txtFullName, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("SĐT:"), gc);
        gc.gridx = 1;
        fields.add(txtPhone, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Ngày sinh:"), gc);
        gc.gridx = 1;
        fields.add(txtBirthDate, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Giới tính:"), gc);
        gc.gridx = 1;
        fields.add(cbGender, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("CCCD:"), gc);
        gc.gridx = 1;
        fields.add(txtCCCD, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Email:"), gc);
        gc.gridx = 1;
        fields.add(txtEmail, gc);
        row++;

        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(chkInactive, BorderLayout.NORTH);
        JLabel lblCheckHint = new JLabel(
                "<html><i style='font-size:10px; color:gray;'>*Tick nếu nhân viên đã nghỉ việc</i></html>");
        pnlStatus.add(lblCheckHint, BorderLayout.CENTER);

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Trạng thái:"), gc);
        gc.gridx = 1;
        fields.add(pnlStatus, gc);

        panel.add(fields, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 10));
        buttons.setOpaque(false);
        styleActionButtons();

        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnClear.addActionListener(e -> clearForm());

        btnUpdate.setEnabled(false);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnClear);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void styleActionButtons() {
        btnAdd.setBackground(SUCCESS);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdate.setBackground(PRIMARY);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        if (!validateForm())
            return;

        User user = new User();
        // Tự động sinh ID NV mới
        user.setUserId(IdGenerator.generateId("NV", User.class, "userId"));
        user.setFullName(txtFullName.getText().trim());
        user.setPhone(txtPhone.getText().trim());

        try {
            user.setBirthDate(LocalDate.parse(txtBirthDate.getText().trim(), formatter));
        } catch (DateTimeParseException ex) {
            showError("Ngày sinh không hợp lệ (định dạng dd/MM/yyyy).");
            return;
        }

        user.setGender((String) cbGender.getSelectedItem());
        user.setCccd(txtCCCD.getText().trim());
        user.setEmail(txtEmail.getText().trim());
        user.setIsActive(!chkInactive.isSelected());

        // Default username/password as null according to rules, created later
        user.setUsername(null);
        user.setPassword(null);

        try {
            userController.addUser(user);
            showSuccess("Thêm nhân viên thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi thêm nhân viên: " + ex.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedUser == null || !validateForm())
            return;

        selectedUser.setFullName(txtFullName.getText().trim());
        selectedUser.setPhone(txtPhone.getText().trim());

        try {
            selectedUser.setBirthDate(LocalDate.parse(txtBirthDate.getText().trim(), formatter));
        } catch (DateTimeParseException ex) {
            showError("Ngày sinh không hợp lệ (định dạng dd/MM/yyyy).");
            return;
        }

        selectedUser.setGender((String) cbGender.getSelectedItem());
        selectedUser.setCccd(txtCCCD.getText().trim());
        selectedUser.setEmail(txtEmail.getText().trim());
        selectedUser.setIsActive(!chkInactive.isSelected());

        try {
            userController.updateUser(selectedUser);
            showSuccess("Cập nhật nhân viên thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError("Lỗi cập nhật nhân viên: " + ex.getMessage());
        }
    }

    private boolean validateForm() {
        if (txtFullName.getText().trim().isEmpty()) {
            showError("Họ tên không được để trống!");
            return false;
        }

        // Validate CCCD: 12 chữ số
        String cccd = txtCCCD.getText().trim();
        if (cccd.isEmpty()) {
            showError("CCCD không được để trống!");
            return false;
        }
        if (!cccd.matches("\\d{12}")) {
            showError("CCCD phải gồm đúng 12 chữ số!");
            return false;
        }

        // Validate SĐT: 10-11 chữ số
        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d{10,11}")) {
            showError("Số điện thoại phải gồm 10-11 chữ số!");
            return false;
        }

        // Validate Email
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Email không hợp lệ! Vui lòng nhập đúng định dạng (vd: abc@gmail.com).");
            return false;
        }

        // Validate ngày sinh
        String birthStr = txtBirthDate.getText().trim();
        if (!birthStr.isEmpty()) {
            try {
                LocalDate.parse(birthStr, formatter);
            } catch (DateTimeParseException ex) {
                showError("Ngày sinh không hợp lệ (định dạng dd/MM/yyyy).");
                return false;
            }
        }

        return true;
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<User> users = userController.getAllUsers();
        for (User u : users) {
            String dob = u.getBirthDate() != null ? u.getBirthDate().format(formatter) : "";
            String status = u.getIsActive() != null && u.getIsActive() ? "Đang làm việc" : "Đã nghỉ việc";
            tableModel.addRow(new Object[] {
                    u.getUserId(), u.getFullName(), u.getPhone(), dob, u.getGender(), u.getCccd(), u.getEmail(), status
            });
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
            selectedUser = userController.getAllUsers().stream()
                    .filter(u -> u.getUserId().equals(userId))
                    .findFirst().orElse(null);

            if (selectedUser != null) {
                txtId.setText(selectedUser.getUserId());
                txtId.setEnabled(false); // Khóa khi cập nhật
                txtFullName.setText(selectedUser.getFullName());
                txtPhone.setText(selectedUser.getPhone());
                txtBirthDate.setText(
                        selectedUser.getBirthDate() != null ? selectedUser.getBirthDate().format(formatter) : "");
                cbGender.setSelectedItem(selectedUser.getGender());
                txtCCCD.setText(selectedUser.getCccd());
                txtEmail.setText(selectedUser.getEmail());
                chkInactive.setSelected(selectedUser.getIsActive() != null && !selectedUser.getIsActive());

                btnUpdate.setEnabled(true);
                btnAdd.setEnabled(false);
            }
        });
    }

    private void clearForm() {
        selectedUser = null;
        txtId.setText(IdGenerator.generateId("NV", User.class, "userId"));
        txtId.setEnabled(false); // Luôn khóa không cho nhập tay
        txtFullName.setText("");
        txtPhone.setText("");
        txtBirthDate.setText("");
        cbGender.setSelectedIndex(0);
        txtCCCD.setText("");
        txtEmail.setText("");
        chkInactive.setSelected(false);
        table.clearSelection();

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
