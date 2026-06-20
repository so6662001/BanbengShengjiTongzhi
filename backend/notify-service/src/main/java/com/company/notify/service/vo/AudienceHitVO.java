package com.company.notify.service.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/** 圈选命中结果：命中数 + 按行业分布 + 命中客户 id（供后续投递）。 */
@Data
public class AudienceHitVO {
    private long hitCount;
    private Map<String, Long> industryDistribution;
    private List<Long> customerIds;
}
