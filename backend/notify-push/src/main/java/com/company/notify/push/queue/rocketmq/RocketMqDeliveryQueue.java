package com.company.notify.push.queue.rocketmq;

import com.company.notify.push.queue.DeliveryMessage;
import com.company.notify.push.queue.DeliveryQueue;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 投递队列。notify.mq.mode=rocketmq 时启用（需配置 rocketmq.name-server）。
 * 削峰 + 失败重试 + 死信队列(%DLQ%consumerGroup) 由 RocketMQ 承接。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notify.mq.mode", havingValue = "rocketmq")
public class RocketMqDeliveryQueue implements DeliveryQueue {

    public static final String TOPIC = "notify-delivery";

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void publish(DeliveryMessage message) {
        rocketMQTemplate.convertAndSend(TOPIC, message);
    }
}
