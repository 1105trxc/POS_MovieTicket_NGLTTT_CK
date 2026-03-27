package com.cinema.management;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.view.main.MainFrame;

import javax.swing.*;

/**
 * Điểm khởi chạy ứng dụng Cinema Management System.
 */
public class App {

    public static void main(String[] args) {
        // Đảm bảo Swing chạy trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Look and Feel hệ thống (Windows/macOS native)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            try {
                // Khởi tạo kết nối database
                JpaUtil.getEntityManagerFactory();

                // TODO: Module Thành viên B – mở LoginFrame trước, sau đó mới mở MainFrame
                // new LoginFrame().setVisible(true);

                // Tạm thời mở thẳng MainFrame để demo Module 1
                new MainFrame();

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
