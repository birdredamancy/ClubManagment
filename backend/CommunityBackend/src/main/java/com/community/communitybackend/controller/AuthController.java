package com.community.communitybackend.controller;

import com.community.communitybackend.common.utils.Result;
import com.community.communitybackend.dto.LoginDTO;
import com.community.communitybackend.dto.RegisterDTO;
import com.community.communitybackend.service.UserService;
import com.community.communitybackend.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器 - 登录注册
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        LoginVO vo = userService.register(dto);
        return Result.success(vo);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return Result.success(vo);
    }
}
