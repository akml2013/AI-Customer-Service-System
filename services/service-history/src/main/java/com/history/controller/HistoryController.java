package com.history.controller;

import com.auth.bean.User;
import com.history.security.SecurityValidator;
import com.history.service.QAService;
import com.history.service.SessionService;
import com.knowledge.bean.Knowledge;
import com.session.bean.QAPair;
import com.session.bean.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private QAService qaService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SecurityValidator securityValidator;


    @GetMapping("/session")
    public ResponseEntity<Session> getSessionDetails(
            @RequestParam String sessionId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Auth-Token") String token) {

        // 验证用户ID和令牌
        if (!securityValidator.validateUserToken(userId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 验证会话是否属于该用户
        Session session = sessionService.getSessionById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<Session>> getUserSessions(@RequestHeader("X-User-Id") Long userId,
                                                         @RequestHeader("X-Auth-Token") String token) {
        // 验证用户ID和令牌
        if (!securityValidator.validateUserToken(userId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 获取会话并按照最后活动时间倒序排序
        List<Session> sessions = sessionService.getSessionsByUser(userId);
        sessions.sort((s1, s2) ->
                s2.getLastActivity().compareTo(s1.getLastActivity()));

        return ResponseEntity.ok(sessions);
    }

    // [新增] 会话创建端点
    @PostMapping("/create")
    public ResponseEntity<String> createSession(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Auth-Token") String token) {

        // 验证用户ID和令牌
        if (!securityValidator.validateUserToken(userId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 保存会话
        String sessionId = sessionService.createSession(userId);

        return ResponseEntity.ok(sessionId);
    }

    @GetMapping("/qa_pairs")
    public ResponseEntity<List<QAPair>> getQaPairsBySession(
            @RequestParam String sessionId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Auth-Token") String token) {

        // 验证用户ID和令牌
        if (!securityValidator.validateUserToken(userId, token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 验证会话是否属于该用户
        Session session = sessionService.getSessionById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<QAPair> qaPairs = qaService.getQaPairsBySession(sessionId);
        return ResponseEntity.ok(qaPairs);
    }

    @GetMapping("/all")
    public ResponseEntity<List<QAPair>> getAllQaPairs(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Auth-Token") String token) {
        // 验证用户ID和令牌
        if (!securityValidator.validateAdminToken(userId, token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<QAPair> qaPairs = qaService.getAllQaPairs();
            return ResponseEntity.ok(qaPairs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/context")
    List<QAPair> getQAPairsBySessionId(@RequestParam("sessionId") String sessionId){
        return qaService.getQaPairsBySession(sessionId, 3);
    }

    @PostMapping("/answer")
    public QAPair saveAnswer(@RequestParam("qaPairId") Long qaPairId,
                             @RequestParam("answer") String answer){
        return qaService.saveAnswer(qaPairId, answer);
    }
}
