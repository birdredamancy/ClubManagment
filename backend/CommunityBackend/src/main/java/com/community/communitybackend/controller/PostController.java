package com.community.communitybackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.common.utils.Result;
import com.community.communitybackend.dto.CommentDTO;
import com.community.communitybackend.dto.PostDTO;
import com.community.communitybackend.service.CommentService;
import com.community.communitybackend.service.PostService;
import com.community.communitybackend.vo.CommentVO;
import com.community.communitybackend.vo.PostVO;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子/话题控制器
 */
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    /**
     * 获取帖子列表
     * GET /api/post/list?tab=latest&clubId=1&page=1&size=20
     */
    @GetMapping("/list")
    public Result<Page<PostVO>> getPostList(
            @RequestParam(defaultValue = "latest") String tab,
            @RequestParam(required = false) Long clubId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = null;
        if ("my".equals(tab)) {
            userId = getCurrentUserId();
        }

        Page<PostVO> postPage = postService.getPostList(tab, clubId, userId, page, size);
        return Result.success(postPage);
    }

    /**
     * 获取帖子详情
     * GET /api/post/{id}
     */
    @GetMapping("/{id}")
    public Result<PostVO> getPostDetail(@PathVariable Long id) {
        // 增加浏览量
        postService.incrementViewCount(id);

        PostVO post = postService.getPostDetail(id);
        return Result.success(post);
    }

    /**
     * 创建帖子
     * POST /api/post
     */
    @PostMapping
    public Result<PostVO> createPost(@Valid @RequestBody PostDTO dto) {
        Long userId = getCurrentUserId();
        PostVO post = postService.createPost(userId, dto);
        return Result.success(post);
    }

    /**
     * 更新帖子
     * PUT /api/post/{id}
     */
    @PutMapping("/{id}")
    public Result<PostVO> updatePost(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        Long userId = getCurrentUserId();
        PostVO post = postService.updatePost(userId, id, dto);
        return Result.success(post);
    }

    /**
     * 删除帖子
     * DELETE /api/post/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        postService.deletePost(userId, id);
        return Result.success(null);
    }

    /**
     * 获取帖子评论列表
     * GET /api/post/{id}/comments
     */
    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> getComments(@PathVariable Long id) {
        Long userId = getCurrentUserIdOrNull();
        List<CommentVO> comments = commentService.getCommentsByPostId(id, userId);
        return Result.success(comments);
    }

    /**
     * 发表评论
     * POST /api/post/{id}/comments
     */
    @PostMapping("/{id}/comments")
    public Result<CommentVO> createComment(@PathVariable Long id, @Valid @RequestBody CommentDTO dto) {
        Long userId = getCurrentUserId();
        CommentVO comment = commentService.createComment(userId, id, dto);
        return Result.success(comment);
    }

    /**
     * 编辑评论
     * PUT /api/post/comments/{commentId}
     */
    @PutMapping("/comments/{commentId}")
    public Result<CommentVO> updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentDTO dto) {
        Long userId = getCurrentUserId();
        CommentVO comment = commentService.updateComment(userId, commentId, dto);
        return Result.success(comment);
    }

    /**
     * 删除评论
     * DELETE /api/post/comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = getCurrentUserId();
        commentService.deleteComment(userId, commentId);
        return Result.success(null);
    }

    private Long getCurrentUserIdOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Long) {
                return (Long) authentication.getPrincipal();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new BusinessException(401, "未登录");
        }
        return (Long) authentication.getPrincipal();
    }
}
