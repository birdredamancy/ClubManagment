package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("users")
public class Users {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long user_id;

    private String role;

    private String avatar;

    private String username;

    private String nickname;

    private String password;

    private String iphone;

    private String email;


}
