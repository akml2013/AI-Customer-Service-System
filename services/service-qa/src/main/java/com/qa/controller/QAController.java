package com.qa.controller;

import com.common.dao.QuestionRequest;
import com.common.dao.QuestionResponse;
import com.qa.service.QAService;
import com.qa.service.SessionService;
import com.session.bean.QAPair;
import com.session.bean.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/qa")
public class QAController {

    @Autowired
    private QAService qaService;

    @Autowired
    private SessionService sessionService;

    // 注入配置好的线程池
    @Autowired
    @Qualifier("modelTaskExecutor")
    private Executor taskExecutor;


    @PostMapping("/ask")
    public QuestionResponse askQuestion(@RequestBody QuestionRequest request,
                                        HttpSession session) {
        // 1. 验证请求有效性
        if (request.getUserId() == null || request.getQuestion() == null) {
            return new QuestionResponse("error", "无效的请求参数", "");
        }

        // 1. 获取或创建会话
        if (request.getSessionId() == null) {
            request.setSessionId(sessionService.createSession(request.getUserId(), request.getQuestion()));
        }

        // 2. 使用线程池异步处理任务
        CompletableFuture.runAsync(() -> {
            try {
                qaService.processQuestion(request);
            } catch (Exception e) {
                // 这里可以添加失败处理，如通过SSE通知客户端
            }
        }, taskExecutor);

        // 3. 返回异步响应
        return new QuestionResponse("received", "正在处理中...", request.getSessionId());
    }

    @PostMapping("/question")
    public QAPair saveAnswer(@RequestParam("qaPairId") Long qaPairId,
                             @RequestParam("answer") String answer){
        return qaService.saveAnswer(qaPairId, answer);
    }
}
