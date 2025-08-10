package com.qa.service;

import com.session.bean.Session;

public interface SessionService {

    Session getSession(String sessionId);

    String createSession(Long userId, String firstQuestion);

    void updateSessionActivity(String sessionId);

    void updateSessionTitle(String sessionId, String title);
}
