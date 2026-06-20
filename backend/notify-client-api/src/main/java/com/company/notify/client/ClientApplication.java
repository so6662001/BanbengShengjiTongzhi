package com.company.notify.client;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 客户端接口应用。面向终端客户，高并发，独立部署，需鉴权与限流。
 */
@SpringBootApplication(scanBasePackages = "com.company.notify")
@MapperScan("com.company.notify.domain.mapper")
public class ClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}
