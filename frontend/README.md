# 版本更新通知工具 · 后台前端

Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router。页面与 `prototype/` 视觉原型对齐。

## 页面
- 登录、数据看板、版本管理、服务器管理、客户分层、发布流程、审批中心。

## 开发
```
pnpm install
pnpm dev      # http://localhost:5173 ，已配置 /admin 代理到 8081 后端
pnpm build    # 类型检查 + 生产构建
```

## 说明
- 接口统一走 `src/api`，Axios 拦截器自动带 JWT、解包 `Result.data`、401 跳登录。
- 默认登录 admin / admin123（需先启动后端并初始化种子数据）。
