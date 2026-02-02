package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_summary")
public class AiSummary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String summaryType;

    private String category;

    private String content;

    private Integer postCount;

    private LocalDateTime dateRangeStart;

    private LocalDateTime dateRangeEnd;

    private LocalDateTime createdAt;
}
