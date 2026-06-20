package com.company.notify.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.notify.common.api.PageResult;
import com.company.notify.common.api.Result;
import com.company.notify.domain.entity.Customer;
import com.company.notify.domain.entity.CustomerProduct;
import com.company.notify.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户管理")
@RestController
@RequestMapping("/admin/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "客户分页")
    @GetMapping
    public Result<PageResult<Customer>> page(@RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size) {
        IPage<Customer> page = customerService.page(keyword, current, size);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), current, size));
    }

    @Operation(summary = "新增客户")
    @PostMapping
    public Result<Long> create(@RequestBody Customer customer) {
        return Result.success(customerService.create(customer));
    }

    @Operation(summary = "更新客户")
    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        customerService.update(customer);
        return Result.success();
    }

    @Operation(summary = "客户产品归属")
    @GetMapping("/{id}/products")
    public Result<List<CustomerProduct>> products(@PathVariable Long id) {
        return Result.success(customerService.productsOf(id));
    }

    @Operation(summary = "维护客户产品归属（服务器+当前版本）")
    @PostMapping("/binding")
    public Result<Void> upsertBinding(@RequestBody CustomerProduct binding) {
        customerService.upsertProductBinding(binding);
        return Result.success();
    }
}
