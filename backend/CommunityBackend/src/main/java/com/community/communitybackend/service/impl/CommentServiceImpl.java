package com.community.communitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.dto.CommentDTO;
import com.community.communitybackend.entity.Comment;
import com.community.communitybackend.entity.Post;
import com.community.communitybackend.entity.Users;
import com.community.communitybackend.mapper.CommentMapper;
import com.community.communitybackend.mapper.PostMapper;
import com.community.communitybackend.mapper.UserMapper;
import com.community.communitybackend.service.CommentService;
import com.community.communitybackend.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    public List<CommentVO> getCommentsByPostId(Long postId, Long currentUserId) {
        // 获取帖子信息（用于判断楼主）
        Post post = postMapper.selectById(postId);
        Long postOwnerId = post != null ? post.getUserId() : null;

        // 获取所有评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId);
        wrapper.eq(Comment::getStatus, 1);
        wrapper.orderByAsc(Comment::getCreatedAt);

        List<Comment> allComments = commentMapper.selectList(wrapper);

        // 分离顶级评论和子评论
        List<Comment> topComments = new ArrayList<>();
        Map<Long, List<Comment>> repliesMap = new HashMap<>();

        for (Comment comment : allComments) {
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                topComments.add(comment);
            } else {
                repliesMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
            }
        }

        // 转换为VO并组装回复
        return topComments.stream()
                .map(comment -> {
                    CommentVO vo = convertToVO(comment, postOwnerId, currentUserId);

                    // 获取子评论
                    List<Comment> replies = repliesMap.get(comment.getId());
                    if (replies != null && !replies.isEmpty()) {
                        vo.setReplies(replies.stream()
                                .map(reply -> {
                                    CommentVO replyVO = convertToVO(reply, postOwnerId, currentUserId);
                                    // 查找回复目标
                                    if (reply.getParentId() != null && reply.getParentId() > 0) {
                                        Comment parentComment = findCommentById(allComments, reply.getParentId());
                                        if (parentComment != null) {
                                            Users parentUser = getUserByUserId(parentComment.getUserId());
                                            if (parentUser != null) {
                                                replyVO.setReplyToName(parentUser.getNickname() != null ? parentUser.getNickname() : parentUser.getUsername());
                                            }
                                        }
                                    }
                                    return replyVO;
                                })
                                .collect(Collectors.toList()));
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    private Comment findCommentById(List<Comment> comments, Long id) {
        return comments.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    @Transactional
    public CommentVO createComment(Long userId, Long postId, CommentDTO dto) {
        // 检查帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(404, "帖子不存在");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());

        commentMapper.insert(comment);

        // 更新帖子评论数
        postMapper.incrementCommentCount(postId);

        return convertToVO(comment, post.getUserId(), userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此评论");
        }

        // 软删除
        comment.setStatus(0);
        commentMapper.updateById(comment);

        // 更新帖子评论数
        postMapper.decrementCommentCount(comment.getPostId());
    }

    @Override
    @Transactional
    public CommentVO updateComment(Long userId, Long commentId, CommentDTO dto) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑此评论");
        }

        // 获取帖子信息（用于判断楼主）
        Post post = postMapper.selectById(comment.getPostId());
        Long postOwnerId = post != null ? post.getUserId() : null;

        // 更新评论内容
        comment.setContent(dto.getContent());
        commentMapper.updateById(comment);

        return convertToVO(comment, postOwnerId, userId);
    }

    private CommentVO convertToVO(Comment comment, Long postOwnerId, Long currentUserId) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setCreatedAtText(formatTimeAgo(comment.getCreatedAt()));
        vo.setIsOwner(comment.getUserId().equals(postOwnerId));
        vo.setIsLiked(false); // TODO: 从Redis或数据库查询

        // 获取作者信息
        Users user = getUserByUserId(comment.getUserId());
        if (user != null) {
            vo.setAuthorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAuthorAvatar(user.getAvatar());
        }

        return vo;
    }

    /**
     * 根据业务userId查询用户
     */
    private Users getUserByUserId(Long userId) {
        if (userId == null) return null;
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserId, userId);
        return userMapper.selectOne(wrapper);
    }

    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "";

        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        if (minutes < 1440) return (minutes / 60) + "小时前";
        if (minutes < 10080) return (minutes / 1440) + "天前";
        return (minutes / 10080) + "周前";
    }
}
