package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户-产品-服务器 三元归属关系。
 * currentVersion 记录该客户该产品当前所在版本，用于计算升级完成率。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_product")
public class CustomerProduct extends BaseEntity {
    private Long customerId;
    private Long productId;
    private Long serverNodeId;
    private String currentVersion;
}
