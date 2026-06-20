package com.company.notify.client.security;

import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 客户端鉴权拦截器：身份校验 + 限流 + 可选签名校验。
 * 头部约定：X-Product-Code, X-Identity, X-Timestamp, X-Sign。
 * 签名通过 notify.client.sign-enabled 开关控制（演示默认关闭）。
 */
@Component
@RequiredArgsConstructor
public class ClientAuthInterceptor implements HandlerInterceptor {

    private final ClientRateLimiter rateLimiter;

    @Value("${notify.client.sign-enabled:false}")
    private boolean signEnabled;

    @Value("${notify.client.sign-secret:client-shared-secret-change-me}")
    private String signSecret;

    @Value("${notify.client.sign-tolerance-sec:300}")
    private long toleranceSec;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String productCode = request.getHeader("X-Product-Code");
        String identity = request.getHeader("X-Identity");
        if (productCode == null || productCode.isBlank() || identity == null || identity.isBlank()) {
            throw BizException.of(ErrorCode.CLIENT_AUTH_FAIL, "缺少身份头");
        }

        // 限流（按 产品+身份 维度）
        if (!rateLimiter.tryAcquire(productCode + ":" + identity)) {
            throw BizException.of(ErrorCode.SYSTEM_ERROR, "请求过于频繁，请稍后再试");
        }

        // 可选签名校验
        if (signEnabled) {
            String timestamp = request.getHeader("X-Timestamp");
            String sign = request.getHeader("X-Sign");
            if (timestamp == null || sign == null) {
                throw BizException.of(ErrorCode.SIGN_INVALID, "缺少签名头");
            }
            long now = System.currentTimeMillis() / 1000;
            try {
                if (Math.abs(now - Long.parseLong(timestamp)) > toleranceSec) {
                    throw BizException.of(ErrorCode.SIGN_INVALID, "签名时间戳过期");
                }
            } catch (NumberFormatException e) {
                throw BizException.of(ErrorCode.SIGN_INVALID, "时间戳格式错误");
            }
            String expect = ClientSignUtil.sign(signSecret, productCode, identity, timestamp);
            if (!expect.equalsIgnoreCase(sign)) {
                throw BizException.of(ErrorCode.SIGN_INVALID);
            }
        }
        return true;
    }
}
