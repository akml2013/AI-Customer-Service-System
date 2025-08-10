package com.model.dao;

import com.session.bean.QAPair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QAPairDao {
    int createQaPair(QAPair qaPair);
    List<QAPair> getQaPairsBySession(@Param("sessionId") String sessionId);
    QAPair getQaPairById(@Param("id") Long pairId);
    int updateAnswer(@Param("id") Long pairId,
                     @Param("answer") String answer,
                     @Param("answerTime") LocalDateTime answerTime);

    int updateResponseDuration(@Param("id") Long pairId,
                               @Param("duration") int duration);
}
