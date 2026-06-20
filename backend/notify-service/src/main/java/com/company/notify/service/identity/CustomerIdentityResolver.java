package com.company.notify.service.identity;

import com.company.notify.common.enums.IdentityMode;

/**
 * 客户身份解析器（可插拔）。按产品配置的 identityMode 选择实现，
 * 把客户端上报的外部身份值解析为内部 customerId。新增模式只需新增实现并声明 supports。
 */
public interface CustomerIdentityResolver {

    boolean supports(IdentityMode mode);

    /** 解析失败返回 null。 */
    Long resolve(Long productId, IdentityMode mode, String identityValue);
}
