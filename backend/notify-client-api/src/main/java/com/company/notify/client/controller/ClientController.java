package com.company.notify.client.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.notify.common.api.PageResult;
import com.company.notify.common.api.Result;
import com.company.notify.common.enums.ChangeCategory;
import com.company.notify.service.ClientService;
import com.company.notify.service.vo.UnreadAnnouncementVO;
import com.company.notify.service.vo.VersionPreviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户端接口。身份通过请求头 X-Product-Code + X-Identity 传入（生产应叠加签名校验与限流）。
 */
@Tag(name = "客户端接口")
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "启动拉取未读公告/弹窗")
    @GetMapping("/announcements/unread")
    public Result<List<UnreadAnnouncementVO>> unread(@RequestHeader("X-Product-Code") String productCode,
                                                     @RequestHeader("X-Identity") String identity) {
        return Result.success(clientService.unread(productCode, identity));
    }

    @Operation(summary = "已读状态上报")
    @PostMapping("/announcements/{id}/read")
    public Result<Void> read(@PathVariable Long id,
                             @RequestHeader("X-Product-Code") String productCode,
                             @RequestHeader("X-Identity") String identity) {
        clientService.markRead(id, productCode, identity);
        return Result.success();
    }

    @Operation(summary = "历史更新日志查询")
    @GetMapping("/changelog")
    public Result<PageResult<VersionPreviewVO>> changelog(@RequestHeader("X-Product-Code") String productCode,
                                                          @RequestParam(required = false) ChangeCategory category,
                                                          @RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "10") long size) {
        IPage<VersionPreviewVO> page = clientService.changelog(productCode, category, current, size);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), current, size));
    }

    @Operation(summary = "提交反馈")
    @PostMapping("/feedback")
    public Result<Void> feedback(@RequestHeader("X-Product-Code") String productCode,
                                 @RequestHeader("X-Identity") String identity,
                                 @RequestBody FeedbackReq req) {
        clientService.feedback(req.getAnnouncementId(), productCode, identity, req.getRating(), req.getContent());
        return Result.success();
    }

    @Data
    public static class FeedbackReq {
        private Long announcementId;
        private Integer rating;
        private String content;
    }
}
