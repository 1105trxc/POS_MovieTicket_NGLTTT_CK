package com.cinema.management.view.management;

import com.cinema.management.model.entity.Movie;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MovieSelectionDialog extends JDialog {

    private final List<Movie> movies;
    private Movie selectedMovie = null;

    private JTextField txtSearch;
    private JTable tblMovie;
    private DefaultTableModel tableModel;

    // Màu sắc FlatLaf hiện đại
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color BG = new Color(245, 247, 250);

    public MovieSelectionDialog(Window owner, List<Movie> movies) {
        super(owner, "Chọn phim", ModalityType.APPLICATION_MODAL);
        this.movies = movies;

        setSize(700, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BG);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        buildUI();
        loadDataToTable(movies);
    }

    private void buildUI() {
        // --- Search Panel ---
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Search Movie Title:");
        lblSearch.setText("Tìm tên phim:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setText("Tìm tên phim:");
        lblSearch.setText("Tìm tên phim:");
        pnlSearch.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập tên phim...");
        txtSearch.setPreferredSize(new Dimension(0, 36));
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        add(pnlSearch, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });

        // --- Table Panel ---
        String[] cols = {"Mã phim", "Tên phim", "Thời lượng (phút)", "Ngày phát hành"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblMovie = new JTable(tableModel);
        tblMovie.setRowHeight(30);
        tblMovie.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblMovie.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblMovie.setSelectionBackground(new Color(224, 242, 254));

        // Ẩn cột ID (Giữ logic nhưng không hiển thị)
        tblMovie.getColumnModel().getColumn(0).setMinWidth(0);
        tblMovie.getColumnModel().getColumn(0).setMaxWidth(0);
        tblMovie.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(tblMovie);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scrollPane, BorderLayout.CENTER);

        // --- Buttons Panel ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        JButton btnSelect = new JButton("Chọn phim");
        btnSelect.setBackground(PRIMARY);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSelect);
        add(pnlButtons, BorderLayout.SOUTH);

        // --- Events ---
        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());
        tblMovie.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Movie> list) {
        tableModel.setRowCount(0);
        for (Movie m : list) {
            tableModel.addRow(new Object[]{
                    m.getMovieId(),
                    m.getTitle(),
                    m.getDuration(),
                    m.getReleaseDate() != null ? m.getReleaseDate().toString() : ""
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadDataToTable(movies);
            return;
        }
        List<Movie> filtered = movies.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(kw))
                .toList();
        loadDataToTable(filtered);
    }

    private void confirmSelection() {
        int row = tblMovie.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            selectedMovie = movies.stream().filter(m -> m.getMovieId().equals(id)).findFirst().orElse(null);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phim trong danh sách!", "Lưu ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Movie getSelectedMovie() {
        return selectedMovie;
    }
}
