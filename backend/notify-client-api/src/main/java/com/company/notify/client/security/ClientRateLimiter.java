package com.company.notify.client.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轻量固定窗口限流（进程内）。按客户端身份限制每秒请求数。
 * 多实例部署应替换为 Redis/Sentinel 分布式限流。
 */
@Component
public class ClientRateLimiter {

    @Value("${notify.client.rate-limit-per-sec:20}")
    private int limitPerSec;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key) {
        long sec = System.currentTimeMillis() / 1000;
        Window w = windows.compute(key, (k, old) -> {
            if (old == null || old.second != sec) {
                return new Window(sec);
            }
            return old;
        });
        return w.counter.incrementAndGet() <= limitPerSec;
    }

    private static final class Window {
        final long second;
        final AtomicInteger counter = new AtomicInteger(0);
        Window(long second) { this.second = second; }
    }
}
