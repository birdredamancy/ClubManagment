package com.community.communitybackend.controller;

import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.common.utils.Result;
import com.community.communitybackend.dto.UserProfileDTO;
import com.community.communitybackend.service.UserService;
import com.community.communitybackend.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 - 需要登录才能访问
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户的完整信息（基本信息 + 详情）
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        Long userId = getCurrentUserId();
        UserProfileVO vo = userService.getUserProfile(userId);
        return Result.success(vo);
    }

    /**
     * 修改当前登录用户的信息
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@Valid @RequestBody UserProfileDTO dto) {
        Long userId = getCurrentUserId();
        UserProfileVO vo = userService.updateUserProfile(userId, dto);
        return Result.success(vo);
    }

    /**
     * 查看其他用户的公开信息
     * GET /api/user/{userId}
     */
    @GetMapping("/{userId}")
    public Result<UserProfileVO> getPublicProfile(@PathVariable Long userId) {
        UserProfileVO vo = userService.getPublicUserProfile(userId);
        return Result.success(vo);
    }

    /**
     * 用户登出
     * POST /api/user/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        userService.logout(token);
        return Result.success(null);
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

    /**
     * 从请求头中解析Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
