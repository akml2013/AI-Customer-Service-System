package com.qa.controller;

import com.qa.service.SessionService;
import com.session.bean.QAPair;
import com.session.bean.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 会话服务控制器
@RestController
@RequestMapping("/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

//    @PostMapping("/new")
//    public Session createNewSession() {
//        // 创建一个新会话
//        return sessionService.createSession();
//    }
//
//    @GetMapping("/{sessionId}/messages")
//    public List<QAPair> getSessionMessages(@PathVariable String sessionId) {
//        // 获取特定会话的消息
//        return sessionService.getMessagesBySession(sessionId);
//    }
}