package com.cinema.management.view.dialog;

import com.cinema.management.model.entity.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class RoomSelectionDialog extends JDialog {

    private final List<Room> rooms;
    private Room selectedRoom = null;

    private JTextField txtSearch;
    private JTable tblRoom;
    private DefaultTableModel tableModel;

    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color BG = new Color(245, 247, 250);

    public RoomSelectionDialog(Window owner, List<Room> rooms) {
        super(owner, "Chọn phòng", ModalityType.APPLICATION_MODAL);
        this.rooms = rooms;

        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BG);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        buildUI();
        loadDataToTable(rooms);
    }

    private void buildUI() {
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("🔍 Search Room Name:");
        lblSearch.setText("Tìm tên phòng:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setText("Tìm tên phòng:");
        lblSearch.setText("Tìm tên phòng:");
        pnlSearch.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập tên phòng...");
        txtSearch.setPreferredSize(new Dimension(0, 36));
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        add(pnlSearch, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });

        String[] cols = {"Mã phòng", "Tên phòng", "Sức chứa"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRoom = new JTable(tableModel);
        tblRoom.setRowHeight(30);
        tblRoom.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblRoom.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblRoom.setSelectionBackground(new Color(224, 242, 254));

        tblRoom.getColumnModel().getColumn(0).setMinWidth(0);
        tblRoom.getColumnModel().getColumn(0).setMaxWidth(0);
        tblRoom.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(tblRoom);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scrollPane, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);
        JButton btnSelect = new JButton("Chọn phòng");
        btnSelect.setBackground(PRIMARY);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSelect);
        add(pnlButtons, BorderLayout.SOUTH);

        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());
        tblRoom.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Room> list) {
        tableModel.setRowCount(0);
        for (Room r : list) {
            tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomName(),
                    r.getCapacity()
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadDataToTable(rooms);
            return;
        }
        List<Room> filtered = rooms.stream()
                .filter(r -> r.getRoomName().toLowerCase().contains(kw))
                .toList();
        loadDataToTable(filtered);
    }

    private void confirmSelection() {
        int row = tblRoom.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            selectedRoom = rooms.stream().filter(r -> r.getRoomId().equals(id)).findFirst().orElse(null);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng!", "Lưu ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Room getSelectedRoom() {
        return selectedRoom;
    }
}
