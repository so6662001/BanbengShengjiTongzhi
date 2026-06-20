package com.company.notify.common.enums;

import lombok.Getter;

/** 审批关联的业务类型 */
@Getter
public enum ApprovalBizType {
    VERSION("版本文案"),
    RELEASE_PLAN("发布计划");

    private final String desc;
    ApprovalBizType(String desc) { this.desc = desc; }
}
