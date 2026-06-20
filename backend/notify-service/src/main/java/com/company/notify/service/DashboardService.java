package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.AnnouncementType;
import com.company.notify.common.enums.Channel;
import com.company.notify.common.enums.ReadStatus;
import com.company.notify.common.enums.SendStatus;
import com.company.notify.domain.entity.Announcement;
import com.company.notify.domain.entity.AppVersion;
import com.company.notify.domain.entity.CustomerProduct;
import com.company.notify.domain.entity.DeliveryRecord;
import com.company.notify.domain.mapper.AnnouncementMapper;
import com.company.notify.domain.mapper.AppVersionMapper;
import com.company.notify.domain.mapper.CustomerProductMapper;
import com.company.notify.domain.mapper.DeliveryRecordMapper;
import com.company.notify.service.support.VersionComparator;
import com.company.notify.service.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据看板统计。
 * 阅读率 = 已读/送达；升级完成率 = 目标客户中 current_version≥目标版本 占比。
 * 当前为实时聚合，数据量大时应改为定时预聚合表。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AnnouncementMapper announcementMapper;
    private final AppVersionMapper appVersionMapper;
    private final DeliveryRecordMapper recordMapper;
    private final CustomerProductMapper customerProductMapper;

    public DashboardVO.Overview overview(Long versionId) {
        DashboardVO.Overview overview = new DashboardVO.Overview();
        overview.setFunnel(funnel(versionId));
        overview.setChannels(channelStats(versionId));
        overview.setServers(serverUpgrade(versionId));
        return overview;
    }

    public DashboardVO.Funnel funnel(Long versionId) {
        DashboardVO.Funnel f = new DashboardVO.Funnel();
        AppVersion version = appVersionMapper.selectById(versionId);
        Announcement ann = updateAnnouncement(versionId);
        List<DeliveryRecord> records = ann == null ? List.of() : recordsOf(ann.getId());

        long target = records.stream().map(DeliveryRecord::getCustomerId).distinct().count();
        long sent = records.stream().filter(r -> r.getSendStatus() == SendStatus.SENT)
                .map(DeliveryRecord::getCustomerId).distinct().count();
        long read = records.stream().filter(r -> r.getReadStatus() == ReadStatus.READ)
                .map(DeliveryRecord::getCustomerId).distinct().count();
        long upgraded = version == null ? 0 : upgradedCount(version);

        f.setTarget(target);
        f.setSent(sent);
        f.setRead(read);
        f.setUpgraded(upgraded);
        f.setReadRate(rate(read, sent));
        f.setUpgradeRate(rate(upgraded, target));
        return f;
    }

    public List<DashboardVO.ChannelStat> channelStats(Long versionId) {
        Announcement ann = updateAnnouncement(versionId);
        List<DeliveryRecord> records = ann == null ? List.of() : recordsOf(ann.getId());
        Map<Channel, List<DeliveryRecord>> byChannel = records.stream()
                .collect(Collectors.groupingBy(DeliveryRecord::getChannel, LinkedHashMap::new, Collectors.toList()));
        List<DashboardVO.ChannelStat> list = new ArrayList<>();
        for (Map.Entry<Channel, List<DeliveryRecord>> e : byChannel.entrySet()) {
            long sent = e.getValue().stream().filter(r -> r.getSendStatus() == SendStatus.SENT).count();
            long read = e.getValue().stream().filter(r -> r.getReadStatus() == ReadStatus.READ).count();
            DashboardVO.ChannelStat cs = new DashboardVO.ChannelStat();
            cs.setChannel(e.getKey().name());
            cs.setChannelDesc(e.getKey().getDesc());
            cs.setSent(sent);
            cs.setRead(read);
            cs.setReadRate(rate(read, sent));
            list.add(cs);
        }
        return list;
    }

    public List<DashboardVO.ServerUpgrade> serverUpgrade(Long versionId) {
        AppVersion version = appVersionMapper.selectById(versionId);
        if (version == null) {
            return List.of();
        }
        List<CustomerProduct> cps = customerProductMapper.selectList(new LambdaQueryWrapper<CustomerProduct>()
                .eq(CustomerProduct::getProductId, version.getProductId()));
        Map<Long, List<CustomerProduct>> byServer = cps.stream()
                .collect(Collectors.groupingBy(CustomerProduct::getServerNodeId, LinkedHashMap::new, Collectors.toList()));
        List<DashboardVO.ServerUpgrade> list = new ArrayList<>();
        for (Map.Entry<Long, List<CustomerProduct>> e : byServer.entrySet()) {
            long total = e.getValue().size();
            long upgraded = e.getValue().stream()
                    .filter(cp -> cp.getCurrentVersion() != null
                            && VersionComparator.gte(cp.getCurrentVersion(), version.getVersionNo()))
                    .count();
            DashboardVO.ServerUpgrade su = new DashboardVO.ServerUpgrade();
            su.setServerNodeId(e.getKey());
            su.setCustomerCount(total);
            su.setUpgradedCount(upgraded);
            su.setRate(rate(upgraded, total));
            list.add(su);
        }
        return list;
    }

    private long upgradedCount(AppVersion version) {
        List<CustomerProduct> cps = customerProductMapper.selectList(new LambdaQueryWrapper<CustomerProduct>()
                .eq(CustomerProduct::getProductId, version.getProductId()));
        return cps.stream()
                .filter(cp -> cp.getCurrentVersion() != null
                        && VersionComparator.gte(cp.getCurrentVersion(), version.getVersionNo()))
                .count();
    }

    private Announcement updateAnnouncement(Long versionId) {
        return announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getVersionId, versionId)
                .eq(Announcement::getType, AnnouncementType.UPDATE)
                .last("limit 1"));
    }

    private List<DeliveryRecord> recordsOf(Long announcementId) {
        return recordMapper.selectList(new LambdaQueryWrapper<DeliveryRecord>()
                .eq(DeliveryRecord::getAnnouncementId, announcementId));
    }

    private double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0d;
        }
        return Math.round((double) numerator / denominator * 1000) / 10.0;
    }
}
