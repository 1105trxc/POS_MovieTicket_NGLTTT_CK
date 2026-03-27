package com.cinema.management;

import com.cinema.management.config.JpaUtil;
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
        // Đảm bảo Swing chạy trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Khởi tạo kết nối database
                JpaUtil.getEntityManagerFactory();
                if (DEV_MODE) {

                    // Giả lập trực tiếp 1 User có quyền ADMIN ném vào Session
                    User devUser = new User();
                    devUser.setFullName("Developer (Admin)");
                    Role devRole = new Role();
                    devRole.setRoleName("ADMIN");
                    devUser.setRole(devRole);
                    UserSessionContext.setCurrentUser(devUser);
                    // Mở thẳng MainFrame
                    // new MainFrame().setVisible(true);
                } else {
                    // Chế độ Production thực tế
                    // new LoginFrame().setVisible(true);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Không thể kết nối cơ sở dữ liệu!\n" + e.getMessage(),
                        "Lỗi khởi động", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });

        // Đảm bảo đóng EntityManagerFactory khi thoát ứng dụng
        Runtime.getRuntime().addShutdownHook(new Thread(JpaUtil::shutdown));
    }
}
