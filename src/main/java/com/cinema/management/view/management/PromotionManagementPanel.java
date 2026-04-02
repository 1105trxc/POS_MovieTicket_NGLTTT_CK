package com.cinema.management.view.management;

import com.cinema.management.controller.PromotionController;
import com.cinema.management.controller.MovieController;
import com.cinema.management.model.entity.Promotion;
import com.cinema.management.model.entity.Movie;
import com.cinema.management.util.FormatUtil;
import com.cinema.management.util.IdGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PromotionManagementPanel extends JPanel {

    private final PromotionController promotionController = new PromotionController();
    private final MovieController movieController = new MovieController();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);

    private final String[] COLUMNS = { "ID Khuyến Mãi", "Tên KM", "Mã Code", "% Giảm", "Max Giảm (VNĐ)", "Ngày Bắt Đầu",
            "Ngày Kết Thúc" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    private final JTextField txtId = new JTextField(16);
    private final JTextField txtName = new JTextField(16); // maps to description
    private final JTextField txtCode = new JTextField(16);
    private final JSpinner spinPercent = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
    private final JTextField txtMaxAmount = new JTextField(16);
    private final JTextField txtStartDate = new JTextField(16);
    private final JTextField txtEndDate = new JTextField(16);
    private final JComboBox<MovieItem> cbApplyToMovie = new JComboBox<>();

    private final JButton btnAdd = new JButton("Tạo KM");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnClear = new JButton("Clear Form");

    private Promotion selectedPromo;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PromotionManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        txtStartDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        txtEndDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        // Khóa mã không cho nhập tay
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));

        loadTable();
        loadMoviesForCombo();
        configureTableSelection();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ KHUYẾN MÃI & CRM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Tạo chương trình khuyến mãi sự kiện & Theo dõi hạn mức chặn lỗ");
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
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterBar.add(lblSearch);

        JTextField txtLiveSearch = new JTextField();
        txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã Code hoặc Tên KM...");
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
                " Thông tin sự kiện khuyến mãi ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(250, 36);
        txtId.setPreferredSize(fieldSize);
        txtName.setPreferredSize(fieldSize);
        txtCode.setPreferredSize(fieldSize);
        spinPercent.setPreferredSize(fieldSize);
        txtMaxAmount.setPreferredSize(fieldSize);
        txtStartDate.setPreferredSize(fieldSize);
        txtEndDate.setPreferredSize(fieldSize);
        cbApplyToMovie.setPreferredSize(fieldSize);

        int row = 0;
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        fields.add(new JLabel("ID Khuyến mãi:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        fields.add(txtId, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Tên khuyến mãi:"), gc);
        gc.gridx = 1;
        fields.add(txtName, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Mã Code:"), gc);
        gc.gridx = 1;
        fields.add(txtCode, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("% Giảm:"), gc);
        gc.gridx = 1;
        fields.add(spinPercent, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Giảm Max (VNĐ):"), gc);
        gc.gridx = 1;
        fields.add(txtMaxAmount, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Ngày bắt đầu:"), gc);
        gc.gridx = 1;
        fields.add(txtStartDate, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Ngày kết thúc:"), gc);
        gc.gridx = 1;
        fields.add(txtEndDate, gc);
        row++;

        gc.gridx = 0;
        gc.gridy = row;
        fields.add(new JLabel("Áp dụng cho phim:"), gc);
        gc.gridx = 1;
        fields.add(cbApplyToMovie, gc);

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

    private void loadTable() {
        tableModel.setRowCount(0);
        List<Promotion> promos = promotionController.getAllPromotions();
        for (Promotion p : promos) {
            String txtPct = (p.getDiscountPercent() != null) ? p.getDiscountPercent() + "%" : "0%";
            String txtMax = (p.getMaxDiscountAmount() != null)
                    ? FormatUtil.formatCurrency(p.getMaxDiscountAmount().doubleValue())
                    : "0";
            String start = p.getStartDate() != null ? p.getStartDate().format(formatter) : "";
            String end = p.getExpiryDate() != null ? p.getExpiryDate().format(formatter) : "";

            tableModel.addRow(new Object[] {
                    p.getPromotionId(), p.getDescription(), p.getCode(), txtPct, txtMax, start, end
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
            selectedPromo = promotionController.getAllPromotions().stream()
                    .filter(x -> x.getPromotionId().equals(id)).findFirst().orElse(null);

            if (selectedPromo != null) {
                txtId.setText(selectedPromo.getPromotionId());
                txtId.setEnabled(false);
                txtName.setText(selectedPromo.getDescription());
                txtCode.setText(selectedPromo.getCode());
                spinPercent.setValue(
                        selectedPromo.getDiscountPercent() != null ? selectedPromo.getDiscountPercent().doubleValue()
                                : 0.0);

                // Rule 3: Khi fill ngược từ bảng lên form, TextBox bỏ phẩy
                txtMaxAmount.setText(selectedPromo.getMaxDiscountAmount() != null
                        ? String.format("%.0f", selectedPromo.getMaxDiscountAmount().doubleValue())
                        : "0");

                txtStartDate.setText(
                        selectedPromo.getStartDate() != null ? selectedPromo.getStartDate().format(formatter) : "");
                txtEndDate.setText(
                        selectedPromo.getExpiryDate() != null ? selectedPromo.getExpiryDate().format(formatter) : "");

                if (selectedPromo.getApplyToMovie() != null) {
                    for (int i = 0; i < cbApplyToMovie.getItemCount(); i++) {
                        if (cbApplyToMovie.getItemAt(i).movieId != null && cbApplyToMovie.getItemAt(i).movieId
                                .equals(selectedPromo.getApplyToMovie().getMovieId())) {
                            cbApplyToMovie.setSelectedIndex(i);
                            break;
                        }
                    }
                } else {
                    if (cbApplyToMovie.getItemCount() > 0)
                        cbApplyToMovie.setSelectedIndex(0);
                }

                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnAdd.setEnabled(false);
            }
        });
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showError("Tên khuyến mãi không được để trống!");
            return false;
        }
        if (txtCode.getText().trim().isEmpty()) {
            showError("Mã Code không được để trống!");
            return false;
        }

        // Validate % giảm
        double pct = (Double) spinPercent.getValue();
        if (pct <= 0) {
            showError("Phần trăm giảm giá phải lớn hơn 0!");
            return false;
        }

        // Validate max giảm (VNĐ) > 0
        String maxAmountStr = txtMaxAmount.getText().trim().replace(",", "");
        if (maxAmountStr.isEmpty()) {
            showError("Giảm Max (VNĐ) không được để trống!");
            return false;
        }
        try {
            double maxAmount = Double.parseDouble(maxAmountStr);
            if (maxAmount <= 0) {
                showError("Giảm Max (VNĐ) phải lớn hơn 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Giảm Max (VNĐ) không hợp lệ! Vui lòng nhập số.");
            return false;
        }

        // Validate ngày bắt đầu / kết thúc
        String startStr = txtStartDate.getText().trim();
        String endStr = txtEndDate.getText().trim();
        if (startStr.isEmpty() || endStr.isEmpty()) {
            showError("Ngày bắt đầu và ngày kết thúc không được để trống!");
            return false;
        }
        try {
            LocalDate startDate = LocalDate.parse(startStr, formatter);
            LocalDate endDate = LocalDate.parse(endStr, formatter);
            if (startDate.isAfter(endDate)) {
                showError("Ngày bắt đầu không được sau ngày kết thúc!");
                return false;
            }
        } catch (DateTimeParseException ex) {
            showError("Định dạng ngày không hợp lệ (dd/MM/yyyy).");
            return false;
        }

        return true;
    }

    private void onAdd() {
        if (!validateForm())
            return;
        Promotion p = new Promotion();
        p.setPromotionId(IdGenerator.generateId("PR", Promotion.class, "promotionId"));
        p.setDescription(txtName.getText().trim());
        p.setCode(txtCode.getText().trim());
        p.setDiscountPercent(BigDecimal.valueOf((Double) spinPercent.getValue()));
        p.setMaxDiscountAmount(BigDecimal.valueOf(FormatUtil.parseCurrency(txtMaxAmount.getText().trim())));

        try {
            p.setStartDate(LocalDate.parse(txtStartDate.getText().trim(), formatter));
            p.setExpiryDate(LocalDate.parse(txtEndDate.getText().trim(), formatter));
        } catch (DateTimeParseException ex) {
            showError("Định dạng ngày không hợp lệ (dd/MM/yyyy).");
            return;
        }

        MovieItem selectedMovie = (MovieItem) cbApplyToMovie.getSelectedItem();
        if (selectedMovie != null && selectedMovie.movieObj != null) {
            p.setApplyToMovie(selectedMovie.movieObj);
        } else {
            p.setApplyToMovie(null);
        }

        try {
            promotionController.addPromotion(p);
            showSuccess("Thêm KM thành công.");
            clearForm();
            loadTable();
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedPromo == null || !validateForm())
            return;
        selectedPromo.setDescription(txtName.getText().trim());
        selectedPromo.setCode(txtCode.getText().trim());
        selectedPromo.setDiscountPercent(BigDecimal.valueOf((Double) spinPercent.getValue()));
        selectedPromo.setMaxDiscountAmount(BigDecimal.valueOf(FormatUtil.parseCurrency(txtMaxAmount.getText().trim())));

        try {
            selectedPromo.setStartDate(LocalDate.parse(txtStartDate.getText().trim(), formatter));
            selectedPromo.setExpiryDate(LocalDate.parse(txtEndDate.getText().trim(), formatter));
        } catch (DateTimeParseException ex) {
            showError("Định dạng ngày không hợp lệ (dd/MM/yyyy).");
            return;
        }

        MovieItem selectedMovie = (MovieItem) cbApplyToMovie.getSelectedItem();
        if (selectedMovie != null && selectedMovie.movieObj != null) {
            selectedPromo.setApplyToMovie(selectedMovie.movieObj);
        } else {
            selectedPromo.setApplyToMovie(null);
        }

        try {
            promotionController.updatePromotion(selectedPromo);
            showSuccess("Cập nhật KM thành công.");
            clearForm();
            loadTable();
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void onDelete() {
        if (selectedPromo == null)
            return;
        int check = JOptionPane.showConfirmDialog(this, "Bạn có muốn xóa Khuyến Mãi này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (check == JOptionPane.YES_OPTION) {
            try {
                promotionController.deletePromotion(selectedPromo.getPromotionId());
                showSuccess("Đã xóa KM.");
                clearForm();
                loadTable();
            } catch (Exception e) {
                showError("Lỗi: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        selectedPromo = null;
        txtId.setText(IdGenerator.generateId("PR", Promotion.class, "promotionId"));
        txtId.setEnabled(false);
        txtName.setText("");
        txtCode.setText("");
        spinPercent.setValue(0.0);
        txtMaxAmount.setText("");
        txtStartDate.setText("");
        txtEndDate.setText("");
        if (cbApplyToMovie.getItemCount() > 0)
            cbApplyToMovie.setSelectedIndex(0);
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

    private void loadMoviesForCombo() {
        cbApplyToMovie.removeAllItems();
        cbApplyToMovie.addItem(new MovieItem()); // Default: All movies
        try {
            List<Movie> movies = movieController.getAllMovies();
            if (movies != null) {
                for (Movie m : movies) {
                    cbApplyToMovie.addItem(new MovieItem(m));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MovieItem {
        String movieId;
        String title;
        Movie movieObj;

        public MovieItem(Movie m) {
            this.movieId = m.getMovieId();
            this.title = m.getTitle();
            this.movieObj = m;
        }

        public MovieItem() {
            this.movieId = null;
            this.title = "- Áp dụng cho tất cả phim -";
            this.movieObj = null;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
