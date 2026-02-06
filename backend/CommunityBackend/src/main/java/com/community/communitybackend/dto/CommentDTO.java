package com.community.communitybackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDTO {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容最长2000个字符")
    private String content;

    /**
     * 父评论ID，0表示顶级评论
     */
    private Long parentId = 0L;
}
