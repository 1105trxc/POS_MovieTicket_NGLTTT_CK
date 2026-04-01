package com.cinema.management.view.auth;

import com.cinema.management.controller.UserController;
import com.cinema.management.view.main.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {

    private final UserController userController = new UserController();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color TEXT_DARK = new Color(15, 23, 42);

    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JButton btnLogin = new JButton("ĐĂNG NHẬP");

    public LoginFrame() {
        initFrame();
        buildUI();
        setVisible(true);
    }

    private void initFrame() {
        setTitle("Cinema Nexus - Login");
        setSize(400, 550);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
    }

    private void buildUI() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 0, 10, 0);
        gc.gridx = 0;
        gc.weightx = 1;

        // 1. Logo/Icon
        JLabel lblLogo = new JLabel("CINEMA NEXUS", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogo.setForeground(TEXT_DARK);
        gc.gridy = 0;
        container.add(lblLogo, gc);

        JLabel lblSub = new JLabel("Hệ thống quản lý rạp chiếu phim chuyên nghiệp", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));
        gc.gridy = 1;
        container.add(lblSub, gc);

        gc.insets = new Insets(30, 0, 5, 0);
        
        // 2. Username
        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gc.gridy = 2;
        container.add(lblUser, gc);

        txtUsername.setPreferredSize(new Dimension(0, 40));
        txtUsername.putClientProperty("JTextField.placeholderText", "Nhập username...");
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 15, 0);
        container.add(txtUsername, gc);

        // 3. Password
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gc.gridy = 4;
        gc.insets = new Insets(10, 0, 5, 0);
        container.add(lblPass, gc);

        txtPassword.setPreferredSize(new Dimension(0, 40));
        txtPassword.putClientProperty("JTextField.placeholderText", "••••••••");
        gc.gridy = 5;
        gc.insets = new Insets(0, 0, 30, 0);
        container.add(txtPassword, gc);

        // 4. Login Button
        btnLogin.setBackground(PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(0, 45));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gc.gridy = 6;
        container.add(btnLogin, gc);

        // Events
        btnLogin.addActionListener(e -> performLogin());
        
        // Enter key to login
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        txtPassword.getInputMap().put(enter, "enter");
        txtPassword.getActionMap().put("enter", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { performLogin(); }
        });

        add(container, BorderLayout.CENTER);
    }

    private void performLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xác thực...");

        // Chạy trên thread riêng để tránh treo UI
        new Thread(() -> {
            boolean success = userController.handleLogin(user, pass);
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    // Lấy ra thông tin User vừa login từ context
                    com.cinema.management.model.entity.User loggedIn = com.cinema.management.util.UserSessionContext.getCurrentUser();
                    new MainFrame(loggedIn.getUserId(), loggedIn.getRole().getRoleName());
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                    btnLogin.setEnabled(true);
                    btnLogin.setText("ĐĂNG NHẬP");
                }
            });
        }).start();
    }
}
