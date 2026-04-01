package com.cinema.management.view.main;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.view.booking.BookingPanel;
import com.cinema.management.view.booking.CheckoutPanel;
import com.cinema.management.view.management.CustomerManagementPanel;
import com.cinema.management.view.management.MovieGenreManagementPanel;
import com.cinema.management.view.management.PromotionManagementPanel;
import com.cinema.management.view.management.RoomManagementPanel;
import com.cinema.management.view.management.SeatManagementPanel;
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

    // Bảng màu hiện đại (Modern Color Palette)
    private static final Color BG_APP = new Color(245, 247, 250);
    private static final Color BG_SIDEBAR = new Color(30, 41, 59); // Dark Slate
    private static final Color TEXT_SIDEBAR = new Color(241, 245, 249);
    private static final Color BG_HEADER = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(14, 165, 233); // Sky Blue

    private final JTabbedPane tabbedPane;
    private String loggedInUserId = "U003";

    private static final String CARD_BOOKING = "BOOKING";
    private static final String CARD_CHECKOUT = "CHECKOUT";

    private final JPanel posCardContainer = new JPanel();
    private final CardLayout posCardLayout = new CardLayout();
    private BookingPanel bookingPanel;
    private JPanel posTabPanel;

    // Lưu trữ đường dẫn Icon để đổi màu
    private final Map<Integer, String> tabIconPaths = new java.util.HashMap<>();

    public MainFrame(String userId, String userRole) {
        this.loggedInUserId = userId;

        setupModernUI();

        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        initFrame();
        buildTabs();
        setVisible(true);
    }

    // ── Hàm hỗ trợ Load SVG Icon & Đổi màu ────────────────────────────────────
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

        // Căn lề an toàn cho Custom Tab (Top 18, Left 20, Bottom 18, Right 25)
        UIManager.put("TabbedPane.tabInsets", new Insets(18, 20, 18, 25));

        // Đẩy toàn bộ Menu xuống dưới một khoảng 15px cho thoáng
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(15, 0, 0, 0));
    }

    private void initFrame() {
        setTitle("Hệ thống quản lý rạp chiếu phim");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(1280, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        String roleDisplay = com.cinema.management.util.UserSessionContext.isAdmin() ? "Quản trị" : "Nhân viên";
        JLabel roleBadge = new JLabel("Vai trò: " + roleDisplay);
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setForeground(ACCENT_COLOR);

        JLabel userLabel = new JLabel("Xin chào, " + (loggedInUserId != null ? loggedInUserId : ""));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(new Color(239, 68, 68)); // Red color
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                com.cinema.management.util.UserSessionContext.logout();
                this.dispose();
                new com.cinema.management.view.auth.LoginFrame().setVisible(true);
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

    // ── KỸ THUẬT CUSTOM TAB COMPONENT GIẢI QUYẾT LỖI LỆCH TRỤC ───────────────
    private void addTab(String title, String iconPath, Component component) {
        int index = tabbedPane.getTabCount();

        // 1. Thêm Tab trống (Không xài chữ và Icon mặc định của Swing)
        tabbedPane.addTab(null, component);

        // 2. Tự vẽ 1 Panel đè lên Tab đó (Sử dụng FlowLayout ép lề TRÁI)
        // FlowLayout.LEFT, hgap = 14 (Khoảng cách Icon và Chữ), vgap = 0
        JPanel customTabPanel = new JPanel(new BorderLayout());
        customTabPanel.setOpaque(false);
        customTabPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        customTabPanel.setPreferredSize(new Dimension(230, 30));

        // 3. Khởi tạo Icon (Màu trắng mặc định) và Text
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

        // 4. Gắn Panel vừa vẽ vào giao diện của Tab
        tabbedPane.setTabComponentAt(index, customTabPanel);

        // Lưu đường dẫn Icon để dùng cho sự kiện đổi màu
        tabIconPaths.put(index, iconPath);
    }

    private void buildTabs() {
        posTabPanel = buildPosTab();

        addTab("Bán vé (POS)", "icons/ticket.svg", posTabPanel);
        addTab("Khách hàng (CRM)", "icons/users.svg", new CustomerManagementPanel());

        if (com.cinema.management.util.UserSessionContext.isAdmin()) {
            addTab("Quản lý phòng", "icons/monitor.svg", new RoomManagementPanel());
            addTab("Quản lý ghế", "icons/grid.svg", new SeatManagementPanel());
            addTab("Quản lý suất chiếu", "icons/clock.svg", new ShowTimeManagementPanel());
            // Merged Movie & Genre Panel
            addTab("Phim & Thể loại", "icons/film.svg", new MovieGenreManagementPanel());
            addTab("Khuyến mãi SK", "icons/popcorn.svg", new PromotionManagementPanel());
            addTab("Quản lý nhân sự", "icons/users.svg", new StaffManagementPanel());
            addTab("Quản lý tài khoản", "icons/settings.svg", new UserManagementPanel());
        }

        // LẮNG NGHE SỰ KIỆN ĐỂ ĐỔI MÀU TEXT VÀ ICON BÊN TRONG CUSTOM TAB
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();

            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                // Lấy Custom Panel ra
                JPanel tabPanel = (JPanel) tabbedPane.getTabComponentAt(i);
                if (tabPanel == null)
                    continue;

                // Lấy component Icon (thứ 0) và Text (thứ 1)
                JPanel content = (JPanel) tabPanel.getComponent(0);
                JLabel lblIcon = (JLabel) content.getComponent(0);
                JLabel lblTitle = (JLabel) content.getComponent(1);
                String path = tabIconPaths.get(i);

                if (i == selectedIndex) {
                    // Đổi sang màu Xanh Dương (Accent)
                    lblIcon.setIcon(createIcon(path, ACCENT_COLOR));
                    lblTitle.setForeground(ACCENT_COLOR);
                } else {
                    // Trả về màu Trắng nhạt (Mặc định)
                    lblIcon.setIcon(createIcon(path, TEXT_SIDEBAR));
                    lblTitle.setForeground(TEXT_SIDEBAR);
                }
            }

            if (tabbedPane.getSelectedComponent() == posTabPanel && bookingPanel != null) {
                bookingPanel.refreshShowTimeList();
            }
        });

        // KÍCH HOẠT MÀU XANH CHO TAB ĐẦU TIÊN KHI MỞ APP
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
        String showTimeId = bookingPanel.getCurrentShowTimeId();
        String staffUserId = bookingPanel.getCurrentUserId();
        List<SeatStatusDto> seats = bookingPanel.getCurrentSelectedSeats();
        Map<String, Integer> fb = bookingPanel.getCurrentFbItems();

        BigDecimal seatTotal = seats.stream()
                .map(SeatStatusDto::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fbTotal = bookingPanel.getCurrentFbTotal();

        CheckoutPanel checkoutPanel = new CheckoutPanel(
                showTimeId, staffUserId, seats, fb, seatTotal, fbTotal);
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
