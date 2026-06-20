package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 钉钉渠道。notify.dingtalk.enabled=true 时通过自定义机器人 webhook（含加签）发送 actionCard。
 * 自定义机器人为群级广播，故对单客户消息以群通知形式投递；如需点对点请改用钉钉工作通知(需企业应用)。
 */
@Slf4j
@Component
public class DingTalkChannel implements MessageChannel {

    private final RestClient http = RestClient.create();

    @Value("${notify.dingtalk.enabled:false}")
    private boolean enabled;

    @Value("${notify.dingtalk.webhook:}")
    private String webhook;

    @Value("${notify.dingtalk.secret:}")
    private String secret;

    @Override
    public Channel channel() {
        return Channel.DINGTALK;
    }

    @Override
    public boolean send(Customer customer, MessageContent content) {
        if (!enabled || webhook.isBlank()) {
            log.info("[钉钉-未启用] 跳过客户 {}", customer.getId());
            return false;
        }
        try {
            String url = sign(webhook, secret);
            Map<String, Object> payload = Map.of(
                    "msgtype", "actionCard",
                    "actionCard", Map.of(
                            "title", content.getTitle(),
                            "text", "### " + content.getTitle() + "\n\n" + (content.getBody() == null ? "" : content.getBody()),
                            "singleTitle", "查看详情",
                            "singleURL", content.getJumpUrl() == null ? "" : content.getJumpUrl()));
            http.post().uri(url).body(payload).retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("钉钉发送失败 客户={}", customer.getId(), e);
            return false;
        }
    }

    /** 钉钉自定义机器人加签：timestamp + HMAC-SHA256(secret) → 拼到 webhook。 */
    private String sign(String webhook, String secret) throws Exception {
        if (secret == null || secret.isBlank()) {
            return webhook;
        }
        long ts = System.currentTimeMillis();
        String stringToSign = ts + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
        return webhook + "&timestamp=" + ts + "&sign=" + sign;
    }
}
