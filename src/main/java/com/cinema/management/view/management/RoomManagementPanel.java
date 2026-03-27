package com.cinema.management.view.management;

import com.cinema.management.controller.RoomController;
import com.cinema.management.model.entity.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel Quản lý Phòng chiếu.
 * Tuân thủ MVC: chỉ gọi Controller, không có business logic.
 * Layout: trái = JTable danh sách phòng | phải = Form thêm/sửa.
 */
public class RoomManagementPanel extends JPanel {

    // ── Controller ───────────────────────────────────────────────────────────
    private final RoomController roomController = new RoomController();

    // ── Table ────────────────────────────────────────────────────────────────
    private final String[] COLUMNS = {"Mã phòng", "Tên phòng", "Sức chứa"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    // ── Form fields ──────────────────────────────────────────────────────────
    private final JTextField txtRoomName = new JTextField(20);
    private final JSpinner spinCapacity   = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
    private final JButton btnAdd    = new JButton("Thêm");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnClear  = new JButton("Làm mới");

    /** ID của phòng đang được chọn để sửa/xóa; null khi đang ở mode Thêm. */
    private String selectedRoomId = null;

    // ── Colors ───────────────────────────────────────────────────────────────
    private static final Color PRIMARY   = new Color(41, 128, 185);
    private static final Color DANGER    = new Color(192, 57, 43);
    private static final Color SUCCESS   = new Color(39, 174, 96);
    private static final Color BG_HEADER = new Color(52, 73, 94);

    public RoomManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(Color.WHITE);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadTable();
        configureTableSelection();
    }

    // ── Build UI ─────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("QUẢN LÝ PHÒNG CHIẾU");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(), buildFormPanel());
        split.setDividerLocation(520);
        split.setDividerSize(6);
        split.setBorder(null);
        return split;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);

        // Table styling
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(210, 234, 255));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowVerticalLines(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Refresh button bar
        JButton btnRefresh = new JButton("🔄 Làm mới danh sách");
        btnRefresh.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRefresh.addActionListener(e -> loadTable());

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 12, 0, 0));

        // ── Fields ──
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(PRIMARY, 1), "Thông tin phòng chiếu",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), PRIMARY));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;

        // Tên phòng
        gc.gridx = 0; gc.gridy = 0; gc.fill = GridBagConstraints.NONE;
        fields.add(new JLabel("Tên phòng (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        fields.add(txtRoomName, gc);

        // Sức chứa
        gc.gridx = 0; gc.gridy = 1; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        fields.add(new JLabel("Sức chứa (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        spinCapacity.setPreferredSize(new Dimension(100, 28));
        fields.add(spinCapacity, gc);

        panel.add(fields, BorderLayout.NORTH);

        // ── Buttons ──
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setBackground(Color.WHITE);
        styleButton(btnAdd,    SUCCESS,  Color.WHITE);
        styleButton(btnUpdate, PRIMARY,  Color.WHITE);
        styleButton(btnDelete, DANGER,   Color.WHITE);
        styleButton(btnClear,  new Color(149, 165, 166), Color.WHITE);

        btnAdd.addActionListener(e    -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e  -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        panel.add(btnPanel, BorderLayout.CENTER);
        return panel;
    }

    // ── Event Handlers (chỉ gọi Controller) ─────────────────────────────────

    private void onAdd() {
        String name = txtRoomName.getText().trim();
        int capacity = (int) spinCapacity.getValue();
        try {
            roomController.addRoom(name, capacity);
            showSuccess("Thêm phòng '" + name + "' thành công!");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedRoomId == null) { showError("Vui lòng chọn phòng cần cập nhật."); return; }
        String name = txtRoomName.getText().trim();
        int capacity = (int) spinCapacity.getValue();
        try {
            roomController.updateRoom(selectedRoomId, name, capacity);
            showSuccess("Cập nhật phòng thành công!");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onDelete() {
        if (selectedRoomId == null) { showError("Vui lòng chọn phòng cần xóa."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa phòng này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            roomController.deleteRoom(selectedRoomId);
            showSuccess("Xóa phòng thành công!");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ── Table helpers ────────────────────────────────────────────────────────

    private void loadTable() {
        tableModel.setRowCount(0);
        List<Room> rooms = roomController.getAllRooms();
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{r.getRoomId(), r.getRoomName(), r.getCapacity()});
        }
    }

    private void configureTableSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0) { clearForm(); return; }
            selectedRoomId = (String) tableModel.getValueAt(row, 0);
            txtRoomName.setText((String) tableModel.getValueAt(row, 1));
            spinCapacity.setValue(tableModel.getValueAt(row, 2));
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
            btnAdd.setEnabled(false);
        });
    }

    private void clearForm() {
        selectedRoomId = null;
        txtRoomName.setText("");
        spinCapacity.setValue(100);
        table.clearSelection();
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        txtRoomName.requestFocus();
    }

    // ── Utility ──────────────────────────────────────────────────────────────

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
