package com.jinse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
public class UserLoginDTO implements Serializable {

    private String username;
    private String password;
    // 注册时可选字段
    private String name;
    private String phone;

}
