package com.community.communitybackend.controller;

import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.common.utils.Result;
import com.community.communitybackend.entity.Users;
import com.community.communitybackend.service.UserService;
import com.community.communitybackend.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器 - 需要登录才能访问
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        Long userId = getCurrentUserId();
        Users user = userService.getUserByUserId(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setCreatedAt(user.getCreatedAt());

        return Result.success(vo);
    }

    /**
     * 从SecurityContext中获取当前登录用户的userId
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new BusinessException(401, "未登录");
        }
        return (Long) authentication.getPrincipal();
    }
}
