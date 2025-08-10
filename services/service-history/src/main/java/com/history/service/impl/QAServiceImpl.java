package com.history.service.impl;

import com.history.service.CacheService;
import com.history.service.SessionService;
import com.history.dao.QAPairDao;
import com.history.service.QAService;
import com.session.bean.QAPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
public class QAServiceImpl implements QAService {

    private CacheService cacheService;
    private SessionService sessionService;
    private QAPairDao qaPairDao;

    @Autowired
    public void QaServiceImpl(SessionService sessionService,
                              CacheService cacheService,
                              QAPairDao qaPairDao) {
        this.sessionService = sessionService;
        this.cacheService = cacheService;
        this.qaPairDao = qaPairDao;
    }


    @Override
    public List<QAPair> getAllQaPairs() {
        return qaPairDao.getAllQaPairs();
    }

    @Override
    public List<QAPair> getQaPairsBySession(String sessionId) {
        return qaPairDao.getQaPairsBySession(sessionId);
    }

    @Transactional
    @Override
    public List<QAPair> getQaPairsBySession(String sessionId, int max) {
        List<QAPair> qaPairs = qaPairDao.getQaPairsBySessionByMax(sessionId, max);
        qaPairs.sort((s2, s1) ->
                s2.getAskTime().compareTo(s1.getAskTime()));
        return qaPairs;
    }

    @Transactional
    @Override
    public QAPair saveAnswer(Long qaPairId, String answer) {

        // 1. 更新数据库
        LocalDateTime answerTime = LocalDateTime.now();
        qaPairDao.updateAnswer(qaPairId, answer, answerTime);

//        // 2. 缓存到Redis
        cacheService.cacheAnswer(qaPairId, answer);
//        String cacheKey = "qa:answer:" + qaPairId;
//        redisTemplate.opsForValue().set(cacheKey, answer, 24, TimeUnit.HOURS);

        // 3. 更新响应时间
        QAPair qaPair = cacheService.getCachedQuestion(qaPairId);
        if(qaPair==null)
            qaPair = qaPairDao.getQaPairById(qaPairId);
        if (qaPair.getAskTime() != null) {
            long duration = ChronoUnit.MILLIS.between(qaPair.getAskTime(), answerTime);
            qaPairDao.updateResponseDuration(qaPairId, (int) duration);
        }

        return qaPair;
    }
}
