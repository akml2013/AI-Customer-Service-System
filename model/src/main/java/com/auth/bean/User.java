package com.auth.bean;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private Date createdAt;
    private Date lastLogin;
    private Integer status;
    private Integer role;

    public User(){
    }
}
