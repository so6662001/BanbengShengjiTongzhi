package com.company.notify.admin;

import com.company.notify.common.enums.ApprovalBizType;
import com.company.notify.common.enums.ApproveResult;
import com.company.notify.domain.entity.ApprovalRecord;
import com.company.notify.service.ClientService;
import com.company.notify.service.approval.ApprovalService;
import com.company.notify.service.vo.UnreadAnnouncementVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 端到端联调验证（H2 + 真实 Controller/Service/Mapper 全链路）：
 * 登录 → 建版本 → 文案两级审批 → 建人群/发布计划 → 计划两级审批 → 发布 → 投递落库
 * → 客户端拉取未读 → 已读上报 → 看板统计。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class EndToEndFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired ApprovalService approvalService;
    @Autowired ClientService clientService;

    @Test
    void full_release_flow() throws Exception {
        String adminToken = login("admin", "admin123");

        // 1. 创建版本（含更新条目 + 版本说明）
        String versionBody = """
            {"productId":101,"versionNo":"v3.2.0","description":"聚焦开单效率与报表性能",
             "releaseType":"RELEASE","planReleaseTime":"2026-06-20T02:00:00",
             "items":[{"category":"ADD","title":"批量开单"},{"category":"OPTIMIZE","title":"报表提速"},{"category":"FIX","title":"跨月结转修复"}]}""";
        long versionId = dataAsLong(postJson("/admin/version", versionBody, adminToken));
        assertTrue(versionId > 0);

        // 2. 提交文案审批 + 两级审批通过
        ok(postJson("/admin/version/" + versionId + "/submit", "", adminToken));
        approveAll(ApprovalBizType.VERSION, versionId);

        // 3. 创建人群（仅按产品圈选 → 命中 401/402）
        String audienceBody = """
            {"name":"ERP全量","condition":{"productId":101}}""";
        long audienceId = dataAsLong(postJson("/admin/audience", audienceBody, adminToken));
        assertTrue(audienceId > 0);

        // 4. 创建发布计划（站内信 + 预告[3,1]）
        String planBody = String.format("""
            {"versionId":%d,"audienceId":%d,"channels":"[\\"IN_APP\\"]","preNotifyDays":"[3,1]",
             "releaseTime":"%s","scheduleEnabled":true}""",
                versionId, audienceId, LocalDateTime.now().plusDays(5).withNano(0));
        long planId = dataAsLong(postJson("/admin/release-plan", planBody, adminToken));

        // 5. 提交计划审批 + 两级通过 → 计划进入定时待发
        ok(postJson("/admin/release-plan/" + planId + "/submit", "", adminToken));
        approveAll(ApprovalBizType.RELEASE_PLAN, planId);

        // 6. 正式发布 → 生成更新公告 + 站内信投递（异步）
        ok(postJson("/admin/release-plan/" + planId + "/publish", "", adminToken));

        // 7. 等待异步投递落库，客户端可拉到未读
        List<UnreadAnnouncementVO> unread = awaitUnread("erp", "LIC-YOUPIN-001");
        assertFalse(unread.isEmpty(), "客户应能拉取到未读更新公告");
        Long announcementId = unread.get(0).getAnnouncementId();
        assertEquals("聚焦开单效率与报表性能", unread.get(0).getDetail().getDescription());

        // 8. 已读上报后再次拉取应为空（幂等 + 缓存失效回源）
        clientService.markRead(announcementId, "erp", "LIC-YOUPIN-001");
        assertTrue(clientService.unread("erp", "LIC-YOUPIN-001").isEmpty());

        // 9. 看板漏斗：目标客户≥2，已读≥1
        JsonNode funnel = dataNode(getJson("/admin/dashboard/funnel?versionId=" + versionId, adminToken));
        assertTrue(funnel.path("target").asLong() >= 2);
        assertTrue(funnel.path("read").asLong() >= 1);
    }

    // ---------- helpers ----------

    private void approveAll(ApprovalBizType bizType, Long bizId) throws Exception {
        for (int guard = 0; guard < 10; guard++) {
            List<ApprovalRecord> pending = approvalService.records(bizType, bizId).stream()
                    .filter(r -> r.getResult() == ApproveResult.PENDING)
                    .sorted(Comparator.comparingInt(ApprovalRecord::getNodeLevel))
                    .toList();
            if (pending.isEmpty()) {
                return;
            }
            ApprovalRecord cur = pending.get(0);
            String token = cur.getApproverId() == 2L ? login("pm", "admin123") : login("owner", "admin123");
            ok(postJson("/admin/approval/" + cur.getId(), "{\"pass\":true,\"comment\":\"ok\"}", token));
        }
        fail("审批未在预期步数内完成");
    }

    private List<UnreadAnnouncementVO> awaitUnread(String productCode, String identity) throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            List<UnreadAnnouncementVO> list = clientService.unread(productCode, identity);
            if (!list.isEmpty()) {
                return list;
            }
            Thread.sleep(100);
        }
        return List.of();
    }

    private String login(String username, String password) throws Exception {
        MvcResult r = mvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn();
        return dataNode(r.getResponse().getContentAsString()).path("token").asText();
    }

    private String postJson(String url, String body, String token) throws Exception {
        return mvc.perform(post(url).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
    }

    private String getJson(String url, String token) throws Exception {
        return mvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
    }

    private void ok(String resp) throws Exception {
        assertEquals(0, om.readTree(resp).path("code").asInt(), "接口返回非成功: " + resp);
    }

    private long dataAsLong(String resp) throws Exception {
        JsonNode node = om.readTree(resp);
        assertEquals(0, node.path("code").asInt(), "接口返回非成功: " + resp);
        return node.path("data").asLong();
    }

    private JsonNode dataNode(String resp) throws Exception {
        return om.readTree(resp).path("data");
    }
}
