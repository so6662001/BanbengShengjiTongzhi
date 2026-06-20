package com.company.notify.common.enums;

import lombok.Getter;

/** 通用启用状态 */
@Getter
public enum CommonStatus {
    ENABLED("启用"),
    DISABLED("停用");

    private final String desc;
    CommonStatus(String desc) { this.desc = desc; }
}
