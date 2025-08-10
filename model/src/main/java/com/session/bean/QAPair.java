package com.session.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class QAPair {
    private Long id;  // 问答ID (自增主键)
    private String sessionId;  // 所属会话ID
    private String question;  // 用户问题
    private String answer;  // AI回复
    private LocalDateTime askTime;  // 提问时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime answerTime;  // 回复时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Integer responseDuration;  // 响应耗时(ms)

    // 关联的会话信息
    @JsonIgnore
    private Session session;

    // 构造器
    public QAPair() {}

    public QAPair(String sessionId, String question) {
        this.sessionId = sessionId;
        this.question = question;
        this.askTime = LocalDateTime.now();
    }

    public void setAnswer(String answer) {
        this.answer = answer;
        if (answer != null && answerTime == null) {
            this.answerTime = LocalDateTime.now();
            this.responseDuration = calculateResponseDuration();
        }
    }

    // 计算响应时长
    private Integer calculateResponseDuration() {
        if (askTime != null && answerTime != null) {
            return (int) Duration.between(askTime, answerTime).toMillis();
        }
        return null;
    }
}
