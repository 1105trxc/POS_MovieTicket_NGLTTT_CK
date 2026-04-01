package com.cinema.management.view.management;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MovieGenreManagementPanel extends JPanel {

    private static final Color BG = new Color(245, 247, 250);
    private static final Color HEADER_BG = new Color(30, 41, 59);

    public MovieGenreManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        // Header chung
        add(buildHeader(), BorderLayout.NORTH);

        // TabbedPane cho 2 phần: Phim và Thể loại
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // 2. Thêm khoảng trắng (space) vào tên Tab để nhìn cân đối giống bên Seat
        tabbedPane.addTab("   Quản lý Phim   ", new MovieManagementPanel());
        tabbedPane.addTab("   Quản lý Thể loại   ", new GenreManagementPanel());

        // 3. Đã bỏ tabbedPane.setBorder(...) vì panel cha đã có khoảng cách bao quanh

        // FlatLaf overrides JTabbedPane content background, so we must force the panels
        // to paint BG
        tabbedPane.setOpaque(false);
        tabbedPane.setBackground(BG);
        UIManager.put("TabbedPane.contentAreaColor", BG);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ PHIM & THỂ LOẠI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Quản lý danh sách phim, thông tin chi tiết và danh mục thể loại");
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
}
