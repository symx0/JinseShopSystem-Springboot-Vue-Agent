package com.jinse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    // 员工ID
    private Long id;
    // 姓名
    private String name;
    // 账号
    private String username;
    // 密码
    private String password;
    // 手机号
    private String phone;
    // 性别 0 女 1 男
    private String sex;
    // 身份证号
    private String idNumber;
    // 状态 0 正常 1 禁用
    private Integer status;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    // 创建人
    private Long createUser;
    // 更新人
    private Long updateUser;

}
