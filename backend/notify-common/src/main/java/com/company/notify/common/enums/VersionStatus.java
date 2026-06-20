package com.company.notify.common.enums;

import lombok.Getter;

import java.util.Set;

/**
 * 版本状态机：DRAFT→REVIEW→APPROVED→SCHEDULED→PUBLISHED，REVOKED 为发布后撤回。
 * 通过 canTransferTo 显式约束合法流转，非法流转抛业务异常。
 */
@Getter
public enum VersionStatus {
    DRAFT("草稿"),
    REVIEW("审批中"),
    APPROVED("已审批"),
    SCHEDULED("定时待发"),
    PUBLISHED("已发布"),
    REVOKED("已撤回");

    private final String desc;
    VersionStatus(String desc) { this.desc = desc; }

    public boolean canTransferTo(VersionStatus target) {
        return switch (this) {
            case DRAFT -> Set.of(REVIEW).contains(target);
            case REVIEW -> Set.of(APPROVED, DRAFT).contains(target);          // 通过 or 驳回退回
            case APPROVED -> Set.of(SCHEDULED, PUBLISHED, DRAFT).contains(target);
            case SCHEDULED -> Set.of(PUBLISHED, REVOKED, DRAFT).contains(target);
            case PUBLISHED -> Set.of(REVOKED).contains(target);
            case REVOKED -> false;
        };
    }
}
