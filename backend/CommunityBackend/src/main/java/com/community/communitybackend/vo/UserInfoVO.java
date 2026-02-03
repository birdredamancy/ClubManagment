package com.community.communitybackend.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO - 不包含敏感信息（如密码）
 */
@Data
public class UserInfoVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
