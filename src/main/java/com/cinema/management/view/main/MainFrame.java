package com.cinema.management.view.main;

import com.cinema.management.model.dto.InvoiceDto;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.util.UserSessionContext;
import com.cinema.management.view.auth.LoginFrame;
import com.cinema.management.view.booking.BookingPanel;
import com.cinema.management.view.booking.CheckoutPanel;
import com.cinema.management.view.management.MovieManagementPanel;
import com.cinema.management.view.management.PaymentManagementPanel;
import com.cinema.management.view.management.ProductManagementPanel;
import com.cinema.management.view.management.PromotionManagementPanel;
import com.cinema.management.view.management.RoomManagementPanel;
import com.cinema.management.view.management.SeatManagementPanel;
import com.cinema.management.view.management.ShiftReportPanel;
import com.cinema.management.view.management.ShiftReportManagementPanel;
import com.cinema.management.view.management.ShowTimeManagementPanel;
import com.cinema.management.view.management.StaffManagementPanel;
import com.cinema.management.view.management.UserManagementPanel;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private static final int WIDTH = 1440;
    private static final int HEIGHT = 900;

    private static final Color BG_APP = new Color(245, 247, 250);
    private static final Color BG_SIDEBAR = new Color(30, 41, 59);
    private static final Color TEXT_SIDEBAR = new Color(241, 245, 249);
    private static final Color BG_HEADER = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(14, 165, 233);

    private static final String CARD_BOOKING = "BOOKING";
    private static final String CARD_CHECKOUT = "CHECKOUT";

    private final JTabbedPane tabbedPane;
    private final JPanel posCardContainer = new JPanel();
    private final CardLayout posCardLayout = new CardLayout();
    private final Map<Integer, String> tabIconPaths = new java.util.HashMap<>();

    private String loggedInUserId = "U003";
    private BookingPanel bookingPanel;
    private JPanel posTabPanel;

    public MainFrame(String userId, String userRole) {
        this.loggedInUserId = userId;
        setupModernUI();
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        initFrame();
        buildTabs();
        setVisible(true);
    }

    private Icon createIcon(String path, Color color) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon(path, 22, 22);
            if (color != null) {
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
            }
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupModernUI() {
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabsOverlapBorder", true);
        UIManager.put("TabbedPane.selected", BG_APP);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(18, 20, 18, 25));
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(15, 0, 0, 0));
    }

    private void initFrame() {
        setTitle("Hệ thống quản lý rạp chiếu phim");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(1280, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (!UserSessionContext.isAdmin()) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Nhân viên không thể tắt chương trình khi chưa chốt ca.\nVui lòng vào mục 'Chốt ca & Báo cáo' để tiến hành xuất báo cáo Z-Report trước khi nghỉ!",
                            "Bắt Buộc Chốt Ca", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Bạn có chắc chắn muốn thoát ứng dụng?",
                        "Xác nhận thoát", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_APP);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        styleSidebarTabs();
        centerPanel.add(tabbedPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        header.setPreferredSize(new Dimension(WIDTH, 60));

        JLabel brand = new JLabel("  CINEMA NEXUS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(new Color(15, 23, 42));
        brand.setIcon(createIcon("icons/film.svg", new Color(15, 23, 42)));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        rightPanel.setOpaque(false);

        String roleDisplay = UserSessionContext.isAdmin() ? "Quản trị" : "Nhân viên";
        JLabel roleBadge = new JLabel("Vai trò: " + roleDisplay);
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setForeground(ACCENT_COLOR);

        JLabel userLabel = new JLabel("Xin chào, " + (loggedInUserId != null ? loggedInUserId : ""));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            if (!UserSessionContext.isAdmin()) {
                JOptionPane.showMessageDialog(this,
                        "Bạn phải hoàn tất chốt ca trực tiếp tại quầy báo cáo trước khi đăng xuất!",
                        "Hướng Dẫn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                UserSessionContext.logout();
                this.dispose();
                new LoginFrame().setVisible(true);
            }
        });

        rightPanel.add(roleBadge);
        rightPanel.add(userLabel);
        rightPanel.add(btnLogout);

        header.add(brand, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(BG_HEADER);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        statusBar.setPreferredSize(new Dimension(WIDTH, 30));

        JLabel statusText = new JLabel("  Hệ thống đang hoạt động | Đồng bộ máy chủ: OK");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusText.setForeground(new Color(100, 116, 139));
        statusBar.add(statusText, BorderLayout.WEST);
        return statusBar;
    }

    private void styleSidebarTabs() {
        tabbedPane.setBackground(BG_SIDEBAR);
    }

    private void addTab(String title, String iconPath, Component component) {
        int index = tabbedPane.getTabCount();
        tabbedPane.addTab(null, component);

        JPanel customTabPanel = new JPanel(new BorderLayout());
        customTabPanel.setOpaque(false);
        customTabPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        customTabPanel.setPreferredSize(new Dimension(230, 30));

        JLabel lblIcon = new JLabel(createIcon(iconPath, TEXT_SIDEBAR));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_SIDEBAR);
        lblTitle.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        content.setOpaque(false);
        content.add(lblIcon);
        content.add(lblTitle);
        customTabPanel.add(content, BorderLayout.WEST);

        tabbedPane.setTabComponentAt(index, customTabPanel);
        tabIconPaths.put(index, iconPath);
    }

    private void buildTabs() {
        posTabPanel = buildPosTab();
        addTab("Bán vé (POS)", "icons/ticket.svg", posTabPanel);

        if (UserSessionContext.isAdmin()) {
            addTab("Quản lý Doanh thu", "icons/wallet.svg", new PaymentManagementPanel());
            addTab("Quản lý Phòng", "icons/monitor.svg", new RoomManagementPanel());
            addTab("Quản lý Ghế", "icons/grid.svg", new SeatManagementPanel());
            addTab("Quản lý Suất chiếu", "icons/clock.svg", new ShowTimeManagementPanel());
            addTab("Quản lý Phim", "icons/movie.svg", new MovieManagementPanel());
            addTab("Quản lý F&B", "icons/popcorn.svg", new ProductManagementPanel());
            addTab("Quản lý Báo cáo ca", "icons/report.svg", new ShiftReportManagementPanel());
            addTab("Khuyến mãi", "icons/promo.svg", new PromotionManagementPanel());
            addTab("Quản lý Nhân sự", "icons/users.svg", new StaffManagementPanel());
            addTab("Quản lý Tài khoản", "icons/settings.svg", new UserManagementPanel());
        } else {
            addTab("Chốt ca & Báo cáo", "icons/settings.svg", new ShiftReportPanel(loggedInUserId));
        }

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                JPanel tabPanel = (JPanel) tabbedPane.getTabComponentAt(i);
                if (tabPanel == null) {
                    continue;
                }
                JPanel content = (JPanel) tabPanel.getComponent(0);
                JLabel lblIcon = (JLabel) content.getComponent(0);
                JLabel lblTitle = (JLabel) content.getComponent(1);
                String path = tabIconPaths.get(i);

                if (i == selectedIndex) {
                    lblIcon.setIcon(createIcon(path, ACCENT_COLOR));
                    lblTitle.setForeground(ACCENT_COLOR);
                } else {
                    lblIcon.setIcon(createIcon(path, TEXT_SIDEBAR));
                    lblTitle.setForeground(TEXT_SIDEBAR);
                }
            }

            if (tabbedPane.getSelectedComponent() == posTabPanel && bookingPanel != null) {
                bookingPanel.refreshShowTimeList();
            }
        });

        if (tabbedPane.getTabCount() > 0) {
            JPanel firstTab = (JPanel) tabbedPane.getTabComponentAt(0);
            if (firstTab != null) {
                JPanel content = (JPanel) firstTab.getComponent(0);
                ((JLabel) content.getComponent(0)).setIcon(createIcon(tabIconPaths.get(0), ACCENT_COLOR));
                ((JLabel) content.getComponent(1)).setForeground(ACCENT_COLOR);
            }
        }
    }

    private JPanel buildPosTab() {
        posCardContainer.setLayout(posCardLayout);
        posCardContainer.setOpaque(false);
        posCardContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        bookingPanel = new BookingPanel(loggedInUserId);
        bookingPanel.setOnProceedToCheckout(this::switchToCheckout);

        posCardContainer.add(bookingPanel, CARD_BOOKING);
        posCardLayout.show(posCardContainer, CARD_BOOKING);
        return posCardContainer;
    }

    private void switchToCheckout() {
        InvoiceDto pendingInvoice = bookingPanel.consumePendingInvoiceToResume();
        if (pendingInvoice != null) {
            CheckoutPanel checkoutPanel = new CheckoutPanel(
                    bookingPanel.getCurrentShowTimeId(),
                    bookingPanel.getCurrentUserId(),
                    java.util.Collections.emptyList(),
                    java.util.Collections.emptyMap(),
                    pendingInvoice.getSeatTotal(),
                    pendingInvoice.getFbTotal(),
                    pendingInvoice);
            checkoutPanel.setOnBack(this::switchToBooking);

            posCardContainer.add(checkoutPanel, CARD_CHECKOUT);
            posCardLayout.show(posCardContainer, CARD_CHECKOUT);
            checkoutPanel.openPendingQrPayment();
            return;
        }

        String showTimeId = bookingPanel.getCurrentShowTimeId();
        String staffUserId = bookingPanel.getCurrentUserId();
        List<SeatStatusDto> seats = bookingPanel.getCurrentSelectedSeats();
        Map<String, Integer> fb = bookingPanel.getCurrentFbItems();

        BigDecimal seatTotal = seats.stream()
                .map(SeatStatusDto::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fbTotal = bookingPanel.getCurrentFbTotal();

        CheckoutPanel checkoutPanel = new CheckoutPanel(showTimeId, staffUserId, seats, fb, seatTotal, fbTotal);
        checkoutPanel.setOnBack(this::switchToBooking);

        posCardContainer.add(checkoutPanel, CARD_CHECKOUT);
        posCardLayout.show(posCardContainer, CARD_CHECKOUT);
    }

    private void switchToBooking() {
        for (Component comp : posCardContainer.getComponents()) {
            if (comp instanceof CheckoutPanel) {
                posCardContainer.remove(comp);
                break;
            }
        }
        posCardLayout.show(posCardContainer, CARD_BOOKING);
    }
}
