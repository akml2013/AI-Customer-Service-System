package com.knowledge.service;

import com.knowledge.bean.Knowledge;
import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

public interface KnowledgeService {
    Knowledge save(Knowledge knowledge);

    Knowledge saveByService(String question, String answer);

    void delete(Long id) throws NotFoundException;
    Knowledge update(Long id, Knowledge knowledge);
    List<Knowledge> findAll();
    Knowledge findById(Long id);
    List<Knowledge> findByQuestion(String question);
    void updateAnswer(Long id, String answer);
}
