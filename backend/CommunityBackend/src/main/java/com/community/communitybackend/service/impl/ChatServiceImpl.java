package com.community.communitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitybackend.entity.ChatMessage;
import com.community.communitybackend.entity.ChatRoom;
import com.community.communitybackend.entity.ChatRoomMember;
import com.community.communitybackend.entity.Users;
import com.community.communitybackend.mapper.ChatMessageMapper;
import com.community.communitybackend.mapper.ChatRoomMapper;
import com.community.communitybackend.mapper.ChatRoomMemberMapper;
import com.community.communitybackend.mapper.UserMapper;
import com.community.communitybackend.service.ChatService;
import com.community.communitybackend.vo.ChatMessageVO;
import com.community.communitybackend.vo.ChatRoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomMapper chatRoomMapper;
    private final ChatRoomMemberMapper chatRoomMemberMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;

    @Override
    public List<ChatRoomVO> getUserChannels(Long userId) {
        // 获取用户加入的频道
        LambdaQueryWrapper<ChatRoomMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ChatRoomMember::getUserId, userId);

        List<ChatRoomMember> memberships = chatRoomMemberMapper.selectList(memberWrapper);
        List<Long> roomIds = memberships.stream()
                .map(ChatRoomMember::getRoomId)
                .collect(Collectors.toList());

        if (roomIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取频道信息
        LambdaQueryWrapper<ChatRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.in(ChatRoom::getId, roomIds);
        roomWrapper.eq(ChatRoom::getRoomType, "channel");

        List<ChatRoom> rooms = chatRoomMapper.selectList(roomWrapper);

        return rooms.stream().map(room -> convertToRoomVO(room, userId)).collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomVO> getUserDirectMessages(Long userId) {
        // 获取用户的私信房间
        LambdaQueryWrapper<ChatRoomMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ChatRoomMember::getUserId, userId);

        List<ChatRoomMember> memberships = chatRoomMemberMapper.selectList(memberWrapper);
        List<Long> roomIds = memberships.stream()
                .map(ChatRoomMember::getRoomId)
                .collect(Collectors.toList());

        if (roomIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取私信房间
        LambdaQueryWrapper<ChatRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.in(ChatRoom::getId, roomIds);
        roomWrapper.eq(ChatRoom::getRoomType, "direct");

        List<ChatRoom> rooms = chatRoomMapper.selectList(roomWrapper);

        return rooms.stream().map(room -> convertToDirectVO(room, userId)).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVO> getChatMessages(Long roomId, Long userId, int page, int size) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getRoomId, roomId);
        wrapper.orderByDesc(ChatMessage::getCreatedAt);
        wrapper.last("LIMIT " + size + " OFFSET " + (page - 1) * size);

        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);

        return messages.stream()
                .map(msg -> convertToMessageVO(msg, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long userId, Long roomId, String content) {
        ChatMessage message = new ChatMessage();
        message.setRoomId(roomId);
        message.setSenderId(userId);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());

        chatMessageMapper.insert(message);

        // 更新房间的最后更新时间
        ChatRoom room = chatRoomMapper.selectById(roomId);
        if (room != null) {
            room.setUpdatedAt(LocalDateTime.now());
            chatRoomMapper.updateById(room);
        }

        return convertToMessageVO(message, userId);
    }

    private ChatRoomVO convertToRoomVO(ChatRoom room, Long userId) {
        ChatRoomVO vo = new ChatRoomVO();
        vo.setId(room.getId());
        vo.setName(room.getRoomName());
        vo.setAvatar(room.getAvatar());
        vo.setRoomType(room.getRoomType());

        // 获取最后一条消息
        LambdaQueryWrapper<ChatMessage> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.eq(ChatMessage::getRoomId, room.getId());
        msgWrapper.orderByDesc(ChatMessage::getCreatedAt);
        msgWrapper.last("LIMIT 1");

        ChatMessage lastMsg = chatMessageMapper.selectOne(msgWrapper);
        if (lastMsg != null) {
            vo.setLastMessage(lastMsg.getContent());
            vo.setLastMessageTime(lastMsg.getCreatedAt());
            vo.setLastMessageTimeText(formatTimeAgo(lastMsg.getCreatedAt()));
        }

        // TODO: 计算未读消息数
        vo.setUnreadCount(0);

        return vo;
    }

    private ChatRoomVO convertToDirectVO(ChatRoom room, Long userId) {
        ChatRoomVO vo = convertToRoomVO(room, userId);

        // 获取对方用户信息
        LambdaQueryWrapper<ChatRoomMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ChatRoomMember::getRoomId, room.getId());
        memberWrapper.ne(ChatRoomMember::getUserId, userId);

        ChatRoomMember otherMember = chatRoomMemberMapper.selectOne(memberWrapper);
        if (otherMember != null) {
            Users otherUser = userMapper.selectById(otherMember.getUserId());
            if (otherUser != null) {
                vo.setName(otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername());
                vo.setAvatar(otherUser.getAvatar());
            }
        }

        return vo;
    }

    private ChatMessageVO convertToMessageVO(ChatMessage message, Long userId) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setRoomId(message.getRoomId());
        vo.setSenderId(message.getSenderId());
        vo.setContent(message.getContent());
        vo.setCreatedAt(message.getCreatedAt());
        vo.setTimeText(formatTimeAgo(message.getCreatedAt()));
        vo.setIsMe(message.getSenderId().equals(userId));

        // 获取发送者信息
        Users sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
            vo.setSenderAvatar(sender.getAvatar());
        }

        return vo;
    }

    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "";

        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        if (minutes < 1440) return (minutes / 60) + "小时前";
        return time.toLocalTime().toString().substring(0, 5);
    }
}
