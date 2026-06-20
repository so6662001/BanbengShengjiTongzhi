import { test, expect } from '@playwright/test'
import { login, navTo, USERS } from './helpers'

test.describe('后台导航', () => {
  test('六大菜单页均可正常打开', async ({ page }) => {
    await login(page, USERS.admin)

    const pages: Array<[string, RegExp]> = [
      ['版本管理', /版本管理/],
      ['服务器管理', /服务器管理/],
      ['客户分层', /客户分层圈选/],
      ['发布流程', /发布流程/],
      ['审批中心', /审批中心/],
      ['数据看板', /数据看板/],
    ]
    for (const [menu, heading] of pages) {
      await navTo(page, menu)
      await expect(page.getByRole('heading', { name: heading }).first()).toBeVisible()
    }
  })
})
