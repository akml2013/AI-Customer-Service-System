package com.model.feign.fallback;

import com.knowledge.bean.Knowledge;
import com.model.feign.KnowledgeFeignClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class KnowledgeFeignClientFallback implements KnowledgeFeignClient {
    @Override
    public List<Knowledge> findByQuestion(String question) {
        return Collections.emptyList();
    }

    @Override
    public Knowledge createByService(String question, String answer) {
        return new Knowledge();
    }
}
