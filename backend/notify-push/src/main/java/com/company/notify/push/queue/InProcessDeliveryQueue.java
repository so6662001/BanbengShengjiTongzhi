package com.company.notify.push.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 进程内投递队列（默认）。用 @Async 线程池异步消费，作为 RocketMQ 的轻量替代。
 * notify.mq.mode 缺省或为 inprocess 时启用。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notify.mq.mode", havingValue = "inprocess", matchIfMissing = true)
public class InProcessDeliveryQueue implements DeliveryQueue {

    private final DeliveryProcessor processor;

    @Override
    @Async("pushExecutor")
    public void publish(DeliveryMessage message) {
        processor.process(message);
    }
}
