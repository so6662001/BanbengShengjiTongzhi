package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 钉钉渠道（二期）。扩展占位。
 * TODO 二期接入：对接钉钉开放平台工作通知/机器人，发送 ActionCard，处理 access_token 与限流。
 */
@Slf4j
@Component
public class DingTalkChannel implements MessageChannel {

    @Override
    public Channel channel() {
        return Channel.DINGTALK;
    }

    @Override
    public boolean send(Customer customer, MessageContent content) {
        log.warn("钉钉渠道为二期功能，暂未启用。客户={}", customer.getId());
        return false;
    }
}
