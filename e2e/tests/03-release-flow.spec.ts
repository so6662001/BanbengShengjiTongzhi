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

    await selectOption(admin, '.el-dialog .el-select', '智控 ERP')
    await dialog.getByPlaceholder('v3.2.0').fill(versionNo)
    await dialog.getByPlaceholder(/本次版本整体概述/).fill('E2E 自动化验证版本：聚焦开单效率')
    await dialog.getByRole('button', { name: '＋ 新增' }).click()
    await dialog.getByPlaceholder('条目标题').first().fill('批量开单')
    await dialog.getByRole('button', { name: '保存' }).click()

    // 列表中出现该版本，提交审批
    const row = admin.locator('tr', { hasText: versionNo })
    await expect(row).toBeVisible()
    await row.getByRole('button', { name: '提交审批' }).click()
    await expect(admin.locator('.el-message').first()).toBeVisible()

    // ---- 产品经理：一级审批通过 ----
    await approveAs(browser, USERS.pm)
    // ---- 产品线负责人：二级审批通过 ----
    await approveAs(browser, USERS.owner)

    // ---- 校验：版本状态变为已审批 ----
    await admin.reload()
    await navTo(admin, '版本管理')
    const finalRow = admin.locator('tr', { hasText: versionNo })
    await expect(finalRow.getByText(/已审批|定时待发|已发布/)).toBeVisible()

    await adminCtx.close()
  })
})

/** 以指定角色登录，在审批中心通过当前待办（若有）。 */
async function approveAs(browser: any, user: { username: string; password: string }) {
  const ctx = await browser.newContext()
  const page: Page = await ctx.newPage()
  await login(page, user)
  await navTo(page, '审批中心')

  const todoRow = page.locator('.el-table__row').first()
  if (await todoRow.count()) {
    await todoRow.getByRole('button', { name: '通过' }).click()
    const dialog = page.locator('.el-dialog', { hasText: '通过审批' })
    await dialog.getByPlaceholder('审批意见').fill('E2E 通过')
    await dialog.getByRole('button', { name: '确认' }).click()
    await expect(page.locator('.el-message').first()).toBeVisible()
  }
  await ctx.close()
}
