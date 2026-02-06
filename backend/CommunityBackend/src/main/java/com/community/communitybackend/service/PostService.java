package com.community.communitybackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.communitybackend.dto.PostDTO;
import com.community.communitybackend.vo.PostVO;

public interface PostService {

    /**
     * 创建帖子
     */
    PostVO createPost(Long userId, PostDTO dto);

    /**
     * 获取帖子详情
     */
    PostVO getPostDetail(Long postId);

    /**
     * 获取帖子列表
     * @param tab 筛选类型: latest, new, hot, my
     * @param clubId 社团ID，可为空
     * @param userId 用户ID（当tab=my时需要）
     * @param page 页码
     * @param size 每页数量
     */
    Page<PostVO> getPostList(String tab, Long clubId, Long userId, int page, int size);

    /**
     * 更新帖子
     */
    PostVO updatePost(Long userId, Long postId, PostDTO dto);

    /**
     * 删除帖子
     */
    void deletePost(Long userId, Long postId);

    /**
     * 增加浏览量
     */
    void incrementViewCount(Long postId);
}
