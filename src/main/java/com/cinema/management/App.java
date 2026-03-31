package com.cinema.management;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.view.main.MainFrame;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.cinema.management.model.entity.User;
import com.cinema.management.util.UserSessionContext;
import com.cinema.management.view.auth.LoginFrame;
import com.cinema.management.view.main.MainFrame;
import com.cinema.management.model.entity.Role;

import javax.swing.*;

/**
 * Điểm khởi chạy ứng dụng Cinema Management System.
 */
public class App {
    public static final boolean DEV_MODE = true;

    public static void main(String[] args) {

        // 1. Thiết lập Look and Feel (FlatLaf) ngay từ đầu để toàn bộ ứng dụng nhận giao diện mới
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
                // Khởi tạo kết nối database (JPA/Hibernate) [cite: 56, 60]
                JpaUtil.getEntityManagerFactory();
                if (DEV_MODE) {

                    // Giả lập trực tiếp 1 User có quyền ADMIN ném vào Session
                    User devUser = new User();
                    devUser.setFullName("Developer (Admin)");
                    Role devRole = new Role();
                    devRole.setRoleName("ADMIN");
                    devUser.setRole(devRole);
                    UserSessionContext.setCurrentUser(devUser);
//                     Mở thẳng MainFrame
//                     new MainFrame().setVisible(true);
                } else {
                    // Chế độ Production thực tế
                    // new LoginFrame().setVisible(true);
                }

                // Tạm thời mở thẳng MainFrame để test.
                // Truyền tham số UserID và Role để test phân quyền hiển thị (BR-02)[cite: 49, 87, 88].
                // Đổi thành "ADMIN" nếu muốn thấy các tab quản trị.
                new MainFrame("U003", "ADMIN");

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