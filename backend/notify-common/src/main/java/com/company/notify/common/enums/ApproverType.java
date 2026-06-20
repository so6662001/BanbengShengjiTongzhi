package com.company.notify.common.enums;

import lombok.Getter;

/** 审批人指派类型 */
@Getter
public enum ApproverType {
    USER("指定人"),
    ROLE("按角色");

    private final String desc;
    ApproverType(String desc) { this.desc = desc; }
}
