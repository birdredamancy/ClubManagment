package com.community.communitybackend.service;

import com.community.communitybackend.dto.CommentDTO;
import com.community.communitybackend.vo.CommentVO;

import java.util.List;

public interface CommentService {

    /**
     * 获取帖子的评论列表
     */
    List<CommentVO> getCommentsByPostId(Long postId, Long currentUserId);

    /**
     * 创建评论
     */
    CommentVO createComment(Long userId, Long postId, CommentDTO dto);

    /**
     * 删除评论
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * 编辑评论
     */
    CommentVO updateComment(Long userId, Long commentId, CommentDTO dto);
}
