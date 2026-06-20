package com.company.notify.common.enums;

import lombok.Getter;

/** 单客户单渠道发送状态 */
@Getter
public enum SendStatus {
    PENDING("待发送"),
    SENT("已送达"),
    FAIL("发送失败");

    private final String desc;
    SendStatus(String desc) { this.desc = desc; }
}
