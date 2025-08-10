package com.qa.service.impl;

import com.common.dao.QuestionRequest;
import com.qa.dao.QAPairDao;
import com.qa.mq.QuestionProducer;
import com.qa.service.CacheService;
import com.qa.service.QAService;
import com.qa.service.SessionService;
import com.session.bean.QAPair;
import com.session.bean.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class QAServiceImpl implements QAService {

    private SessionService sessionService;
    private CacheService cacheService;
    private QuestionProducer questionProducer;
    private QAPairDao qaPairDao;

    @Autowired
    public void QaServiceImpl(SessionService sessionService,
                              CacheService cacheService,
                              QuestionProducer questionProducer,
                              QAPairDao qaPairDao) {
        this.sessionService = sessionService;
        this.cacheService = cacheService;
        this.questionProducer = questionProducer;
        this.qaPairDao = qaPairDao;
    }

    @Override
    @Transactional
    public String processQuestion(QuestionRequest request) {
        // 1. 获取或创建会话
        String sessionId = request.getSessionId();
        Session session = sessionService.getSession(sessionId);
//        if (sessionId == null || sessionId.isEmpty() || session == null) {
//            sessionId = sessionService.createSession(request.getUserId(), request.getQuestion());
//            session = sessionService.getSession(sessionId);
//        }

        // 2. 若无标题，更新标题
        if(Objects.equals(session.getTitle(), "")){
            String title = request.getQuestion().length() > 10 ?
                    request.getQuestion().substring(0, 7) + "..." : request.getQuestion();
            sessionService.updateSessionTitle(sessionId, title);
        }

        // 3. 创建问答对
        QAPair qaPair = new QAPair(sessionId, request.getQuestion());
        qaPairDao.createQaPair(qaPair);

        // 4. 更新会话活动时间
        sessionService.updateSessionActivity(sessionId);

        // 5. 缓存问题（Redis）
        cacheService.cacheQuestion(qaPair);

        // 6. 发送消息到消息队列
        questionProducer.sendQuestionMessage(request, qaPair.getId());

        return sessionId;
    }


    @Override
    public QAPair saveAnswer(Long qaPairId, String answer) {

        // 1. 更新数据库
        qaPairDao.updateAnswer(qaPairId, answer);

        // 2. 缓存到Redis
        cacheService.cacheAnswer(qaPairId, answer);
//        String cacheKey = "qa:answer:" + qaPairId;
//        redisTemplate.opsForValue().set(cacheKey, answer, 24, TimeUnit.HOURS);

        // 3. 更新响应时间
        QAPair qaPair = cacheService.getCachedQuestion(qaPairId);
        if(qaPair==null)
            qaPair = qaPairDao.getQaPairById(qaPairId);
        if (qaPair.getAskTime() != null) {
            LocalDateTime answerTime = LocalDateTime.now();
            long duration = ChronoUnit.MILLIS.between(qaPair.getAskTime(), answerTime);
            qaPairDao.updateResponseDuration(qaPairId, (int) duration);
        }

        return qaPair;
    }
}
