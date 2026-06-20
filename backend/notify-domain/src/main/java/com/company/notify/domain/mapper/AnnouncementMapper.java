package com.company.notify.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.notify.domain.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
