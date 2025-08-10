package com.auth.controller;

import com.auth.bean.User;
import com.auth.security.CustomUserDetails;
import com.session.bean.Session;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/session")
public class SessionController {

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session,
                                Model model) {
        // 添加当前用户信息
        model.addAttribute("user", (User) session.getAttribute("currentUser"));

        // 设置会话标题等数据
        Session session0 = new Session();
        session0.setTitle("用户服务咨询");
        model.addAttribute("activeSession", session0);

        return "session/dashboard";
    }
}