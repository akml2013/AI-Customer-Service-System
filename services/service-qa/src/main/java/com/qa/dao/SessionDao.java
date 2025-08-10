package com.qa.dao;

import com.session.bean.Session;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SessionDao {
    int createSession(Session session);
    int updateSession(Session session);
    Session getSessionById(@Param("id") String sessionId);
    List<Session> getSessionsByUser(@Param("userId") Long userId);
    int updateLastActivity(@Param("id") String sessionId, @Param("time") LocalDateTime lastActivity);
    int closeSession(@Param("id") String sessionId);
    void updateTitle(@Param("id") String sessionId, @Param("title") String title);
}