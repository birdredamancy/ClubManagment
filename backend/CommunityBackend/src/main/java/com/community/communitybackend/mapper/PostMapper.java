package com.community.communitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.communitybackend.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Update("UPDATE post SET like_count = like_count + 1 WHERE id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId);

    @Update("UPDATE post SET like_count = like_count - 1 WHERE id = #{postId} AND like_count > 0")
    int decrementLikeCount(@Param("postId") Long postId);

    @Update("UPDATE post SET comment_count = comment_count + 1 WHERE id = #{postId}")
    int incrementCommentCount(@Param("postId") Long postId);

    @Update("UPDATE post SET comment_count = comment_count - 1 WHERE id = #{postId} AND comment_count > 0")
    int decrementCommentCount(@Param("postId") Long postId);

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{postId}")
    int incrementViewCount(@Param("postId") Long postId);
}
