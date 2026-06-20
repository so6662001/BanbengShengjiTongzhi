import { test, expect } from '@playwright/test'
import { login, USERS } from './helpers'

test.describe('登录鉴权', () => {
  test('未登录访问受保护页应跳转登录', async ({ page }) => {
    await page.goto('/version')
    await expect(page).toHaveURL(/\/login/)
  })

  test('正确账号可登录并进入工作台', async ({ page }) => {
    await login(page, USERS.admin)
    await expect(page).toHaveURL(/\/dashboard/)
    await expect(page.getByRole('heading', { name: '数据看板' })).toBeVisible()
  })

  test('错误密码应提示失败', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder('用户名').fill('admin')
    await page.getByPlaceholder('密码').fill('wrong-password')
    await page.getByRole('button', { name: '登录' }).click()
    // 登录失败：不应进入工作台，停留在登录页且未写入 token
    await page.waitForTimeout(1000)
    await expect(page).toHaveURL(/\/login/)
    const token = await page.evaluate(() => localStorage.getItem('token'))
    expect(token).toBeFalsy()
  })
})
