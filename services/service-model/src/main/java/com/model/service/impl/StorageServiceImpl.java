package com.model.service.impl;

import com.knowledge.bean.Knowledge;
import com.model.dao.QAPairDao;
import com.model.service.StorageService;
import com.session.bean.QAPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class StorageServiceImpl implements StorageService {

    private final QAPairDao qaPairDao;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public StorageServiceImpl(QAPairDao qaPairDao, RedisTemplate<String, String> redisTemplate) {
        this.qaPairDao = qaPairDao;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveAnswer(Long qaPairId, String answer) {
        // 1. 更新数据库
        LocalDateTime answerTime = LocalDateTime.now();
        qaPairDao.updateAnswer(qaPairId, answer, answerTime);

        // 2. 缓存到Redis
        String cacheKey = "qa:answer:" + qaPairId;
        redisTemplate.opsForValue().set(cacheKey, answer, 24, TimeUnit.HOURS);

        // 3. 更新响应时间
        QAPair qaPair = qaPairDao.getQaPairById(qaPairId);
        if (qaPair != null && qaPair.getAskTime() != null) {
            long duration = ChronoUnit.MILLIS.between(qaPair.getAskTime(), answerTime);
            qaPairDao.updateResponseDuration(qaPairId, (int) duration);
        }
    }
}