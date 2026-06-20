package com.company.notify.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 上下文加载冒烟测试：验证全部 Bean 装配成功，
 * 重点确认审批引擎与版本/发布服务之间的循环依赖已被 @Lazy 正确打破。
 */
@SpringBootTest
@ActiveProfiles("test")
class AdminApplicationTests {

    @Test
    void contextLoads() {
    }
}
