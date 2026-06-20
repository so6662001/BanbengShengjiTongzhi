package com.company.notify.common.enums;

import lombok.Getter;

/** 更新条目分类 */
@Getter
public enum ChangeCategory {
    ADD("新增"),
    OPTIMIZE("优化"),
    FIX("修复");

    private final String desc;
    ChangeCategory(String desc) { this.desc = desc; }
}
