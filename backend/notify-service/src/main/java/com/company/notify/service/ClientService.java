package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.notify.common.enums.AnnouncementType;
import com.company.notify.common.enums.ChangeCategory;
import com.company.notify.common.enums.ReadStatus;
import com.company.notify.common.enums.VersionStatus;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.Announcement;
import com.company.notify.domain.entity.AppVersion;
import com.company.notify.domain.entity.DeliveryRecord;
import com.company.notify.domain.entity.Feedback;
import com.company.notify.domain.entity.Product;
import com.company.notify.domain.mapper.AnnouncementMapper;
import com.company.notify.domain.mapper.AppVersionMapper;
import com.company.notify.domain.mapper.DeliveryRecordMapper;
import com.company.notify.domain.mapper.FeedbackMapper;
import com.company.notify.service.identity.IdentityResolverRegistry;
import com.company.notify.service.vo.UnreadAnnouncementVO;
import com.company.notify.service.vo.VersionPreviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端接口服务：未读公告、历史日志、已读上报、反馈。
 * 通过 IdentityResolverRegistry 完成身份解析；公告访问做产品归属隔离，防越权。
 */
@Service
@RequiredArgsConstructor
public class ClientService {

    private final IdentityResolverRegistry identityRegistry;
    private final DeliveryRecordMapper recordMapper;
    private final AnnouncementMapper announcementMapper;
    private final AppVersionMapper appVersionMapper;
    private final AppVersionService appVersionService;
    private final FeedbackMapper feedbackMapper;

    /** 启动拉取未读公告（仅本客户、本产品）。 */
    public List<UnreadAnnouncementVO> unread(String productCode, String identityValue) {
        Product product = identityRegistry.getProductByCode(productCode);
        Long customerId = identityRegistry.resolve(productCode, identityValue);

        List<DeliveryRecord> records = recordMapper.selectList(new LambdaQueryWrapper<DeliveryRecord>()
                .eq(DeliveryRecord::getCustomerId, customerId)
                .eq(DeliveryRecord::getReadStatus, ReadStatus.UNREAD));

        List<UnreadAnnouncementVO> result = new ArrayList<>();
        for (DeliveryRecord record : records) {
            Announcement ann = announcementMapper.selectById(record.getAnnouncementId());
            if (ann == null) {
                continue;
            }
            AppVersion version = appVersionMapper.selectById(ann.getVersionId());
            // 产品归属隔离：只返回属于该产品的公告
            if (version == null || !version.getProductId().equals(product.getId())) {
                continue;
            }
            UnreadAnnouncementVO vo = new UnreadAnnouncementVO();
            vo.setAnnouncementId(ann.getId());
            vo.setType(ann.getType());
            vo.setTypeDesc(ann.getType().getDesc());
            vo.setTitle(ann.getTitle());
            vo.setJumpUrl(ann.getJumpUrl());
            vo.setDetail(appVersionService.preview(ann.getVersionId()));
            vo.setForceRead(ann.getType() == AnnouncementType.PRE_NOTICE);
            result.add(vo);
        }
        return result;
    }

    /** 已读上报（幂等：仅未读→已读）。 */
    public void markRead(Long announcementId, String productCode, String identityValue) {
        Long customerId = identityRegistry.resolve(productCode, identityValue);
        recordMapper.update(null, new LambdaUpdateWrapper<DeliveryRecord>()
                .eq(DeliveryRecord::getAnnouncementId, announcementId)
                .eq(DeliveryRecord::getCustomerId, customerId)
                .eq(DeliveryRecord::getReadStatus, ReadStatus.UNREAD)
                .set(DeliveryRecord::getReadStatus, ReadStatus.READ)
                .set(DeliveryRecord::getReadTime, LocalDateTime.now()));
    }

    /** 历史更新日志（已发布版本），可按分类过滤条目。 */
    public IPage<VersionPreviewVO> changelog(String productCode, ChangeCategory category, long current, long size) {
        Product product = identityRegistry.getProductByCode(productCode);
        IPage<AppVersion> page = appVersionMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<AppVersion>()
                        .eq(AppVersion::getProductId, product.getId())
                        .eq(AppVersion::getStatus, VersionStatus.PUBLISHED)
                        .orderByDesc(AppVersion::getPlanReleaseTime));
        IPage<VersionPreviewVO> voPage = page.convert(v -> {
            VersionPreviewVO preview = appVersionService.preview(v.getId());
            if (category != null && preview.getItems() != null) {
                preview.setItems(preview.getItems().stream()
                        .filter(it -> it.getCategory() == category).toList());
            }
            return preview;
        });
        return voPage;
    }

    /** 客户反馈收集。 */
    public void feedback(Long announcementId, String productCode, String identityValue, Integer rating, String content) {
        Long customerId = identityRegistry.resolve(productCode, identityValue);
        if (announcementMapper.selectById(announcementId) == null) {
            throw BizException.of(ErrorCode.DATA_NOT_FOUND, "公告不存在");
        }
        Feedback fb = new Feedback();
        fb.setAnnouncementId(announcementId);
        fb.setCustomerId(customerId);
        fb.setRating(rating);
        fb.setContent(content);
        feedbackMapper.insert(fb);
    }
}
