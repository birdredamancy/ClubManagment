package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String studentId;

    private String nickname;

    private String gender;

    private String bio;

    private String major;

    private String grade;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
