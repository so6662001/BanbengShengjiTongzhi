package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.AnnouncementType;
import com.company.notify.common.enums.ApprovalBizType;
import com.company.notify.common.enums.Channel;
import com.company.notify.common.enums.VersionStatus;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.Announcement;
import com.company.notify.domain.entity.AppVersion;
import com.company.notify.domain.entity.ReleasePlan;
import com.company.notify.domain.mapper.ReleasePlanMapper;
import com.company.notify.service.approval.ApprovalListener;
import com.company.notify.service.approval.ApprovalService;
import com.company.notify.service.support.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 发布计划：关联版本+人群+渠道+事前预告，提交审批、定时发布、撤回。
 * 实现 ApprovalListener 接收审批终态回调。
 */
@Service
@RequiredArgsConstructor
public class ReleasePlanService implements ApprovalListener {

    private final ReleasePlanMapper planMapper;
    private final AppVersionService appVersionService;
    private final AnnouncementService announcementService;
    private final DeliveryService deliveryService;
    @Lazy
    private final ApprovalService approvalService;

    public ReleasePlan getById(Long id) {
        ReleasePlan p = planMapper.selectById(id);
        if (p == null) {
            throw BizException.of(ErrorCode.DATA_NOT_FOUND, "发布计划不存在");
        }
        return p;
    }

    public List<ReleasePlan> list(VersionStatus status) {
        return planMapper.selectList(new LambdaQueryWrapper<ReleasePlan>()
                .eq(status != null, ReleasePlan::getStatus, status)
                .orderByDesc(ReleasePlan::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(ReleasePlan plan) {
        plan.setStatus(VersionStatus.DRAFT);
        if (plan.getScheduleEnabled() == null) {
            plan.setScheduleEnabled(true);
        }
        planMapper.insert(plan);
        return plan.getId();
    }

    /** 提交审批：按版本所属产品线加载审批流。 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long planId) {
        ReleasePlan plan = getById(planId);
        if (plan.getStatus() != VersionStatus.DRAFT) {
            throw BizException.of(ErrorCode.OPERATION_NOT_ALLOWED, "仅草稿计划可提交审批");
        }
        AppVersion version = appVersionService.getById(plan.getVersionId());
        approvalService.submit(ApprovalBizType.RELEASE_PLAN, planId, version.getProductId());
        plan.setStatus(VersionStatus.REVIEW);
        planMapper.updateById(plan);
    }

    @Override
    public boolean support(ApprovalBizType bizType) {
        return bizType == ApprovalBizType.RELEASE_PLAN;
    }

    /** 审批通过 → 进入定时待发。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onApproved(Long planId) {
        ReleasePlan plan = getById(planId);
        plan.setStatus(VersionStatus.SCHEDULED);
        planMapper.updateById(plan);
        // 版本同步进入定时待发
        appVersionService.transferStatus(plan.getVersionId(), VersionStatus.APPROVED);
        appVersionService.transferStatus(plan.getVersionId(), VersionStatus.SCHEDULED);
    }

    /** 审批驳回 → 退回草稿。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onRejected(Long planId, String comment) {
        ReleasePlan plan = getById(planId);
        plan.setStatus(VersionStatus.DRAFT);
        planMapper.updateById(plan);
    }

    /** 正式发布：生成更新公告并即时投递，版本与计划置为已发布。 */
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long planId) {
        ReleasePlan plan = getById(planId);
        if (plan.getStatus() != VersionStatus.SCHEDULED && plan.getStatus() != VersionStatus.APPROVED) {
            throw BizException.of(ErrorCode.RELEASE_PLAN_NOT_APPROVED);
        }
        Announcement ann = announcementService.getOrCreate(plan.getVersionId(), AnnouncementType.UPDATE);
        deliveryService.createAndDispatch(ann, plan.getAudienceId(), channelsOf(plan), "RELEASE");
        appVersionService.transferStatus(plan.getVersionId(), VersionStatus.PUBLISHED);
        plan.setStatus(VersionStatus.PUBLISHED);
        planMapper.updateById(plan);
    }

    /** 撤回/停推：取消未发送任务，版本与计划置为已撤回。 */
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long planId) {
        ReleasePlan plan = getById(planId);
        Announcement update = announcementService.getOrCreate(plan.getVersionId(), AnnouncementType.UPDATE);
        deliveryService.cancelByAnnouncement(update.getId());
        Announcement pre = announcementService.getOrCreate(plan.getVersionId(), AnnouncementType.PRE_NOTICE);
        deliveryService.cancelByAnnouncement(pre.getId());
        if (plan.getStatus() == VersionStatus.PUBLISHED || plan.getStatus() == VersionStatus.SCHEDULED) {
            appVersionService.transferStatus(plan.getVersionId(), VersionStatus.REVOKED);
        }
        plan.setStatus(VersionStatus.REVOKED);
        planMapper.updateById(plan);
    }

    /**
     * 由定时任务调用：为到达预告时间点(releaseTime - N 天)的计划生成预告投递。
     * 幂等键含预告天数，保证每个预告节点只发一次。
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateDuePreNotices(LocalDateTime now) {
        List<ReleasePlan> plans = planMapper.selectList(new LambdaQueryWrapper<ReleasePlan>()
                .eq(ReleasePlan::getStatus, VersionStatus.SCHEDULED)
                .eq(ReleasePlan::getScheduleEnabled, true)
                .isNotNull(ReleasePlan::getReleaseTime));
        for (ReleasePlan plan : plans) {
            List<Integer> days = JsonUtil.parseList(plan.getPreNotifyDays(), Integer.class);
            for (Integer d : days) {
                LocalDateTime noticeTime = plan.getReleaseTime().minusDays(d);
                if (noticeTime.isAfter(now)) {
                    continue; // 尚未到点
                }
                Announcement ann = announcementService.getOrCreate(plan.getVersionId(), AnnouncementType.PRE_NOTICE);
                deliveryService.createAndDispatch(ann, plan.getAudienceId(), channelsOf(plan), "PRE-" + d);
            }
        }
    }

    private List<Channel> channelsOf(ReleasePlan plan) {
        return JsonUtil.parseList(plan.getChannels(), Channel.class);
    }
}
