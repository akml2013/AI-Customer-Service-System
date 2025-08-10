package com.history.service.impl;

import com.history.service.CacheService;
import com.session.bean.QAPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cacheQuestion(QAPair qaPair) {
        String key = "qa:question:" + qaPair.getId();
        redisTemplate.opsForValue().set(key, qaPair, 1, TimeUnit.HOURS);
    }

    @Override
    public void cacheAnswer(Long qaPairId, String answer) {
        String key = "qa:answer:" + qaPairId;
        redisTemplate.opsForValue().set(key, answer, 24, TimeUnit.HOURS);
    }

    @Override
    public QAPair getCachedQuestion(Long qaPairId) {
        String key = "qa:question:" + qaPairId;
        return (QAPair) redisTemplate.opsForValue().get(key);
    }

    @Override
    public String getCachedAnswer(Long qaPairId) {
        String key = "qa:answer:" + qaPairId;
        return (String) redisTemplate.opsForValue().get(key);
    }
}
