package com.company.notify.push.channel.wecom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 企业微信开放平台客户端：access_token 缓存 + 应用消息(textcard)发送。
 * 仅在 notify.wecom.enabled=true 且配置 corpId/secret/agentId 时真正调用。
 */
@Slf4j
@Component
public class WeComApiClient {

    @Value("${notify.wecom.corp-id:}")
    private String corpId;
    @Value("${notify.wecom.secret:}")
    private String secret;
    @Value("${notify.wecom.agent-id:0}")
    private long agentId;

    private final RestClient http = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    /** 简单的 token 缓存（含过期时间）。多实例应放 Redis。 */
    private final AtomicReference<CachedToken> tokenCache = new AtomicReference<>();

    /** 获取 access_token（带缓存，提前 60s 过期刷新）。 */
    public String getAccessToken() {
        CachedToken cached = tokenCache.get();
        long now = System.currentTimeMillis();
        if (cached != null && cached.expireAt > now) {
            return cached.token;
        }
        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + secret;
        String body = http.get().uri(url).retrieve().body(String.class);
        try {
            JsonNode node = mapper.readTree(body);
            if (node.path("errcode").asInt(0) != 0) {
                throw new IllegalStateException("获取企微 token 失败: " + body);
            }
            String token = node.path("access_token").asText();
            long expiresIn = node.path("expires_in").asLong(7200);
            tokenCache.set(new CachedToken(token, now + (expiresIn - 60) * 1000));
            return token;
        } catch (Exception e) {
            throw new IllegalStateException("解析企微 token 响应失败", e);
        }
    }

    /**
     * 发送图文卡片(textcard)应用消息。
     * @param toUsers 接收人 userid，竖线分隔（企微格式）
     * @return 是否发送成功
     */
    public boolean sendTextCard(String toUsers, String title, String description, String url) {
        String token = getAccessToken();
        Map<String, Object> payload = Map.of(
                "touser", toUsers,
                "msgtype", "textcard",
                "agentid", agentId,
                "textcard", Map.of(
                        "title", title,
                        "description", description,
                        "url", url == null ? "" : url,
                        "btntxt", "查看详情"));
        String resp = http.post()
                .uri("https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token)
                .body(payload)
                .retrieve()
                .body(String.class);
        try {
            JsonNode node = mapper.readTree(resp);
            int errcode = node.path("errcode").asInt(-1);
            if (errcode != 0) {
                log.warn("企微消息发送返回非0: {}", resp);
            }
            return errcode == 0;
        } catch (Exception e) {
            log.error("解析企微发送响应失败: {}", resp, e);
            return false;
        }
    }

    private record CachedToken(String token, long expireAt) {}
}
