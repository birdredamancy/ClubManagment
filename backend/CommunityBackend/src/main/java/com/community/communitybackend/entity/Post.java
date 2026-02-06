package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String images;

    private Long clubId;

    private String tags;

    private Boolean isAnonymous;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer status;

    private Boolean pinned;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
