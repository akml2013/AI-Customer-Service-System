package com.session.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Session {
    private String id;  // 会话ID (UUID格式)
    private Long userId;  // 用户ID
    private String title;  // 会话标题

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;  // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivity;  // 最后活动时间
    private Integer status;  // 状态(0关闭,1活跃)

    // 关联的问答对
    @JsonIgnore
    private List<QAPair> qaPairs;

    // 构造器
    public Session() {}

    public Session(String id, Long userId) {
        this.id = id;
        this.userId = userId;
        this.title = "";
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.status = 1;  // 默认活跃
    }

    public Session(String id, Long userId, String title) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.status = 1;  // 默认活跃
    }

    public boolean isActive() {
        return status != null && status == STATUS_ACTIVE;
    }

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_CLOSED = 0;
}
