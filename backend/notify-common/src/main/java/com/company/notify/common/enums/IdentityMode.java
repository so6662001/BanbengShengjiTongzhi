package com.company.notify.common.enums;

import lombok.Getter;

/** 客户身份模式（按产品配置，决定客户端如何解析为内部 customer_id） */
@Getter
public enum IdentityMode {
    LICENSE("授权码"),
    TENANT("租户账号"),
    DEVICE("设备实例");

    private final String desc;
    IdentityMode(String desc) { this.desc = desc; }
}
