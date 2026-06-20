package com.company.notify.common.enums;

import lombok.Getter;

/** 已读状态 */
@Getter
public enum ReadStatus {
    UNREAD("未读"),
    READ("已读");

    private final String desc;
    ReadStatus(String desc) { this.desc = desc; }
}
