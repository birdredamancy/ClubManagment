package com.community.communitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.dto.PostDTO;
import com.community.communitybackend.entity.Club;
import com.community.communitybackend.entity.Comment;
import com.community.communitybackend.entity.Post;
import com.community.communitybackend.entity.Users;
import com.community.communitybackend.mapper.ClubMapper;
import com.community.communitybackend.mapper.CommentMapper;
import com.community.communitybackend.mapper.PostMapper;
import com.community.communitybackend.mapper.UserMapper;
import com.community.communitybackend.service.PostService;
import com.community.communitybackend.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final ClubMapper clubMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public PostVO createPost(Long userId, PostDTO dto) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setClubId(dto.getClubId());
        post.setTags(dto.getTags() != null ? String.join(",", dto.getTags()) : null);
        post.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
        post.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        post.setPinned(false);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        postMapper.insert(post);

        return convertToVO(post);
    }

    @Override
    public PostVO getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(404, "帖子不存在");
        }
        return convertToVO(post);
    }

    @Override
    public Page<PostVO> getPostList(String tab, Long clubId, Long userId, int page, int size) {
        Page<Post> postPage = new Page<>(page, size);

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);

        // 按社团筛选
        if (clubId != null) {
            wrapper.eq(Post::getClubId, clubId);
        }

        // 按tab类型筛选和排序
        switch (tab) {
            case "new":
                // 24小时内的新帖
                wrapper.ge(Post::getCreatedAt, LocalDateTime.now().minusHours(24));
                wrapper.orderByDesc(Post::getCreatedAt);
                break;
            case "hot":
                // 按热度排序 (点赞+评论)
                wrapper.orderByDesc(Post::getLikeCount);
                wrapper.orderByDesc(Post::getCommentCount);
                break;
            case "my":
                // 我的帖子
                if (userId != null) {
                    wrapper.eq(Post::getUserId, userId);
                }
                wrapper.orderByDesc(Post::getCreatedAt);
                break;
            case "latest":
            default:
                // 最新更新 (置顶优先，然后按更新时间)
                wrapper.orderByDesc(Post::getPinned);
                wrapper.orderByDesc(Post::getUpdatedAt);
                break;
        }

        Page<Post> result = postMapper.selectPage(postPage, wrapper);

        // 转换为VO
        Page<PostVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    public PostVO updatePost(Long userId, Long postId, PostDTO dto) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此帖子");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setClubId(dto.getClubId());
        post.setTags(dto.getTags() != null ? String.join(",", dto.getTags()) : null);
        post.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
        post.setUpdatedAt(LocalDateTime.now());

        postMapper.updateById(post);

        return convertToVO(post);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此帖子");
        }

        // 软删除
        post.setStatus(0);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
    }

    @Override
    public void incrementViewCount(Long postId) {
        postMapper.incrementViewCount(postId);
    }

    /**
     * 将Post实体转换为PostVO
     */
    private PostVO convertToVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setSummary(generateSummary(post.getContent()));
        vo.setImages(post.getImages() != null ? Arrays.asList(post.getImages().split(",")) : new ArrayList<>());
        vo.setTags(post.getTags() != null ? Arrays.asList(post.getTags().split(",")) : new ArrayList<>());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setPinned(post.getPinned() != null && post.getPinned());
        vo.setIsHot(post.getLikeCount() > 50 || post.getCommentCount() > 20);
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        vo.setLastActiveTime(formatTimeAgo(post.getUpdatedAt()));
        vo.setIsAnonymous(post.getIsAnonymous());

        // 获取作者信息
        vo.setUserId(post.getUserId());
        if (post.getIsAnonymous() != null && post.getIsAnonymous()) {
            vo.setAuthorName("匿名用户");
            vo.setAuthorAvatar(null);
        } else {
            Users user = getUserByUserId(post.getUserId());
            if (user != null) {
                vo.setAuthorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                vo.setAuthorAvatar(user.getAvatar());
            }
        }

        // 获取社团信息
        if (post.getClubId() != null) {
            Club club = clubMapper.selectById(post.getClubId());
            if (club != null) {
                vo.setClubId(club.getId());
                vo.setClubName(club.getName());
                vo.setClubColor(club.getColor());
            }
        }

        // 获取参与者（最近评论的用户）
        vo.setParticipants(getParticipants(post.getId()));

        return vo;
    }

    /**
     * 获取帖子的参与者列表
     */
    private List<PostVO.ParticipantVO> getParticipants(Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId);
        wrapper.orderByDesc(Comment::getCreatedAt);
        wrapper.last("LIMIT 10");

        List<Comment> comments = commentMapper.selectList(wrapper);

        // 去重并获取用户信息
        Set<Long> userIds = new LinkedHashSet<>();
        for (Comment comment : comments) {
            userIds.add(comment.getUserId());
            if (userIds.size() >= 4) break;
        }

        List<PostVO.ParticipantVO> participants = new ArrayList<>();
        for (Long uid : userIds) {
            Users user = getUserByUserId(uid);
            if (user != null) {
                PostVO.ParticipantVO p = new PostVO.ParticipantVO();
                p.setUserId(user.getUserId());
                p.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                p.setAvatar(user.getAvatar());
                participants.add(p);
            }
        }

        return participants;
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

    /**
     * 生成摘要
     */
    private String generateSummary(String content) {
        if (content == null) return "";
        // 去除HTML标签并截取前200字符
        String text = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }

    /**
     * 格式化时间为"xx前"
     */
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
