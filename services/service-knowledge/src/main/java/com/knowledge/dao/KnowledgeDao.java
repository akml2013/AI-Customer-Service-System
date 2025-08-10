package com.knowledge.dao;

import com.knowledge.bean.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KnowledgeDao {
    /**
     * 创建知识记录
     * @param knowledge 知识对象
     * @return 受影响的行数
     */
    int createKnowledge(Knowledge knowledge);

    /**
     * 根据ID获取知识记录
     * @param id 知识ID
     * @return 知识对象
     */
    Knowledge getKnowledgeById(@Param("id") Long id);

    /**
     * 获取所有知识记录
     * @return 知识列表
     */
    List<Knowledge> getAllKnowledge();

    /**
     * 根据问题关键词搜索知识记录
     * @param keyword 搜索关键词
     * @return 匹配的知识列表
     */
    List<Knowledge> searchByQuestion(@Param("keyword") String keyword);

    /**
     * 更新知识记录
     * @param knowledge 知识对象
     * @return 受影响的行数
     */
    int updateKnowledge(Knowledge knowledge);

    /**
     * 更新答案内容
     * @param id 知识ID
     * @param answer 新的答案内容
     * @param lastActivity 最后活动时间
     * @return 受影响的行数
     */
    int updateAnswer(
            @Param("id") Long id,
            @Param("answer") String answer,
            @Param("lastActivity") LocalDateTime lastActivity
    );

    /**
     * 删除知识记录
     * @param id 知识ID
     * @return 受影响的行数
     */
    int deleteKnowledge(@Param("id") Long id);
}
