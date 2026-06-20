package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.CommonStatus;
import com.company.notify.common.enums.IdentityMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 产品 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {
    private String name;
    private String code;
    private String icon;
    /** 客户身份模式，决定客户端身份解析方式 */
    private IdentityMode identityMode;
    private CommonStatus status;
}
