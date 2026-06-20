package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 邮件渠道（二期）。当前为扩展占位：保留 Bean 以便 PushDispatcher 注册，但默认不投递。
 * TODO 二期接入：对接邮件网关（阿里云/SendCloud），渲染 HTML 模板、退信处理、发送限速。
 */
@Slf4j
@Component
public class EmailChannel implements MessageChannel {

    @Override
    public Channel channel() {
        return Channel.EMAIL;
    }

    @Override
    public boolean send(Customer customer, MessageContent content) {
        log.warn("邮件渠道为二期功能，暂未启用。客户={} 标题={}", customer.getId(), content.getTitle());
        return false;
    }
}
