package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private Long senderId;

    private String content;

    private String msgType;

    private Integer status;

    private LocalDateTime createdAt;
}
