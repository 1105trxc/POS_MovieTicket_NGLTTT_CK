package com.cinema.management.view.main;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.view.booking.BookingPanel;
import com.cinema.management.view.booking.CheckoutPanel;
import com.cinema.management.view.management.RoomManagementPanel;
import com.cinema.management.view.management.SeatManagementPanel;
import com.cinema.management.view.management.ShowTimeManagementPanel;

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
    private String userRole = "STAFF"; // Mặc định là Staff để đảm bảo an toàn

    private static final String CARD_BOOKING = "BOOKING";
    private static final String CARD_CHECKOUT = "CHECKOUT";

    private final JPanel posCardContainer = new JPanel();
    private final CardLayout posCardLayout = new CardLayout();
    private BookingPanel bookingPanel;
    private JPanel posTabPanel;

    // Constructor cập nhật thêm userRole để phân quyền theo yêu cầu SRS
    public MainFrame(String userId, String userRole) {
        this.loggedInUserId = userId;
        this.userRole = userRole;

        // Tinh chỉnh UIManager cho JTabbedPane nhìn phẳng và hiện đại hơn
        setupModernUI();

        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        initFrame();
        buildTabs();
        setVisible(true);
    }

    private void setupModernUI() {
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabsOverlapBorder", true);
        UIManager.put("TabbedPane.selected", BG_APP);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
    }

    private void initFrame() {
        setTitle("Cinema POS & Management System - Enterprise Edition");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(1280, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_APP);

        // Header
        add(buildHeader(), BorderLayout.NORTH);

        // Main Content Area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        // Cấu hình Sidebar TabbedPane
        styleSidebarTabs();
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Status Bar
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
        brand.setIcon(UIManager.getIcon("FileView.computerIcon")); // Thay bằng Icon Logo thực tế

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        rightPanel.setOpaque(false);

        JLabel roleBadge = new JLabel("Role: " + userRole);
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setForeground(ACCENT_COLOR);

        JLabel userLabel = new JLabel("Hi, " + loggedInUserId);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        rightPanel.add(roleBadge);
        rightPanel.add(userLabel);

        header.add(brand, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(BG_HEADER);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        statusBar.setPreferredSize(new Dimension(WIDTH, 30));

        JLabel statusText = new JLabel("  System Online | Server Sync: OK");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusText.setForeground(new Color(100, 116, 139));

        statusBar.add(statusText, BorderLayout.WEST);
        return statusBar;
    }

    private void styleSidebarTabs() {
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(BG_SIDEBAR);
        tabbedPane.setForeground(TEXT_SIDEBAR);
        // Padding cho text trong tab để tạo cảm giác rộng rãi như button menu
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(10, 0, 0, 0));
    }

    private void buildTabs() {
        // Tab dùng chung cho cả Staff và Admin (POS, CRM) [cite: 37]
        posTabPanel = buildPosTab();
        tabbedPane.addTab("   Ticket Sales (POS)   ", posTabPanel);
        tabbedPane.addTab("   Customers (CRM)   ", buildPlaceholder("CRM Module is currently active."));

        // Tab dành riêng cho Admin dựa trên Business Rule BR-02 [cite: 49, 87, 88]
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            tabbedPane.addTab("   Room Management   ", new RoomManagementPanel());
            tabbedPane.addTab("   Seat Management   ", new SeatManagementPanel());
            tabbedPane.addTab("   Showtime Mgmt     ", new ShowTimeManagementPanel());
            tabbedPane.addTab("   Movies & F&B      ", buildPlaceholder("Catalog Management..."));
            tabbedPane.addTab("   Users & Audit Log ", buildPlaceholder("System Logs & Staff Management..."));
        }

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == posTabPanel && bookingPanel != null) {
                bookingPanel.refreshShowTimeList();
            }
        });
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
        BigDecimal fbTotal = BigDecimal.ZERO;

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

    private JPanel buildPlaceholder(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_APP);
        JLabel lbl = new JLabel("<html><center><h3>" + message + "</h3><p>Under construction or restricted access.</p></center></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(100, 116, 139));
        panel.add(lbl);
        return panel;
    }
}
