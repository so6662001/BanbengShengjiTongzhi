package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 服务器-产品 多对多关系。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("server_product")
public class ServerProduct extends BaseEntity {
    private Long serverNodeId;
    private Long productId;
}
