# @company/notify-ui

版本更新通知 · 可复用 Vue3 组件库。B/S 系统直接引入；C/S 桌面端可用 WebView 复用同一组件。

## 安装
```
pnpm add @company/notify-ui
```

## 一行初始化（自动弹窗 + 已读上报）
```ts
import { initUpdateNotifier } from '@company/notify-ui'
import '@company/notify-ui/style.css'

const notifier = initUpdateNotifier({
  clientId: 'LIC-YOUPIN-001',   // 客户端身份值（License/租户/设备）
  productCode: 'erp',
  apiBase: 'http://localhost:8082',
  token: '可选',
})
// notifier.refresh() 手动刷新；notifier.destroy() 卸载
```

## 组件用法
```vue
<script setup>
import { UpdatePopup, ChangelogPage } from '@company/notify-ui'
import '@company/notify-ui/style.css'
</script>

<template>
  <!-- 软件内更新弹窗 -->
  <UpdatePopup :data="data" :force-read="false" @read="onRead" @close="onClose" />

  <!-- 嵌入“系统设置 → 关于”的更新日志页 -->
  <ChangelogPage :options="{ clientId, productCode: 'erp', apiBase }" title="智控 ERP 更新日志" />
</template>
```

### `<UpdatePopup>` props
```ts
{
  data: { type:'PRE_NOTICE'|'UPDATE', title, description, versionNo, releaseTime,
          items:[{category:'ADD'|'OPTIMIZE'|'FIX', title, content}], jumpUrl, announcementId },
  forceRead?: boolean   // 强提醒：必读才可关闭
}
// emits: read(announcementId) / close
```

## 构建
```
pnpm install
pnpm build   # 产出 dist/notify-ui.js (ESM) + umd + style.css
```

## 本地 Demo（:5174，可连真实 client-api）
```
pnpm demo    # 启动 demo/，已代理 /client → http://localhost:8082
```
Demo 提供：mock 弹窗预览、连真实 client-api 的「一行初始化自动弹窗」、嵌入式更新日志页。
