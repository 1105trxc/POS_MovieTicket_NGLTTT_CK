package com.cinema.management.view.main;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.view.booking.BookingPanel;
import com.cinema.management.view.booking.CheckoutPanel;
import com.cinema.management.view.management.RoomManagementPanel;
import com.cinema.management.view.management.SeatManagementPanel;
import com.cinema.management.view.management.ShowTimeManagementPanel;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Cửa sổ chính của ứng dụng Cinema Management System.
 * Sử dụng JTabbedPane để phân chia các phân hệ theo SRS §4.1.
 *
 * Module 1 (Thành viên A): Phòng chiếu, Ghế, Suất chiếu.
 * Module 2 (Thành viên A): Bán vé POS.
 * Module 3 (Thành viên A): Thanh toán & Xuất vé.
 * Module B (Thành viên B): Phim/F&B, Khách hàng, Tài khoản – placeholder.
 *
 * Luồng Module 2 → 3: Tab "Bán vé" dùng CardLayout để chuyển giữa
 * BookingPanel (chọn ghế) và CheckoutPanel (thanh toán) mà không đổi tab.
 */
public class MainFrame extends JFrame {

    private static final int WIDTH  = 1280;
    private static final int HEIGHT = 820;

    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

    /**
     * userId nhân viên đang đăng nhập.
     * TODO: nhận từ LoginFrame khi Module Auth (Thành viên B) hoàn thành.
     */
    private String loggedInUserId = "STAFF_DEMO";

    // ── Tab Bán vé dùng CardLayout ────────────────────────────────────────────
    private static final String CARD_BOOKING  = "BOOKING";
    private static final String CARD_CHECKOUT = "CHECKOUT";

    private final JPanel      posCardContainer = new JPanel();
    private final CardLayout  posCardLayout    = new CardLayout();
    private BookingPanel      bookingPanel;

    public MainFrame() {
        initFrame();
        buildTabs();
        setVisible(true);
    }

    public MainFrame(String userId) {
        this.loggedInUserId = userId;
        initFrame();
        buildTabs();
        setVisible(true);
    }

    private void initFrame() {
        setTitle("Cinema Management System  |  " + loggedInUserId);
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabbedPane, BorderLayout.CENTER);

        JLabel statusBar = new JLabel(
                "  Cinema Management System  |  Nhân viên: " + loggedInUserId + "  |  Sẵn sàng");
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

        // ── Module 2 + 3: Thành viên A ─────────────────────────────────────
        tabbedPane.addTab("🎫 Bán vé (POS)", buildPosTab());

        // ── Module B placeholder (Thành viên B sẽ ráp vào) ─────────────────
        tabbedPane.addTab("🎥 Phim & F&B",
                buildPlaceholder("Module Phim & F&B\n(Thành viên B – đang phát triển)"));
        tabbedPane.addTab("👥 Khách hàng",
                buildPlaceholder("Module CRM & Khuyến mãi\n(Thành viên B – đang phát triển)"));
        tabbedPane.addTab("🔐 Tài khoản",
                buildPlaceholder("Module Phân quyền & Audit Log\n(Thành viên B – đang phát triển)"));
    }

    // ── POS tab: BookingPanel ⇄ CheckoutPanel ─────────────────────────────────

    private JPanel buildPosTab() {
        posCardContainer.setLayout(posCardLayout);

        bookingPanel = new BookingPanel(loggedInUserId);

        // Khi bấm "Thanh toán" trên BookingPanel → chuyển sang CheckoutPanel
        bookingPanel.setOnProceedToCheckout(this::switchToCheckout);

        posCardContainer.add(bookingPanel, CARD_BOOKING);
        // CheckoutPanel được tạo động khi cần (switchToCheckout)
        posCardLayout.show(posCardContainer, CARD_BOOKING);
        return posCardContainer;
    }

    /**
     * Tạo CheckoutPanel với snapshot dữ liệu hiện tại của BookingPanel,
     * rồi chuyển card sang CheckoutPanel.
     */
    private void switchToCheckout() {
        String showTimeId     = bookingPanel.getCurrentShowTimeId();
        String staffUserId    = bookingPanel.getCurrentUserId();
        List<SeatStatusDto> seats = bookingPanel.getCurrentSelectedSeats();
        Map<String, Integer> fb   = bookingPanel.getCurrentFbItems();

        // Tính seatTotal để truyền vào CheckoutPanel hiển thị ngay
        BigDecimal seatTotal = seats.stream()
                .map(SeatStatusDto::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // fbTotal: lấy xấp xỉ 0 vì CheckoutPanel sẽ tự tính khi cần
        BigDecimal fbTotal = BigDecimal.ZERO;

        CheckoutPanel checkoutPanel = new CheckoutPanel(
                showTimeId, staffUserId, seats, fb, seatTotal, fbTotal);

        // Nút "Quay lại" trên CheckoutPanel → trở về BookingPanel
        checkoutPanel.setOnBack(this::switchToBooking);

        // Gỡ CheckoutPanel cũ (nếu có) và thêm mới để tránh stale data
        posCardContainer.add(checkoutPanel, CARD_CHECKOUT);
        posCardLayout.show(posCardContainer, CARD_CHECKOUT);
        posCardContainer.revalidate();
        posCardContainer.repaint();
    }

    /** Quay về BookingPanel và refresh sơ đồ ghế. */
    private void switchToBooking() {
        // Gỡ CheckoutPanel cũ để giải phóng bộ nhớ
        for (Component comp : posCardContainer.getComponents()) {
            if (CARD_CHECKOUT.equals(((JPanel) posCardContainer).getName())
                    || comp instanceof CheckoutPanel) {
                posCardContainer.remove(comp);
                break;
            }
        }
        posCardLayout.show(posCardContainer, CARD_BOOKING);
        posCardContainer.revalidate();
        posCardContainer.repaint();
    }

    // ── Placeholder ───────────────────────────────────────────────────────────

    private JPanel buildPlaceholder(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        JLabel lbl = new JLabel(
                "<html><center>" + message.replace("\n", "<br>") + "</center></html>");
        lbl.setFont(new Font("Arial", Font.ITALIC, 16));
        lbl.setForeground(new Color(150, 150, 150));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl);
        return panel;
    }
}
