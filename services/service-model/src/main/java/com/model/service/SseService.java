package com.model.service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

    boolean haveConnections(String clientId);

    SseEmitter createConnection(String clientId);

    void cleanupUserConnections(String clientId);

    void disconnectClient(String clientId);

    void pushAnswer(String clientId, String answer, int isend);
}
