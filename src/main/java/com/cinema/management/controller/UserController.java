package com.cinema.management.controller;

import com.cinema.management.model.entity.User;
import com.cinema.management.service.IUserService;
import com.cinema.management.util.UserSessionContext;
import java.util.List;

public class UserController {
    private final IUserService userService;

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
