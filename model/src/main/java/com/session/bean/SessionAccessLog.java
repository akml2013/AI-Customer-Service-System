package com.session.bean;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionAccessLog {
    private Long id;
    private String sessionId;
    private Long userId;
    private LocalDateTime accessTime;
    private String operation;  // create, read, delete

    // 构造器
    public SessionAccessLog() {}

    public SessionAccessLog(String sessionId, Long userId, String operation) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.operation = operation;
        this.accessTime = LocalDateTime.now();
    }

    public static final String OP_CREATE = "create";
    public static final String OP_READ = "read";
    public static final String OP_DELETE = "delete";
}
