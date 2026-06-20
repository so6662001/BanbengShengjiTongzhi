package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.AnnouncementType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 公告（预告 / 正式更新），由版本内容生成。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcement")
public class Announcement extends BaseEntity {
    private Long versionId;
    private AnnouncementType type;
    private String title;
    private String popupContent;
    private String jumpUrl;
}
