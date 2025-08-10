package com.qa.dao;

import com.session.bean.QAPair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QAPairDao {
    int createQaPair(QAPair qaPair);
    int updateAnswer(@Param("id") Long pairId, @Param("answer") String answer);
    List<QAPair> getQaPairsBySession(@Param("sessionId") String sessionId);
    QAPair getQaPairById(@Param("id") Long pairId);
    void updateResponseDuration(Long qaPairId, int duration);
}
