package com.history.dao;

import com.session.bean.QAPair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QAPairDao {
    int createQaPair(QAPair qaPair);
    int updateAnswer(@Param("id") Long pairId, @Param("answer") String answer, @Param("answerTime") LocalDateTime answerTime);
    List<QAPair> getQaPairsBySession(@Param("sessionId") String sessionId);
    List<QAPair> getQaPairsBySessionByMax(@Param("sessionId") String sessionId, @Param("max") Integer max);
    QAPair getQaPairById(@Param("id") Long pairId);
    void updateResponseDuration(@Param("id") Long qaPairId, @Param("duration") int duration);
    List<QAPair> getAllQaPairs();
}
