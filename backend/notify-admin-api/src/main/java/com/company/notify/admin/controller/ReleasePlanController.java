package com.company.notify.admin.controller;

import com.company.notify.common.api.Result;
import com.company.notify.common.enums.VersionStatus;
import com.company.notify.domain.entity.ReleasePlan;
import com.company.notify.service.ReleasePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "发布计划")
@RestController
@RequestMapping("/admin/release-plan")
@RequiredArgsConstructor
public class ReleasePlanController {

    private final ReleasePlanService releasePlanService;

    @Operation(summary = "计划列表")
    @GetMapping
    public Result<List<ReleasePlan>> list(@RequestParam(required = false) VersionStatus status) {
        return Result.success(releasePlanService.list(status));
    }

    @Operation(summary = "创建发布计划")
    @PostMapping
    public Result<Long> create(@RequestBody ReleasePlan plan) {
        return Result.success(releasePlanService.create(plan));
    }

    @Operation(summary = "提交审批")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        releasePlanService.submit(id);
        return Result.success();
    }

    @Operation(summary = "正式发布")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        releasePlanService.publish(id);
        return Result.success();
    }

    @Operation(summary = "撤回/停推")
    @PostMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id) {
        releasePlanService.revoke(id);
        return Result.success();
    }
}
