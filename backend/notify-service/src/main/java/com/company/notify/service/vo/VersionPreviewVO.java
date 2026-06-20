package com.company.notify.service.vo;

import com.company.notify.common.enums.ChangeCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 版本弹窗预览聚合：版本说明 + 按分类分组的更新条目。客户端弹窗与更新日志页共用。 */
@Data
public class VersionPreviewVO {
    private Long versionId;
    private Long productId;
    private String productName;
    private String versionNo;
    private String description;
    private LocalDateTime planReleaseTime;
    private List<Item> items;

    @Data
    public static class Item {
        private ChangeCategory category;
        private String categoryDesc;
        private String title;
        private String content;
    }
}
