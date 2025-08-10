package com.auth.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.auth.feign.ModelFeignClient;
import com.auth.service.AuthService;
import com.auth.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthService authService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    ModelFeignClient modelFeignClient;

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        String token = authService.login(username, password);
        User user = new User();

        try {
            user = authService.getUserByUsername(username);
            session.setAttribute("currentUser", user);
            session.setAttribute("AUTH_TOKEN", token);
        }
        catch (RuntimeException e) {
            return "redirect:http://localhost/view/login?error=1";
        }
        // 将Token添加到Cookie中
        Cookie tokenCookie = new Cookie("AUTH_TOKEN", token);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(3600); // 1小时
        response.addCookie(tokenCookie);

        redirectAttributes.addAttribute("userId", user.getId());
        redirectAttributes.addAttribute("userName", user.getUsername());
        redirectAttributes.addAttribute("token", token);

        return "redirect:http://localhost/view/dashboard";
    }

//    @PostMapping("/login")
//    public String login() {
//        return "登录成功";
//    }
//
//    @GetMapping("/register")
//    public String showRegisterForm() {
//        return "user/register";
//    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            authService.register(user);

            // 将成功消息通过RedirectAttributes传递
            redirectAttributes.addFlashAttribute("success", "注册成功！请登录");

            // 重定向到登录Controller
            return "redirect:http://localhost/view/login";
        } catch (RuntimeException e) {
            // 注册失败，返回注册页并显示错误
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:http://localhost/view/register";
        }
    }

//    @GetMapping("/dashboard")
//    public String showDashboard(@AuthenticationPrincipal User user, Model model) {
//        model.addAttribute("user", user);
//        return "session/dashboard";
//    }

    @GetMapping("/logout")
    public String logout(@RequestParam(required = false) String clientId,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        // 清理该用户的SSE连接
        if (Objects.equals(clientId, "") || clientId==null) {
            log.info("clientId:{}", clientId);
            modelFeignClient.cleanupUserConnections(clientId);
        }

        // 从请求中获取Token
        String token = getTokenFromRequest(request);

        if (token != null) {
            log.info("token:{}", token);

            // 从Redis中移除Token
            String redisKey = "user:token:" + token;
            redisTemplate.delete(redisKey);

            // 移除Cookie
            Cookie cookie = new Cookie("AUTH_TOKEN", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        return "redirect:http://localhost/view/login";
    }

    // 其他微服务重定向
    @GetMapping("/redirect/qa")
    public String redirectToQaService() {
        return "redirect:http://qa-service/qa";
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

//    // 管理员用户管理页面
//    @GetMapping("/admin/users")
//    public String adminUsers(@AuthenticationPrincipal User user, Model model) {
//        if (user.getRole() != 0) {
//            return "redirect:/user/dashboard";
//        }
//
//        // 获取所有用户
//        List<User> users = authService.getAllUsers();
//        model.addAttribute("users", users);
//        return "admin/users";
//    }
}
