import { defineConfig, devices } from '@playwright/test'

/**
 * 浏览器级联调配置。
 * 前置：后端 admin-api(:8081) 已启动并初始化库表/种子；前端 dev server(:5173) 已启动。
 * 可通过环境变量 E2E_BASE_URL 覆盖前端地址。
 */
export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  // 如需 Playwright 自动拉起前端，可解开下方配置（需后端已在 8081 运行）：
  // webServer: {
  //   command: 'pnpm --dir ../frontend dev',
  //   url: 'http://localhost:5173',
  //   reuseExistingServer: true,
  //   timeout: 120_000,
  // },
})
