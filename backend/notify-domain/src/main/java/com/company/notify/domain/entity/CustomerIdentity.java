package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.IdentityMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 客户身份映射：把外部身份值（license/租户/设备）映射到内部 customerId。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_identity")
public class CustomerIdentity extends BaseEntity {
    private Long customerId;
    private Long productId;
    private IdentityMode identityType;
    private String identityValue;
}
