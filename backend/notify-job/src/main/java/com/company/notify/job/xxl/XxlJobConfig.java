package com.company.notify.job.xxl;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-JOB 执行器配置。notify.job.mode=xxl 时启用，需配置 xxl.job.admin.addresses。
 * 启用后由 XXL-JOB 调度中心统一管理预告任务（替代 Spring @Scheduled）。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "notify.job.mode", havingValue = "xxl")
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses:}")
    private String adminAddresses;
    @Value("${xxl.job.access-token:}")
    private String accessToken;
    @Value("${xxl.job.executor.appname:notify-executor}")
    private String appName;
    @Value("${xxl.job.executor.port:9999}")
    private int port;
    @Value("${xxl.job.executor.logpath:/data/applogs/xxl-job/jobhandler}")
    private String logPath;
    @Value("${xxl.job.executor.logretentiondays:30}")
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("初始化 XXL-JOB 执行器 admin={}", adminAddresses);
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddresses);
        executor.setAppname(appName);
        executor.setPort(port);
        executor.setAccessToken(accessToken);
        executor.setLogPath(logPath);
        executor.setLogRetentionDays(logRetentionDays);
        return executor;
    }
}
