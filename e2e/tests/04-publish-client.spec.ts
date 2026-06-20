import { test, expect } from '@playwright/test'
import { ADMIN_URL, CLIENT_URL, apiLogin, apiData, approveBiz } from './api'

/**
 * 跨服务运行态联调（真实 HTTP，admin-api + client-api 两个服务）：
 * 发布版本 → 投递落库 → 客户端拉未读 → 已读上报 → 看板阅读率体现。
 * 客户端身份用种子的 LIC-YOUPIN-001（产品 erp / 客户 优品商贸）。
 */
test.describe('发布到客户端全链路', () => {
  test('发布 → 客户端未读 → 已读 → 看板', async ({ request }) => {
    const admin = await apiLogin(request, 'admin', 'admin123')
    const versionNo = `v8.${Date.now() % 100000}.0`

    // 1. 建版本 + 文案两级审批
    const versionId = await apiData(request, 'post', `${ADMIN_URL}/admin/version`, admin, {
      productId: 101, versionNo, description: 'E2E 跨服务联调版本',
      releaseType: 'RELEASE', planReleaseTime: '2026-06-20T02:00:00',
      items: [{ category: 'ADD', title: '批量开单' }, { category: 'FIX', title: '修复结转' }],
    })
    await apiData(request, 'post', `${ADMIN_URL}/admin/version/${versionId}/submit`, admin)
    await approveBiz(request, admin, 'VERSION', String(versionId))

    // 2. 建人群（按产品圈选）
    const audienceId = await apiData(request, 'post', `${ADMIN_URL}/admin/audience`, admin, {
      name: `E2E人群-${versionNo}`, condition: { productId: 101 },
    })

    // 3. 建发布计划（仅站内信，无预告）→ 提交并两级审批
    const planId = await apiData(request, 'post', `${ADMIN_URL}/admin/release-plan`, admin, {
      versionId, audienceId, channels: '["IN_APP"]', preNotifyDays: '[]',
      releaseTime: '2026-06-25T02:00:00', scheduleEnabled: true,
    })
    await apiData(request, 'post', `${ADMIN_URL}/admin/release-plan/${planId}/submit`, admin)
    await approveBiz(request, admin, 'RELEASE_PLAN', String(planId))

    // 4. 正式发布
    await apiData(request, 'post', `${ADMIN_URL}/admin/release-plan/${planId}/publish`, admin)

    // 5. 客户端拉取未读（异步投递，轮询等待）
    const clientHeaders = { 'X-Product-Code': 'erp', 'X-Identity': 'LIC-YOUPIN-001' }
    let unread: any[] = []
    let announcementId: string | undefined
    for (let i = 0; i < 30; i++) {
      const resp = await request.get(`${CLIENT_URL}/client/announcements/unread`, { headers: clientHeaders })
      const body = await resp.json()
      expect(body.code).toBe(0)
      unread = body.data || []
      const hit = unread.find((u: any) => u.detail?.versionNo === versionNo)
      if (hit) { announcementId = hit.announcementId; break }
      await new Promise((r) => setTimeout(r, 300))
    }
    expect(announcementId, '客户端应能拉到本次发布的更新公告').toBeTruthy()

    // 6. 已读上报
    const readResp = await request.post(`${CLIENT_URL}/client/announcements/${announcementId}/read`, { headers: clientHeaders })
    expect((await readResp.json()).code).toBe(0)

    // 7. 再拉未读应不含该公告
    const after = await request.get(`${CLIENT_URL}/client/announcements/unread`, { headers: clientHeaders })
    const afterList: any[] = (await after.json()).data || []
    expect(afterList.find((u: any) => u.announcementId === announcementId)).toBeFalsy()

    // 8. 看板漏斗：已读 ≥ 1
    const funnel = await apiData(request, 'get', `${ADMIN_URL}/admin/dashboard/funnel?versionId=${versionId}`, admin)
    // Long 计数按策略序列化为字符串，比较时转 Number
    expect(Number(funnel.sent)).toBeGreaterThanOrEqual(1)
    expect(Number(funnel.read)).toBeGreaterThanOrEqual(1)
  })
})
