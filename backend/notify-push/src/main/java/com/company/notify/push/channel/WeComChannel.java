package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import com.company.notify.push.channel.wecom.WeComApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 企业微信渠道：通过企业自建应用消息接口发送图文卡片。
 * notify.wecom.enabled=true 时调用真实 API（WeComApiClient）；否则走 MOCK 仅记录日志，便于本地联调。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeComChannel implements MessageChannel {

    private final WeComApiClient apiClient;

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
        // 库内逗号分隔 → 企微要求竖线分隔
        String toUsers = userIds.replace(",", "|");
        if (!enabled) {
            log.info("[企微-MOCK] 向 {} 发送: {} | {}", toUsers, content.getTitle(), content.getJumpUrl());
            return true;
        }
        return apiClient.sendTextCard(toUsers, content.getTitle(), content.getBody(), content.getJumpUrl());
    }
}
