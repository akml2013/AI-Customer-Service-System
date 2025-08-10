package com.model.mq;

import com.common.dao.ModelTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.config.RocketMqConfig;
import com.model.service.ModelService;
import com.model.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class QuestionConsumer {

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelService modelService;

    @Autowired
    private SseService sseService;

    @Autowired
    @Qualifier("modelTaskExecutor")
    private Executor taskExecutor;

    @PostConstruct
    public void init() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rocketMqConfig.getConsumerGroup());
        consumer.setNamesrvAddr(rocketMqConfig.getMqAddr());
        consumer.subscribe("QUESTION_TOPIC", "QUESTION_TAG");
        consumer.setMessageModel(MessageModel.BROADCASTING);

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    Map<String, Object> data = objectMapper.readValue(json, Map.class);

                    if(!sseService.haveConnections(data.get("clientId").toString())){
                        continue;
                    }

                    // 创建任务并提交到线程池
                    ModelTask task = new ModelTask(
                            Long.parseLong(data.get("userId").toString()),
                            data.get("sessionId").toString(),
                            data.get("question").toString(),
                            Long.parseLong(data.get("qaPairId").toString()),
                            data.get("clientId").toString()
                    );

                    taskExecutor.execute(() -> modelService.processModelTask(task));

                } catch (Exception e) {
                    log.error("Failed to process message: {}", msg.getMsgId(), e);
                    // 消费失败，稍后重试
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        log.info("RocketMQ Consumer started. Group: {}", rocketMqConfig.getConsumerGroup());
    }
}
