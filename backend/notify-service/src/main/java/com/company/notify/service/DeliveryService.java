package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.company.notify.common.enums.Channel;
import com.company.notify.common.enums.DeliveryTaskStatus;
import com.company.notify.domain.entity.Announcement;
import com.company.notify.domain.entity.DeliveryTask;
import com.company.notify.domain.mapper.DeliveryTaskMapper;
import com.company.notify.push.MessageContent;
import com.company.notify.push.PushDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投递编排：把「公告 × 人群 × 渠道」拆成投递任务并触发推送。
 * 任务幂等键 = announcementId:channel:scheduledKey，避免预告/发布重复生成。
 */
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryTaskMapper taskMapper;
    private final AudienceService audienceService;
    private final PushDispatcher pushDispatcher;

    /**
     * 为公告在多个渠道创建投递任务并立即分发（用于正式发布或已到点的预告）。
     * scheduledKey 用于幂等标识（如 "RELEASE" 或 "PRE-3"）。
     */
    public void createAndDispatch(Announcement announcement, Long audienceId,
                                  List<Channel> channels, String scheduledKey) {
        List<Long> customerIds = audienceService.resolveCustomerIds(audienceId);
        MessageContent content = MessageContent.builder()
                .title(announcement.getTitle())
                .body(announcement.getPopupContent())
                .jumpUrl(announcement.getJumpUrl())
                .build();
        for (Channel channel : channels) {
            String idemKey = announcement.getId() + ":" + channel.name() + ":" + scheduledKey;
            DeliveryTask task = taskMapper.selectOne(new LambdaQueryWrapper<DeliveryTask>()
                    .eq(DeliveryTask::getIdempotentKey, idemKey).last("limit 1"));
            if (task != null) {
                continue; // 幂等：已生成
            }
            task = new DeliveryTask();
            task.setAnnouncementId(announcement.getId());
            task.setAudienceId(audienceId);
            task.setChannel(channel);
            task.setScheduledTime(LocalDateTime.now());
            task.setStatus(DeliveryTaskStatus.RUNNING);
            task.setIdempotentKey(idemKey);
            taskMapper.insert(task);
            pushDispatcher.dispatch(task, customerIds, content);
        }
    }

    /** 撤回/停推：取消该公告下所有未执行/执行中的任务（已送达记录不回收）。 */
    public void cancelByAnnouncement(Long announcementId) {
        taskMapper.update(null, new LambdaUpdateWrapper<DeliveryTask>()
                .eq(DeliveryTask::getAnnouncementId, announcementId)
                .in(DeliveryTask::getStatus, DeliveryTaskStatus.PENDING, DeliveryTaskStatus.RUNNING)
                .set(DeliveryTask::getStatus, DeliveryTaskStatus.CANCELED));
    }
}
