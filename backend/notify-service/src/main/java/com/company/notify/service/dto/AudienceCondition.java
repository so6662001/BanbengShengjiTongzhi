package com.company.notify.service.dto;

import com.company.notify.common.enums.PackageLevel;
import lombok.Data;

import java.util.List;

/**
 * 人群圈选条件：套餐 ∩ 行业 ∩ 产品 ∩ 服务器 ∩ 版本（交集）。
 * versionBelow 不为空时，仅命中“当前版本低于该版本”的待升级客户。
 */
@Data
public class AudienceCondition {
    /** 必填：使用产品（一个圈选针对一个产品的客户归属） */
    private Long productId;
    private List<PackageLevel> packages;
    private List<String> industries;
    private List<Long> serverNodeIds;
    /** 仅命中 current_version 低于该版本的客户（待升级） */
    private String versionBelow;

    public boolean isEmpty() {
        return productId == null
                && (packages == null || packages.isEmpty())
                && (industries == null || industries.isEmpty())
                && (serverNodeIds == null || serverNodeIds.isEmpty())
                && (versionBelow == null || versionBelow.isBlank());
    }
}
