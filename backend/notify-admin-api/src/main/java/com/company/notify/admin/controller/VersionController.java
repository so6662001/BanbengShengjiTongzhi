package com.company.notify.admin.controller;

import com.company.notify.common.api.PageResult;
import com.company.notify.common.api.Result;
import com.company.notify.common.enums.VersionStatus;
import com.company.notify.domain.entity.AppVersion;
import com.company.notify.domain.entity.VersionChangeItem;
import com.company.notify.service.AppVersionService;
import com.company.notify.service.dto.VersionSaveDTO;
import com.company.notify.service.vo.VersionPreviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "版本管理")
@RestController
@RequestMapping("/admin/version")
@RequiredArgsConstructor
public class VersionController {

    private final AppVersionService appVersionService;

    @Operation(summary = "版本分页")
    @GetMapping
    public Result<PageResult<AppVersion>> page(@RequestParam(required = false) Long productId,
                                               @RequestParam(required = false) VersionStatus status,
                                               @RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size) {
        IPage<AppVersion> page = appVersionService.page(productId, status, current, size);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), current, size));
    }

    @Operation(summary = "保存版本（含更新条目）")
    @PostMapping
    public Result<Long> save(@RequestBody @Valid VersionSaveDTO dto) {
        return Result.success(appVersionService.save(dto));
    }

    @Operation(summary = "版本更新条目")
    @GetMapping("/{id}/items")
    public Result<List<VersionChangeItem>> items(@PathVariable Long id) {
        return Result.success(appVersionService.items(id));
    }

    @Operation(summary = "弹窗预览聚合")
    @GetMapping("/{id}/preview")
    public Result<VersionPreviewVO> preview(@PathVariable Long id) {
        return Result.success(appVersionService.preview(id));
    }

    @Operation(summary = "提交文案审批")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        appVersionService.submitForApproval(id);
        return Result.success();
    }
}
