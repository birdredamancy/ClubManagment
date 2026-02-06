package com.community.communitybackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PostDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容最长5000个字符")
    private String content;

    private Long clubId;

    private List<String> tags;

    private List<String> images;

    private Boolean isAnonymous = false;
}
