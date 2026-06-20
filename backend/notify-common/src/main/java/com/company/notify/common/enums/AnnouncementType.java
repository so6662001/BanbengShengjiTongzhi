package com.company.notify.common.enums;

import lombok.Getter;

/** 公告类型 */
@Getter
public enum AnnouncementType {
    PRE_NOTICE("事前预告"),
    UPDATE("正式更新");

    private final String desc;
    AnnouncementType(String desc) { this.desc = desc; }
}
