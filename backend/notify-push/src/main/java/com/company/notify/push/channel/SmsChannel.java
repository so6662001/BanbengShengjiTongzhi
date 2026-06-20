package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 短信渠道。notify.sms.enabled=true 时向配置的短信网关 POST {phone, content}。
 * 采用厂商无关的 HTTP 网关形态；对接阿里云/腾讯云时在此替换为对应 SDK 与签名。
 */
@Slf4j
@Component
public class SmsChannel implements MessageChannel {

    private final RestClient http = RestClient.create();

    @Value("${notify.sms.enabled:false}")
    private boolean enabled;

    @Value("${notify.sms.gateway-url:}")
    private String gatewayUrl;

    @Value("${notify.sms.api-key:}")
    private String apiKey;

    @Override
    public Channel channel() {
        return Channel.SMS;
    }

    @Override
    public boolean send(Customer customer, MessageContent content) {
        if (!enabled) {
            log.info("[短信-未启用] 跳过客户 {}", customer.getId());
            return false;
        }
        if (customer.getPhone() == null || customer.getPhone().isBlank() || gatewayUrl.isBlank()) {
            log.warn("客户[{}]无手机号或未配置短信网关", customer.getId());
            return false;
        }
        try {
            // 短信通常仅发送精简文案（标题），避免超长与审核问题
            http.post().uri(gatewayUrl)
                    .header("X-Api-Key", apiKey)
                    .body(Map.of("phone", customer.getPhone(), "content", content.getTitle()))
                    .retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("短信发送失败 客户={}", customer.getId(), e);
            return false;
        }
    }
}
