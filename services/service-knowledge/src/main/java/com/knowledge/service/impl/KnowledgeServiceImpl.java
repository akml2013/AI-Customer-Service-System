package com.knowledge.service.impl;

import com.knowledge.bean.Knowledge;
import com.knowledge.dao.KnowledgeDao;
import com.knowledge.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final String CACHE_PREFIX = "knowledge:";
    private static final String QUESTION_CACHE_PREFIX = "knowledge:question:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30);
    private static final String NULL_CACHE_VALUE = "NULL_VALUE";
    private static final Duration NULL_CACHE_DURATION = Duration.ofMinutes(5);

    @Autowired
    private KnowledgeDao knowledgeDao;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional
    @Override
    public Knowledge save(Knowledge knowledge) {
        List<Knowledge> temp = findByQuestion(knowledge.getQuestion());
        if(temp.isEmpty()) {
            // 设置时间
            knowledge.setCreatedAt(LocalDateTime.now());
            knowledge.setLastActivity(LocalDateTime.now());

            // 创建记录
            knowledgeDao.createKnowledge(knowledge);

            // 清除所有相关缓存
            clearAllKnowledgeCaches();
        }
        return knowledge;
    }

    @Override
    public Knowledge saveByService(String question, String answer) {
        Knowledge knowledge = new Knowledge();
        List<Knowledge> temp = findByQuestion(question);
        if(temp.isEmpty()) {
            knowledge.setQuestion(question);
            knowledge.setAnswer(answer);
            knowledge.setCreatedAt(LocalDateTime.now());
            knowledge.setLastActivity(LocalDateTime.now());

            // 创建记录
            knowledgeDao.createKnowledge(knowledge);

            // 清除所有相关缓存
            clearAllKnowledgeCaches();
        }
        return knowledge;
    }

    @Override
    public void delete(Long id) {
        Knowledge knowledge = findById(id); // 确保存在
        knowledgeDao.deleteKnowledge(id);

        // 删除缓存
        redisTemplate.delete(getKey(id));
    }

    @Override
    public Knowledge update(Long id, Knowledge knowledge) {
        Knowledge existing = findById(id); // 确保存在

        // 更新字段
        existing.setQuestion(knowledge.getQuestion());
        existing.setAnswer(knowledge.getAnswer());
        existing.setLastActivity(LocalDateTime.now());

        knowledgeDao.updateKnowledge(existing);

        // 更新缓存
        redisTemplate.opsForValue().set(
                getKey(id),
                existing,
                CACHE_DURATION
        );

        return existing;
    }

    @Override
    public List<Knowledge> findAll() {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = CACHE_PREFIX + "all";

        // 尝试从缓存获取所有知识
        Object cached = ops.get(key);
        if (cached != null) {
            if (NULL_CACHE_VALUE.equals(cached)) {
                return Collections.emptyList(); // 空结果缓存
            }
            return (List<Knowledge>) cached;
        }

        // 缓存未命中则查询数据库
        List<Knowledge> knowledgeList = knowledgeDao.getAllKnowledge();

        // 如果结果为空，设置短暂空值缓存防止穿透
        if (knowledgeList.isEmpty()) {
            ops.set(key, NULL_CACHE_VALUE, NULL_CACHE_DURATION);
            return knowledgeList;
        }

        // 缓存有效结果
        ops.set(key, knowledgeList, CACHE_DURATION);
        return knowledgeList;
    }

    @Override
    public Knowledge findById(Long id) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = getKey(id);

        // 尝试从缓存获取
        Object cached = ops.get(key);
        if (cached != null) {
            if (NULL_CACHE_VALUE.equals(cached)) {
                return null; // 空结果缓存
            }
            return (Knowledge) cached;
        }

        // 缓存未命中则查询数据库
        Knowledge knowledge = knowledgeDao.getKnowledgeById(id);

        // 处理空值情况
        if (knowledge == null) {
            // 设置短暂空值缓存防止穿透
            ops.set(key, NULL_CACHE_VALUE, NULL_CACHE_DURATION);
            return null;
        }

        // 缓存有效结果
        ops.set(key, knowledge, CACHE_DURATION);
        return knowledge;
    }

    @Transactional
    @Override
    public List<Knowledge> findByQuestion(String question) {
        // 1. 规范化问题文本（去除首尾空格，转为小写）
        String normalizedQuestion = normalizeQuestion(question);

        // 2. 尝试从缓存获取
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String cacheKey = QUESTION_CACHE_PREFIX + normalizedQuestion;

        Object cached = ops.get(cacheKey);
        if (cached != null) {
            if (NULL_CACHE_VALUE.equals(cached)) {
                return Collections.emptyList(); // 空结果缓存
            }
            // 缓存命中，返回单个知识项列表
            log.info("缓存命中 {}", (Knowledge) cached);
            return Collections.singletonList((Knowledge) cached);
        }

        // 3. 缓存未命中，查询数据库
        List<Knowledge> results = knowledgeDao.searchByQuestion(normalizedQuestion);

        // 4. 处理空结果
        if (results.isEmpty()) {
            // 设置短暂空值缓存防止穿透
            ops.set(cacheKey, NULL_CACHE_VALUE, NULL_CACHE_DURATION);
            return Collections.emptyList();
        }

        // 5. 获取第一个匹配项（实际使用的知识）
        Knowledge firstKnowledge = results.get(0);

        // 6. 缓存第一个匹配项
        ops.set(cacheKey, firstKnowledge, CACHE_DURATION);

        // 7. 同时缓存所有结果（按ID缓存）
        cacheAllResults(results);

        // 8. 返回第一个匹配项
        return Collections.singletonList(firstKnowledge);
    }

    // 规范化问题文本
    private String normalizeQuestion(String question) {
        return question.trim().toLowerCase();
    }

    // 缓存所有结果（按ID）
    private void cacheAllResults(List<Knowledge> results) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        for (Knowledge knowledge : results) {
            String key = getKey(knowledge.getId());
            ops.set(key, knowledge, CACHE_DURATION);
        }
    }

    @Override
    public void updateAnswer(Long id, String answer) {
        Knowledge existing = findById(id); // 确保存在

        knowledgeDao.updateAnswer(id, answer, LocalDateTime.now());

        // 更新缓存
        if (existing != null) {
            existing.setAnswer(answer);
            existing.setLastActivity(LocalDateTime.now());
            redisTemplate.opsForValue().set(
                    getKey(id),
                    existing,
                    CACHE_DURATION
            );
        }
    }

    // ==== 私有辅助方法 ====

    private String getKey(Long id) {
        return CACHE_PREFIX + "id:" + id;
    }

    private void clearAllKnowledgeCaches() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
