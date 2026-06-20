package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.CommonStatus;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.Product;
import com.company.notify.domain.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 产品管理 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public List<Product> list() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getId));
    }

    public Product getById(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw BizException.of(ErrorCode.DATA_NOT_FOUND, "产品不存在");
        }
        return p;
    }

    public Long create(Product product) {
        ensureCodeUnique(product.getCode(), null);
        if (product.getStatus() == null) {
            product.setStatus(CommonStatus.ENABLED);
        }
        productMapper.insert(product);
        return product.getId();
    }

    public void update(Product product) {
        getById(product.getId());
        ensureCodeUnique(product.getCode(), product.getId());
        productMapper.updateById(product);
    }

    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    private void ensureCodeUnique(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            return;
        }
        Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCode, code)
                .ne(excludeId != null, Product::getId, excludeId));
        if (count != null && count > 0) {
            throw BizException.of(ErrorCode.DATA_CONFLICT, "产品编码已存在");
        }
    }
}
