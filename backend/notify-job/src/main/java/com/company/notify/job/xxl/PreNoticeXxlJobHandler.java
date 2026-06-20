package com.company.notify.job.xxl;

import com.company.notify.service.ReleasePlanService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * XXL-JOB 预告任务处理器。在调度中心配置 JobHandler="preNoticeJobHandler" 与 cron 即可。
 * notify.job.mode=xxl 时生效；幂等由投递任务幂等键保证。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notify.job.mode", havingValue = "xxl")
public class PreNoticeXxlJobHandler {

    private final ReleasePlanService releasePlanService;

    @XxlJob("preNoticeJobHandler")
    public void execute() {
        releasePlanService.generateDuePreNotices(LocalDateTime.now());
    }
}
