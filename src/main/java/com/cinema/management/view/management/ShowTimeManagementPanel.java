package com.cinema.management.view.management;

import com.cinema.management.controller.RoomController;
import com.cinema.management.controller.ShowTimeController;
import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Room;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.view.dialog.RoomSelectionDialog;
import com.cinema.management.view.dialog.MovieSelectionDialog;

import javax.swing.text.MaskFormatter;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ShowTimeManagementPanel extends JPanel {

    private final ShowTimeController showTimeController = new ShowTimeController();
    private final RoomController roomController = new RoomController();
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Bảng màu hiện đại
    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);

    // Thêm Movie ID và Room ID để lưu trữ ngầm, phục vụ việc click vào Table nạp
    // lại dữ liệu
    private final String[] COLUMNS = { "Mã suất chiếu", "Mã phim", "Mã phòng", "Phim", "Phòng", "Giờ bắt đầu",
            "Giờ kết thúc" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    // Bộ lọc TableRowSorter cho tính năng Live Search
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Form nhập liệu sử dụng Dialog
    private final JTextField txtSelectedMovie = new JTextField(16);
    private final JTextField txtSelectedRoom = new JTextField(16);
    private String currentFormMovieId = null;
    private String currentFormRoomId = null;
    private Integer currentFormMovieDuration = null;
    private boolean autoSuggestEndTimeEnabled = true;

    private JFormattedTextField txtStartTime;
    private JFormattedTextField txtEndTime;

    private final JButton btnAdd = new JButton("Thêm mới");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnClear = new JButton("Đặt lại form");

    private String selectedShowTimeId;

    public ShowTimeManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        // --- KHỞI TẠO MASK FORMATTER ---
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/#### ##:##");
            dateMask.setPlaceholderCharacter('_'); // Hiển thị dấu _ cho vị trí chưa nhập

            txtStartTime = new JFormattedTextField(dateMask);
            txtEndTime = new JFormattedTextField(dateMask);

            // Vẫn giữ lại placeholder mờ của FlatLaf khi trường rỗng
            txtStartTime.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy HH:mm");
            txtEndTime.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy HH:mm");
        } catch (ParseException e) {
            e.printStackTrace();
            // Fallback nếu mask lỗi (rất hiếm khi xảy ra)
            txtStartTime = new JFormattedTextField();
            txtEndTime = new JFormattedTextField();
        }

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        registerAutoSuggestListeners();

        loadTable();
        configureTableSelection();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ SUẤT CHIẾU");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Quản lý lịch chiếu phim, phòng chiếu và thời gian");
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

        // --- LIVE SEARCH BAR ---
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setOpaque(false);

        JLabel lblSearch = new JLabel(" Tìm nhanh:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterBar.add(lblSearch);

        JTextField txtLiveSearch = new JTextField();
        txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo phim, phòng, thời gian hoặc mã...");
        txtLiveSearch.setPreferredSize(new Dimension(350, 36));
        filterBar.add(txtLiveSearch);

        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setOpaque(false);
        filterWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        filterWrapper.add(filterBar, BorderLayout.CENTER);
        panel.add(filterWrapper, BorderLayout.NORTH);

        // Table setup
        styleTable(table);

        // Khởi tạo RowSorter và gắn vào JTable
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Lắng nghe sự kiện gõ phím để lọc tức thì
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
                    // (?i) để không phân biệt hoa thường
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        // Ẩn cột Movie ID (index 1) và Room ID (index 2)
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(0);
        table.getColumnModel().getColumn(2).setMinWidth(0);
        table.getColumnModel().getColumn(2).setMaxWidth(0);
        table.getColumnModel().getColumn(2).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildFormPanel() {
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
                "  Thông tin suất chiếu  ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.anchor = GridBagConstraints.WEST;

        // --- Tích hợp Movie Dialog ---
        txtSelectedMovie.setEditable(false);
        txtSelectedMovie.putClientProperty("JTextField.placeholderText", "Chưa chọn phim...");
        JButton btnChooseMovie = new JButton("...");
        btnChooseMovie.addActionListener(e -> openMovieSelectionDialog());

        JPanel pnlMovieInput = new JPanel(new BorderLayout(5, 0));
        pnlMovieInput.setOpaque(false);
        pnlMovieInput.add(txtSelectedMovie, BorderLayout.CENTER);
        pnlMovieInput.add(btnChooseMovie, BorderLayout.EAST);

        // --- Tích hợp Room Dialog ---
        txtSelectedRoom.setEditable(false);
        txtSelectedRoom.putClientProperty("JTextField.placeholderText", "Chưa chọn phòng...");
        JButton btnChooseRoom = new JButton("...");
        btnChooseRoom.addActionListener(e -> openRoomSelectionDialog());

        JPanel pnlRoomInput = new JPanel(new BorderLayout(5, 0));
        pnlRoomInput.setOpaque(false);
        pnlRoomInput.add(txtSelectedRoom, BorderLayout.CENTER);
        pnlRoomInput.add(btnChooseRoom, BorderLayout.EAST);

        Dimension fieldSize = new Dimension(220, 36);
        txtStartTime.setPreferredSize(fieldSize);
        txtEndTime.setPreferredSize(fieldSize);

        gc.gridx = 0;
        gc.gridy = 0;
        fields.add(new JLabel("Phim:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(pnlMovieInput, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Phòng:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(pnlRoomInput, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Giờ bắt đầu:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtStartTime, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Giờ kết thúc:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtEndTime, gc);

        panel.add(fields, BorderLayout.NORTH);

        // Buttons Panel
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

    // Mở hộp thoại chọn Phim
    private void openMovieSelectionDialog() {
        List<Movie> allMovies = showTimeController.getAllMovies();
        MovieSelectionDialog dialog = new MovieSelectionDialog(SwingUtilities.getWindowAncestor(this), allMovies);
        dialog.setVisible(true);

        Movie selected = dialog.getSelectedMovie();
        if (selected != null) {
            currentFormMovieId = selected.getMovieId();
            currentFormMovieDuration = selected.getDuration();
            txtSelectedMovie.setText(selected.getTitle());
            suggestEndTimeFromMovieDuration();
        }
    }

    // Mở hộp thoại chọn Phòng
    private void openRoomSelectionDialog() {
        List<Room> allRooms = roomController.getAllRooms();
        RoomSelectionDialog dialog = new RoomSelectionDialog(SwingUtilities.getWindowAncestor(this), allRooms);
        dialog.setVisible(true);

        Room selected = dialog.getSelectedRoom();
        if (selected != null) {
            currentFormRoomId = selected.getRoomId();
            txtSelectedRoom.setText(selected.getRoomName());
        }
    }

    private void onAdd() {
        if (currentFormMovieId == null || currentFormRoomId == null) {
            showError("Vui lòng chọn phim và phòng chiếu.");
            return;
        }

        LocalDateTime start = parseDateTime(txtStartTime.getText());
        LocalDateTime end = parseDateTime(txtEndTime.getText());
        if (start == null || end == null)
            return;
        if (!validateEndTimeByMovieDuration(start, end))
            return;

        try {
            showTimeController.addShowTime(currentFormMovieId, currentFormRoomId, start, end);
            showSuccess("Thêm suất chiếu thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedShowTimeId == null) {
            showError("Vui lòng chọn suất chiếu cần cập nhật.");
            return;
        }
        if (currentFormMovieId == null || currentFormRoomId == null) {
            showError("Vui lòng chọn phim và phòng chiếu.");
            return;
        }

        LocalDateTime start = parseDateTime(txtStartTime.getText());
        LocalDateTime end = parseDateTime(txtEndTime.getText());
        if (start == null || end == null)
            return;
        if (!validateEndTimeByMovieDuration(start, end))
            return;

        try {
            showTimeController.updateShowTime(selectedShowTimeId, currentFormMovieId, currentFormRoomId, start, end);
            showSuccess("Cập nhật suất chiếu thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onDelete() {
        if (selectedShowTimeId == null) {
            showError("Vui lòng chọn suất chiếu cần xóa.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa suất chiếu này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION)
            return;

        try {
            showTimeController.deleteShowTime(selectedShowTimeId);
            showSuccess("Xóa suất chiếu thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    public void refreshData() {
        loadTable();
    }

    // Hàm loadTable giờ không cần truyền tham số filter nữa
    private void loadTable() {
        tableModel.setRowCount(0);
        List<ShowTime> list = showTimeController.getAllShowTimes();

        for (ShowTime st : list) {
            String mId = st.getMovie() != null ? st.getMovie().getMovieId() : "";
            String rId = st.getRoom() != null ? st.getRoom().getRoomId() : "";
            String mTitle = st.getMovie() != null ? st.getMovie().getTitle() : "-";
            String rName = st.getRoom() != null ? st.getRoom().getRoomName() : "-";

            tableModel.addRow(new Object[] {
                    st.getShowTimeId(),
                    mId,
                    rId,
                    mTitle,
                    rName,
                    st.getStartTime().format(DT_FMT),
                    st.getEndTime().format(DT_FMT)
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
            int row = table.convertRowIndexToModel(viewRow);

            // Đọc dữ liệu từ bảng (bao gồm cột ẩn)
            selectedShowTimeId = (String) tableModel.getValueAt(row, 0);
            currentFormMovieId = (String) tableModel.getValueAt(row, 1);
            currentFormRoomId = (String) tableModel.getValueAt(row, 2);
            currentFormMovieDuration = findMovieDurationById(currentFormMovieId);

            // Đưa lên Form hiển thị
            autoSuggestEndTimeEnabled = false;
            txtSelectedMovie.setText((String) tableModel.getValueAt(row, 3));
            txtSelectedRoom.setText((String) tableModel.getValueAt(row, 4));
            txtStartTime.setText((String) tableModel.getValueAt(row, 5));
            txtEndTime.setText((String) tableModel.getValueAt(row, 6));
            autoSuggestEndTimeEnabled = true;

            btnAdd.setEnabled(false);
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
        });
    }

    private LocalDateTime parseDateTime(String text) {
        // Nếu chuỗi rỗng hoặc người dùng chưa nhập gì (chỉ chứa Mask)
        if (text == null || text.trim().isEmpty() || text.equals("__/__/____ __:__")) {
            showError("Không được để trống thời gian.\nVui lòng nhập ngày giờ hợp lệ.");
            return null;
        }
        try {
            return LocalDateTime.parse(text.trim(), DT_FMT);
        } catch (DateTimeParseException ex) {
            showError("Định dạng ngày giờ không hợp lệ.\nVui lòng nhập đúng: dd/MM/yyyy HH:mm");
            return null;
        }
    }

    private void registerAutoSuggestListeners() {
        txtStartTime.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                suggestEndTimeFromMovieDuration();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                suggestEndTimeFromMovieDuration();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                suggestEndTimeFromMovieDuration();
            }
        });
    }

    private void suggestEndTimeFromMovieDuration() {
        if (!autoSuggestEndTimeEnabled)
            return;
        if (currentFormMovieDuration == null || currentFormMovieDuration <= 0)
            return;

        LocalDateTime start = parseDateTimeSilently(txtStartTime.getText());
        if (start == null)
            return;

        LocalDateTime suggestedEnd = start.plusMinutes(currentFormMovieDuration);
        txtEndTime.setText(suggestedEnd.format(DT_FMT));
    }

    private LocalDateTime parseDateTimeSilently(String text) {
        if (text == null || text.trim().isEmpty() || text.equals("__/__/____ __:__")) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.trim(), DT_FMT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Integer findMovieDurationById(String movieId) {
        if (movieId == null || movieId.isBlank())
            return null;
        List<Movie> allMovies = showTimeController.getAllMovies();
        for (Movie movie : allMovies) {
            if (movieId.equals(movie.getMovieId())) {
                return movie.getDuration();
            }
        }
        return null;
    }

    private boolean validateEndTimeByMovieDuration(LocalDateTime start, LocalDateTime end) {
        if (currentFormMovieDuration == null || currentFormMovieDuration <= 0) {
            return true;
        }
        LocalDateTime minEnd = start.plusMinutes(currentFormMovieDuration);
        if (end.isBefore(minEnd)) {
            showError("Giờ kết thúc phải lớn hơn hoặc bằng Giờ bắt đầu + thời lượng phim (" +
                    minEnd.format(DT_FMT) + ").");
            return false;
        }
        if (false && end.isBefore(minEnd)) {
            showError("End time phải lớn hơn hoặc bằng Start time + thời lượng phim (" +
                    minEnd.format(DT_FMT) + ").");
            return false;
        }
        return true;
    }

    private void clearForm() {
        selectedShowTimeId = null;
        currentFormMovieId = null;
        currentFormRoomId = null;
        currentFormMovieDuration = null;

        txtSelectedMovie.setText("");
        txtSelectedRoom.setText("");

        // Reset JFormattedTextField
        txtStartTime.setValue(null);
        txtEndTime.setValue(null);

        table.clearSelection();
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
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

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
