package com.common.dao;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelTask {
    private Long userId;
    private String sessionId;
    private String question;
    private Long qaPairId;
    private String clientId;
}
