package com.cinema.management.view.management;

import com.cinema.management.controller.RoomController;
import com.cinema.management.model.entity.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class RoomManagementPanel extends JPanel {

    private final RoomController roomController = new RoomController();

    // Đồng bộ bảng màu với hệ thống MainFrame & ShowTime
    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233); // Sky Blue
    private static final Color SUCCESS = new Color(34, 197, 94); // Green
    private static final Color DANGER = new Color(239, 68, 68); // Red

    private final String[] COLUMNS = { "Mã phòng", "Tên phòng", "Sức chứa" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    private final JTextField txtRoomName = new JTextField(16);
    private final JSpinner spinCapacity = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));

    private final JButton btnAdd = new JButton("Thêm mới");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnClear = new JButton("Đặt lại form");

    private String selectedRoomId;

    public RoomManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        // Placeholder cho Textfield
        txtRoomName.putClientProperty("JTextField.placeholderText", "Nhập tên phòng...");

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadTable();
        configureTableSelection();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ PHÒNG CHIẾU");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Quản lý phòng chiếu và sức chứa ghế");
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
        // Thay thế JSplitPane bằng BorderLayout với Form cố định bên phải
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

        // --- LIVE SEARCH BAR ---
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm nhanh:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterBar.add(lblSearch);

        JTextField txtLiveSearch = new JTextField();
        txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã phòng hoặc tên phòng...");
        txtLiveSearch.setPreferredSize(new Dimension(350, 36));
        filterBar.add(txtLiveSearch);

        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setOpaque(false);
        filterWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        filterWrapper.add(filterBar, BorderLayout.CENTER);
        panel.add(filterWrapper, BorderLayout.NORTH);

        // --- TABLE SETUP ---
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
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
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
        panel.setPreferredSize(new Dimension(380, 0)); // Fix chiều rộng Form
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "  Thông tin phòng  ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(220, 36);
        txtRoomName.setPreferredSize(fieldSize);
        spinCapacity.setPreferredSize(fieldSize);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        fields.add(new JLabel("Tên phòng:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtRoomName, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Sức chứa:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(spinCapacity, gc);

        panel.add(fields, BorderLayout.NORTH);

        // --- BUTTONS PANEL ---
        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);

        styleActionButtons();

        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
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

        btnDelete.setBackground(DANGER);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void styleTable(JTable tbl) {
        tbl.setRowHeight(38);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tbl.setSelectionBackground(new Color(224, 242, 254)); // Light Sky Blue
        tbl.setSelectionForeground(new Color(15, 23, 42));
        tbl.setGridColor(new Color(241, 245, 249));
        tbl.setShowVerticalLines(false);
    }

    private void onAdd() {
        String name = txtRoomName.getText().trim();
        if (name.isEmpty()) {
            showError("Tên phòng không được để trống.");
            return;
        }
        int capacity = (int) spinCapacity.getValue();
        try {
            roomController.addRoom(name, capacity);
            showSuccess("Thêm phòng thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedRoomId == null) {
            showError("Vui lòng chọn phòng cần cập nhật.");
            return;
        }
        String name = txtRoomName.getText().trim();
        if (name.isEmpty()) {
            showError("Tên phòng không được để trống.");
            return;
        }
        int capacity = (int) spinCapacity.getValue();
        try {
            roomController.updateRoom(selectedRoomId, name, capacity);
            showSuccess("Cập nhật phòng thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onDelete() {
        if (selectedRoomId == null) {
            showError("Vui lòng chọn phòng cần xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this, "Bạn có chắc chắn muốn xóa phòng này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            roomController.deleteRoom(selectedRoomId);
            showSuccess("Xóa phòng thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    public void refreshData() {
        loadTable();
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<Room> rooms = roomController.getAllRooms();
        for (Room r : rooms) {
            tableModel.addRow(new Object[] { r.getRoomId(), r.getRoomName(), r.getCapacity() });
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
            // Ánh xạ lại index khi bảng bị filter
            int modelRow = table.convertRowIndexToModel(viewRow);

            selectedRoomId = (String) tableModel.getValueAt(modelRow, 0);
            txtRoomName.setText((String) tableModel.getValueAt(modelRow, 1));
            spinCapacity.setValue(tableModel.getValueAt(modelRow, 2));

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
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
