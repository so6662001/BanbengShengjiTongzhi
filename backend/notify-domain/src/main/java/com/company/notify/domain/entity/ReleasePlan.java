package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.VersionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 发布计划：关联版本+人群+渠道+事前预告天数+发布时间。
 * channels / preNotifyDays 以 JSON 字符串存储。status 复用版本状态机表达计划阶段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("release_plan")
public class ReleasePlan extends BaseEntity {
    private Long versionId;
    private Long audienceId;
    /** JSON 数组，如 ["IN_APP","WECOM"] */
    private String channels;
    /** JSON 数组，如 [3,1]（升级前 N 天预告） */
    private String preNotifyDays;
    private LocalDateTime releaseTime;
    private Boolean scheduleEnabled;
    private VersionStatus status;
}
