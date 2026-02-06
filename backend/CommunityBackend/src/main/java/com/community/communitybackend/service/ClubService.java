package com.community.communitybackend.service;

import com.community.communitybackend.vo.ClubVO;

import java.util.List;

public interface ClubService {

    /**
     * 获取所有社团列表
     */
    List<ClubVO> getAllClubs();

    /**
     * 获取社团详情
     */
    ClubVO getClubDetail(Long clubId);
}
