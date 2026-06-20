package com.company.notify.push.queue;

import com.company.notify.common.enums.Channel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 投递消息体（可 JSON 序列化，用于 MQ 传输）。 */
@Data
public class DeliveryMessage implements Serializable {
    private Long taskId;
    private Long announcementId;
    private Channel channel;
    private List<Long> customerIds;
    private String title;
    private String body;
    private String jumpUrl;
}
