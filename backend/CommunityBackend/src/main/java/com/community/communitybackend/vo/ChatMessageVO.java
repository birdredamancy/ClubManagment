package com.community.communitybackend.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;

    private Long roomId;

    private Long senderId;

    private String senderName;

    private String senderAvatar;

    private String content;

    private LocalDateTime createdAt;

    private String timeText;

    private Boolean isMe;
}
