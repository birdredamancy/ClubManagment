package com.community.communitybackend.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoomVO {

    private Long id;

    private String name;

    private String avatar;

    /**
     * 房间类型: channel(频道) / direct(私信)
     */
    private String roomType;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private String lastMessageTimeText;

    private Integer unreadCount;
}
