package com.company.notify.admin.controller;

import com.company.notify.common.api.Result;
import com.company.notify.domain.entity.Audience;
import com.company.notify.service.AudienceService;
import com.company.notify.service.dto.AudienceCondition;
import com.company.notify.service.vo.AudienceHitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户分层圈选")
@RestController
@RequestMapping("/admin/audience")
@RequiredArgsConstructor
public class AudienceController {

    private final AudienceService audienceService;

    @Operation(summary = "圈选预览（命中数+分布）")
    @PostMapping("/preview")
    public Result<AudienceHitVO> preview(@RequestBody AudienceCondition cond) {
        return Result.success(audienceService.hit(cond));
    }

    @Operation(summary = "保存人群模板")
    @PostMapping
    public Result<Long> save(@RequestBody SaveReq req) {
        return Result.success(audienceService.save(req.getName(), req.getCondition()));
    }

    @Operation(summary = "人群模板列表")
    @GetMapping
    public Result<List<Audience>> list() {
        return Result.success(audienceService.list());
    }

    @Data
    public static class SaveReq {
        private String name;
        private AudienceCondition condition;
    }
}
