package com.company.notify.common.enums;

import lombok.Getter;

/** 客户套餐 */
@Getter
public enum PackageLevel {
    FLAGSHIP("旗舰版"),
    PRO("专业版"),
    STANDARD("标准版"),
    BASIC("基础版");

    private final String desc;
    PackageLevel(String desc) { this.desc = desc; }
}
