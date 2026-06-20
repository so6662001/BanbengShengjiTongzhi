package com.company.notify.admin.controller;

import com.company.notify.common.api.Result;
import com.company.notify.common.context.UserContext;
import com.company.notify.common.enums.ApprovalBizType;
import com.company.notify.domain.entity.ApprovalFlow;
import com.company.notify.domain.entity.ApprovalFlowNode;
import com.company.notify.domain.entity.ApprovalRecord;
import com.company.notify.service.approval.ApprovalConfigService;
import com.company.notify.service.approval.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "审批中心")
@RestController
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ApprovalConfigService approvalConfigService;

    @Operation(summary = "待我审批")
    @GetMapping("/todo")
    public Result<List<ApprovalRecord>> todo() {
        return Result.success(approvalService.todo(UserContext.getUserId()));
    }

    @Operation(summary = "审批留痕记录")
    @GetMapping("/records")
    public Result<List<ApprovalRecord>> records(@RequestParam ApprovalBizType bizType, @RequestParam Long bizId) {
        return Result.success(approvalService.records(bizType, bizId));
    }

    @Operation(summary = "处理审批（通过/驳回）")
    @PostMapping("/{recordId}")
    public Result<Void> approve(@PathVariable Long recordId, @RequestBody ApproveReq req) {
        approvalService.approve(recordId, UserContext.getUserId(), req.isPass(), req.getComment());
        return Result.success();
    }

    @Operation(summary = "审批流配置列表")
    @GetMapping("/flow")
    public Result<List<ApprovalFlow>> flows() {
        return Result.success(approvalConfigService.listFlows());
    }

    @Operation(summary = "审批流节点")
    @GetMapping("/flow/{flowId}/nodes")
    public Result<List<ApprovalFlowNode>> nodes(@PathVariable Long flowId) {
        return Result.success(approvalConfigService.nodesOf(flowId));
    }

    @Operation(summary = "保存审批流（含节点）")
    @PostMapping("/flow")
    public Result<Long> saveFlow(@RequestBody FlowSaveReq req) {
        return Result.success(approvalConfigService.saveFlow(req.getFlow(), req.getNodes()));
    }

    @Data
    public static class ApproveReq {
        private boolean pass;
        private String comment;
    }

    @Data
    public static class FlowSaveReq {
        private ApprovalFlow flow;
        private List<ApprovalFlowNode> nodes;
    }
}
