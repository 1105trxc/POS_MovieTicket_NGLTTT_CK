package com.cinema.management.view.management;

import com.cinema.management.controller.ProductController;
import com.cinema.management.model.entity.Product;
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
import java.util.List;

public class ProductManagementPanel extends JPanel {

    private final ProductController productController = new ProductController();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color HEADER = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);

    private final String[] COLUMNS = { "Mã sản phẩm", "Tên sản phẩm", "Giá bán (VNĐ)" };
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    private final JTextField txtId = new JTextField(16);
    private final JTextField txtName = new JTextField(16);
    private final JTextField txtPrice = new JTextField(16);

    private final JButton btnAdd = new JButton("Thêm mới");
    private final JButton btnUpdate = new JButton("Cập nhật");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnClear = new JButton("Clear Form");

    private Product selectedProduct;

    public ProductManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        txtName.putClientProperty("JTextField.placeholderText", "Nhập tên sản phẩm F&B...");
        txtPrice.putClientProperty("JTextField.placeholderText", "Nhập giá bán (VNĐ)...");

        // Khóa mã không cho nhập tay
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadTable();
        configureTableSelection();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ F&B (ĐỒ ĂN & NƯỚC UỐNG)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Quản lý bắp, nước uống, combo và các sản phẩm tại quầy");
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

        JLabel lblSearch = new JLabel("🔍 Tìm nhanh:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterBar.add(lblSearch);

        JTextField txtLiveSearch = new JTextField();
        txtLiveSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã SP hoặc Tên SP...");
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
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "  Thông tin sản phẩm F&B  ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(71, 85, 105)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(220, 36);
        txtId.setPreferredSize(fieldSize);
        txtName.setPreferredSize(fieldSize);
        txtPrice.setPreferredSize(fieldSize);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        fields.add(new JLabel("Mã sản phẩm:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtId, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Tên sản phẩm:"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtName, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        fields.add(new JLabel("Giá bán (VNĐ):"), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        fields.add(txtPrice, gc);

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
        tbl.setSelectionBackground(new Color(224, 242, 254));
        tbl.setSelectionForeground(new Color(15, 23, 42));
        tbl.setGridColor(new Color(241, 245, 249));
        tbl.setShowVerticalLines(false);
    }

    private boolean validateForm() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showError("Tên sản phẩm không được để trống!");
            return false;
        }

        String priceText = txtPrice.getText().trim().replace(",", "");
        if (priceText.isEmpty()) {
            showError("Giá bán không được để trống!");
            return false;
        }
        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Giá bán phải lớn hơn 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Giá bán không hợp lệ! Vui lòng nhập số.");
            return false;
        }

        return true;
    }

    private BigDecimal parsePriceFromField() {
        String priceText = txtPrice.getText().trim().replace(",", "");
        return new BigDecimal(priceText);
    }

    private void onAdd() {
        if (!validateForm()) return;

        Product p = new Product();
        p.setProductId(IdGenerator.generateId("FB", Product.class, "productId"));
        p.setProductName(txtName.getText().trim());
        p.setCurrentPrice(parsePriceFromField());

        try {
            productController.addProduct(p);
            showSuccess("Thêm sản phẩm F&B thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError("Lỗi thêm sản phẩm: " + ex.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedProduct == null) {
            showError("Vui lòng chọn sản phẩm cần cập nhật.");
            return;
        }
        if (!validateForm()) return;

        selectedProduct.setProductName(txtName.getText().trim());
        selectedProduct.setCurrentPrice(parsePriceFromField());

        try {
            productController.updateProduct(selectedProduct);
            showSuccess("Cập nhật sản phẩm thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError("Lỗi cập nhật: " + ex.getMessage());
        }
    }

    private void onDelete() {
        if (selectedProduct == null) {
            showError("Vui lòng chọn sản phẩm cần xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this, "Bạn có chắc chắn muốn xóa sản phẩm này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            productController.deleteProduct(selectedProduct.getProductId());
            showSuccess("Xóa sản phẩm thành công.");
            clearForm();
            loadTable();
        } catch (Exception ex) {
            showError("Lỗi xóa (sản phẩm có thể đang được sử dụng trong hóa đơn): " + ex.getMessage());
        }
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<Product> products = productController.getAllProducts();
        for (Product p : products) {
            String priceDisplay = p.getCurrentPrice() != null
                    ? FormatUtil.formatCurrency(p.getCurrentPrice().doubleValue())
                    : "0";
            tableModel.addRow(new Object[] {
                    p.getProductId(), p.getProductName(), priceDisplay
            });
        }
    }

    private void configureTableSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                clearForm();
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);

            String id = (String) tableModel.getValueAt(modelRow, 0);
            selectedProduct = productController.getAllProducts().stream()
                    .filter(p -> p.getProductId().equals(id))
                    .findFirst().orElse(null);

            if (selectedProduct != null) {
                txtId.setText(selectedProduct.getProductId());
                txtId.setEnabled(false);
                txtName.setText(selectedProduct.getProductName());
                txtPrice.setText(selectedProduct.getCurrentPrice() != null
                        ? String.format("%.0f", selectedProduct.getCurrentPrice().doubleValue())
                        : "0");

                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnAdd.setEnabled(false);
            }
        });
    }

    private void clearForm() {
        selectedProduct = null;
        txtId.setText(IdGenerator.generateId("FB", Product.class, "productId"));
        txtId.setEnabled(false);
        txtName.setText("");
        txtPrice.setText("");
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
