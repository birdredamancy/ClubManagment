package com.community.communitybackend.controller;

import com.community.communitybackend.common.utils.Result;
import com.community.communitybackend.service.ClubService;
import com.community.communitybackend.vo.ClubVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社团控制器
 */
@RestController
@RequestMapping("/api/club")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    /**
     * 获取所有社团列表
     * GET /api/club/list
     */
    @GetMapping("/list")
    public Result<List<ClubVO>> getClubList() {
        List<ClubVO> clubs = clubService.getAllClubs();
        return Result.success(clubs);
    }

    /**
     * 获取社团详情
     * GET /api/club/{id}
     */
    @GetMapping("/{id}")
    public Result<ClubVO> getClubDetail(@PathVariable Long id) {
        ClubVO club = clubService.getClubDetail(id);
        return Result.success(club);
    }
}
