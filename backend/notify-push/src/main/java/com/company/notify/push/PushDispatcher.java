package com.company.notify.push;

import com.company.notify.domain.entity.DeliveryTask;
import com.company.notify.push.queue.DeliveryMessage;
import com.company.notify.push.queue.DeliveryQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 推送编排入口：把投递任务拆为消息发布到投递队列（进程内 @Async 或 RocketMQ）。
 * 真正的发送与回写在 DeliveryProcessor 中完成；发送幂等由 delivery_record 唯一键保证。
 */
@Component
@RequiredArgsConstructor
public class PushDispatcher {

    private final DeliveryQueue deliveryQueue;

    public void dispatch(DeliveryTask task, List<Long> customerIds, MessageContent content) {
        DeliveryMessage msg = new DeliveryMessage();
        msg.setTaskId(task.getId());
        msg.setAnnouncementId(task.getAnnouncementId());
        msg.setChannel(task.getChannel());
        msg.setCustomerIds(customerIds);
        msg.setTitle(content.getTitle());
        msg.setBody(content.getBody());
        msg.setJumpUrl(content.getJumpUrl());
        deliveryQueue.publish(msg);
    }
}
