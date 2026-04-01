package com.cinema.management.view.management;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerCRMPanel extends JPanel {

    private static final Color BG = new Color(245, 247, 250);
    private static final Color HEADER_BG = new Color(30, 41, 59);

    private final CustomerManagementPanel customerPanel;
    private final PointHistoryPanel historyPanel;

    public CustomerCRMPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        // Header chung
        add(buildHeader(), BorderLayout.NORTH);

        // TabbedPane cho 2 phần: Khách hàng và Lịch sử
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        customerPanel = new CustomerManagementPanel();
        historyPanel = new PointHistoryPanel();

        tabbedPane.addTab("   Quản lý Khách hàng   ", customerPanel);
        tabbedPane.addTab("   Lịch sử tích điểm   ", historyPanel);

        tabbedPane.setOpaque(false);
        tabbedPane.setBackground(BG);
        UIManager.put("TabbedPane.contentAreaColor", BG);

        // Listener để tải lại dữ liệu khi chuyển tab
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                historyPanel.loadData();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ KHÁCH HÀNG & CRM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Quản lý thông tin hội viên, điểm thưởng và tra cứu lịch sử giao dịch");
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
