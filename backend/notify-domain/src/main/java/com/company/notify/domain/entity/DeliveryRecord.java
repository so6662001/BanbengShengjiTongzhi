package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.Channel;
import com.company.notify.common.enums.ReadStatus;
import com.company.notify.common.enums.SendStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 逐客户触达记录。阅读率、送达率统计来源。
 * 唯一索引 (task_id, customer_id, channel) 保证发送幂等。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_record")
public class DeliveryRecord extends BaseEntity {
    private Long taskId;
    private Long announcementId;
    private Long customerId;
    private Channel channel;
    private SendStatus sendStatus;
    private ReadStatus readStatus;
    private LocalDateTime readTime;
    private String failReason;
}
