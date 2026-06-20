package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 企业微信渠道：通过企业自建应用消息接口发送图文卡片。
 * 当前为占位实现（记录日志），接入时补充：获取 access_token、调用 message/send、按 wecom_user_ids 投递、处理频率限制。
 */
@Slf4j
@Component
public class WeComChannel implements MessageChannel {

    @Value("${notify.wecom.enabled:false}")
    private boolean enabled;

    @Override
    public Channel channel() {
        return Channel.WECOM;
    }

    @Override
    public boolean send(Customer customer, MessageContent content) {
        String userIds = customer.getWecomUserIds();
        if (userIds == null || userIds.isBlank()) {
            log.warn("客户[{}]未配置企微接收人，跳过企微推送", customer.getId());
            return false;
        }
        if (!enabled) {
            log.info("[企微-MOCK] 向 {} 发送: {} | {}", userIds, content.getTitle(), content.getJumpUrl());
            return true;
        }
        // TODO 接入企业微信应用消息 API：token 缓存、textcard 消息、错误码与限流处理
        log.info("[企微] 向 {} 发送图文卡片: {}", userIds, content.getTitle());
        return true;
    }
}
