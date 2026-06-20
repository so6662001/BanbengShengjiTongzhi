import { test, expect, type Page } from '@playwright/test'
import { login, navTo, selectOption, USERS } from './helpers'

/**
 * 浏览器级全流程联调：
 * 管理员建版本(含更新条目+说明) → 提交审批 → 产品经理通过 → 产品线负责人通过 → 版本进入「已审批」。
 * 多角色用独立浏览器上下文模拟。
 */
test.describe('版本发布全流程', () => {
  test('建版本 → 两级审批通过', async ({ browser }) => {
    const versionNo = `v9.${Date.now() % 100000}.0`

    // ---- 管理员：创建版本并提交审批 ----
    const adminCtx = await browser.newContext()
    const admin = await adminCtx.newPage()
    await login(admin, USERS.admin)
    await navTo(admin, '版本管理')

    await admin.getByRole('button', { name: '新建版本' }).click()
    const dialog = admin.locator('.el-dialog')
    await expect(dialog).toBeVisible()

    await selectOption(admin, '.el-dialog .el-select__wrapper', '智控 ERP')
    await dialog.getByPlaceholder('v3.2.0').fill(versionNo)
    await dialog.getByPlaceholder(/本次版本整体概述/).fill('E2E 自动化验证版本：聚焦开单效率')
    await dialog.getByRole('button', { name: '＋ 新增' }).click()
    await dialog.getByPlaceholder('条目标题').first().fill('批量开单')
    const saveResp = admin.waitForResponse((r) => r.url().includes('/admin/version') && r.request().method() === 'POST')
    await dialog.getByRole('button', { name: '保存' }).click()
    await saveResp
    await expect(dialog).toBeHidden()

    // 列表中出现该版本，提交审批（等待 submit 请求真正完成）
    const row = admin.locator('tr', { hasText: versionNo })
    await expect(row).toBeVisible()
    const submitResp = admin.waitForResponse((r) => r.url().includes('/submit') && r.request().method() === 'POST')
    await row.getByRole('button', { name: '提交审批' }).click()
    const submitted = await submitResp
    expect(submitted.ok()).toBeTruthy()

    // ---- 产品经理：一级审批通过 ----
    await approveAs(browser, USERS.pm)
    // ---- 产品线负责人：二级审批通过 ----
    await approveAs(browser, USERS.owner)

    // ---- 校验：版本状态变为已审批 ----
    const listLoaded = admin.waitForResponse((r) => r.url().includes('/admin/version') && r.request().method() === 'GET')
    await admin.goto('/version')
    await listLoaded
    const finalRow = admin.locator('tr', { hasText: versionNo })
    await expect(finalRow).toBeVisible()
    await expect(finalRow.getByText(/已审批|定时待发|已发布/)).toBeVisible()

    await adminCtx.close()
  })
})

/**
 * 以指定角色登录，清空其「待我审批」队列（全部通过）。
 * 因连真实持久库，历史运行可能遗留待办，逐条通过可确保本次版本也被处理。
 */
async function approveAs(browser: any, user: { username: string; password: string }) {
  const ctx = await browser.newContext()
  const page: Page = await ctx.newPage()
  await login(page, user)
  // 进入审批中心并等待待办列表加载完成（避免在异步加载前就判断为空）
  const todoLoaded = page.waitForResponse((r) => r.url().includes('/admin/approval/todo'))
  await navTo(page, '审批中心')
  await todoLoaded
  await page.waitForTimeout(300)

  const todoCard = page.locator('.el-card', { hasText: '待我审批' })
  for (let i = 0; i < 30; i++) {
    const todoRows = todoCard.locator('.el-table__row')
    if ((await todoRows.count()) === 0) {
      break
    }
    const approveResp = page.waitForResponse((r) => /\/admin\/approval\/\d+/.test(r.url()) && r.request().method() === 'POST')
    const reloaded = page.waitForResponse((r) => r.url().includes('/admin/approval/todo'))
    await todoRows.first().getByRole('button', { name: '通过' }).click()
    const dialog = page.locator('.el-dialog', { hasText: '通过审批' })
    await dialog.getByPlaceholder('审批意见').fill('E2E 通过')
    await dialog.getByRole('button', { name: '确认' }).click()
    await approveResp
    await reloaded // 等待列表刷新后再判断
    await page.waitForTimeout(200)
  }
  await ctx.close()
}
