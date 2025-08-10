package com.model.service.impl;

import com.model.service.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseServiceImpl implements SseService {
    private static final Logger logger = LoggerFactory.getLogger(SseServiceImpl.class);

    // 存储SSE连接 (clientId -> SseEmitter)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;

    // 默认超时时间 (30分钟)
    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    @Override
    public boolean haveConnections(String clientId) {
        // 是否存在连接
        logger.info("是否存在连接");
        return emitters.containsKey(clientId);
    }

    @Override
    public SseEmitter createConnection(String clientId) {
        // 移除旧的连接（如果存在）
        if (emitters.containsKey(clientId)) {
            emitters.get(clientId).complete();
            emitters.remove(clientId);
        }

        // 创建新的SSE连接
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.put(clientId, emitter);

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(clientId);
        });

        emitter.onCompletion(() -> emitters.remove(clientId));

        return emitter;
    }

    @Override
    public void cleanupUserConnections(String clientId) {
        // 清理特定用户的所有连接
        emitters.keySet().stream()
                .filter(key -> key.startsWith(clientId))
                .forEach(key -> {
                    SseEmitter emitter = emitters.get(key);
                    if (emitter != null) {
                        emitter.complete();
                    }
                    emitters.remove(key);
                });
        logger.info("清理用户 {} 的所有SSE连接", clientId);
    }

    @Override
    public void disconnectClient(String clientId) {
        if (emitters.containsKey(clientId)) {
            SseEmitter emitter = emitters.get(clientId);
            emitter.complete();
            emitters.remove(clientId);
            logger.info("客户端 {} 已断开连接", clientId);
        }
    }

    @Override
    public void pushAnswer(String clientId, String answer, int isend) {
        if (!emitters.containsKey(clientId)) {
            return;
        }

        SseEmitter emitter = emitters.get(clientId);
        try {
            // 处理非空的回答，按字符拆分发送
            if (answer != null && !answer.isEmpty()) {
                // 将字符串转换为字符数组，每个字符作为一个块
                char[] characters = answer.toCharArray();
                for (char c : characters) {
                    // 发送一个字符
                    emitter.send(SseEmitter.event()
                            .name("answer-chunk")
                            .data(String.valueOf(c)));
//                    // 模拟延迟
                    Thread.sleep(10);
                }
            }

            // 如果isend为1，则发送完成事件
            if (isend == 1) {
                emitter.send(SseEmitter.event()
                        .name("answer-complete")
                        .data(""));
            }
        } catch (IOException e) {
            // 推送失败，移除连接
            emitter.completeWithError(e);
            emitters.remove(clientId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 将回答分割成块（模拟流式传输）
    private String[] splitAnswerToChunks(String answer) {
        int chunkSize = 10; // 每块大约10个字符
        int chunks = (int) Math.ceil((double) answer.length() / chunkSize);
        String[] result = new String[chunks];

        for (int i = 0; i < chunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, answer.length());
            result[i] = answer.substring(start, end);
        }

        return result;
    }
}
