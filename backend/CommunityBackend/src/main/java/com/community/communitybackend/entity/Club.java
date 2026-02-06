package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("club")
public class Club {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String color;

    private String avatar;

    private Long ownerId;

    private Integer memberCount;

    private Integer postCount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
