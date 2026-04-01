package com.cinema.management.view.main;

import com.cinema.management.model.dto.InvoiceDto;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.util.UserSessionContext;
import com.cinema.management.view.auth.LoginFrame;
import com.cinema.management.view.booking.BookingPanel;
import com.cinema.management.view.booking.CheckoutPanel;
import com.cinema.management.view.management.MovieManagementPanel;
import com.cinema.management.view.management.ProductManagementPanel;
import com.cinema.management.view.management.PromotionManagementPanel;
import com.cinema.management.view.management.RoomManagementPanel;
import com.cinema.management.view.management.SeatManagementPanel;
import com.cinema.management.view.management.ShiftReportPanel;
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
        setTitle("He thong quan ly rap chieu phim");
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

        String roleDisplay = UserSessionContext.isAdmin() ? "Quan tri" : "Nhan vien";
        JLabel roleBadge = new JLabel("Vai tro: " + roleDisplay);
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setForeground(ACCENT_COLOR);

        JLabel userLabel = new JLabel("Xin chao, " + (loggedInUserId != null ? loggedInUserId : ""));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnLogout = new JButton("Dang xuat");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Ban co chac chan muon dang xuat?",
                    "Xac nhan", JOptionPane.YES_NO_OPTION);
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

        JLabel statusText = new JLabel("  He thong dang hoat dong | Dong bo may chu: OK");
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
        addTab("Ban ve (POS)", "icons/ticket.svg", posTabPanel);

        if (UserSessionContext.isAdmin()) {
            addTab("Quan ly phong", "icons/monitor.svg", new RoomManagementPanel());
            addTab("Quan ly ghe", "icons/grid.svg", new SeatManagementPanel());
            addTab("Quan ly suat chieu", "icons/clock.svg", new ShowTimeManagementPanel());
            addTab("Quan ly phim", "icons/film.svg", new MovieManagementPanel());
            addTab("Quan ly F&B", "icons/popcorn.svg", new ProductManagementPanel());
            addTab("Khuyen mai", "icons/gift.svg", new PromotionManagementPanel());
            addTab("Quan ly nhan su", "icons/users.svg", new StaffManagementPanel());
            addTab("Quan ly tai khoan", "icons/settings.svg", new UserManagementPanel());
        } else {
            addTab("Chot ca & Bao cao", "icons/settings.svg", new ShiftReportPanel(loggedInUserId));
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
