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
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel Quản lý Ghế và Loại Ghế.
 * Gồm 2 tab: "Danh sách ghế" (lọc theo phòng) và "Loại ghế" (CRUD SeatType).
 * Tuân thủ MVC: chỉ gọi Controller.
 */
public class SeatManagementPanel extends JPanel {

    private final SeatController seatController   = new SeatController();
    private final RoomController roomController   = new RoomController();

    // ── Colors ───────────────────────────────────────────────────────────────
    private static final Color PRIMARY   = new Color(41, 128, 185);
    private static final Color DANGER    = new Color(192, 57, 43);
    private static final Color SUCCESS   = new Color(39, 174, 96);
    private static final Color BG_HEADER = new Color(52, 73, 94);

    // ── Tab: Ghế ─────────────────────────────────────────────────────────────
    private final JComboBox<RoomItem> cmbRoom        = new JComboBox<>();
    private final String[] SEAT_COLS   = {"Mã ghế", "Hàng", "Số ghế", "Loại ghế", "Giá cơ bản"};
    private final DefaultTableModel seatTableModel   = new DefaultTableModel(SEAT_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable seatTable                   = new JTable(seatTableModel);
    private final JTextField txtRowChar              = new JTextField(5);
    private final JSpinner   spinSeatNumber          = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JComboBox<SeatTypeItem> cmbSeatType = new JComboBox<>();
    private final JButton btnAddSeat    = new JButton("Thêm ghế");
    private final JButton btnUpdateSeat = new JButton("Đổi loại ghế");
    private final JButton btnDeleteSeat = new JButton("Xóa ghế");
    private final JButton btnClearSeat  = new JButton("Làm mới");
    private String selectedSeatId = null;

    // ── Tab: Loại ghế ────────────────────────────────────────────────────────
    private final String[] TYPE_COLS  = {"Mã loại ghế", "Tên loại ghế", "Giá cơ bản (VNĐ)"};
    private final DefaultTableModel typeTableModel  = new DefaultTableModel(TYPE_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable  typeTable       = new JTable(typeTableModel);
    private final JTextField txtTypeName  = new JTextField(20);
    private final JFormattedTextField txtBasePrice;
    private final JButton btnAddType    = new JButton("Thêm loại ghế");
    private final JButton btnUpdateType = new JButton("Cập nhật");
    private final JButton btnDeleteType = new JButton("Xóa loại ghế");
    private final JButton btnClearType  = new JButton("Làm mới");
    private String selectedSeatTypeId = null;

    public SeatManagementPanel() {
        // Format số tiền
        java.text.NumberFormat fmt = java.text.NumberFormat.getNumberInstance();
        fmt.setGroupingUsed(false);
        txtBasePrice = new JFormattedTextField(fmt);
        txtBasePrice.setColumns(12);
        txtBasePrice.setValue(0);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setBackground(Color.WHITE);

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("🪑 Danh sách ghế", buildSeatTab());
        tabs.addTab("📋 Loại ghế",       buildSeatTypeTab());
        add(tabs, BorderLayout.CENTER);

        loadRoomCombo();
        loadSeatTypeCombo();
        loadSeatTypeTable();
        configureSeatTableSelection();
        configureSeatTypeTableSelection();

        // Khi chọn phòng → load ghế
        cmbRoom.addActionListener(e -> loadSeatTable());
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG_HEADER);
        h.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel lbl = new JLabel("QUẢN LÝ GHẾ NGỒI");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);
        h.add(lbl, BorderLayout.WEST);
        return h;
    }

    // ── Tab: Ghế ─────────────────────────────────────────────────────────────

    private JPanel buildSeatTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        // Bộ lọc phòng
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.setBackground(Color.WHITE);
        filter.add(new JLabel("Chọn phòng:"));
        cmbRoom.setPreferredSize(new Dimension(220, 28));
        filter.add(cmbRoom);
        JButton btnLoadSeats = new JButton("Xem ghế");
        btnLoadSeats.addActionListener(e -> loadSeatTable());
        filter.add(btnLoadSeats);
        panel.add(filter, BorderLayout.NORTH);

        // Table
        styleTable(seatTable);
        seatTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        seatTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        seatTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        seatTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        seatTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        panel.add(new JScrollPane(seatTable), BorderLayout.CENTER);

        // Form
        panel.add(buildSeatForm(), BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSeatForm() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBorder(new EmptyBorder(0, 8, 0, 0));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(PRIMARY), "Thông tin ghế",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 12), PRIMARY));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        fields.add(new JLabel("Hàng (A-Z) (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        txtRowChar.setToolTipText("Ví dụ: A, B, C...");
        fields.add(txtRowChar, gc);

        gc.gridx = 0; gc.gridy = 1; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        fields.add(new JLabel("Số ghế (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        fields.add(spinSeatNumber, gc);

        gc.gridx = 0; gc.gridy = 2; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        fields.add(new JLabel("Loại ghế (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        cmbSeatType.setPreferredSize(new Dimension(140, 28));
        fields.add(cmbSeatType, gc);

        panel.add(fields, BorderLayout.NORTH);

        JPanel btns = new JPanel(new GridLayout(2, 2, 6, 6));
        btns.setBackground(Color.WHITE);
        styleButton(btnAddSeat,    SUCCESS, Color.WHITE);
        styleButton(btnUpdateSeat, PRIMARY, Color.WHITE);
        styleButton(btnDeleteSeat, DANGER,  Color.WHITE);
        styleButton(btnClearSeat,  new Color(149, 165, 166), Color.WHITE);
        btnAddSeat.addActionListener(e    -> onAddSeat());
        btnUpdateSeat.addActionListener(e -> onUpdateSeat());
        btnDeleteSeat.addActionListener(e -> onDeleteSeat());
        btnClearSeat.addActionListener(e  -> clearSeatForm());
        btnUpdateSeat.setEnabled(false);
        btnDeleteSeat.setEnabled(false);
        btns.add(btnAddSeat); btns.add(btnUpdateSeat);
        btns.add(btnDeleteSeat); btns.add(btnClearSeat);
        panel.add(btns, BorderLayout.CENTER);
        return panel;
    }

    // ── Tab: Loại ghế ────────────────────────────────────────────────────────

    private JPanel buildSeatTypeTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        styleTable(typeTable);
        typeTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        typeTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        typeTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        panel.add(new JScrollPane(typeTable), BorderLayout.CENTER);
        panel.add(buildSeatTypeForm(), BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSeatTypeForm() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(270, 0));
        panel.setBorder(new EmptyBorder(0, 8, 0, 0));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(PRIMARY), "Thông tin loại ghế",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 12), PRIMARY));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        fields.add(new JLabel("Tên loại ghế (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        fields.add(txtTypeName, gc);

        gc.gridx = 0; gc.gridy = 1; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        fields.add(new JLabel("Giá cơ bản (VNĐ) (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        fields.add(txtBasePrice, gc);

        JLabel note = new JLabel("* Đổi giá sẽ tự động ghi Audit Log");
        note.setFont(new Font("Arial", Font.ITALIC, 11));
        note.setForeground(Color.GRAY);
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(note, gc);

        panel.add(fields, BorderLayout.NORTH);

        JPanel btns = new JPanel(new GridLayout(2, 2, 6, 6));
        btns.setBackground(Color.WHITE);
        styleButton(btnAddType,    SUCCESS, Color.WHITE);
        styleButton(btnUpdateType, PRIMARY, Color.WHITE);
        styleButton(btnDeleteType, DANGER,  Color.WHITE);
        styleButton(btnClearType,  new Color(149, 165, 166), Color.WHITE);
        btnAddType.addActionListener(e    -> onAddSeatType());
        btnUpdateType.addActionListener(e -> onUpdateSeatType());
        btnDeleteType.addActionListener(e -> onDeleteSeatType());
        btnClearType.addActionListener(e  -> clearSeatTypeForm());
        btnUpdateType.setEnabled(false);
        btnDeleteType.setEnabled(false);
        btns.add(btnAddType); btns.add(btnUpdateType);
        btns.add(btnDeleteType); btns.add(btnClearType);
        panel.add(btns, BorderLayout.CENTER);
        return panel;
    }

    // ── Event Handlers ───────────────────────────────────────────────────────

    private void onAddSeat() {
        RoomItem ri = (RoomItem) cmbRoom.getSelectedItem();
        if (ri == null) { showError("Vui lòng chọn phòng trước."); return; }
        SeatTypeItem sti = (SeatTypeItem) cmbSeatType.getSelectedItem();
        if (sti == null) { showError("Vui lòng chọn loại ghế."); return; }
        String row = txtRowChar.getText().trim();
        int num = (int) spinSeatNumber.getValue();
        try {
            seatController.addSeat(ri.id, sti.id, row, num);
            showSuccess("Thêm ghế " + row.toUpperCase() + num + " thành công!");
            clearSeatForm();
            loadSeatTable();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onUpdateSeat() {
        if (selectedSeatId == null) { showError("Vui lòng chọn ghế cần cập nhật."); return; }
        SeatTypeItem sti = (SeatTypeItem) cmbSeatType.getSelectedItem();
        if (sti == null) { showError("Vui lòng chọn loại ghế."); return; }
        try {
            seatController.updateSeatType(selectedSeatId, sti.id);
            showSuccess("Cập nhật loại ghế thành công!");
            clearSeatForm();
            loadSeatTable();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onDeleteSeat() {
        if (selectedSeatId == null) { showError("Vui lòng chọn ghế cần xóa."); return; }
        int c = JOptionPane.showConfirmDialog(this, "Xóa ghế này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            seatController.deleteSeat(selectedSeatId);
            showSuccess("Xóa ghế thành công!");
            clearSeatForm();
            loadSeatTable();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onAddSeatType() {
        String name = txtTypeName.getText().trim();
        BigDecimal price = parsePriceField();
        if (price == null) return;
        try {
            seatController.addSeatType(name, price);
            showSuccess("Thêm loại ghế '" + name + "' thành công!");
            clearSeatTypeForm();
            loadSeatTypeTable();
            loadSeatTypeCombo();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onUpdateSeatType() {
        if (selectedSeatTypeId == null) { showError("Vui lòng chọn loại ghế cần cập nhật."); return; }
        String name = txtTypeName.getText().trim();
        BigDecimal price = parsePriceField();
        if (price == null) return;
        try {
            // TODO: lấy userId từ session thực tế (Module 3 - Thành viên B)
            seatController.updateSeatType(selectedSeatTypeId, name, price, null);
            showSuccess("Cập nhật loại ghế thành công!");
            clearSeatTypeForm();
            loadSeatTypeTable();
            loadSeatTypeCombo();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onDeleteSeatType() {
        if (selectedSeatTypeId == null) { showError("Vui lòng chọn loại ghế cần xóa."); return; }
        int c = JOptionPane.showConfirmDialog(this, "Xóa loại ghế này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            seatController.deleteSeatType(selectedSeatTypeId);
            showSuccess("Xóa loại ghế thành công!");
            clearSeatTypeForm();
            loadSeatTypeTable();
            loadSeatTypeCombo();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    // ── Data loaders ─────────────────────────────────────────────────────────

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
            String typeName  = s.getSeatType() != null ? s.getSeatType().getTypeName() : "-";
            String basePrice = s.getSeatType() != null
                    ? String.format("%,.0f", s.getSeatType().getBasePrice()) : "-";
            seatTableModel.addRow(new Object[]{
                    s.getSeatId(), s.getRowChar(), s.getSeatNumber(), typeName, basePrice});
        }
    }

    private void loadSeatTypeTable() {
        typeTableModel.setRowCount(0);
        for (SeatType st : seatController.getAllSeatTypes()) {
            typeTableModel.addRow(new Object[]{
                    st.getSeatTypeId(), st.getTypeName(),
                    String.format("%,.0f", st.getBasePrice())});
        }
    }

    private void configureSeatTableSelection() {
        seatTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = seatTable.getSelectedRow();
            if (row < 0) { clearSeatForm(); return; }
            selectedSeatId = (String) seatTableModel.getValueAt(row, 0);
            txtRowChar.setText((String) seatTableModel.getValueAt(row, 1));
            spinSeatNumber.setValue(seatTableModel.getValueAt(row, 2));
            btnUpdateSeat.setEnabled(true);
            btnDeleteSeat.setEnabled(true);
            btnAddSeat.setEnabled(false);
        });
    }

    private void configureSeatTypeTableSelection() {
        typeTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = typeTable.getSelectedRow();
            if (row < 0) { clearSeatTypeForm(); return; }
            selectedSeatTypeId = (String) typeTableModel.getValueAt(row, 0);
            txtTypeName.setText((String) typeTableModel.getValueAt(row, 1));
            String priceStr = ((String) typeTableModel.getValueAt(row, 2)).replace(",", "");
            try { txtBasePrice.setValue(Long.parseLong(priceStr)); } catch (Exception ignored) {}
            btnUpdateType.setEnabled(true);
            btnDeleteType.setEnabled(true);
            btnAddType.setEnabled(false);
        });
    }

    // ── Clear forms ──────────────────────────────────────────────────────────

    private void clearSeatForm() {
        selectedSeatId = null;
        txtRowChar.setText("");
        spinSeatNumber.setValue(1);
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

    // ── Utility ──────────────────────────────────────────────────────────────

    private BigDecimal parsePriceField() {
        try {
            Object val = txtBasePrice.getValue();
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            showError("Giá cơ bản không hợp lệ. Vui lòng nhập số.");
            return null;
        }
    }

    private void styleTable(JTable tbl) {
        tbl.setRowHeight(30);
        tbl.setFont(new Font("Arial", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tbl.getTableHeader().setBackground(PRIMARY);
        tbl.getTableHeader().setForeground(Color.WHITE);
        tbl.setSelectionBackground(new Color(210, 234, 255));
        tbl.setGridColor(new Color(220, 220, 220));
        tbl.setShowVerticalLines(false);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ── Inner display classes ─────────────────────────────────────────────────

    private static class RoomItem {
        final String id, name;
        RoomItem(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private static class SeatTypeItem {
        final String id, name;
        final BigDecimal price;
        SeatTypeItem(String id, String name, BigDecimal price) {
            this.id = id; this.name = name; this.price = price;
        }
        @Override public String toString() {
            return name + " (" + String.format("%,.0f", price) + " VNĐ)";
        }
    }
}
