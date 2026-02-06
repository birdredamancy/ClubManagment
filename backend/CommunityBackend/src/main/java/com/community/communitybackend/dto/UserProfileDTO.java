package com.community.communitybackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息修改DTO
 */
@Data
public class UserProfileDTO {

    @Size(max = 50, message = "昵称最长50个字符")
    private String nickname;

    @Size(max = 500, message = "头像URL最长500个字符")
    private String avatar;

    @Size(max = 20, message = "手机号最长20个字符")
    private String phone;

    @Size(max = 30, message = "学号最长30个字符")
    private String studentId;

    @Size(max = 10, message = "性别格式不正确")
    private String gender;

    @Size(max = 500, message = "个人简介最长500个字符")
    private String bio;

    @Size(max = 100, message = "专业名称最长100个字符")
    private String major;

    @Size(max = 20, message = "年级最长20个字符")
    private String grade;

    @Size(max = 100, message = "学院名称最长100个字符")
    private String college;

    @Size(max = 20, message = "生日格式不正确")
    private String birthday;

    @Size(max = 20, message = "QQ号最长20个字符")
    private String qqNumber;

    @Size(max = 50, message = "微信号最长50个字符")
    private String wechatId;

    // === 通知设置 ===
    private Boolean notifyPostReply;
    private Boolean notifyCommentReply;
    private Boolean notifyMention;
    private Boolean notifyClubActivity;
    private Boolean notifySystem;

    // === 邮件设置 ===
    private Boolean emailSystemNotify;
    private Boolean emailActivityRemind;
    private Boolean emailWeeklyDigest;

    // === 隐私设置 ===
    private Boolean publicProfile;
    private Boolean showOnline;
    private Boolean allowMessage;

    // === 界面设置 ===
    private Boolean darkMode;
    private Boolean compactMode;
}
