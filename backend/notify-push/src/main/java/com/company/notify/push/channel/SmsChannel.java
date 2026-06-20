package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信渠道（二期）。扩展占位。
 * TODO 二期接入：对接短信网关（阿里云/腾讯云），按签名+模板ID发送，注意字数与审核限制、发送限速。
 */
@Slf4j
@Component
public class SmsChannel implements MessageChannel {

    @Override
    public Channel channel() {
        return Channel.SMS;
    }

    @Override
    public boolean send(Customer customer, MessageContent content) {
        log.warn("短信渠道为二期功能，暂未启用。客户={}", customer.getId());
        return false;
    }
}
