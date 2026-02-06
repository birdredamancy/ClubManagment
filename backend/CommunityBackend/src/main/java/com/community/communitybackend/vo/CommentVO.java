package com.community.communitybackend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;

    private Long postId;

    private Long userId;

    private String authorName;

    private String authorAvatar;

    private String content;

    private Long parentId;

    private Integer likeCount;

    private Boolean isLiked;

    private Boolean isOwner;

    private LocalDateTime createdAt;

    private String createdAtText;

    /**
     * 子评论/回复列表
     */
    private List<CommentVO> replies;

    /**
     * 回复目标用户名（当parentId > 0时）
     */
    private String replyToName;
}
