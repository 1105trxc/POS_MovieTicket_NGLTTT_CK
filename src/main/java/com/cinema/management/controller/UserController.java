package com.cinema.management.controller;

import com.cinema.management.model.entity.User;
import com.cinema.management.service.IUserService;
import com.cinema.management.util.UserSessionContext;
import com.cinema.management.service.impl.UserServiceImpl;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.repository.UserRepository;
import com.cinema.management.service.impl.AuditLogServiceImpl;
import java.util.List;

public class UserController {
    private final IUserService userService;

    public UserController() {
        this.userService = new UserServiceImpl(
                new UserRepository(),
                new AuditLogServiceImpl(new AuditLogRepository()));
    }

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    // Giao diện (Form) sẽ gọi hàm này:
    public boolean handleLogin(String username, String password) {
        try {
            User loggedInUser = userService.login(username, password);
            if (loggedInUser != null) {
                // Đăng nhập thành công, lưu phiên làm việc
                UserSessionContext.setCurrentUser(loggedInUser);
                return true;
            } else {
                return false;
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public void addUser(User user) {
        userService.createUser(user);
    }

    public void updateUser(User user) {
        userService.updateUser(user);
    }
}
