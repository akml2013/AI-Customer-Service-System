package com.model.controller;

import com.model.service.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequestMapping("/sse")
@RestController
public class SseController {

    private static final Logger log = LoggerFactory.getLogger(SseController.class);
    private final SseService sseService;

    @Autowired
    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/connect")
    public SseEmitter connect(@RequestParam String clientId) {
        log.info("client id is {}", clientId);
        return sseService.createConnection(clientId);
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect() {
        // 实际应用中应该验证客户端ID来源
        // 这里简化处理
        return ResponseEntity.ok("Disconnect initiated");
    }

    @GetMapping("/disconnectById")
    public void cleanupUserConnections(@RequestParam String clientId) {
        log.info("cleanupUserConnections clientId:{}", clientId);
        sseService.cleanupUserConnections(clientId);
    }
}