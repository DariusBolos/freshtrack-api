package com.freshtrack.api.user.service;

import com.freshtrack.api.user.User;
import java.util.List;

public interface IUserService {
    User createUser(User user);
    User getUserById(Long id);

    User getUserByEmail(String token);

    List<User> getAllUsers();
    User updateUser(Long id, User user);
    void deleteUser(Long id);
}
