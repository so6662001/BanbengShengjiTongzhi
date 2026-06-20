package com.company.notify.push.queue.rocketmq;

import com.company.notify.push.queue.DeliveryMessage;
import com.company.notify.push.queue.DeliveryProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 投递消费者。按消费组消费，异常自动重试，超出重试进入死信队列。
 * 实际渠道 QPS 限速可在各 MessageChannel 实现内做（或在此消费者配置 consumeThreadMax/拉取速率）。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notify.mq.mode", havingValue = "rocketmq")
@RocketMQMessageListener(topic = RocketMqDeliveryQueue.TOPIC, consumerGroup = "notify-delivery-group")
public class RocketMqDeliveryListener implements RocketMQListener<DeliveryMessage> {

    private final DeliveryProcessor processor;

    @Override
    public void onMessage(DeliveryMessage message) {
        processor.process(message);
    }
}
