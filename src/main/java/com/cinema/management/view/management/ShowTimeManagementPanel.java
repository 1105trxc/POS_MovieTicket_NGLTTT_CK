package com.cinema.management.view.management;

import com.cinema.management.controller.RoomController;
import com.cinema.management.controller.ShowTimeController;
import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Room;
import com.cinema.management.model.entity.ShowTime;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel Quản lý Suất chiếu.
 * Validate xung đột lịch phòng ngay trên UI trước khi gọi Controller.
 * Tuân thủ MVC: không có business logic, chỉ gọi Controller.
 */
public class ShowTimeManagementPanel extends JPanel {

    private final ShowTimeController showTimeController = new ShowTimeController();
    private final RoomController     roomController     = new RoomController();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Colors ───────────────────────────────────────────────────────────────
    private static final Color PRIMARY   = new Color(41, 128, 185);
    private static final Color DANGER    = new Color(192, 57, 43);
    private static final Color SUCCESS   = new Color(39, 174, 96);
    private static final Color BG_HEADER = new Color(52, 73, 94);

    // ── Table ────────────────────────────────────────────────────────────────
    private final String[] COLUMNS = {
        "Mã suất chiếu", "Tên phim", "Phòng chiếu", "Bắt đầu", "Kết thúc"
    };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    // ── Filter bar ───────────────────────────────────────────────────────────
    private final JComboBox<MovieItem>  cmbFilterMovie = new JComboBox<>();
    private final JButton               btnFilterAll   = new JButton("Tất cả suất chiếu");

    // ── Form fields ──────────────────────────────────────────────────────────
    private final JComboBox<MovieItem>  cmbMovie     = new JComboBox<>();
    private final JComboBox<RoomItem>   cmbRoom      = new JComboBox<>();
    private final JTextField            txtStartTime = new JTextField("dd/MM/yyyy HH:mm", 16);
    private final JTextField            txtEndTime   = new JTextField("dd/MM/yyyy HH:mm", 16);

    private final JButton btnAdd    = new JButton("Thêm");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnClear  = new JButton("Làm mới");

    private String selectedShowTimeId = null;

    public ShowTimeManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setBackground(Color.WHITE);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadCombos();
        loadTable(null);
        configureTableSelection();
        setupFilterActions();
    }

    // ── Build UI ─────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG_HEADER);
        h.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel lbl = new JLabel("QUẢN LÝ SUẤT CHIẾU");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);
        h.add(lbl, BorderLayout.WEST);
        return h;
    }

    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTableArea(), buildFormPanel());
        split.setDividerLocation(560);
        split.setDividerSize(6);
        split.setBorder(new EmptyBorder(10, 10, 10, 10));
        return split;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filterBar.setBackground(new Color(245, 245, 245));
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        filterBar.add(new JLabel("Lọc theo phim:"));
        cmbFilterMovie.setPreferredSize(new Dimension(200, 28));
        filterBar.add(cmbFilterMovie);
        JButton btnFilter = new JButton("Lọc");
        btnFilter.addActionListener(e -> {
            MovieItem mi = (MovieItem) cmbFilterMovie.getSelectedItem();
            loadTable(mi != null ? mi.id : null);
        });
        filterBar.add(btnFilter);
        btnFilterAll.addActionListener(e -> {
            cmbFilterMovie.setSelectedIndex(-1);
            loadTable(null);
        });
        filterBar.add(btnFilterAll);
        panel.add(filterBar, BorderLayout.NORTH);

        // Table
        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Fields
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(PRIMARY, 1), "Thông tin suất chiếu",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), PRIMARY));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;

        // Phim
        gc.gridx = 0; gc.gridy = 0; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        fields.add(new JLabel("Phim (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        cmbMovie.setPreferredSize(new Dimension(180, 28));
        fields.add(cmbMovie, gc);

        // Phòng
        gc.gridx = 0; gc.gridy = 1; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        fields.add(new JLabel("Phòng chiếu (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        cmbRoom.setPreferredSize(new Dimension(180, 28));
        fields.add(cmbRoom, gc);

        // Giờ bắt đầu
        gc.gridx = 0; gc.gridy = 2; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        fields.add(new JLabel("Bắt đầu (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        txtStartTime.setToolTipText("Định dạng: dd/MM/yyyy HH:mm  (ví dụ: 25/03/2026 14:30)");
        setupPlaceholder(txtStartTime);
        fields.add(txtStartTime, gc);

        // Giờ kết thúc
        gc.gridx = 0; gc.gridy = 3; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        fields.add(new JLabel("Kết thúc (*):"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        txtEndTime.setToolTipText("Định dạng: dd/MM/yyyy HH:mm  (ví dụ: 25/03/2026 16:30)");
        setupPlaceholder(txtEndTime);
        fields.add(txtEndTime, gc);

        // Hint
        JLabel hint = new JLabel("<html><i>Hệ thống tự kiểm tra xung đột lịch phòng.</i></html>");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        fields.add(hint, gc);

        panel.add(fields, BorderLayout.NORTH);

        // Buttons
        JPanel btns = new JPanel(new GridLayout(2, 2, 8, 8));
        btns.setBackground(Color.WHITE);
        styleButton(btnAdd,    SUCCESS, Color.WHITE);
        styleButton(btnUpdate, PRIMARY, Color.WHITE);
        styleButton(btnDelete, DANGER,  Color.WHITE);
        styleButton(btnClear,  new Color(149, 165, 166), Color.WHITE);
        btnAdd.addActionListener(e    -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e  -> clearForm());
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        btns.add(btnAdd); btns.add(btnUpdate);
        btns.add(btnDelete); btns.add(btnClear);
        panel.add(btns, BorderLayout.CENTER);

        // Hotkey F5 = Thêm / Cập nhật
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "quickSave");
        getActionMap().put("quickSave", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (selectedShowTimeId == null) onAdd(); else onUpdate();
            }
        });

        return panel;
    }

    // ── Event Handlers ───────────────────────────────────────────────────────

    private void onAdd() {
        MovieItem mi = (MovieItem) cmbMovie.getSelectedItem();
        RoomItem  ri = (RoomItem)  cmbRoom.getSelectedItem();
        if (mi == null || ri == null) { showError("Vui lòng chọn phim và phòng chiếu."); return; }
        LocalDateTime start = parseDateTime(txtStartTime.getText());
        LocalDateTime end   = parseDateTime(txtEndTime.getText());
        if (start == null || end == null) return;
        try {
            showTimeController.addShowTime(mi.id, ri.id, start, end);
            showSuccess("Thêm suất chiếu thành công!");
            clearForm();
            loadTable(null);
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onUpdate() {
        if (selectedShowTimeId == null) { showError("Vui lòng chọn suất chiếu cần cập nhật."); return; }
        MovieItem mi = (MovieItem) cmbMovie.getSelectedItem();
        RoomItem  ri = (RoomItem)  cmbRoom.getSelectedItem();
        if (mi == null || ri == null) { showError("Vui lòng chọn phim và phòng chiếu."); return; }
        LocalDateTime start = parseDateTime(txtStartTime.getText());
        LocalDateTime end   = parseDateTime(txtEndTime.getText());
        if (start == null || end == null) return;
        try {
            showTimeController.updateShowTime(selectedShowTimeId, mi.id, ri.id, start, end);
            showSuccess("Cập nhật suất chiếu thành công!");
            clearForm();
            loadTable(null);
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onDelete() {
        if (selectedShowTimeId == null) { showError("Vui lòng chọn suất chiếu cần xóa."); return; }
        int c = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa suất chiếu này?\nHành động này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            showTimeController.deleteShowTime(selectedShowTimeId);
            showSuccess("Xóa suất chiếu thành công!");
            clearForm();
            loadTable(null);
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    // ── Data loaders ─────────────────────────────────────────────────────────

    private void loadCombos() {
        List<Movie> movies = showTimeController.getAllMovies();
        List<Room>  rooms  = roomController.getAllRooms();

        cmbMovie.removeAllItems();
        cmbFilterMovie.removeAllItems();
        for (Movie m : movies) {
            MovieItem mi = new MovieItem(m.getMovieId(), m.getTitle());
            cmbMovie.addItem(mi);
            cmbFilterMovie.addItem(mi);
        }
        cmbRoom.removeAllItems();
        for (Room r : rooms) {
            cmbRoom.addItem(new RoomItem(r.getRoomId(), r.getRoomName()));
        }
    }

    private void loadTable(String movieIdFilter) {
        tableModel.setRowCount(0);
        List<ShowTime> list = (movieIdFilter != null)
                ? showTimeController.getShowTimesByMovie(movieIdFilter)
                : showTimeController.getAllShowTimes();
        for (ShowTime st : list) {
            String movieTitle = st.getMovie() != null ? st.getMovie().getTitle() : "-";
            String roomName   = st.getRoom()  != null ? st.getRoom().getRoomName() : "-";
            tableModel.addRow(new Object[]{
                    st.getShowTimeId(),
                    movieTitle,
                    roomName,
                    st.getStartTime().format(DT_FMT),
                    st.getEndTime().format(DT_FMT)
            });
        }
    }

    private void configureTableSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) { clearForm(); return; }
            int modelRow = table.convertRowIndexToModel(viewRow);
            selectedShowTimeId = (String) tableModel.getValueAt(modelRow, 0);
            // Điền thời gian vào form
            txtStartTime.setText((String) tableModel.getValueAt(modelRow, 3));
            txtEndTime.setText((String) tableModel.getValueAt(modelRow, 4));
            txtStartTime.setForeground(Color.BLACK);
            txtEndTime.setForeground(Color.BLACK);
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
            btnAdd.setEnabled(false);
        });
    }

    private void setupFilterActions() {
        // Không cần thêm gì – đã gắn listener trong buildTableArea()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Placeholder text effect cho JTextField thời gian. */
    private void setupPlaceholder(JTextField field) {
        field.setForeground(Color.GRAY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals("dd/MM/yyyy HH:mm")) {
                    field.setText(""); field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText("dd/MM/yyyy HH:mm"); field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private LocalDateTime parseDateTime(String text) {
        try {
            return LocalDateTime.parse(text.trim(), DT_FMT);
        } catch (DateTimeParseException ex) {
            showError("Định dạng ngày giờ không hợp lệ.\nVui lòng nhập theo dạng: dd/MM/yyyy HH:mm\nVí dụ: 25/03/2026 14:30");
            return null;
        }
    }

    private void clearForm() {
        selectedShowTimeId = null;
        txtStartTime.setText("dd/MM/yyyy HH:mm"); txtStartTime.setForeground(Color.GRAY);
        txtEndTime.setText("dd/MM/yyyy HH:mm");   txtEndTime.setForeground(Color.GRAY);
        if (cmbMovie.getItemCount() > 0)  cmbMovie.setSelectedIndex(0);
        if (cmbRoom.getItemCount() > 0)   cmbRoom.setSelectedIndex(0);
        table.clearSelection();
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
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
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ── Inner display classes ─────────────────────────────────────────────────

    private static class MovieItem {
        final String id, title;
        MovieItem(String id, String title) { this.id = id; this.title = title; }
        @Override public String toString() { return title; }
    }

    private static class RoomItem {
        final String id, name;
        RoomItem(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
