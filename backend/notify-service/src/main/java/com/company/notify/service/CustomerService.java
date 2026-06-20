package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.notify.common.enums.CommonStatus;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.Customer;
import com.company.notify.domain.entity.CustomerProduct;
import com.company.notify.domain.mapper.CustomerMapper;
import com.company.notify.domain.mapper.CustomerProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 客户与“客户-产品-服务器”归属管理 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerProductMapper customerProductMapper;

    public IPage<Customer> page(String keyword, long current, long size) {
        return customerMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Customer>()
                        .like(keyword != null && !keyword.isBlank(), Customer::getName, keyword)
                        .orderByDesc(Customer::getId));
    }

    public Customer getById(Long id) {
        Customer c = customerMapper.selectById(id);
        if (c == null) {
            throw BizException.of(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        return c;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(Customer customer) {
        if (customer.getStatus() == null) {
            customer.setStatus(CommonStatus.ENABLED);
        }
        customerMapper.insert(customer);
        return customer.getId();
    }

    public void update(Customer customer) {
        getById(customer.getId());
        customerMapper.updateById(customer);
    }

    public List<CustomerProduct> productsOf(Long customerId) {
        return customerProductMapper.selectList(new LambdaQueryWrapper<CustomerProduct>()
                .eq(CustomerProduct::getCustomerId, customerId));
    }

    /** 维护客户某产品的归属服务器与当前版本（用于升级完成率统计）。 */
    @Transactional(rollbackFor = Exception.class)
    public void upsertProductBinding(CustomerProduct binding) {
        CustomerProduct exist = customerProductMapper.selectOne(new LambdaQueryWrapper<CustomerProduct>()
                .eq(CustomerProduct::getCustomerId, binding.getCustomerId())
                .eq(CustomerProduct::getProductId, binding.getProductId()));
        if (exist == null) {
            customerProductMapper.insert(binding);
        } else {
            exist.setServerNodeId(binding.getServerNodeId());
            exist.setCurrentVersion(binding.getCurrentVersion());
            customerProductMapper.updateById(exist);
        }
    }
}
