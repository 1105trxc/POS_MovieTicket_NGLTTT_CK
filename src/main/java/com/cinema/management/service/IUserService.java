package com.cinema.management.service;

import com.cinema.management.model.entity.User;
import java.util.List;

public interface IUserService {
    User login(String username, String password);

    void updateUser(User user);

    void createUser(User user);

    List<User> getAllUsers();

}
