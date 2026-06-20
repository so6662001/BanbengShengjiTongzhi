package com.company.notify.service.vo;

import com.company.notify.common.enums.AnnouncementType;
import lombok.Data;

/** 客户端未读公告（弹窗）数据。 */
@Data
public class UnreadAnnouncementVO {
    private Long announcementId;
    private AnnouncementType type;
    private String typeDesc;
    private String title;
    private String jumpUrl;
    /** 版本说明 + 分类条目 */
    private VersionPreviewVO detail;
    /** 预告类是否强提醒（需阅读后才可关闭） */
    private boolean forceRead;
}
