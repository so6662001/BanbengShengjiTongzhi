package com.company.notify.common.enums;

import lombok.Getter;

/** 投递任务状态 */
@Getter
public enum DeliveryTaskStatus {
    PENDING("待执行"),
    RUNNING("执行中"),
    DONE("已完成"),
    CANCELED("已取消");

    private final String desc;
    DeliveryTaskStatus(String desc) { this.desc = desc; }
}
