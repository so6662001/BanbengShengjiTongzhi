package com.company.notify.service.support;

import com.company.notify.service.vo.UnreadAnnouncementVO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 客户端未读公告缓存。高频接口走 Redis，降低数据库压力。
 * Redis 不可用时优雅降级（返回 null 让上层回源 DB），不影响主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientUnreadCache {

    private static final String KEY = "notify:unread:%d:%d"; // productId:customerId
    private static final Duration TTL = Duration.ofSeconds(60);

    private final ObjectProvider<StringRedisTemplate> redisProvider;

    public List<UnreadAnnouncementVO> get(Long productId, Long customerId) {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis == null) {
            return null;
        }
        try {
            String json = redis.opsForValue().get(key(productId, customerId));
            if (json == null) {
                return null;
            }
            return JsonUtil.parse(json, new TypeReference<List<UnreadAnnouncementVO>>() {});
        } catch (Exception e) {
            log.warn("未读缓存读取失败，回源DB: {}", e.getMessage());
            return null;
        }
    }

    public void put(Long productId, Long customerId, List<UnreadAnnouncementVO> data) {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis == null) {
            return;
        }
        // 不缓存空结果，避免冷启动/无数据时把空列表写入污染后续投递
        if (data == null || data.isEmpty()) {
            return;
        }
        try {
            redis.opsForValue().set(key(productId, customerId), JsonUtil.toJson(data), TTL);
        } catch (Exception e) {
            log.warn("未读缓存写入失败: {}", e.getMessage());
        }
    }

    public void evict(Long productId, Long customerId) {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis == null) {
            return;
        }
        try {
            redis.delete(key(productId, customerId));
        } catch (Exception e) {
            log.warn("未读缓存清除失败: {}", e.getMessage());
        }
    }

    private String key(Long productId, Long customerId) {
        return String.format(KEY, productId, customerId);
    }
}
