package com.community.communitybackend.vo;

import lombok.Data;

@Data
public class ClubVO {

    private Long id;

    private String name;

    private String description;

    private String color;

    private String avatar;

    private Integer memberCount;

    private Integer postCount;
}
