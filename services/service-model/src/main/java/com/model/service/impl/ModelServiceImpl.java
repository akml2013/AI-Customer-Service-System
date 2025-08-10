package com.model.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.dao.ModelTask;
import com.knowledge.bean.Knowledge;
import com.model.controller.MonitorController;
import com.model.feign.HistoryFeignClient;
import com.model.feign.KnowledgeFeignClient;
import com.model.service.ModelService;
import com.model.service.SseService;
import com.model.service.StorageService;

import com.session.bean.QAPair;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

    private final StorageService storageService;
    private final SseService sseService;

    @Autowired
    public ModelServiceImpl(StorageService storageService, SseService sseService) {
        this.storageService = storageService;
        this.sseService = sseService;
    }

    @Autowired
    HistoryFeignClient historyFeignClient;

    @Autowired
    KnowledgeFeignClient knowledgeFeignClient;

    @Autowired
    KnowledgeAsyncService knowledgeAsyncService;

    @Autowired
    HistoryAsyncService historyAsyncService;

    @Autowired
    MonitorController monitorController;

    // 讯飞API配置
    @Value("${model.api.url}")
    private String modelApiUrl;

    @Value("${model.api.password}")
    private String apiPassword;

    @GlobalTransactional
    @Override
    public void processModelTask(ModelTask task) {
        try {
            monitorController.getProcessedCount().incrementAndGet();

            //        try {
//            // 1. 调用大模型API（测试用固定回复）
//            String answer = generateTestAnswer(task.getQuestion());
//
//            // 2. 持久化回答
//            storageService.saveAnswer(task.getQaPairId(), answer);
//
//            // 3. 通过SSE推送回答
//            sseService.pushAnswer(task.getClientId(), answer);
//
//            log.info("Processed model task for client: {}", task.getClientId());
//        } catch (Exception e) {
//            log.error("Failed to process model task: {}", task, e);
//        }

            //TODO
            // 在这里获取知识库，若知识库有答案，则直接用sseService.pushAnswer发过去并持久化数据
            List<Knowledge> knowledgeList = knowledgeFeignClient.findByQuestion(task.getQuestion());
            if (knowledgeList != null && !knowledgeList.isEmpty()) {
                // 使用找到的第一个知识库答案
                Knowledge knowledge = knowledgeList.get(0);
                String answer = knowledge.getAnswer();

                // 推送知识库答案
                sseService.pushAnswer(task.getClientId(), answer, 0);

                // 添加系统通知
                String notification = "\n\n[系统提示：以上回答来源于知识库]";
                sseService.pushAnswer(task.getClientId(), notification, 0);
                sseService.pushAnswer(task.getClientId(), "", 1);

                // 持久化到当前会话
                historyFeignClient.saveAnswer(task.getQaPairId(), answer);
//            storageService.saveAnswer(task.getQaPairId(), answer);

                // 更新知识库使用时间
                knowledge.setLastActivity(LocalDateTime.now());
                knowledgeFeignClient.createByService(knowledge.getQuestion(), knowledge.getAnswer());

                log.info("Used knowledge base answer for question: {}", task.getQuestion());
                monitorController.getRedisCount().incrementAndGet();
                monitorController.getSuccessCount().incrementAndGet();
                return; // 不需要继续调用大模型
            }

            // 获取上下文
            List<QAPair> qaPairs = historyFeignClient.getQAPairsBySessionId(task.getSessionId());
            if(!qaPairs.isEmpty()) {
                qaPairs.remove(qaPairs.size()-1);
            }
            // 构建请求体
//        String requestJson = buildRequestJson(task.getQuestion());
            String requestJson = buildRequestJson(task.getQuestion(), qaPairs);

            log.info("Starting model task for client: {}", task.getClientId());

            try {
                // 创建WebClient实例
                WebClient client = WebClient.builder()
                        .baseUrl(modelApiUrl)
                        .defaultHeader("Content-Type", "application/json")
                        .defaultHeader("Authorization", "Bearer " + apiPassword)
                        .build();

                // 发起HTTP流式请求
                Flux<String> responseStream = client.post()
                        .bodyValue(requestJson)
                        .retrieve()
                        .bodyToFlux(String.class)
                        .timeout(Duration.ofSeconds(60)); // 60秒超时

                // 处理流式响应
                StringBuilder fullContent = new StringBuilder();
                AtomicReference<String> lastSid = new AtomicReference<>("");

                responseStream.subscribe(
                        // 处理每个数据块 - 根据图片修正
                        chunk -> {
                            // 1. 检查并跳过[DONE]消息 - 根据图片建议
                            if (chunk.startsWith("data: [DONE]") || chunk.equals("[DONE]")) {
                                return;
                            }

                            // 2. 直接解析JSON响应 - 图片显示没有"data:"前缀
                            try {
                                // 图片显示响应格式示例：
                                // { "code": 0, "message": "Success", "sid": "cha000...", ... }
                                JSONObject json = JSON.parseObject(chunk);

                                // 3. 提取消息内容 - 根据图片中的方法
                                String content = extractContent(json);

                                if (content != null && !content.isEmpty()) {
                                    // 保存到完整响应
                                    fullContent.append(content);

                                    // 更新最新会话ID
                                    if (json.containsKey("sid")) {
                                        lastSid.set(json.getString("sid"));
                                    }

                                    // 通过SSE推送当前片段
                                    sseService.pushAnswer(task.getClientId(), content, 0);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse model response chunk: {}", chunk, e);
                            }
                        },

                        // 错误处理
                        error -> {
                            log.error("Model API error for client: {}", task.getClientId(), error);
//                        sseService.pushError(task.getClientId(), "模型API错误: " + error.getMessage());
                        },

                        // 完成处理
                        () -> {
                            try {
                                log.info("Model response completed for client: {} (SID: {})",
                                        task.getClientId(), lastSid.get());

                                sseService.pushAnswer(task.getClientId(), "", 1);

                                // 持久化完整回答
//                            storageService.saveAnswer(task.getQaPairId(), fullContent.toString());

                                log.info("Persisted response for client: {}", task.getClientId());

                                //TODO
                                // 在这里调用知识库create，将新知识和回答存入
                                // 由于这里切换了上下文，Feign客户端找不到上下文，所以采用异步调用
                                historyAsyncService.saveAnswer(task.getQaPairId(), fullContent.toString());
                                knowledgeAsyncService.createKnowledgeAsync(task.getQuestion(), fullContent.toString());
                            } catch (Exception e) {
                                log.error("Failed to save response for client: {}", task.getClientId(), e);
//                            sseService.pushError(task.getClientId(), "持久化失败: " + e.getMessage());
                            }
                        }
                );
            } catch (Exception e) {
                log.error("Failed to process model task: {}", task, e);
//            sseService.pushError(task.getClientId(), "处理失败: " + e.getMessage());
            }

            monitorController.getSuccessCount().incrementAndGet();
        }
        catch (Exception e) {
            monitorController.getErrorCount().incrementAndGet();
            log.error("处理失败", e);
        }
    }

    // 异步服务
    @Service
    static class KnowledgeAsyncService {

        @Autowired
        private KnowledgeFeignClient knowledgeFeignClient;

        @Async
        public void createKnowledgeAsync(String question, String content) {
            Knowledge knowledge = knowledgeFeignClient.createByService(question, content);
            log.info("Created knowledge base answer for question: {}", knowledge.getQuestion());
        }
    }

    @Service
    static class HistoryAsyncService {

        @Autowired
        private HistoryFeignClient historyFeignClient;

        @Async
        public void saveAnswer(Long qaPairId, String answer) {
            QAPair qaPair = historyFeignClient.saveAnswer(qaPairId, answer);
            log.info("Save for answer: {}", qaPair.getQuestion());
        }
    }

    // 测试用固定回答生成
    private String generateTestAnswer(String question) throws InterruptedException {
        Thread.sleep(1000);
        return "这是针对问题 '" + question + "' 的测试回答。实际将替换为真实模型API调用。";
    }

    private String extractContent(JSONObject json) {
        // 检查错误代码
        if (json.getIntValue("code") != 0) {
            return null;
        }

        // 提取choices数组 - 根据图片中的结构
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }

        // 提取第一个choice的内容
        JSONObject choice = choices.getJSONObject(0);

        // 优先提取delta中的content
        JSONObject delta = choice.getJSONObject("delta");
        if (delta != null) {
            String content = delta.getString("content");
            if (content != null && !content.isEmpty()) {
                return content;
            }
        }

        // 如果delta中没有content，直接提取choice中的content
        return choice.getString("content");
    }

    private String buildRequestJson(String question) {
        JSONObject request = new JSONObject();

        // 使用正确的模型名称
        request.put("model", "lite");

        // 使用messages数组而不是payload
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", question);
        messages.add(message);
        request.put("messages", messages);

        // 添加stream参数
        request.put("stream", true);

        log.debug("Request body: {}", request.toJSONString());
        return request.toJSONString();
    }

    // 构建包含上下文的请求JSON
    private String buildRequestJson(String currentQuestion, List<QAPair> contextPairs) {
        JSONObject request = new JSONObject();

        // 1. 设置模型版本（根据参数说明）
        request.put("model", "lite"); // 示例模型，可根据需要调整

        // 2. 构建messages数组（用户 + 系统 + 历史问答）
        JSONArray messages = new JSONArray();

        // a. 添加系统指令（可选）
        if (shouldAddSystemInstruction()) {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的客服助手，根据历史对话内容回答用户当前的问题。");
            messages.add(systemMessage);
        }

        // b. 添加上下文对话历史
        contextPairs.forEach(qaPair -> {
            // 用户提问
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", qaPair.getQuestion());
            messages.add(userMessage);

            // AI回答
            JSONObject assistantMessage = new JSONObject();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("content", qaPair.getAnswer());
            messages.add(assistantMessage);
        });

        // c. 添加当前问题
        JSONObject currentQuestionMessage = new JSONObject();
        currentQuestionMessage.put("role", "user");
        currentQuestionMessage.put("content", currentQuestion);
        messages.add(currentQuestionMessage);

        // 3. 设置messages数组
        request.put("messages", messages);

        // 4. 设置其他可选参数
        JSONObject parameters = new JSONObject();
        parameters.put("max_tokens", 4096);   // 最大token数
        parameters.put("temperature", 0.7);    // 创造力参数
        request.put("parameters", parameters);

        // 5. 设置流式响应标识
        request.put("stream", true);

        // 调试日志
        log.debug("Request JSON with context:\n{}", request.toJSONString());

        return request.toJSONString();
    }

    // 判断是否添加系统指令（可根据业务需求定制）
    private boolean shouldAddSystemInstruction() {
        return true; // 默认添加，可根据具体情况调整
    }

}