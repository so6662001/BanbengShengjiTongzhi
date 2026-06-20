package com.company.notify.common.enums;

import lombok.Getter;

/** 节点审批模式 */
@Getter
public enum ApproveMode {
    SINGLE("单人审批"),
    AND("会签（全部通过）"),
    OR("或签（一人通过即可）");

    private final String desc;
    ApproveMode(String desc) { this.desc = desc; }
}
