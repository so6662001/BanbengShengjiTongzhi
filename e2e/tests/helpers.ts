import { type Page, expect } from '@playwright/test'

export const USERS = {
  admin: { username: 'admin', password: 'admin123' },
  pm: { username: 'pm', password: 'admin123' },
  owner: { username: 'owner', password: 'admin123' },
}

/** 通过 UI 登录并进入工作台。 */
export async function login(page: Page, user: { username: string; password: string }) {
  await page.goto('/login')
  await page.getByPlaceholder('用户名').fill(user.username)
  await page.getByPlaceholder('密码').fill(user.password)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/dashboard/)
}

/** 侧边栏导航到指定菜单项。 */
export async function navTo(page: Page, menuText: string) {
  await page.getByRole('menuitem', { name: menuText }).click()
}

/** Element Plus 下拉选择：点击触发器后选中含指定文案的选项。 */
export async function selectOption(page: Page, triggerLocator: string, optionText: string) {
  await page.locator(triggerLocator).click()
  await page.locator('.el-select-dropdown__item', { hasText: optionText }).first().click()
}
