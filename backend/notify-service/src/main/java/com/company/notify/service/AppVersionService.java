package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.notify.common.enums.ApprovalBizType;
import com.company.notify.common.enums.ReleaseType;
import com.company.notify.common.enums.VersionStatus;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.AppVersion;
import com.company.notify.domain.entity.Product;
import com.company.notify.domain.entity.VersionChangeItem;
import com.company.notify.domain.mapper.AppVersionMapper;
import com.company.notify.domain.mapper.ProductMapper;
import com.company.notify.domain.mapper.VersionChangeItemMapper;
import com.company.notify.service.approval.ApprovalListener;
import com.company.notify.service.approval.ApprovalService;
import com.company.notify.service.dto.VersionSaveDTO;
import com.company.notify.service.vo.VersionPreviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** 版本管理：多产品多版本、更新条目结构化、版本状态机、弹窗预览、文案审批。 */
@Service
@RequiredArgsConstructor
public class AppVersionService implements ApprovalListener {

    private final AppVersionMapper appVersionMapper;
    private final VersionChangeItemMapper itemMapper;
    private final ProductMapper productMapper;
    @Lazy
    private final ApprovalService approvalService;

    public IPage<AppVersion> page(Long productId, VersionStatus status, long current, long size) {
        return appVersionMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<AppVersion>()
                        .eq(productId != null, AppVersion::getProductId, productId)
                        .eq(status != null, AppVersion::getStatus, status)
                        .orderByDesc(AppVersion::getId));
    }

    public AppVersion getById(Long id) {
        AppVersion v = appVersionMapper.selectById(id);
        if (v == null) {
            throw BizException.of(ErrorCode.DATA_NOT_FOUND, "版本不存在");
        }
        return v;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long save(VersionSaveDTO dto) {
        AppVersion version;
        if (dto.getId() == null) {
            version = new AppVersion();
            version.setStatus(VersionStatus.DRAFT);
        } else {
            version = getById(dto.getId());
            if (version.getStatus() != VersionStatus.DRAFT) {
                throw BizException.of(ErrorCode.OPERATION_NOT_ALLOWED, "仅草稿状态版本可编辑");
            }
        }
        ensureVersionNoUnique(dto.getProductId(), dto.getVersionNo(), dto.getId());
        version.setProductId(dto.getProductId());
        version.setVersionNo(dto.getVersionNo());
        version.setDescription(dto.getDescription());
        version.setReleaseType(dto.getReleaseType() == null ? ReleaseType.RELEASE : dto.getReleaseType());
        version.setPlanReleaseTime(dto.getPlanReleaseTime());

        if (dto.getId() == null) {
            appVersionMapper.insert(version);
        } else {
            appVersionMapper.updateById(version);
            itemMapper.delete(new LambdaQueryWrapper<VersionChangeItem>()
                    .eq(VersionChangeItem::getVersionId, version.getId()));
        }
        saveItems(version.getId(), dto.getItems());
        return version.getId();
    }

    private void saveItems(Long versionId, List<VersionSaveDTO.ChangeItemDTO> items) {
        if (items == null) {
            return;
        }
        int idx = 0;
        for (VersionSaveDTO.ChangeItemDTO it : items) {
            VersionChangeItem entity = new VersionChangeItem();
            entity.setVersionId(versionId);
            entity.setCategory(it.getCategory());
            entity.setTitle(it.getTitle());
            entity.setContent(it.getContent());
            entity.setSort(it.getSort() == null ? idx++ : it.getSort());
            itemMapper.insert(entity);
        }
    }

    public List<VersionChangeItem> items(Long versionId) {
        return itemMapper.selectList(new LambdaQueryWrapper<VersionChangeItem>()
                .eq(VersionChangeItem::getVersionId, versionId)
                .orderByAsc(VersionChangeItem::getSort));
    }

    /** 状态机流转。非法流转抛业务异常。 */
    public void transferStatus(Long versionId, VersionStatus target) {
        AppVersion v = getById(versionId);
        if (!v.getStatus().canTransferTo(target)) {
            throw BizException.of(ErrorCode.VERSION_STATUS_ILLEGAL,
                    "版本状态不能从 " + v.getStatus() + " 流转到 " + target);
        }
        v.setStatus(target);
        appVersionMapper.updateById(v);
    }

    /** 尝试流转，非法则跳过并返回 false（用于发布计划审批联动版本状态，避免抛异常中断主流程）。 */
    public boolean tryTransfer(Long versionId, VersionStatus target) {
        AppVersion v = getById(versionId);
        if (v.getStatus() == target) {
            return true;
        }
        if (!v.getStatus().canTransferTo(target)) {
            return false;
        }
        v.setStatus(target);
        appVersionMapper.updateById(v);
        return true;
    }

    /** 弹窗预览聚合（版本说明 + 分类条目），客户端与日志页共用。 */
    public VersionPreviewVO preview(Long versionId) {
        AppVersion v = getById(versionId);
        VersionPreviewVO vo = new VersionPreviewVO();
        vo.setVersionId(v.getId());
        vo.setProductId(v.getProductId());
        Product p = productMapper.selectById(v.getProductId());
        vo.setProductName(p == null ? null : p.getName());
        vo.setVersionNo(v.getVersionNo());
        vo.setDescription(v.getDescription());
        vo.setPlanReleaseTime(v.getPlanReleaseTime());
        List<VersionPreviewVO.Item> list = new ArrayList<>();
        for (VersionChangeItem it : items(versionId)) {
            VersionPreviewVO.Item i = new VersionPreviewVO.Item();
            i.setCategory(it.getCategory());
            i.setCategoryDesc(it.getCategory().getDesc());
            i.setTitle(it.getTitle());
            i.setContent(it.getContent());
            list.add(i);
        }
        vo.setItems(list);
        return vo;
    }

    /** 提交版本文案审批：按所属产品线加载审批流，版本进入审批中。 */
    @Transactional(rollbackFor = Exception.class)
    public void submitForApproval(Long versionId) {
        AppVersion v = getById(versionId);
        if (v.getStatus() != VersionStatus.DRAFT) {
            throw BizException.of(ErrorCode.OPERATION_NOT_ALLOWED, "仅草稿版本可提交审批");
        }
        approvalService.submit(ApprovalBizType.VERSION, versionId, v.getProductId());
        transferStatus(versionId, VersionStatus.REVIEW);
    }

    @Override
    public boolean support(ApprovalBizType bizType) {
        return bizType == ApprovalBizType.VERSION;
    }

    @Override
    public void onApproved(Long versionId) {
        transferStatus(versionId, VersionStatus.APPROVED);
    }

    @Override
    public void onRejected(Long versionId, String comment) {
        transferStatus(versionId, VersionStatus.DRAFT);
    }

    private void ensureVersionNoUnique(Long productId, String versionNo, Long excludeId) {
        Long count = appVersionMapper.selectCount(new LambdaQueryWrapper<AppVersion>()
                .eq(AppVersion::getProductId, productId)
                .eq(AppVersion::getVersionNo, versionNo)
                .ne(excludeId != null, AppVersion::getId, excludeId));
        if (count != null && count > 0) {
            throw BizException.of(ErrorCode.VERSION_NO_DUPLICATED);
        }
    }
}
