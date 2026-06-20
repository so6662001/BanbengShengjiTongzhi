package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.ChangeCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 更新条目（新增/优化/修复） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("version_change_item")
public class VersionChangeItem extends BaseEntity {
    private Long versionId;
    private ChangeCategory category;
    private String title;
    private String content;
    private Integer sort;
}
