package com.cinema.management.view.dialog;

import com.cinema.management.model.entity.Genre;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GenreSelectionDialog extends JDialog {

    private final DefaultListModel<GenreItem> listModel = new DefaultListModel<>();
    private final JList<GenreItem> list = new JList<>(listModel);
    private final JTextField txtSearch = new JTextField(20);
    private final Consumer<List<Genre>> onConfirmCallback;
    private final List<Genre> allGenres;

    public GenreSelectionDialog(Window owner, List<Genre> allGenres, List<Genre> initialSelection,
            Consumer<List<Genre>> onConfirmCallback) {
        super(owner, "Chọn Thể Loại Phim (Nhấn để chọn/bỏ chọn)", ModalityType.APPLICATION_MODAL);
        this.allGenres = allGenres;
        this.onConfirmCallback = onConfirmCallback;

        setSize(400, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 1. Search Panel
        JPanel pnlNorth = new JPanel(new BorderLayout(5, 5));
        pnlNorth.setBorder(new EmptyBorder(15, 15, 5, 15));
        JLabel lblSearch = new JLabel("🔍 Tìm kiếm thể loại:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlNorth.add(lblSearch, BorderLayout.NORTH);

        txtSearch.putClientProperty("JTextField.placeholderText", "Ví dụ: Hành động, Kinh dị...");
        txtSearch.setPreferredSize(new Dimension(0, 32));
        pnlNorth.add(txtSearch, BorderLayout.CENTER);
        add(pnlNorth, BorderLayout.NORTH);

        // 2. Custom JList with Toggle Logic
        list.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                } else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });

        list.setFixedCellHeight(35);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        list.setSelectionBackground(new Color(14, 165, 233));
        list.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 15, 5, 15),
                BorderFactory.createLineBorder(new Color(226, 232, 240))));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Button Panel
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlSouth.setBorder(new EmptyBorder(0, 15, 10, 15));

        JButton btnConfirm = new JButton("Xác nhận");
        btnConfirm.setBackground(new Color(34, 197, 94));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirm.addActionListener(e -> onConfirm());

        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        pnlSouth.add(btnCancel);
        pnlSouth.add(btnConfirm);
        add(pnlSouth, BorderLayout.SOUTH);

        // 4. Events & Data Load
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });

        loadData(initialSelection);
    }

    private void loadData(List<Genre> initialSelection) {
        listModel.removeAllElements();
        List<Integer> initialIndices = new ArrayList<>();

        for (int i = 0; i < allGenres.size(); i++) {
            Genre g = allGenres.get(i);
            GenreItem item = new GenreItem(g);
            listModel.addElement(item);

            if (initialSelection != null) {
                for (Genre selected : initialSelection) {
                    if (selected.getGenreId().equals(g.getGenreId())) {
                        initialIndices.add(i);
                        break;
                    }
                }
            }
        }

        // Set initial selection
        for (int index : initialIndices) {
            list.addSelectionInterval(index, index);
        }
    }

    private void filter() {
        String input = txtSearch.getText().toLowerCase().trim();
        // Simple filtering: Just highlight or rebuild model (rebuilding is cleaner)
        // For simplicity with JList and selection, we'll keep the list but maybe
        // in a production app we'd use a filtered model.
        // Showing only matches:
        List<GenreItem> selectedItems = list.getSelectedValuesList();
        listModel.removeAllElements();

        for (Genre g : allGenres) {
            if (g.getGenreName().toLowerCase().contains(input)) {
                GenreItem item = new GenreItem(g);
                listModel.addElement(item);
                // Preserve selection if it was already selected
                for (GenreItem sel : selectedItems) {
                    if (sel.genre.getGenreId().equals(g.getGenreId())) {
                        int index = listModel.size() - 1;
                        list.addSelectionInterval(index, index);
                    }
                }
            }
        }
    }

    private void onConfirm() {
        List<Genre> result = new ArrayList<>();
        for (GenreItem item : list.getSelectedValuesList()) {
            result.add(item.genre);
        }
        if (onConfirmCallback != null) {
            onConfirmCallback.accept(result);
        }
        dispose();
    }

    private static class GenreItem {
        Genre genre;

        GenreItem(Genre g) {
            this.genre = g;
        }

        @Override
        public String toString() {
            return genre.getGenreName();
        }
    }
}
