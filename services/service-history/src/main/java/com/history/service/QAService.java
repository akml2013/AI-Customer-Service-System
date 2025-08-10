package com.history.service;

import com.session.bean.QAPair;

import java.util.List;

public interface QAService {
    List<QAPair> getAllQaPairs();

    List<QAPair> getQaPairsBySession(String sessionId);

    List<QAPair> getQaPairsBySession(String sessionId, int max);

    QAPair saveAnswer(Long qaPairId, String answer);
}
