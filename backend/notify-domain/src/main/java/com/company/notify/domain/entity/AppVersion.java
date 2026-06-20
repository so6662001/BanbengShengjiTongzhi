package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.ReleaseType;
import com.company.notify.common.enums.VersionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 版本。description 为版本说明（整体概述），展示在弹窗顶部与更新日志页。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_version")
public class AppVersion extends BaseEntity {
    private Long productId;
    private String versionNo;
    private String description;
    private ReleaseType releaseType;
    private LocalDateTime planReleaseTime;
    private VersionStatus status;
}
