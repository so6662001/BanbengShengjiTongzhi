package com.company.notify.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.notify.domain.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
