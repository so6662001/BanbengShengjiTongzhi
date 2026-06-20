package com.company.notify.client.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/** 客户端请求签名工具（HMAC-SHA256）。 */
public final class ClientSignUtil {

    private ClientSignUtil() {}

    /** 待签名串：productCode\nidentity\ntimestamp */
    public static String sign(String secret, String productCode, String identity, String timestamp) {
        String raw = productCode + "\n" + identity + "\n" + timestamp;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("签名计算失败", e);
        }
    }
}
