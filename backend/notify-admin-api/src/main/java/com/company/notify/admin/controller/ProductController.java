package com.company.notify.admin.controller;

import com.company.notify.common.api.Result;
import com.company.notify.domain.entity.Product;
import com.company.notify.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "产品管理")
@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "产品列表")
    @GetMapping
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }

    @Operation(summary = "新增产品")
    @PostMapping
    public Result<Long> create(@RequestBody Product product) {
        return Result.success(productService.create(product));
    }

    @Operation(summary = "更新产品")
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.success();
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
}
