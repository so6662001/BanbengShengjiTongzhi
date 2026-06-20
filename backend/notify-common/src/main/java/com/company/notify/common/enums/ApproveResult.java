package com.company.notify.common.enums;

import lombok.Getter;

/** 审批结果 */
@Getter
public enum ApproveResult {
    PENDING("待处理"),
    PASS("通过"),
    REJECT("驳回");

    private final String desc;
    ApproveResult(String desc) { this.desc = desc; }
}
