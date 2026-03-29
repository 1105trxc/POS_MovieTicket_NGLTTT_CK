package com.cinema.management.view.management;

import com.cinema.management.controller.RoomController;
import com.cinema.management.controller.SeatController;
import com.cinema.management.model.entity.Room;
import com.cinema.management.model.entity.Seat;
import com.cinema.management.model.entity.SeatType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class SeatManagementPanel extends JPanel {

    private final SeatController seatController = new SeatController();
    private final RoomController roomController = new RoomController();

    // Bảng màu hiện đại
    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(245, 158, 11);

    // --- SEAT TAB VARIABLES ---
    private final JComboBox<RoomItem> cmbRoom = new JComboBox<>();
    private final String[] SEAT_COLS = {"Seat ID", "Row", "Number", "Type", "Base Price"};
    private final DefaultTableModel seatTableModel = new DefaultTableModel(SEAT_COLS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable seatTable = new JTable(seatTableModel);
    private TableRowSorter<DefaultTableModel> seatRowSorter;

    // Thay đổi: Dùng TextField cho chuỗi Pattern thay vì Spinner
    private final JTextField txtRowChar = new JTextField(16);
    private final JTextField txtSeatPattern = new JTextField(16);
    private final JComboBox<SeatTypeItem> cmbSeatType = new JComboBox<>();

    private final JButton btnAddSeat = new JButton("Add Seats (Batch)");
    private final JButton btnUpdateSeat = new JButton("Update Type");
    private final JButton btnDeleteSeat = new JButton("Delete Seat");
    private final JButton btnClearSeat = new JButton("Reset Form");
    private String selectedSeatId;

    // --- SEAT TYPE TAB VARIABLES ---
    private final String[] TYPE_COLS = {"SeatType ID", "Type Name", "Base Price"};
    private final DefaultTableModel typeTableModel = new DefaultTableModel(TYPE_COLS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable typeTable = new JTable(typeTableModel);
    private TableRowSorter<DefaultTableModel> typeRowSorter;

    private final JTextField txtTypeName = new JTextField(16);
    private final JFormattedTextField txtBasePrice;

    private final JButton btnAddType = new JButton("Add Type");
    private final JButton btnUpdateType = new JButton("Update Type");
    private final JButton btnDeleteType = new JButton("Delete Type");
    private final JButton btnClearType = new JButton("Reset Form");
    private String selectedSeatTypeId;

    public SeatManagementPanel() {
        java.text.NumberFormat fmt = java.text.NumberFormat.getNumberInstance();
        fmt.setGroupingUsed(false);
        txtBasePrice = new JFormattedTextField(fmt);
        txtBasePrice.setValue(0);

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        // Placeholders
        txtRowChar.putClientProperty("JTextField.placeholderText", "e.g., A, B, C...");
        txtSeatPattern.putClientProperty("JTextField.placeholderText", "e.g., 1-5, 8-12, 15");
        txtTypeName.putClientProperty("JTextField.placeholderText", "e.g., Standard, VIP...");

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);

        loadRoomCombo();
        loadSeatTypeCombo();
        loadSeatTypeTable();

        configureSeatTableSelection();
        configureSeatTypeTableSelection();

        cmbRoom.addActionListener(e -> loadSeatTable());
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("SEAT MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Manage room seats, bulk layouts, and pricing categories");
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

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("   Seat Layout Setup   ", buildSeatTab());
        tabs.addTab("   Seat Types & Pricing   ", buildSeatTypeTab());
        return tabs;
    }

    // ==========================================
    // SEAT TAB
    // ==========================================
    private JPanel buildSeatTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel tableArea = new JPanel(new BorderLayout(0, 12));
        tableArea.setBackground(CARD);
        tableArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        // Filter Bar (Room + Live Search + Clone Button)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterBar.setOpaque(false);

        filterBar.add(new JLabel("<html><b>Room:</b></html>"));
        cmbRoom.setPreferredSize(new Dimension(150, 36));
        filterBar.add(cmbRoom);

        // Nút Clone Layout tích hợp
        JButton btnClone = new JButton("Clone Layout");
        btnClone.setBackground(WARNING);
        btnClone.setForeground(Color.WHITE);
        btnClone.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClone.addActionListener(e -> onCloneLayout());
        filterBar.add(btnClone);

        filterBar.add(Box.createHorizontalStrut(10));
        filterBar.add(new JLabel("<html><b>Search:</b></html>"));

        JTextField txtLiveSearchSeat = new JTextField();
        txtLiveSearchSeat.putClientProperty("JTextField.placeholderText", "Row, Number, Type...");
        txtLiveSearchSeat.setPreferredSize(new Dimension(200, 36));
        filterBar.add(txtLiveSearchSeat);

        tableArea.add(filterBar, BorderLayout.NORTH);

        // Table
        styleTable(seatTable);
        seatRowSorter = new TableRowSorter<>(seatTableModel);
        seatTable.setRowSorter(seatRowSorter);
        hideColumn(seatTable, 0); // Ẩn Seat ID

        txtLiveSearchSeat.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String text = txtLiveSearchSeat.getText().trim();
                seatRowSorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        JScrollPane scrollPane = new JScrollPane(seatTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableArea.add(scrollPane, BorderLayout.CENTER);

        panel.add(tableArea, BorderLayout.CENTER);
        panel.add(buildSeatForm(), BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSeatForm() {
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
                "  Seat Batch Generator  ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(220, 36);
        txtRowChar.setPreferredSize(fieldSize);
        txtSeatPattern.setPreferredSize(fieldSize);
        cmbSeatType.setPreferredSize(fieldSize);

        gc.gridx = 0;
        gc.gridy = 0;
        fields.add(new JLabel("Row (A-Z):"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtRowChar, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Number(s):"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtSeatPattern, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Seat Type:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(cmbSeatType, gc);

        panel.add(fields, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);
        styleActionButtons(btnAddSeat, btnUpdateSeat, btnDeleteSeat, btnClearSeat);

        btnAddSeat.addActionListener(e -> onAddSeat());
        btnUpdateSeat.addActionListener(e -> onUpdateSeat());
        btnDeleteSeat.addActionListener(e -> onDeleteSeat());
        btnClearSeat.addActionListener(e -> clearSeatForm());

        btnUpdateSeat.setEnabled(false);
        btnDeleteSeat.setEnabled(false);

        buttons.add(btnAddSeat);
        buttons.add(btnUpdateSeat);
        buttons.add(btnDeleteSeat);
        buttons.add(btnClearSeat);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // SEAT TYPE TAB (Không đổi)
    // ==========================================
    private JPanel buildSeatTypeTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel tableArea = new JPanel(new BorderLayout(0, 12));
        tableArea.setBackground(CARD);
        tableArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("<html><b>🔍 Quick Search:</b></html>"));

        JTextField txtLiveSearchType = new JTextField();
        txtLiveSearchType.putClientProperty("JTextField.placeholderText", "Search type name...");
        txtLiveSearchType.setPreferredSize(new Dimension(350, 36));
        filterBar.add(txtLiveSearchType);

        tableArea.add(filterBar, BorderLayout.NORTH);

        styleTable(typeTable);
        typeRowSorter = new TableRowSorter<>(typeTableModel);
        typeTable.setRowSorter(typeRowSorter);
        hideColumn(typeTable, 0);

        txtLiveSearchType.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String text = txtLiveSearchType.getText().trim();
                typeRowSorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        JScrollPane scrollPane = new JScrollPane(typeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableArea.add(scrollPane, BorderLayout.CENTER);

        panel.add(tableArea, BorderLayout.CENTER);
        panel.add(buildSeatTypeForm(), BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSeatTypeForm() {
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
                "  Seat Type Details  ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(220, 36);
        txtTypeName.setPreferredSize(fieldSize);
        txtBasePrice.setPreferredSize(fieldSize);

        gc.gridx = 0;
        gc.gridy = 0;
        fields.add(new JLabel("Type Name:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtTypeName, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Base Price:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtBasePrice, gc);

        panel.add(fields, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);
        styleActionButtons(btnAddType, btnUpdateType, btnDeleteType, btnClearType);

        btnAddType.addActionListener(e -> onAddSeatType());
        btnUpdateType.addActionListener(e -> onUpdateSeatType());
        btnDeleteType.addActionListener(e -> onDeleteSeatType());
        btnClearType.addActionListener(e -> clearSeatTypeForm());

        btnUpdateType.setEnabled(false);
        btnDeleteType.setEnabled(false);

        buttons.add(btnAddType);
        buttons.add(btnUpdateType);
        buttons.add(btnDeleteType);
        buttons.add(btnClearType);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // CONTROLLER ACTIONS (Đã cập nhật)
    // ==========================================
    private void onAddSeat() {
        RoomItem ri = (RoomItem) cmbRoom.getSelectedItem();
        SeatTypeItem sti = (SeatTypeItem) cmbSeatType.getSelectedItem();
        if (ri == null || sti == null) {
            showError("Please choose a room and a seat type.");
            return;
        }

        String row = txtRowChar.getText().trim().toUpperCase();
        String pattern = txtSeatPattern.getText().trim(); // Lấy chuỗi pattern

        if (row.isEmpty() || pattern.isEmpty()) {
            showError("Row and Number(s) cannot be empty.");
            return;
        }

        try {
            // GỌI HÀM MỚI TỪ CONTROLLER
            seatController.addSeatsByPattern(ri.id, sti.id, row, pattern);
            showSuccess("Seats generated successfully!");
            clearSeatForm();
            loadSeatTable();
        } catch (Exception ex) {
            showError("Error generating seats: " + ex.getMessage());
        }
    }

    private void onCloneLayout() {
        RoomItem targetRoom = (RoomItem) cmbRoom.getSelectedItem();
        if (targetRoom == null) {
            showError("Please select a target room in the filter bar first.");
            return;
        }

        List<Room> allRooms = roomController.getAllRooms();
        allRooms.removeIf(r -> r.getRoomId().equals(targetRoom.id));

        if (allRooms.isEmpty()) {
            showError("No other rooms available to clone from.");
            return;
        }

        JComboBox<RoomItem> cmbSource = new JComboBox<>();
        for (Room r : allRooms) {
            cmbSource.addItem(new RoomItem(r.getRoomId(), r.getRoomName()));
        }

        Object[] message = {
                "Cloning will replace or add to the current layout.",
                "Select Source Room to Clone to [" + targetRoom.name + "]:",
                cmbSource
        };

        int result = JOptionPane.showConfirmDialog(this, message, "Clone Room Layout", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            RoomItem sourceRoom = (RoomItem) cmbSource.getSelectedItem();
            if (sourceRoom != null) {
                try {
                    // GỌI HÀM CLONE TỪ CONTROLLER
                    seatController.cloneRoomLayout(sourceRoom.id, targetRoom.id);
                    showSuccess("Layout cloned successfully from " + sourceRoom.name + " to " + targetRoom.name);
                    loadSeatTable();
                } catch (Exception ex) {
                    showError("Error cloning layout: " + ex.getMessage());
                }
            }
        }
    }

    private void onUpdateSeat() {
        if (selectedSeatId == null) {
            showError("Please select a seat to update.");
            return;
        }
        SeatTypeItem sti = (SeatTypeItem) cmbSeatType.getSelectedItem();
        if (sti == null) {
            showError("Please choose a seat type.");
            return;
        }
        try {
            seatController.updateSeatType(selectedSeatId, sti.id);
            showSuccess("Seat updated successfully.");
            clearSeatForm();
            loadSeatTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onDeleteSeat() {
        if (selectedSeatId == null) {
            showError("Please select a seat to delete.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this seat?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            seatController.deleteSeat(selectedSeatId);
            showSuccess("Seat deleted successfully.");
            clearSeatForm();
            loadSeatTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onAddSeatType() { /* Giữ nguyên như cũ */
        String name = txtTypeName.getText().trim();
        if (name.isEmpty()) {
            showError("Type name cannot be empty.");
            return;
        }
        BigDecimal price = parsePriceField();
        if (price == null) return;
        try {
            seatController.addSeatType(name, price);
            showSuccess("Seat type added successfully.");
            clearSeatTypeForm();
            loadSeatTypeTable();
            loadSeatTypeCombo();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onUpdateSeatType() { /* Giữ nguyên như cũ */
        if (selectedSeatTypeId == null) {
            showError("Please select a seat type to update.");
            return;
        }
        String name = txtTypeName.getText().trim();
        if (name.isEmpty()) {
            showError("Type name cannot be empty.");
            return;
        }
        BigDecimal price = parsePriceField();
        if (price == null) return;
        try {
            seatController.updateSeatType(selectedSeatTypeId, name, price, "U001");
            showSuccess("Seat type updated successfully.");
            clearSeatTypeForm();
            loadSeatTypeTable();
            loadSeatTypeCombo();
            loadSeatTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onDeleteSeatType() { /* Giữ nguyên như cũ */
        if (selectedSeatTypeId == null) {
            showError("Please select a seat type to delete.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this seat type?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            seatController.deleteSeatType(selectedSeatTypeId);
            showSuccess("Seat type deleted successfully.");
            clearSeatTypeForm();
            loadSeatTypeTable();
            loadSeatTypeCombo();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================
    private void loadRoomCombo() {
        cmbRoom.removeAllItems();
        for (Room r : roomController.getAllRooms()) {
            cmbRoom.addItem(new RoomItem(r.getRoomId(), r.getRoomName()));
        }
    }

    private void loadSeatTypeCombo() {
        cmbSeatType.removeAllItems();
        for (SeatType st : seatController.getAllSeatTypes()) {
            cmbSeatType.addItem(new SeatTypeItem(st.getSeatTypeId(), st.getTypeName(), st.getBasePrice()));
        }
    }

    private void loadSeatTable() {
        seatTableModel.setRowCount(0);
        RoomItem ri = (RoomItem) cmbRoom.getSelectedItem();
        if (ri == null) return;
        List<Seat> seats = seatController.getSeatsByRoom(ri.id);
        for (Seat s : seats) {
            String typeName = s.getSeatType() != null ? s.getSeatType().getTypeName() : "-";
            String basePrice = s.getSeatType() != null ? String.format("%,.0f", s.getSeatType().getBasePrice()) : "-";
            seatTableModel.addRow(new Object[]{
                    s.getSeatId(), s.getRowChar(), s.getSeatNumber(), typeName, basePrice
            });
        }
    }

    private void loadSeatTypeTable() {
        typeTableModel.setRowCount(0);
        for (SeatType st : seatController.getAllSeatTypes()) {
            typeTableModel.addRow(new Object[]{
                    st.getSeatTypeId(), st.getTypeName(), String.format("%,.0f", st.getBasePrice())
            });
        }
    }

    private void configureSeatTableSelection() {
        seatTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = seatTable.getSelectedRow();
            if (viewRow < 0) {
                clearSeatForm();
                return;
            }
            int modelRow = seatTable.convertRowIndexToModel(viewRow);

            selectedSeatId = (String) seatTableModel.getValueAt(modelRow, 0);
            txtRowChar.setText((String) seatTableModel.getValueAt(modelRow, 1));
            // Đẩy số ghế vào ô Pattern để có thể xem/chỉnh sửa
            txtSeatPattern.setText(seatTableModel.getValueAt(modelRow, 2).toString());

            btnAddSeat.setEnabled(false); // Khi select thì chỉ được Update Type hoặc Delete
            btnUpdateSeat.setEnabled(true);
            btnDeleteSeat.setEnabled(true);
        });
    }

    private void configureSeatTypeTableSelection() {
        typeTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = typeTable.getSelectedRow();
            if (viewRow < 0) {
                clearSeatTypeForm();
                return;
            }
            int modelRow = typeTable.convertRowIndexToModel(viewRow);

            selectedSeatTypeId = (String) typeTableModel.getValueAt(modelRow, 0);
            txtTypeName.setText((String) typeTableModel.getValueAt(modelRow, 1));
            String priceStr = ((String) typeTableModel.getValueAt(modelRow, 2)).replace(",", "");
            try {
                txtBasePrice.setValue(Long.parseLong(priceStr));
            } catch (Exception ignored) {
            }

            btnAddType.setEnabled(false);
            btnUpdateType.setEnabled(true);
            btnDeleteType.setEnabled(true);
        });
    }

    private void clearSeatForm() {
        selectedSeatId = null;
        txtRowChar.setText("");
        txtSeatPattern.setText("");
        seatTable.clearSelection();

        btnAddSeat.setEnabled(true);
        btnUpdateSeat.setEnabled(false);
        btnDeleteSeat.setEnabled(false);
    }

    private void clearSeatTypeForm() {
        selectedSeatTypeId = null;
        txtTypeName.setText("");
        txtBasePrice.setValue(0);
        typeTable.clearSelection();

        btnAddType.setEnabled(true);
        btnUpdateType.setEnabled(false);
        btnDeleteType.setEnabled(false);
    }

    private BigDecimal parsePriceField() {
        try {
            Object val = txtBasePrice.getValue();
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            showError("Base price is invalid.");
            return null;
        }
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

    private void styleActionButtons(JButton add, JButton update, JButton delete, JButton clear) {
        add.setBackground(SUCCESS);
        add.setForeground(Color.WHITE);
        add.setFont(new Font("Segoe UI", Font.BOLD, 13));

        update.setBackground(PRIMARY);
        update.setForeground(Color.WHITE);
        update.setFont(new Font("Segoe UI", Font.BOLD, 13));

        delete.setBackground(DANGER);
        delete.setForeground(Color.WHITE);
        delete.setFont(new Font("Segoe UI", Font.BOLD, 13));

        clear.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void hideColumn(JTable tbl, int colIndex) {
        tbl.getColumnModel().getColumn(colIndex).setMinWidth(0);
        tbl.getColumnModel().getColumn(colIndex).setMaxWidth(0);
        tbl.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class RoomItem {
        final String id;
        final String name;

        RoomItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class SeatTypeItem {
        final String id;
        final String name;
        final BigDecimal price;

        SeatTypeItem(String id, String name, BigDecimal price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return name + " (" + String.format("%,.0f", price) + " VND)";
        }
    }
}