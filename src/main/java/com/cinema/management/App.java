package com.cinema.management;

import com.cinema.management.config.JpaUtil;

import javax.swing.*;

/**
 * Điểm khởi chạy ứng dụng Cinema Management System.
 */
public class App {

    public static void main(String[] args) {
        // Đảm bảo Swing chạy trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Khởi tạo kết nối database
                JpaUtil.getEntityManagerFactory();

                // TODO: Mở màn hình đăng nhập
                // new LoginFrame().setVisible(true);

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
