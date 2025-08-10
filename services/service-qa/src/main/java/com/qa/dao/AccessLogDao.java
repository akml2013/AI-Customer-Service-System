package com.qa.dao;

import com.session.bean.SessionAccessLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessLogDao {
    int createAccessLog(SessionAccessLog log);
}