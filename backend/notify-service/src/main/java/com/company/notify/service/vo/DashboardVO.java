package com.company.notify.service.vo;

import lombok.Data;

import java.util.List;

/** 看板聚合 VO。 */
public class DashboardVO {

    /** 触达漏斗 */
    @Data
    public static class Funnel {
        private long target;       // 目标客户
        private long sent;         // 成功送达
        private long read;         // 已读
        private long upgraded;     // 升级完成
        private double readRate;   // 阅读率 = read/sent
        private double upgradeRate;// 升级完成率 = upgraded/target
    }

    /** 渠道效果 */
    @Data
    public static class ChannelStat {
        private String channel;
        private String channelDesc;
        private long sent;
        private long read;
        private double readRate;
    }

    /** 按服务器升级完成率 */
    @Data
    public static class ServerUpgrade {
        private Long serverNodeId;
        private long customerCount;
        private long upgradedCount;
        private double rate;
    }

    @Data
    public static class Overview {
        private Funnel funnel;
        private List<ChannelStat> channels;
        private List<ServerUpgrade> servers;
    }
}
