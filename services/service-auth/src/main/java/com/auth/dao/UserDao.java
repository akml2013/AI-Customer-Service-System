package com.auth.dao;

import com.auth.bean.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDao {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Insert("INSERT INTO user(username, password, email, phone, role) " +
            "VALUES(#{username}, #{password}, #{email}, #{phone}, #{role})")
    void save(User user);

    @Update("UPDATE user SET last_login = now() WHERE id = #{id}")
    void updateLastLogin(Long id);
}
