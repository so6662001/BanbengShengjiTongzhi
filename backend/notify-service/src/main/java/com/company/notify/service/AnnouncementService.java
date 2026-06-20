package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.AnnouncementType;
import com.company.notify.domain.entity.Announcement;
import com.company.notify.domain.entity.AppVersion;
import com.company.notify.domain.mapper.AnnouncementMapper;
import com.company.notify.service.vo.VersionPreviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 公告生成：由版本内容生成「预告/更新」公告，幂等（版本+类型唯一）。 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final AppVersionService appVersionService;

    /** 获取或创建指定版本+类型的公告。 */
    public Announcement getOrCreate(Long versionId, AnnouncementType type) {
        Announcement exist = announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getVersionId, versionId)
                .eq(Announcement::getType, type)
                .last("limit 1"));
        if (exist != null) {
            return exist;
        }
        AppVersion version = appVersionService.getById(versionId);
        VersionPreviewVO preview = appVersionService.preview(versionId);
        Announcement ann = new Announcement();
        ann.setVersionId(versionId);
        ann.setType(type);
        ann.setTitle(buildTitle(type, preview));
        ann.setPopupContent(preview.getDescription());
        ann.setJumpUrl("/changelog?product=" + version.getProductId() + "&version=" + version.getVersionNo());
        announcementMapper.insert(ann);
        return ann;
    }

    private String buildTitle(AnnouncementType type, VersionPreviewVO preview) {
        String product = preview.getProductName() == null ? "产品" : preview.getProductName();
        if (type == AnnouncementType.PRE_NOTICE) {
            return product + " 即将升级 " + preview.getVersionNo();
        }
        return product + " 已升级 " + preview.getVersionNo();
    }

    public Announcement getById(Long id) {
        return announcementMapper.selectById(id);
    }
}
