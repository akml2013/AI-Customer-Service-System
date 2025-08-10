package com.history.service;

import com.session.bean.QAPair;

public interface CacheService {
    void cacheQuestion(QAPair qaPair);

    void cacheAnswer(Long qaPairId, String answer);

    QAPair getCachedQuestion(Long qaPairId);

    String getCachedAnswer(Long qaPairId);
}
