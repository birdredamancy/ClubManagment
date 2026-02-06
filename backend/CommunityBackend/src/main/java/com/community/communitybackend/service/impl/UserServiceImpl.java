package com.community.communitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.common.utils.JwtUtils;
import com.community.communitybackend.dto.LoginDTO;
import com.community.communitybackend.dto.RegisterDTO;
import com.community.communitybackend.dto.UserProfileDTO;
import com.community.communitybackend.entity.UserProfile;
import com.community.communitybackend.entity.Users;
import com.community.communitybackend.mapper.UserMapper;
import com.community.communitybackend.mapper.UserProfileMapper;
import com.community.communitybackend.service.UserService;
import com.community.communitybackend.vo.LoginVO;
import com.community.communitybackend.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "user:token:";
    private static final long TOKEN_EXPIRE_DAYS = 7;

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
        // 设置默认值
        setDefaultProfileSettings(profile);
        userProfileMapper.insert(profile);

        // 生成Token并存入Redis
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, String.valueOf(user.getUserId()), TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
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

        // 生成Token并存入Redis
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, String.valueOf(user.getUserId()), TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        return buildLoginVO(user, token);
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            redisTemplate.delete(TOKEN_PREFIX + token);
        }
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

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        Users user = getUserByUserId(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserProfile profile = getProfileByUserId(userId);
        return buildUserProfileVO(user, profile);
    }

    @Override
    @Transactional
    public UserProfileVO updateUserProfile(Long userId, UserProfileDTO dto) {
        Users user = getUserByUserId(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 更新 users 表中的字段
        boolean userUpdated = false;
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
            userUpdated = true;
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
            userUpdated = true;
        }
        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhone(dto.getPhone());
            userUpdated = true;
        }
        if (userUpdated) {
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }

        // 更新 user_profile 表中的字段
        UserProfile profile = getProfileByUserId(userId);
        if (profile == null) {
            // 如果profile不存在，创建一个
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(LocalDateTime.now());
            // 设置默认值
            setDefaultProfileSettings(profile);
        }

        if (StringUtils.hasText(dto.getNickname())) {
            profile.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getStudentId())) {
            profile.setStudentId(dto.getStudentId());
        }
        if (StringUtils.hasText(dto.getGender())) {
            profile.setGender(dto.getGender());
        }
        if (StringUtils.hasText(dto.getBio())) {
            profile.setBio(dto.getBio());
        }
        if (StringUtils.hasText(dto.getMajor())) {
            profile.setMajor(dto.getMajor());
        }
        if (StringUtils.hasText(dto.getGrade())) {
            profile.setGrade(dto.getGrade());
        }
        if (StringUtils.hasText(dto.getCollege())) {
            profile.setCollege(dto.getCollege());
        }
        if (StringUtils.hasText(dto.getBirthday())) {
            profile.setBirthday(dto.getBirthday());
        }
        if (StringUtils.hasText(dto.getQqNumber())) {
            profile.setQqNumber(dto.getQqNumber());
        }
        if (StringUtils.hasText(dto.getWechatId())) {
            profile.setWechatId(dto.getWechatId());
        }

        // 更新通知设置
        if (dto.getNotifyPostReply() != null) {
            profile.setNotifyPostReply(dto.getNotifyPostReply());
        }
        if (dto.getNotifyCommentReply() != null) {
            profile.setNotifyCommentReply(dto.getNotifyCommentReply());
        }
        if (dto.getNotifyMention() != null) {
            profile.setNotifyMention(dto.getNotifyMention());
        }
        if (dto.getNotifyClubActivity() != null) {
            profile.setNotifyClubActivity(dto.getNotifyClubActivity());
        }
        if (dto.getNotifySystem() != null) {
            profile.setNotifySystem(dto.getNotifySystem());
        }

        // 更新邮件设置
        if (dto.getEmailSystemNotify() != null) {
            profile.setEmailSystemNotify(dto.getEmailSystemNotify());
        }
        if (dto.getEmailActivityRemind() != null) {
            profile.setEmailActivityRemind(dto.getEmailActivityRemind());
        }
        if (dto.getEmailWeeklyDigest() != null) {
            profile.setEmailWeeklyDigest(dto.getEmailWeeklyDigest());
        }

        // 更新隐私设置
        if (dto.getPublicProfile() != null) {
            profile.setPublicProfile(dto.getPublicProfile());
        }
        if (dto.getShowOnline() != null) {
            profile.setShowOnline(dto.getShowOnline());
        }
        if (dto.getAllowMessage() != null) {
            profile.setAllowMessage(dto.getAllowMessage());
        }

        // 更新界面设置
        if (dto.getDarkMode() != null) {
            profile.setDarkMode(dto.getDarkMode());
        }
        if (dto.getCompactMode() != null) {
            profile.setCompactMode(dto.getCompactMode());
        }

        profile.setUpdatedAt(LocalDateTime.now());

        if (profile.getId() == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }

        return buildUserProfileVO(user, profile);
    }

    /**
     * 设置默认的个人资料设置
     */
    private void setDefaultProfileSettings(UserProfile profile) {
        // 通知设置默认开启
        profile.setNotifyPostReply(true);
        profile.setNotifyCommentReply(true);
        profile.setNotifyMention(true);
        profile.setNotifyClubActivity(true);
        profile.setNotifySystem(true);
        // 邮件设置
        profile.setEmailSystemNotify(true);
        profile.setEmailActivityRemind(true);
        profile.setEmailWeeklyDigest(false);
        // 隐私设置默认开启
        profile.setPublicProfile(true);
        profile.setShowOnline(true);
        profile.setAllowMessage(true);
        // 界面设置默认关闭
        profile.setDarkMode(false);
        profile.setCompactMode(false);
        // 安全设置默认关闭
        profile.setTwoFactorEnabled(false);
        profile.setSmsVerifyEnabled(false);
        // 关联账户默认未绑定
        profile.setWechatLinked(false);
        profile.setQqLinked(false);
        profile.setGithubLinked(false);
        profile.setEmailVerified(false);
    }

    @Override
    public UserProfileVO getPublicUserProfile(Long userId) {
        Users user = getUserByUserId(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserProfile profile = getProfileByUserId(userId);

        // 公开信息，隐藏敏感字段
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setCreatedAt(user.getCreatedAt());

        if (profile != null) {
            vo.setGender(profile.getGender());
            vo.setBio(profile.getBio());
            vo.setMajor(profile.getMajor());
            vo.setGrade(profile.getGrade());
        }

        // 不返回 phone, email, studentId 等隐私字段
        return vo;
    }

    private UserProfile getProfileByUserId(Long userId) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        return userProfileMapper.selectOne(wrapper);
    }

    private UserProfileVO buildUserProfileVO(Users user, UserProfile profile) {
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setCreatedAt(user.getCreatedAt());

        if (profile != null) {
            // 基本资料
            vo.setStudentId(profile.getStudentId());
            vo.setGender(profile.getGender());
            vo.setBio(profile.getBio());
            vo.setMajor(profile.getMajor());
            vo.setGrade(profile.getGrade());
            vo.setCollege(profile.getCollege());
            vo.setBirthday(profile.getBirthday());
            vo.setQqNumber(profile.getQqNumber());
            vo.setWechatId(profile.getWechatId());

            // 关联账户状态
            vo.setWechatLinked(profile.getWechatLinked() != null ? profile.getWechatLinked() : false);
            vo.setQqLinked(profile.getQqLinked() != null ? profile.getQqLinked() : false);
            vo.setGithubLinked(profile.getGithubLinked() != null ? profile.getGithubLinked() : false);

            // 邮箱和安全设置
            vo.setEmailVerified(profile.getEmailVerified() != null ? profile.getEmailVerified() : false);
            vo.setPasswordUpdatedAt(profile.getPasswordUpdatedAt());
            vo.setTwoFactorEnabled(profile.getTwoFactorEnabled() != null ? profile.getTwoFactorEnabled() : false);
            vo.setSmsVerifyEnabled(profile.getSmsVerifyEnabled() != null ? profile.getSmsVerifyEnabled() : false);

            // 通知设置
            vo.setNotifyPostReply(profile.getNotifyPostReply() != null ? profile.getNotifyPostReply() : true);
            vo.setNotifyCommentReply(profile.getNotifyCommentReply() != null ? profile.getNotifyCommentReply() : true);
            vo.setNotifyMention(profile.getNotifyMention() != null ? profile.getNotifyMention() : true);
            vo.setNotifyClubActivity(profile.getNotifyClubActivity() != null ? profile.getNotifyClubActivity() : true);
            vo.setNotifySystem(profile.getNotifySystem() != null ? profile.getNotifySystem() : true);

            // 邮件设置
            vo.setEmailSystemNotify(profile.getEmailSystemNotify() != null ? profile.getEmailSystemNotify() : true);
            vo.setEmailActivityRemind(profile.getEmailActivityRemind() != null ? profile.getEmailActivityRemind() : true);
            vo.setEmailWeeklyDigest(profile.getEmailWeeklyDigest() != null ? profile.getEmailWeeklyDigest() : false);

            // 隐私设置
            vo.setPublicProfile(profile.getPublicProfile() != null ? profile.getPublicProfile() : true);
            vo.setShowOnline(profile.getShowOnline() != null ? profile.getShowOnline() : true);
            vo.setAllowMessage(profile.getAllowMessage() != null ? profile.getAllowMessage() : true);

            // 界面设置
            vo.setDarkMode(profile.getDarkMode() != null ? profile.getDarkMode() : false);
            vo.setCompactMode(profile.getCompactMode() != null ? profile.getCompactMode() : false);
        } else {
            // 如果profile为空，设置默认值
            vo.setWechatLinked(false);
            vo.setQqLinked(false);
            vo.setGithubLinked(false);
            vo.setEmailVerified(false);
            vo.setTwoFactorEnabled(false);
            vo.setSmsVerifyEnabled(false);
            vo.setNotifyPostReply(true);
            vo.setNotifyCommentReply(true);
            vo.setNotifyMention(true);
            vo.setNotifyClubActivity(true);
            vo.setNotifySystem(true);
            vo.setEmailSystemNotify(true);
            vo.setEmailActivityRemind(true);
            vo.setEmailWeeklyDigest(false);
            vo.setPublicProfile(true);
            vo.setShowOnline(true);
            vo.setAllowMessage(true);
            vo.setDarkMode(false);
            vo.setCompactMode(false);
        }
        return vo;
    }

    private Long generateUserId() {
        return System.currentTimeMillis() * 1000 + (long) (Math.random() * 1000);
    }
}
