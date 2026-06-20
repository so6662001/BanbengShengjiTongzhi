package com.company.notify.push.queue;

/**
 * 投递队列抽象。默认进程内实现（@Async）；配置 notify.mq.mode=rocketmq 时切换为 RocketMQ。
 */
public interface DeliveryQueue {
    void publish(DeliveryMessage message);
}
