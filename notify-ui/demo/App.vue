<template>
  <div style="max-width: 880px; margin: 0 auto; padding: 24px">
    <h1 style="font-size: 22px">notify-ui · 客户端通用组件 Demo</h1>
    <p style="color: #99a0b0">演示「软件内更新弹窗」「更新日志页」与「一行初始化」。可连真实 client-api(:8082)。</p>

    <div style="display: flex; gap: 10px; flex-wrap: wrap; margin: 16px 0">
      <button class="btn" @click="showMock('UPDATE')">预览：正式更新弹窗(mock)</button>
      <button class="btn" @click="showMock('PRE_NOTICE')">预览：事前预告弹窗(mock)</button>
    </div>

    <div class="card">
      <h3>连接真实 client-api 拉取未读</h3>
      <div class="row">
        <label>产品 code</label><input v-model="productCode" />
        <label>身份(identity)</label><input v-model="clientId" />
      </div>
      <div class="row">
        <label>apiBase</label><input v-model="apiBase" placeholder="留空走 /client 代理" style="flex:1" />
      </div>
      <button class="btn primary" @click="liveNotify">一行初始化（自动弹窗+已读上报）</button>
      <span style="margin-left:10px;color:#99a0b0">{{ liveTip }}</span>
    </div>

    <div class="card">
      <h3>更新日志页（嵌入「系统设置 → 关于」形态）</h3>
      <ChangelogPage v-if="showChangelog" :options="changelogOptions" title="智控 ERP 更新日志" />
      <button v-else class="btn" @click="showChangelog = true">加载更新日志页</button>
    </div>

    <!-- mock 弹窗 -->
    <UpdatePopup v-if="mock" :data="mock" :force-read="mock.type === 'PRE_NOTICE'" @read="onMockRead" @close="mock = null" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { UpdatePopup, ChangelogPage, initUpdateNotifier, type UpdateData } from '../src'

const productCode = ref('erp')
const clientId = ref('LIC-YOUPIN-001')
const apiBase = ref('')
const liveTip = ref('')
const showChangelog = ref(false)
const mock = ref<UpdateData | null>(null)

const changelogOptions = computed(() => ({
  clientId: clientId.value,
  productCode: productCode.value,
  apiBase: apiBase.value,
}))

function showMock(type: 'UPDATE' | 'PRE_NOTICE') {
  mock.value = {
    type,
    title: type === 'UPDATE' ? '智控 ERP 已升级 v3.2.0' : '智控 ERP 将于 06-20 升级',
    description: '本次升级聚焦开单效率与报表性能，新增批量开单与移动端审批提醒，并修复跨月结转金额问题。',
    versionNo: 'v3.2.0',
    releaseTime: '2026-06-20T02:00:00',
    items: [
      { category: 'ADD', title: '批量开单与一键导入历史订单', content: '支持 Excel 模板批量导入' },
      { category: 'OPTIMIZE', title: '报表加载速度提升约 40%' },
      { category: 'FIX', title: '修复跨月结转偶发金额错误' },
    ],
    jumpUrl: '#',
  }
}

function onMockRead(id?: number) {
  liveTip.value = `已读回调触发（announcementId=${id ?? 'mock'}）`
}

function liveNotify() {
  liveTip.value = '已初始化，正在拉取未读…'
  initUpdateNotifier({
    clientId: clientId.value,
    productCode: productCode.value,
    apiBase: apiBase.value,
  })
}
</script>

<style>
.btn { border: 1px solid #e9ecf2; background: #fff; padding: 8px 14px; border-radius: 9px; cursor: pointer; }
.btn.primary { background: #3b5bfd; color: #fff; border-color: #3b5bfd; }
.card { background: #fff; border: 1px solid #e9ecf2; border-radius: 12px; padding: 16px 18px; margin-bottom: 16px; }
.row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.row label { color: #5a6273; font-size: 13px; }
.row input { padding: 6px 10px; border: 1px solid #e9ecf2; border-radius: 8px; }
</style>
