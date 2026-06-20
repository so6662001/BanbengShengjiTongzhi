<template>
  <div>
    <h2 class="page-title">数据看板</h2>
    <p class="page-sub">触达漏斗 · 阅读率 · 升级完成率 · 按服务器统计</p>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6"><el-card class="stat-card"><div class="stat-label">目标客户</div><div class="stat-value">{{ funnel.target }}</div></el-card></el-col>
      <el-col :span="6"><el-card class="stat-card"><div class="stat-label">弹窗阅读率</div><div class="stat-value">{{ funnel.readRate }}%</div></el-card></el-col>
      <el-col :span="6"><el-card class="stat-card"><div class="stat-label">升级完成率</div><div class="stat-value">{{ funnel.upgradeRate }}%</div></el-card></el-col>
      <el-col :span="6"><el-card class="stat-card"><div class="stat-label">已读客户</div><div class="stat-value">{{ funnel.read }}</div></el-card></el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header>触达漏斗</template>
          <div v-for="row in funnelRows" :key="row.label" class="funnel-row">
            <span class="fl-label">{{ row.label }}</span>
            <el-progress :percentage="row.pct" :stroke-width="18" :show-text="false" />
            <span class="fl-val">{{ row.val }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>渠道效果</template>
          <el-table :data="channels" size="small">
            <el-table-column prop="channelDesc" label="渠道" />
            <el-table-column prop="sent" label="送达" />
            <el-table-column prop="read" label="已读" />
            <el-table-column prop="readRate" label="阅读率">
              <template #default="{ row }">{{ row.readRate }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header>按服务器 · 升级完成率</template>
      <el-table :data="servers" size="small">
        <el-table-column prop="serverNodeId" label="服务器ID" />
        <el-table-column prop="customerCount" label="客户数" />
        <el-table-column prop="upgradedCount" label="已升级" />
        <el-table-column label="完成率">
          <template #default="{ row }">
            <el-progress :percentage="row.rate" :stroke-width="14" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-alert v-if="!versionId" type="info" :closable="false" style="margin-top: 16px"
      title="提示：看板按版本统计。请在版本管理中选择版本后查看，或在地址栏指定 versionId。" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { dashboardApi } from '@/api'

const route = useRoute()
const versionId = ref<number>(Number(route.query.versionId) || 0)
const funnel = ref<any>({ target: 0, sent: 0, read: 0, upgraded: 0, readRate: 0, upgradeRate: 0 })
const channels = ref<any[]>([])
const servers = ref<any[]>([])

const funnelRows = computed(() => {
  const t = funnel.value.target || 1
  return [
    { label: '目标客户', val: funnel.value.target, pct: 100 },
    { label: '成功送达', val: funnel.value.sent, pct: Math.round((funnel.value.sent / t) * 100) },
    { label: '已读', val: funnel.value.read, pct: Math.round((funnel.value.read / t) * 100) },
    { label: '升级完成', val: funnel.value.upgraded, pct: Math.round((funnel.value.upgraded / t) * 100) },
  ]
})

async function load() {
  if (!versionId.value) return
  funnel.value = await dashboardApi.funnel(versionId.value)
  channels.value = await dashboardApi.channel(versionId.value)
  servers.value = await dashboardApi.serverUpgrade(versionId.value)
}

onMounted(load)
</script>

<style scoped>
.funnel-row { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.fl-label { width: 80px; color: #5a6273; font-size: 13px; }
.fl-val { width: 60px; text-align: right; color: #5a6273; }
.funnel-row :deep(.el-progress) { flex: 1; }
</style>
