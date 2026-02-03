package com.community.communitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.common.utils.JwtUtils;
import com.community.communitybackend.dto.LoginDTO;
import com.community.communitybackend.dto.RegisterDTO;
import com.community.communitybackend.entity.UserProfile;
import com.community.communitybackend.entity.Users;
import com.community.communitybackend.mapper.UserMapper;
import com.community.communitybackend.mapper.UserProfileMapper;
import com.community.communitybackend.service.UserService;
import com.community.communitybackend.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查邮箱是否已注册
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            LambdaQueryWrapper<Users> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(Users::getEmail, dto.getEmail());
            if (userMapper.selectCount(emailWrapper) > 0) {
                throw new BusinessException(400, "该邮箱已被注册");
            }
        }

        // 创建用户
        Users user = new Users();
        user.setUserId(generateUserId());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole("USER");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 同步创建用户资料表记录
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getUserId());
        profile.setNickname(dto.getNickname());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileMapper.insert(profile);

        // 生成Token并返回
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        return buildLoginVO(user, token);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUsername, dto.getUsername());
        Users user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 生成Token并返回
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        return buildLoginVO(user, token);
    }

    @Override
    public Users getUserByUserId(Long userId) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserId, userId);
        return userMapper.selectOne(wrapper);
    }

    private LoginVO buildLoginVO(Users user, String token) {
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setToken(token);
        return vo;
    }

    private Long generateUserId() {
        return System.currentTimeMillis() * 1000 + (long) (Math.random() * 1000);
    }
}
