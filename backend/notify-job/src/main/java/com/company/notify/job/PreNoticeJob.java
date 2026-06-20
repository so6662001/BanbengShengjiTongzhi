package com.company.notify.job;

import com.company.notify.service.ReleasePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 事前预告定时任务。
 * 当前用 Spring @Scheduled 实现，生产可替换为 XXL-JOB（将方法体抽到 JobHandler）。
 * 幂等性由 DeliveryService 的投递任务幂等键保证，可安全重复触发。
 */
@Slf4j
@Component
@RequiredArgsConstructor
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
