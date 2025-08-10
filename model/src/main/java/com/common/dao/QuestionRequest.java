package com.common.dao;

import lombok.Data;

@Data
public class QuestionRequest {
    private Long userId;
    private String sessionId;
    private String question;
    private String clientId; // 用于SSE连接标识
}
