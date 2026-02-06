package com.community.communitybackend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {

    private Long id;

    private String title;

    private String content;

    private String summary;

    private List<String> images;

    private List<String> tags;

    // 社团信息
    private Long clubId;
    private String clubName;
    private String clubColor;

    // 作者信息
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Boolean isAnonymous;

    // 统计信息
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;

    // 状态
    private Boolean pinned;
    private Boolean isHot;

    // 参与者列表
    private List<ParticipantVO> participants;

    // 时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastActiveTime;

    @Data
    public static class ParticipantVO {
        private Long userId;
        private String name;
        private String avatar;
    }
}
