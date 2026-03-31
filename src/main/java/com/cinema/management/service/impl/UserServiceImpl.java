package com.cinema.management.service.impl;

import com.cinema.management.model.entity.User;
import com.cinema.management.repository.UserRepository;
import com.cinema.management.service.IUserService;
import java.util.List;

public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private String hashPassword(String password) {
        if (password == null) return null;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(null);

        if (user != null) {
            if (!user.getIsActive()) {
                throw new RuntimeException("Tài khoản bạn không tồn tại.");
            }
            // Hỗ trợ cả mật khẩu cũ chưa mã hóa và mật khẩu mới đã mã hóa SHA-256
            String hashedInput = hashPassword(password);
            if (user.getPassword().equals(password) || user.getPassword().equalsIgnoreCase(hashedInput)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void createUser(User user) {
        if (userRepository.findById(user.getUserId()) != null) {
            throw new RuntimeException("Mã Nhân Viên (UserID) '" + user.getUserId() + "' đã tồn tại!");
        }
        User existUser = userRepository.findByUsername(user.getUsername()).orElseThrow(null);
        if (existUser != null && !existUser.getUsername().startsWith("[NV:")) {
            throw new RuntimeException("Tên đăng nhập này đã tồn tại trong phần mềm!");
        }
        
        if (user.getPassword() != null && user.getPassword().length() != 64) {
            user.setPassword(hashPassword(user.getPassword()));
        }
        
        userRepository.save(user);
    }

    @Override
    public void updateUser(User user) {
        if (user.getPassword() != null && user.getPassword().length() != 64) {
            user.setPassword(hashPassword(user.getPassword()));
        }
        userRepository.update(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
