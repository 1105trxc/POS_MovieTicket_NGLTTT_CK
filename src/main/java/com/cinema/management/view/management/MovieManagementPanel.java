package com.cinema.management.view.management;

import com.cinema.management.controller.MovieController;
import com.cinema.management.model.entity.Genre;
import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.MovieGenre;
import com.cinema.management.model.entity.MovieGenreId;
import com.cinema.management.repository.GenreRepository;
import com.cinema.management.util.IdGenerator;
import com.cinema.management.view.dialog.GenreSelectionDialog;

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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieManagementPanel extends JPanel {

    private final MovieController movieController = new MovieController();
    private final GenreRepository genreRepository = new GenreRepository();
    private final java.util.Map<String, Genre> genreMap = new java.util.HashMap<>();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);

    private final String[] COLUMNS = { "Mã phim", "Tên phim", "Thể loại", "Thời lượng", "Ngày chiếu", "Nhãn tuổi" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    private final JTextField txtId = new JTextField(16);
    private final JTextField txtTitle = new JTextField(16);
    private final JSpinner spinDuration = new JSpinner(new SpinnerNumberModel(120, 1, 500, 1));
    private final JTextField txtReleaseDate = new JTextField(16);
    private final JTextArea txtDescription = new JTextArea(4, 16);
    private final JComboBox<String> cbAge = new JComboBox<>(new String[] { "P", "K", "T13", "T16", "T18" });

    // Thay thế JList bằng Nút Chọn & Text hiển thị
    private List<Genre> selectedGenres = new ArrayList<>();
    private final JTextField txtGenresDisplay = new JTextField(16);
    private final JButton btnSelectGenre = new JButton("Chọn thể loại...");

    private final JButton btnAdd = new JButton("Thêm mới");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnClear = new JButton("Clear Form");

    private Movie selectedMovie;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MovieManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        txtReleaseDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        // Khóa mã không cho nhập tay
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));

        // add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadGenres();
        loadTable();
        configureTableSelection();
    }

    // private JPanel buildHeader() {
    // JPanel header = new JPanel(new BorderLayout());
    // header.setBackground(HEADER);
    // header.setBorder(new EmptyBorder(16, 20, 16, 20));

    // // JLabel title = new JLabel("QUẢN LÝ PHIM");
    // // title.setFont(new Font("Segoe UI", Font.BOLD, 22));
    // // title.setForeground(Color.WHITE);

    // // JLabel sub = new JLabel("Thêm / Sửa thông tin và nhãn phân loại tuổi của
    // phim");
    // // sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    // // sub.setForeground(new Color(148, 163, 184));

    // // JPanel left = new JPanel();
    // // left.setOpaque(false);
    // // left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    // // left.add(title);
    // // left.add(Box.createVerticalStrut(4));
    // // left.add(sub);
    // // header.add(left, BorderLayout.WEST);
    // // return header;
    // }

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

        // JTextField txtLiveSearch = new JTextField();
        // txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã,
        // Tên, Thể loại...");
        // txtLiveSearch.setPreferredSize(new Dimension(350, 36));
        // filterBar.add(txtLiveSearch);
        JTextField txtLiveSearch = new JTextField(20);
        txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã, Tên, Thể loại...");
        txtLiveSearch.setPreferredSize(new Dimension(350, 35));
        txtLiveSearch.setMinimumSize(new Dimension(200, 35));
        filterBar.add(lblSearch);
        filterBar.add(txtLiveSearch);
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
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
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
                " Thông tin Phim ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(250, 36);
        txtId.setPreferredSize(fieldSize);
        txtTitle.setPreferredSize(fieldSize);
        spinDuration.setPreferredSize(fieldSize);
        txtReleaseDate.setPreferredSize(fieldSize);
        cbAge.setPreferredSize(fieldSize);

        txtGenresDisplay.setEditable(false);
        txtGenresDisplay.setBackground(new Color(241, 245, 249));
        txtGenresDisplay.setPreferredSize(new Dimension(150, 36));
        btnSelectGenre.setPreferredSize(new Dimension(100, 36));
        btnSelectGenre.addActionListener(e -> openGenreSelectionDialog());

        JPanel pnlGenreSelection = new JPanel(new BorderLayout(5, 0));
        pnlGenreSelection.setOpaque(false);
        pnlGenreSelection.add(txtGenresDisplay, BorderLayout.CENTER);
        pnlGenreSelection.add(btnSelectGenre, BorderLayout.EAST);
        pnlGenreSelection.setPreferredSize(fieldSize);

        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        scrollDesc.setPreferredSize(new Dimension(250, 60));

        int row = 0;
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        fields.add(new JLabel("Mã Phim:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        fields.add(txtId, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Tên Phim:"), gc);
        gc.gridx = 1;
        fields.add(txtTitle, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Thời lượng:"), gc);
        gc.gridx = 1;
        fields.add(spinDuration, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Ngày chiếu:"), gc);
        gc.gridx = 1;
        fields.add(txtReleaseDate, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Nhãn tuổi (Đỏ):"), gc);
        gc.gridx = 1;
        fields.add(cbAge, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Thể loại:"), gc);
        gc.gridx = 1;
        fields.add(pnlGenreSelection, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Mô tả:"), gc);
        gc.gridx = 1;
        fields.add(scrollDesc, gc);

        panel.add(fields, BorderLayout.NORTH);

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
        tbl.setSelectionBackground(new Color(224, 242, 254));
        tbl.setSelectionForeground(new Color(15, 23, 42));
        tbl.setShowVerticalLines(false);
    }

    private void loadGenres() {
        genreMap.clear();
        List<Genre> genres = genreRepository.findAll();
        for (Genre g : genres) {
            genreMap.put(g.getGenreId(), g);
        }
    }

    private void openGenreSelectionDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        List<Genre> allGenres = genreRepository.findAll(); // Re-fetch to ensure fresh data
        GenreSelectionDialog dialog = new GenreSelectionDialog(
                owner, allGenres, selectedGenres, result -> {
                    this.selectedGenres = result;
                    updateGenresDisplayText();
                });
        dialog.setVisible(true);
    }

    private void updateGenresDisplayText() {
        if (selectedGenres == null || selectedGenres.isEmpty()) {
            txtGenresDisplay.setText("");
            return;
        }
        String names = selectedGenres.stream()
                .map(Genre::getGenreName)
                .collect(java.util.stream.Collectors.joining(", "));
        txtGenresDisplay.setText(names);
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<Movie> movies = movieController.getAllMovies();
        for (Movie m : movies) {
            String release = m.getReleaseDate() != null ? m.getReleaseDate().format(formatter) : "";
            String genres = m.getMovieGenres() != null ? m.getMovieGenres().stream()
                    .map(mg -> mg.getGenre().getGenreName()).collect(Collectors.joining(", ")) : "";

            tableModel.addRow(new Object[] {
                    m.getMovieId(), m.getTitle(), genres, m.getDuration() + " phút",
                    release, m.getAgeRestriction()
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
            String id = (String) tableModel.getValueAt(modelRow, 0);

            selectedMovie = movieController.getAllMovies().stream()
                    .filter(x -> x.getMovieId().equals(id)).findFirst().orElse(null);

            if (selectedMovie != null) {
                txtId.setText(selectedMovie.getMovieId());
                txtId.setEnabled(false);
                txtTitle.setText(selectedMovie.getTitle());
                spinDuration.setValue(selectedMovie.getDuration());
                txtReleaseDate.setText(
                        selectedMovie.getReleaseDate() != null ? selectedMovie.getReleaseDate().format(formatter) : "");
                cbAge.setSelectedItem(selectedMovie.getAgeRestriction());
                txtDescription.setText(selectedMovie.getDescription());

                // Set genres
                selectedGenres = new ArrayList<>();
                if (selectedMovie.getMovieGenres() != null) {
                    for (MovieGenre mg : selectedMovie.getMovieGenres()) {
                        selectedGenres.add(mg.getGenre());
                    }
                }
                updateGenresDisplayText();

                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnAdd.setEnabled(false);
            }
        });
    }

    private boolean validateForm() {
        if (txtTitle.getText().trim().isEmpty()) {
            showError("Tên phim không được rỗng!");
            return false;
        }
        return true;
    }

    private void onAdd() {
        if (!validateForm())
            return;
        Movie m = new Movie();
        m.setMovieId(IdGenerator.generateId("MV", Movie.class, "movieId"));
        m.setTitle(txtTitle.getText().trim());
        m.setDuration((Integer) spinDuration.getValue());
        m.setAgeRestriction((String) cbAge.getSelectedItem());
        m.setDescription(txtDescription.getText().trim());

        try {
            m.setReleaseDate(LocalDate.parse(txtReleaseDate.getText().trim(), formatter));
        } catch (DateTimeParseException ex) {
            showError("Ngày phát hành không hợp lệ (dd/MM/yyyy).");
            return;
        }

        m.setMovieGenres(buildMovieGenres(m));

        try {
            movieController.addMovie(m);
            showSuccess("Thêm Phim thành công.");
            clearForm();
            loadTable();
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedMovie == null || !validateForm())
            return;
        selectedMovie.setTitle(txtTitle.getText().trim());
        selectedMovie.setDuration((Integer) spinDuration.getValue());
        selectedMovie.setAgeRestriction((String) cbAge.getSelectedItem());
        selectedMovie.setDescription(txtDescription.getText().trim());

        try {
            selectedMovie.setReleaseDate(LocalDate.parse(txtReleaseDate.getText().trim(), formatter));
        } catch (DateTimeParseException ex) {
            showError("Ngày phát hành không hợp lệ (dd/MM/yyyy).");
            return;
        }

        // Thay doi the loai
        selectedMovie.getMovieGenres().clear();
        selectedMovie.getMovieGenres().addAll(buildMovieGenres(selectedMovie));

        try {
            movieController.updateMovie(selectedMovie);
            showSuccess("Cập nhật Phim thành công.");
            clearForm();
            loadTable();
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void onDelete() {
        if (selectedMovie == null)
            return;
        int check = JOptionPane.showConfirmDialog(this, "Xóa phim này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (check != JOptionPane.YES_OPTION)
            return;

        try {
            movieController.deleteMovie(selectedMovie.getMovieId());
            showSuccess("Xóa Phim thành công.");
            clearForm();
            loadTable();
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private List<MovieGenre> buildMovieGenres(Movie movie) {
        List<MovieGenre> mgs = new ArrayList<>();
        if (selectedGenres == null)
            return mgs;

        for (Genre g : selectedGenres) {
            // Fix: Create new Genre proxy to avoid session/collection conflict
            Genre proxy = new Genre();
            proxy.setGenreId(g.getGenreId());

            MovieGenre mg = new MovieGenre();
            mg.setId(new MovieGenreId(movie.getMovieId(), proxy.getGenreId()));
            mg.setMovie(movie);
            mg.setGenre(proxy);
            mgs.add(mg);
        }
        return mgs;
    }

    private void clearForm() {
        selectedMovie = null;
        txtId.setText(IdGenerator.generateId("MV", Movie.class, "movieId"));
        txtId.setEnabled(false);
        txtTitle.setText("");
        spinDuration.setValue(120);
        txtReleaseDate.setText("");
        txtDescription.setText("");
        cbAge.setSelectedIndex(0);
        selectedGenres = new ArrayList<>();
        updateGenresDisplayText();
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
