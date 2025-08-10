package com.history.service;

import com.session.bean.Session;

import java.util.List;

public interface SessionService {

    Session getSession(String sessionId);

    String createSession(Long userId);

    void updateSessionActivity(String sessionId);

    List<Session> getSessionsByUser(Long userId);
    Session getSessionById(String sessionId);
}
