package com.common.dao;

import lombok.Data;

@Data
public class QuestionResponse {
    private String status;
    private String message;
    private String sessionId;

    public QuestionResponse(String status, String message, String sessionId) {
        this.status = status;
        this.message = message;
        this.sessionId = sessionId;
    }
}
