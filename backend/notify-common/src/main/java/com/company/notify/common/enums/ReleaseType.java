package com.company.notify.common.enums;

import lombok.Getter;

/** 发布类型 */
@Getter
public enum ReleaseType {
    RELEASE("正式发布"),
    GRAY("灰度发布");

    private final String desc;
    ReleaseType(String desc) { this.desc = desc; }
}
