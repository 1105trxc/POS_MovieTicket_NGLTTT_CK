package com.cinema.management.service.impl;

import com.cinema.management.model.entity.User;
import com.cinema.management.repository.UserRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.IUserService;
import java.util.List;

public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final IAuditLogService auditLogService;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.auditLogService = null;
    }

    public UserServiceImpl(UserRepository userRepository, IAuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    private String hashPassword(String password) {
        if (password == null)
            return null;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
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
        if (userRepository.findById(user.getUserId()).isPresent()) {
            throw new RuntimeException("Mã Nhân Viên (UserID) '" + user.getUserId() + "' đã tồn tại!");
        }

        // Kiểm tra CCCD trùng
        if (user.getCccd() != null && !user.getCccd().trim().isEmpty()) {
            User existCccd = userRepository.findByCccd(user.getCccd().trim()).orElse(null);
            if (existCccd != null) {
                throw new RuntimeException("Số CCCD này đã tồn tại trên hệ thống! Vui lòng kiểm tra lại.");
            }
        }

        if (user.getUsername() == null) {
            user.setUsername("[NV:" + user.getUserId() + "]");
        }
        if (user.getPassword() == null) {
            user.setPassword("123456");
        }

        User existUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (existUser != null && !existUser.getUsername().startsWith("[NV:")) {
            throw new RuntimeException("Tên đăng nhập này đã tồn tại trong phần mềm!");
        }

        if (user.getPassword() != null && user.getPassword().length() != 64) {
            user.setPassword(hashPassword(user.getPassword()));
        }

        // Cập nhật RoleID thành ROLE_STAFF nếu chưa có
        if (user.getRole() == null) {
            com.cinema.management.model.entity.Role defaultRole = new com.cinema.management.model.entity.Role();
            defaultRole.setRoleId("ROLE_STAFF");
            user.setRole(defaultRole);
        }

        userRepository.save(user);

        if (auditLogService != null) {
            auditLogService.logAction("CREATE", "User", "UserID",
                    "N/A", user.getUserId() + " - " + user.getFullName());
        }
    }

    @Override
    public void updateUser(User user) {
        // Kiểm tra trùng Tên đăng nhập
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            User existUser = userRepository.findByUsername(user.getUsername().trim()).orElse(null);
            if (existUser != null && !existUser.getUserId().equals(user.getUserId())) {
                throw new RuntimeException("Tên đăng nhập này đã tồn tại trong phần mềm! Vui lòng chọn tên khác.");
            }
        }

        // Kiểm tra trùng CCCD
        if (user.getCccd() != null && !user.getCccd().trim().isEmpty()) {
            User existCccd = userRepository.findByCccd(user.getCccd().trim()).orElse(null);
            if (existCccd != null && !existCccd.getUserId().equals(user.getUserId())) {
                throw new RuntimeException("Số CCCD này đã tồn tại trên hệ thống của nhân viên khác!");
            }
        }

        // Lấy dữ liệu cũ để ghi log
        User old = userRepository.findById(user.getUserId()).orElse(null);

        if (user.getPassword() != null && user.getPassword().length() != 64) {
            user.setPassword(hashPassword(user.getPassword()));
        }
        userRepository.update(user);

        // Ghi log các thay đổi
        if (auditLogService != null && old != null) {
            if (old.getFullName() != null && !old.getFullName().equals(user.getFullName())) {
                auditLogService.logAction("UPDATE", "User", "FullName",
                        old.getFullName(), user.getFullName());
            }
            if (old.getPhone() != null && !old.getPhone().equals(user.getPhone())) {
                auditLogService.logAction("UPDATE", "User", "Phone",
                        old.getPhone(), user.getPhone());
            }
            if (old.getIsActive() != null && !old.getIsActive().equals(user.getIsActive())) {
                auditLogService.logAction("UPDATE", "User", "IsActive",
                        String.valueOf(old.getIsActive()), String.valueOf(user.getIsActive()));
            }
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
