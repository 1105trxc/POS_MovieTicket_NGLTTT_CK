package com.cinema.management.view.auth;

import com.cinema.management.controller.UserController;
import com.cinema.management.util.UserSessionContext;
import com.cinema.management.view.main.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class LoginFrame extends JFrame {

    private final UserController userController = new UserController();

    private static final Color BG = new Color(245, 247, 250);
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color TEXT_DARK = new Color(15, 23, 42);

    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JButton btnLogin = new JButton("DANG NHAP");

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

        JLabel lblLogo = new JLabel("CINEMA NEXUS", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogo.setForeground(TEXT_DARK);
        gc.gridy = 0;
        container.add(lblLogo, gc);

        JLabel lblSub = new JLabel("He thong quan ly rap chieu phim", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));
        gc.gridy = 1;
        container.add(lblSub, gc);

        gc.insets = new Insets(30, 0, 5, 0);

        JLabel lblUser = new JLabel("Ten dang nhap");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gc.gridy = 2;
        container.add(lblUser, gc);

        txtUsername.setPreferredSize(new Dimension(0, 40));
        txtUsername.putClientProperty("JTextField.placeholderText", "Nhap username...");
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 15, 0);
        container.add(txtUsername, gc);

        JLabel lblPass = new JLabel("Mat khau");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gc.gridy = 4;
        gc.insets = new Insets(10, 0, 5, 0);
        container.add(lblPass, gc);

        txtPassword.setPreferredSize(new Dimension(0, 40));
        txtPassword.putClientProperty("JTextField.placeholderText", "********");
        gc.gridy = 5;
        gc.insets = new Insets(0, 0, 30, 0);
        container.add(txtPassword, gc);

        btnLogin.setBackground(PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(0, 45));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gc.gridy = 6;
        container.add(btnLogin, gc);

        btnLogin.addActionListener(e -> performLogin());

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        txtPassword.getInputMap().put(enter, "enter");
        txtPassword.getActionMap().put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                performLogin();
            }
        });

        add(container, BorderLayout.CENTER);
    }

    private void performLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap day du thong tin!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Dang xac thuc...");

        new Thread(() -> {
            boolean success = userController.handleLogin(user, pass);
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    com.cinema.management.model.entity.User loggedIn = UserSessionContext.getCurrentUser();
                    if (!UserSessionContext.isAdmin()) {
                        UserSessionContext.setOpeningCash(promptOpeningCash());
                    } else {
                        UserSessionContext.setOpeningCash(BigDecimal.ZERO);
                    }
                    new MainFrame(loggedIn.getUserId(), loggedIn.getRole() != null ? loggedIn.getRole().getRoleName() : "");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Sai tai khoan hoac mat khau!", "Loi dang nhap", JOptionPane.ERROR_MESSAGE);
                    btnLogin.setEnabled(true);
                    btnLogin.setText("DANG NHAP");
                }
            });
        }).start();
    }

    private BigDecimal promptOpeningCash() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    this,
                    "Nhap tien dau ca (VND):",
                    "Khoi tao ca lam",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (input == null) {
                return BigDecimal.ZERO;
            }
            String normalized = input.replaceAll("[^0-9]", "");
            if (normalized.isBlank()) {
                return BigDecimal.ZERO;
            }
            try {
                return new BigDecimal(normalized);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "So tien khong hop le. Vui long nhap lai.", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
