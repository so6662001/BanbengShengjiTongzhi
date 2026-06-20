package com.company.notify.admin.controller;

import com.company.notify.common.api.Result;
import com.company.notify.service.DashboardService;
import com.company.notify.service.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据看板")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "看板总览")
    @GetMapping("/overview")
    public Result<DashboardVO.Overview> overview(@RequestParam Long versionId) {
        return Result.success(dashboardService.overview(versionId));
    }

    @Operation(summary = "触达漏斗")
    @GetMapping("/funnel")
    public Result<DashboardVO.Funnel> funnel(@RequestParam Long versionId) {
        return Result.success(dashboardService.funnel(versionId));
    }

    @Operation(summary = "渠道效果")
    @GetMapping("/channel")
    public Result<List<DashboardVO.ChannelStat>> channel(@RequestParam Long versionId) {
        return Result.success(dashboardService.channelStats(versionId));
    }

    @Operation(summary = "按服务器升级完成率")
    @GetMapping("/server-upgrade")
    public Result<List<DashboardVO.ServerUpgrade>> serverUpgrade(@RequestParam Long versionId) {
        return Result.success(dashboardService.serverUpgrade(versionId));
    }
}
