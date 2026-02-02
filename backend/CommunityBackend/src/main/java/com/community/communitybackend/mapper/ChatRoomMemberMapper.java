package com.community.communitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.communitybackend.entity.ChatRoomMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatRoomMemberMapper extends BaseMapper<ChatRoomMember> {
}
