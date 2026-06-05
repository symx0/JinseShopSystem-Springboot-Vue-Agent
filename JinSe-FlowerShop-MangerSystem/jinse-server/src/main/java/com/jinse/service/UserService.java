package com.jinse.service;

import com.jinse.dto.UserInfoDTO;
import com.jinse.dto.UserLoginDTO;
import com.jinse.entity.User;
import com.jinse.vo.UserInfoVO;


public interface UserService {
    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    public User login(UserLoginDTO userLoginDTO);

    /**
     * 修改用户信息
     * @param userInfoDTO
     */
    void modifyInfo(UserInfoDTO userInfoDTO);

    /**
     * 注册新用户
     * @param userLoginDTO
     * @return
     */
    User register(UserLoginDTO userLoginDTO);

    /**
     * 获取用户信息
     * @return
     */
    UserInfoVO getUserInfo();

    /**
     * 修改密码
     * @param oldPassword
     * @param newPassword
     */
    void changePassword(String oldPassword, String newPassword);
}
