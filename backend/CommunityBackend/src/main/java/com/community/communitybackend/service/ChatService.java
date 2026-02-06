package com.community.communitybackend.service;

import com.community.communitybackend.vo.ChatRoomVO;
import com.community.communitybackend.vo.ChatMessageVO;

import java.util.List;

public interface ChatService {

    /**
     * 获取用户的聊天频道列表
     */
    List<ChatRoomVO> getUserChannels(Long userId);

    /**
     * 获取用户的私信列表
     */
    List<ChatRoomVO> getUserDirectMessages(Long userId);

    /**
     * 获取聊天室消息列表
     */
    List<ChatMessageVO> getChatMessages(Long roomId, Long userId, int page, int size);

    /**
     * 发送消息
     */
    ChatMessageVO sendMessage(Long userId, Long roomId, String content);
}
