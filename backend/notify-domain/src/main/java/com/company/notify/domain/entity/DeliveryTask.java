package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.Channel;
import com.company.notify.common.enums.DeliveryTaskStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 投递任务（一个公告 × 一个人群 × 一个渠道）。
 * idempotentKey 唯一，保证定时预告/发布任务不重复生成。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_task")
public class DeliveryTask extends BaseEntity {
    private Long announcementId;
    private Long audienceId;
    private Channel channel;
    private LocalDateTime scheduledTime;
    private DeliveryTaskStatus status;
    private String idempotentKey;
}
