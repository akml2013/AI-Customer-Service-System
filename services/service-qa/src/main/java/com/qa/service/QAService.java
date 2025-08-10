package com.qa.service;

import com.common.dao.QuestionRequest;
import com.session.bean.QAPair;

public interface QAService {
    String processQuestion(QuestionRequest request);

    QAPair saveAnswer(Long qaPairId, String answer);
}
