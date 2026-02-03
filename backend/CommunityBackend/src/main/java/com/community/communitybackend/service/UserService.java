package com.community.communitybackend.service;

import com.community.communitybackend.dto.LoginDTO;
import com.community.communitybackend.dto.RegisterDTO;
import com.community.communitybackend.entity.Users;
import com.community.communitybackend.vo.LoginVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    LoginVO register(RegisterDTO dto);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 根据业务userId查询用户
     */
    Users getUserByUserId(Long userId);
}
