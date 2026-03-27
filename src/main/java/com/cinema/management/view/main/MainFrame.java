package com.cinema.management.view.main;

import com.cinema.management.view.management.RoomManagementPanel;
import com.cinema.management.view.management.SeatManagementPanel;
import com.cinema.management.view.management.ShowTimeManagementPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Cửa sổ chính của ứng dụng Cinema Management System.
 * Sử dụng JTabbedPane để phân chia các phân hệ theo SRS §4.1.
 * Module 1 (Thành viên A): tab Phòng chiếu, Ghế, Suất chiếu.
 * Module B (Thành viên B): các tab còn lại sẽ được ráp vào theo Git Workflow.
 */
public class MainFrame extends JFrame {

    private static final int WIDTH  = 1280;
    private static final int HEIGHT = 820;

    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

    public MainFrame() {
        initFrame();
        buildTabs();
        setVisible(true);
    }

    private void initFrame() {
        setTitle("Cinema Management System");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Global font
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JLabel statusBar = new JLabel("  Cinema Management System  |  Sẵn sàng");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        statusBar.setFont(new Font("Arial", Font.PLAIN, 11));
        add(statusBar, BorderLayout.SOUTH);
    }

    private void buildTabs() {
        // ── Module 1: Thành viên A ──────────────────────────────────────────
        tabbedPane.addTab("🏠 Phòng chiếu",  new RoomManagementPanel());
        tabbedPane.addTab("🪑 Ghế ngồi",     new SeatManagementPanel());
        tabbedPane.addTab("🎬 Suất chiếu",   new ShowTimeManagementPanel());

        // ── Module B placeholder (Thành viên B sẽ ráp vào) ─────────────────
        tabbedPane.addTab("🎥 Phim & F&B",   buildPlaceholder("Module Phim & F&B\n(Thành viên B – đang phát triển)"));
        tabbedPane.addTab("👥 Khách hàng",   buildPlaceholder("Module CRM & Khuyến mãi\n(Thành viên B – đang phát triển)"));
        tabbedPane.addTab("🔐 Tài khoản",    buildPlaceholder("Module Phân quyền & Audit Log\n(Thành viên B – đang phát triển)"));

        // ── Module 2 & 3: Thành viên A (sẽ làm tiếp) ──────────────────────
        tabbedPane.addTab("🎫 Bán vé (POS)", buildPlaceholder("Module Bán vé POS\n(Thành viên A – Module 2)"));
        tabbedPane.addTab("💳 Thanh toán",   buildPlaceholder("Module Thanh toán\n(Thành viên A – Module 3)"));
    }

    private JPanel buildPlaceholder(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        JLabel lbl = new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>");
        lbl.setFont(new Font("Arial", Font.ITALIC, 16));
        lbl.setForeground(new Color(150, 150, 150));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl);
        return panel;
    }
}
