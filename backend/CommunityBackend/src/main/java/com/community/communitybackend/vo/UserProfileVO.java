package com.community.communitybackend.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户完整信息VO（基本信息 + 详情信息）
 */
@Data
public class UserProfileVO {

    // === 基本信息（来自 users 表） ===
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    // === 详情信息（来自 user_profile 表） ===
    private String studentId;
    private String gender;
    private String bio;
    private String major;
    private String grade;
    private String college;           // 学院
    private String birthday;          // 生日
    private String qqNumber;          // QQ号
    private String wechatId;          // 微信号

    // === 关联账户状态 ===
    private Boolean wechatLinked;     // 微信是否绑定
    private Boolean qqLinked;         // QQ是否绑定
    private Boolean githubLinked;     // GitHub是否绑定

    // === 邮箱验证状态 ===
    private Boolean emailVerified;    // 邮箱是否验证
    private LocalDateTime passwordUpdatedAt;  // 密码最后修改时间

    // === 安全设置 ===
    private Boolean twoFactorEnabled;    // 两步验证是否启用
    private Boolean smsVerifyEnabled;    // 短信验证是否启用

    // === 通知设置 ===
    private Boolean notifyPostReply;      // 帖子回复通知
    private Boolean notifyCommentReply;   // 评论回复通知
    private Boolean notifyMention;        // @提及通知
    private Boolean notifyClubActivity;   // 社团活动通知
    private Boolean notifySystem;         // 系统通知

    // === 邮件设置 ===
    private Boolean emailSystemNotify;    // 系统通知邮件
    private Boolean emailActivityRemind;  // 活动提醒邮件
    private Boolean emailWeeklyDigest;    // 周报邮件

    // === 隐私设置 ===
    private Boolean publicProfile;        // 公开个人资料
    private Boolean showOnline;           // 显示在线状态
    private Boolean allowMessage;         // 允许私信

    // === 界面设置 ===
    private Boolean darkMode;             // 深色模式
    private Boolean compactMode;          // 紧凑模式
}
