package com.auth.service.impl;

import com.auth.bean.User;
import com.auth.dao.UserDao;
import com.auth.service.AuthService;
import com.auth.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public String login(String username, String password) {
        User user = userDao.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 更新最后登录时间
        userDao.updateLastLogin(user.getId());

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user);

        // 将令牌存入Redis，设置过期时间
        String redisKey = "user:token:" + token;
        redisTemplate.opsForValue().set(redisKey, user, jwtUtils.getExpiration(), TimeUnit.MILLISECONDS);

        User user0 = validateToken(token);



        return token;
    }

    @Override
    public void register(User user) {
        if (userDao.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        if (userDao.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认角色（普通用户）
        if (user.getRole() == null) {
            user.setRole(1);
        }

        // 设置默认状态（启用）
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        userDao.save(user);
    }

    @Override
    public User validateToken(String token) {
        // 验证token有效性
        if (!jwtUtils.validateToken(token)) {
            return null;
        }

        // 从Redis获取用户信息
        String redisKey = "user:token:" + token;
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }
}
