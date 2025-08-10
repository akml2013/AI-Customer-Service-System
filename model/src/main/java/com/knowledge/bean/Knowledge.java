package com.knowledge.bean;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Knowledge {
    private Long id;
    private String question;
    private String answer;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastActivity = LocalDateTime.now();
}