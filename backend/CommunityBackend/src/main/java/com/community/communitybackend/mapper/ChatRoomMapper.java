package com.community.communitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.communitybackend.entity.ChatRoom;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatRoomMapper extends BaseMapper<ChatRoom> {
}
