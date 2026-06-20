package com.company.notify.common.enums;

import lombok.Getter;

/** 服务器环境 */
@Getter
public enum Env {
    PROD("生产"),
    TEST("测试");

    private final String desc;
    Env(String desc) { this.desc = desc; }
}
