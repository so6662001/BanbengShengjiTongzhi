package com.company.notify.common.enums;

import lombok.Getter;

/** 推送渠道。一期实现 IN_APP / WECOM，其余预留。 */
@Getter
public enum Channel {
    IN_APP("站内信", true),
    WECOM("企业微信", true),
    EMAIL("邮件", false),
    SMS("短信", false),
    DINGTALK("钉钉", false);

    private final String desc;
    /** 一期是否已启用 */
    private final boolean enabledPhase1;

    Channel(String desc, boolean enabledPhase1) {
        this.desc = desc;
        this.enabledPhase1 = enabledPhase1;
    }
}
