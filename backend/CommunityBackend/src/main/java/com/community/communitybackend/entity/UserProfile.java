package com.community.communitybackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_profile")
public class UserProfile {
    private Long user_id;
    private Long student_id;
    private String nickname;
    private String gender;
    private String bio;
    private String major;
    private String grade;

}
