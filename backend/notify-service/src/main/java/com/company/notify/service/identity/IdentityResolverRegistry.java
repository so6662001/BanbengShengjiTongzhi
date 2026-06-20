package com.company.notify.service.identity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.IdentityMode;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.Product;
import com.company.notify.domain.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** 按产品 identityMode 选择解析器并完成「外部身份 → customerId」解析。 */
@Component
@RequiredArgsConstructor
public class IdentityResolverRegistry {

    private final List<CustomerIdentityResolver> resolvers;
    private final ProductMapper productMapper;

    public Long resolve(String productCode, String identityValue) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getCode, productCode).last("limit 1"));
        if (product == null) {
            throw BizException.of(ErrorCode.IDENTITY_RESOLVE_FAIL, "产品不存在: " + productCode);
        }
        IdentityMode mode = product.getIdentityMode();
        CustomerIdentityResolver resolver = resolvers.stream()
                .filter(r -> r.supports(mode))
                .findFirst()
                .orElseThrow(() -> BizException.of(ErrorCode.IDENTITY_RESOLVE_FAIL, "无可用身份解析器"));
        Long customerId = resolver.resolve(product.getId(), mode, identityValue);
        if (customerId == null) {
            throw BizException.of(ErrorCode.CLIENT_AUTH_FAIL, "客户身份无法识别");
        }
        return customerId;
    }

    public Product getProductByCode(String productCode) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getCode, productCode).last("limit 1"));
        if (product == null) {
            throw BizException.of(ErrorCode.DATA_NOT_FOUND, "产品不存在: " + productCode);
        }
        return product;
    }
}
