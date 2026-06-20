package com.company.notify.push;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;

/**
 * 渠道适配接口。新增渠道只需实现本接口并注册为 Bean，主流程无需改动（策略模式）。
 */
public interface MessageChannel {

    /** 本适配器负责的渠道 */
    Channel channel();

    /**
     * 向单个客户发送。返回是否送达成功。
     * 实现需保证可重试；站内信仅落库供客户端拉取，企微调用应用消息接口。
     */
    boolean send(Customer customer, MessageContent content);
}
