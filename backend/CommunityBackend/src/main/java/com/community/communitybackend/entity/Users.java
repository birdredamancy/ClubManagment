package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("users")
public class Users {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    private String password;

    private String avatar;

    private String phone;

    private String email;

    private String role;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
