package com.company.notify.job;

import com.company.notify.service.ReleasePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 事前预告定时任务（默认 Spring @Scheduled 实现）。
 * notify.job.mode=xxl 时由 {@code PreNoticeXxlJobHandler} 接管，本组件停用。
 * 幂等性由 DeliveryService 的投递任务幂等键保证，可安全重复触发。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notify.job.mode", havingValue = "spring", matchIfMissing = true)
public class PreNoticeJob {

    private final ReleasePlanService releasePlanService;

    /** 每 5 分钟扫描一次到点的预告计划。 */
    @Scheduled(cron = "${notify.job.pre-notice-cron:0 */5 * * * ?}")
    public void scanPreNotices() {
        try {
            releasePlanService.generateDuePreNotices(LocalDateTime.now());
        } catch (Exception e) {
            log.error("预告扫描任务执行失败", e);
        }
    }
}
