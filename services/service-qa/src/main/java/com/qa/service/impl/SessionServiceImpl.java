package com.qa.service.impl;

import com.qa.dao.AccessLogDao;
import com.qa.dao.SessionDao;
import com.qa.service.SessionService;
import com.session.bean.Session;
import com.session.bean.SessionAccessLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionDao sessionDao;
    private final AccessLogDao accessLogDao;

    @Autowired
    public SessionServiceImpl(SessionDao sessionDao, AccessLogDao accessLogDao) {
        this.sessionDao = sessionDao;
        this.accessLogDao = accessLogDao;
    }

    @Override
    public Session getSession(String sessionId) {
        return sessionDao.getSessionById(sessionId);
    }

    @Override
    @Transactional
    public String createSession(Long userId, String firstQuestion) {
        // 创建新会话
        String sessionId = UUID.randomUUID().toString();
        String title = firstQuestion.length() > 50 ?
                firstQuestion.substring(0, 47) + "..." : firstQuestion;

        Session session = new Session(sessionId, userId, title);
        sessionDao.createSession(session);

        // 记录访问日志
        SessionAccessLog log = new SessionAccessLog(
                sessionId, userId, SessionAccessLog.OP_CREATE);
        accessLogDao.createAccessLog(log);

        return sessionId;
    }

    @Override
    @Transactional
    public void updateSessionActivity(String sessionId) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 更新会话最后活动时间
        sessionDao.updateLastActivity(sessionId, now); // 传入sessionId和时间

        // 记录访问日志
        Session session = sessionDao.getSessionById(sessionId);
        if (session != null) {
            SessionAccessLog log = new SessionAccessLog(
                    sessionId, session.getUserId(), SessionAccessLog.OP_READ);
            accessLogDao.createAccessLog(log);
        }
    }

    @Override
    public void updateSessionTitle(String sessionId, String title) {
        // 更新会话标题
        sessionDao.updateTitle(sessionId, title);

        // 记录访问日志
        Session session = sessionDao.getSessionById(sessionId);
        if (session != null) {
            SessionAccessLog log = new SessionAccessLog(
                    sessionId, session.getUserId(), SessionAccessLog.OP_READ);
            accessLogDao.createAccessLog(log);
        }
    }
}