package com.community.communitybackend.service;

import com.community.communitybackend.dto.LoginDTO;
import com.community.communitybackend.dto.RegisterDTO;
import com.community.communitybackend.dto.UserProfileDTO;
import com.community.communitybackend.entity.Users;
import com.community.communitybackend.vo.LoginVO;
import com.community.communitybackend.vo.UserProfileVO;

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
     * 用户登出（使Token失效）
     */
    void logout(String token);

    /**
     * 根据业务userId查询用户
     */
    Users getUserByUserId(Long userId);

    /**
     * 获取用户完整信息（基本信息 + 详情）
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 修改用户信息
     */
    UserProfileVO updateUserProfile(Long userId, UserProfileDTO dto);

    /**
     * 查看其他用户的公开信息
     */
    UserProfileVO getPublicUserProfile(Long userId);
}
