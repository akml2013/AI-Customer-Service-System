package com.auth.service;

import com.auth.bean.User;

public interface AuthService {
    public String login(String username, String password);

    void register(User user);

    User validateToken(String token);

    User getUserByUsername(String username);
}
