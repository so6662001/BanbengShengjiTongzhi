# 浏览器级端到端联调（Playwright）

对 `frontend`（Vue3 后台）↔ `backend`（admin-api）做真实浏览器自动化联调。

## 前置条件
1. 起依赖中间件并初始化数据：
   ```
   cd ../backend && docker compose up -d        # MySQL + Redis（自动执行 sql/schema.sql、seed.sql）
   ```
2. 启动后端：
   ```
   cd ../backend && mvn -pl notify-admin-api -am spring-boot:run   # :8081
   ```
3. 启动前端 dev server：
   ```
   cd ../frontend && pnpm install && pnpm dev                      # :5173（已代理 /admin → 8081）
   ```

## 安装与运行
```
cd e2e
pnpm install                # 或 npm install
pnpm exec playwright install chromium
pnpm test                   # 无头运行
pnpm test:headed            # 有头观察
pnpm report                 # 查看 HTML 报告
```

可用 `E2E_BASE_URL` 覆盖前端地址：`E2E_BASE_URL=http://host:5173 pnpm test`。

## 用例
- `01-auth.spec.ts`：未登录跳转、正确登录、错误密码提示。
- `02-navigation.spec.ts`：六大菜单页可正常打开。
- `03-release-flow.spec.ts`：管理员建版本(含条目+说明) → 提交审批 → 产品经理/产品线负责人两级审批通过（多浏览器上下文模拟多角色）。

## 说明
- 演示账号：admin / pm / owner，密码均 admin123。
- 选择器以语义化（角色/占位符/文案）为主，贴合 Element Plus DOM；如前端 UI 文案调整，请同步更新选择器。
