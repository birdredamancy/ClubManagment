package com.community.communitybackend.controller;

import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.common.utils.Result;
import com.community.communitybackend.service.ChatService;
import com.community.communitybackend.vo.ChatMessageVO;
import com.community.communitybackend.vo.ChatRoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取用户的频道列表
     * GET /api/chat/channels
     */
    @GetMapping("/channels")
    public Result<List<ChatRoomVO>> getChannels() {
        Long userId = getCurrentUserId();
        List<ChatRoomVO> channels = chatService.getUserChannels(userId);
        return Result.success(channels);
    }

    /**
     * 获取用户的私信列表
     * GET /api/chat/direct
     */
    @GetMapping("/direct")
    public Result<List<ChatRoomVO>> getDirectMessages() {
        Long userId = getCurrentUserId();
        List<ChatRoomVO> directMessages = chatService.getUserDirectMessages(userId);
        return Result.success(directMessages);
    }

    /**
     * 获取聊天室消息
     * GET /api/chat/room/{roomId}/messages
     */
    @GetMapping("/room/{roomId}/messages")
    public Result<List<ChatMessageVO>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = getCurrentUserId();
        List<ChatMessageVO> messages = chatService.getChatMessages(roomId, userId, page, size);
        return Result.success(messages);
    }

    /**
     * 发送消息
     * POST /api/chat/room/{roomId}/send
     */
    @PostMapping("/room/{roomId}/send")
    public Result<ChatMessageVO> sendMessage(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "消息内容不能为空");
        }
        ChatMessageVO message = chatService.sendMessage(userId, roomId, content);
        return Result.success(message);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new BusinessException(401, "未登录");
        }
        return (Long) authentication.getPrincipal();
    }
}
