package com.company.notify.service.identity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.IdentityMode;
import com.company.notify.domain.entity.CustomerIdentity;
import com.company.notify.domain.mapper.CustomerIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 默认基于 customer_identity 表的解析器，覆盖 LICENSE/TENANT/DEVICE 三种模式。
 * 如某模式需特殊解析（如调用外部租户中心），可新增实现并提高其优先级。
 */
@Component
@RequiredArgsConstructor
public class DbIdentityResolver implements CustomerIdentityResolver {

    private final CustomerIdentityMapper identityMapper;

    @Override
    public boolean supports(IdentityMode mode) {
        return true;
    }

    @Override
    public Long resolve(Long productId, IdentityMode mode, String identityValue) {
        if (identityValue == null || identityValue.isBlank()) {
            return null;
        }
        CustomerIdentity ci = identityMapper.selectOne(new LambdaQueryWrapper<CustomerIdentity>()
                .eq(CustomerIdentity::getProductId, productId)
                .eq(CustomerIdentity::getIdentityType, mode)
                .eq(CustomerIdentity::getIdentityValue, identityValue)
                .last("limit 1"));
        return ci == null ? null : ci.getCustomerId();
    }
}
