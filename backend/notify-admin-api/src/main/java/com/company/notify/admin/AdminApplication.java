package com.company.notify.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 后台管理端应用。聚合 service + push + job：内含审批、推送编排与预告定时。
 */
@SpringBootApplication(scanBasePackages = "com.company.notify")
@MapperScan("com.company.notify.domain.mapper")
@EnableScheduling
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
