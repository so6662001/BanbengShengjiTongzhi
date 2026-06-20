package com.company.notify.admin.controller;

import com.company.notify.common.api.Result;
import com.company.notify.domain.entity.ServerNode;
import com.company.notify.service.ServerNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "服务器管理")
@RestController
@RequestMapping("/admin/server")
@RequiredArgsConstructor
public class ServerController {

    private final ServerNodeService serverNodeService;

    @Operation(summary = "服务器列表")
    @GetMapping
    public Result<List<ServerNode>> list() {
        return Result.success(serverNodeService.list());
    }

    @Operation(summary = "某服务器部署的产品ID")
    @GetMapping("/{id}/products")
    public Result<List<Long>> products(@PathVariable Long id) {
        return Result.success(serverNodeService.productIdsOfServer(id));
    }

    @Operation(summary = "新增服务器（含部署产品，可多个）")
    @PostMapping
    public Result<Long> create(@RequestBody ServerSaveReq req) {
        return Result.success(serverNodeService.create(req.getServer(), req.getProductIds()));
    }

    @Operation(summary = "更新服务器")
    @PutMapping
    public Result<Void> update(@RequestBody ServerSaveReq req) {
        serverNodeService.update(req.getServer(), req.getProductIds());
        return Result.success();
    }

    @Data
    public static class ServerSaveReq {
        private ServerNode server;
        private List<Long> productIds;
    }
}
