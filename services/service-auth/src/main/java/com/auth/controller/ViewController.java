package com.auth.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.auth.bean.User;
import com.auth.security.SecurityValidator;
import com.auth.service.AuthService;
import com.session.bean.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/view")
public class ViewController {
    @Autowired
    private AuthService authService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SecurityValidator securityValidator;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @ModelAttribute("success") String success,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }

        if (success != null && !success.isEmpty()) {
            model.addAttribute("success", success);
        }

        return "user/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(@ModelAttribute("user") User user,
                                   @ModelAttribute("error") String error,
                                   Model model) {
        if (user == null) {
            model.addAttribute("user", new User());
        }

        if (error != null && !error.isEmpty()) {
            model.addAttribute("error", error);
        }

        return "user/register";
    }

    @GetMapping("/dashboard")
    public String showDashboardPage(@RequestParam("userId") Long userId,
                                    @RequestParam("userName") String userName,
                                    @RequestParam("token") String AUTH_TOKEN,
                                    HttpSession session,
                                    Model model) {
        if(!securityValidator.validateUserToken(userId, AUTH_TOKEN)){
            return "redirect:http://localhost/view/login";
        }

        // 添加当前用户信息
//        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        model.addAttribute("AUTH_TOKEN", AUTH_TOKEN);
        model.addAttribute("userName", userName);
        session.setAttribute("userId", userId);

        return "session/dashboard";
    }

    @GetMapping("/knowledge")
    public String showKnowledgePage(@RequestParam("userId") Long userId,
                                    @RequestParam("userName") String userName,
                                    @RequestParam("token") String AUTH_TOKEN,
                                    HttpSession session,
                                    Model model) {
        // 添加当前用户信息
        model.addAttribute("userId", userId);
        model.addAttribute("AUTH_TOKEN", AUTH_TOKEN);
        model.addAttribute("userName", userName);
        session.setAttribute("userId", userId);

        if(!securityValidator.validateAdminToken(userId, AUTH_TOKEN)){
            return "session/dashboard";
        }

        return "session/knowledge";
    }

    @GetMapping("/history")
    public String showHistoryPage(@RequestParam("userId") Long userId,
                                    @RequestParam("userName") String userName,
                                    @RequestParam("token") String AUTH_TOKEN,
                                    HttpSession session,
                                    Model model) {

        // 添加当前用户信息
        model.addAttribute("userId", userId);
        model.addAttribute("AUTH_TOKEN", AUTH_TOKEN);
        model.addAttribute("userName", userName);
        session.setAttribute("userId", userId);

        if(!securityValidator.validateAdminToken(userId, AUTH_TOKEN)){
            return "session/dashboard";
        }

        return "session/history";
    }

}
