package com.cinema.management;

import com.cinema.management.config.JpaUtil;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.cinema.management.view.auth.LoginFrame;
import javax.swing.*;
import com.cinema.management.model.entity.User;

/**
 * Điểm khởi chạy ứng dụng Cinema Management System.
 */
public class App {
    public static final boolean DEV_MODE = true;

    public static void main(String[] args) {

        // 1. Thiết lập Look and Feel (FlatLaf) ngay từ đầu để toàn bộ ứng dụng nhận
        // giao diện mới
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
            // Tùy chỉnh bo góc chuẩn hiện đại
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("TabbedPane.showTabSeparators", true);
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo giao diện FlatLaf. Đang dùng giao diện mặc định.");
        }

        // 2. Chạy ứng dụng trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Khởi tạo kết nối database (JPA/Hibernate)
                JpaUtil.getEntityManagerFactory();

                // 2. Kiểm tra chế độ Dev Mode hoặc Production
                if (DEV_MODE) {
                    // Ưu tiên tìm User có quyền ADMIN để test toàn bộ tính năng
                    jakarta.persistence.EntityManager em = JpaUtil.getEntityManager();
                    try {
                        // Thử tìm admin trước
                        User devUser = em.createQuery(
                            "SELECT u FROM User u JOIN FETCH u.role WHERE u.role.roleName LIKE :role", User.class)
                            .setParameter("role", "%ADMIN%")
                            .setMaxResults(1)
                            .getResultList()
                            .stream().findFirst().orElse(null);

                        // Nếu không có admin, lấy đại 1 user bất kỳ
                        if (devUser == null) {
                            devUser = em.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.role", User.class)
                                .setMaxResults(1)
                                .getResultList()
                                .stream().findFirst().orElse(null);
                        }

                        if (devUser != null) {
                            com.cinema.management.util.UserSessionContext.setCurrentUser(devUser);
                            // Force "ADMIN" role in MainFrame so UI shows all tabs even if user is staff in DB
                            new com.cinema.management.view.main.MainFrame(devUser.getUserId(), "ADMIN").setVisible(true);
                        } else {
                            // Fallback nếu DB trống trơn
                            User mockDev = new User();
                            mockDev.setUserId("DEV_ADMIN");
                            mockDev.setFullName("Developer Admin");
                            com.cinema.management.util.UserSessionContext.setCurrentUser(mockDev);
                            new com.cinema.management.view.main.MainFrame("DEV_ADMIN", "ADMIN").setVisible(true);
                        }
                    } finally {
                        em.close();
                    }
                } else {
                    // Chế độ Production: Yêu cầu đăng nhập thực tế
                    new LoginFrame().setVisible(true);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Không thể kết nối cơ sở dữ liệu!\nVui lòng kiểm tra lại MySQL.\nChi tiết: " + e.getMessage(),
                        "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });

        // 3. Đảm bảo đóng tài nguyên an toàn khi tắt ứng dụng
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Đang đóng kết nối database...");
            JpaUtil.shutdown();
        }));
    }
}