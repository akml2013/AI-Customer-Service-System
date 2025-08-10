package com.qa.mq;

import com.common.dao.QuestionRequest;
import com.qa.config.RocketMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class QuestionProducer {

    private DefaultMQProducer producer;

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new DefaultMQProducer(rocketMqConfig.getProducerGroup());
        producer.setNamesrvAddr(rocketMqConfig.getMqAddr());

        // 检查并创建主题
        createTopicIfNotExists();

        producer.start();
        log.info("RocketMQ Producer started. Group: {}", rocketMqConfig.getProducerGroup());
    }

    private void createTopicIfNotExists() throws MQClientException {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt();
        adminExt.setNamesrvAddr(rocketMqConfig.getMqAddr());
        adminExt.start();

        try {
            // 检查主题是否存在
            TopicList topicList = adminExt.fetchAllTopicList();
            System.out.println("topicList.getTopicList():"+topicList.getTopicList());
            if (!topicList.getTopicList().contains("QUESTION_TOPIC")) {
                // 创建主题
                adminExt.createTopic("DefaultCluster", "QUESTION_TOPIC", 8, null);
                log.info("Created topic: QUESTION_TOPIC");
            }
        } catch (Exception e) {
            log.error("Failed to create topic", e);
        } finally {
            adminExt.shutdown();
        }
    }

    @PreDestroy
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
            log.info("RocketMQ Producer shutdown.");
        }
    }

    public void sendQuestionMessage(QuestionRequest request, Long qaPairId) {
        try {
            // 构建消息体
            String messageBody = buildMessageBody(request, qaPairId);

            // 创建消息对象
            Message msg = new Message(
                    "QUESTION_TOPIC",
                    "QUESTION_TAG",
                    messageBody.getBytes(StandardCharsets.UTF_8)
            );

            // 发送消息
            producer.send(msg);
            log.debug("Sent question message: {}", messageBody);
        } catch (Exception e) {
            log.error("Failed to send question message", e);
        }
    }

    private String buildMessageBody(QuestionRequest request, Long qaPairId) {
        // 使用JSON格式构建消息体
        return String.format(
                "{\"userId\":%d,\"sessionId\":\"%s\",\"question\":\"%s\",\"qaPairId\":%d,\"clientId\":\"%s\"}",
                request.getUserId(),
                request.getSessionId(),
                escapeJson(request.getQuestion()),
                qaPairId,
                request.getClientId()
        );
    }

    private String escapeJson(String str) {
        return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}