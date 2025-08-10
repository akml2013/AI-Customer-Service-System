package com.qa.feign.fallback;

import com.qa.feign.HistoryFeignClient;
import com.session.bean.QAPair;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class HistoryFeignClientFallback implements HistoryFeignClient {
    @Override
    public List<QAPair> getQAPairsBySessionId(String sessionId) {
        return Collections.emptyList();
    }

    @Override
    public QAPair saveAnswer(Long qaPairId, String answer) {
        return new QAPair();
    }
}
