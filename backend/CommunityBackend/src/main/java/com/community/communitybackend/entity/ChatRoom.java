package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_room")
public class ChatRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomName;

    private String roomType;

    private Long ownerId;

    private String avatar;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
