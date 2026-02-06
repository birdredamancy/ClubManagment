package com.community.communitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitybackend.common.exception.BusinessException;
import com.community.communitybackend.entity.Club;
import com.community.communitybackend.mapper.ClubMapper;
import com.community.communitybackend.service.ClubService;
import com.community.communitybackend.vo.ClubVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubMapper clubMapper;

    @Override
    public List<ClubVO> getAllClubs() {
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Club::getStatus, 1);
        wrapper.orderByDesc(Club::getMemberCount);

        List<Club> clubs = clubMapper.selectList(wrapper);

        return clubs.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ClubVO getClubDetail(Long clubId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null || club.getStatus() == 0) {
            throw new BusinessException(404, "社团不存在");
        }
        return convertToVO(club);
    }

    private ClubVO convertToVO(Club club) {
        ClubVO vo = new ClubVO();
        vo.setId(club.getId());
        vo.setName(club.getName());
        vo.setDescription(club.getDescription());
        vo.setColor(club.getColor());
        vo.setAvatar(club.getAvatar());
        vo.setMemberCount(club.getMemberCount());
        vo.setPostCount(club.getPostCount());
        return vo;
    }
}
