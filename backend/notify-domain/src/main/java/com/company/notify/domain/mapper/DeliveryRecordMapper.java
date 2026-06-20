package com.company.notify.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.notify.domain.entity.DeliveryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeliveryRecordMapper extends BaseMapper<DeliveryRecord> {

    /** 统计某公告下指定发送状态的记录数（送达统计用） */
    @Select("SELECT COUNT(*) FROM delivery_record WHERE announcement_id = #{announcementId} AND send_status = #{sendStatus} AND deleted = 0")
    long countByAnnouncementAndSend(Long announcementId, String sendStatus);

    /** 统计某公告下已读记录数（阅读率分子） */
    @Select("SELECT COUNT(*) FROM delivery_record WHERE announcement_id = #{announcementId} AND read_status = 'READ' AND deleted = 0")
    long countReadByAnnouncement(Long announcementId);
}
