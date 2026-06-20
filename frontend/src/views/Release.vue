<template>
  <div>
    <div class="head">
      <div>
        <h2 class="page-title">发布流程</h2>
        <p class="page-sub">定时发布 + 事前预告 + 多渠道投递（站内信 / 企业微信）</p>
      </div>
      <el-button type="primary" @click="openCreate">＋ 新建发布计划</el-button>
    </div>

    <el-card>
      <el-table :data="rows">
        <el-table-column prop="versionId" label="版本ID" width="100" />
        <el-table-column prop="audienceId" label="人群ID" width="100" />
        <el-table-column prop="channels" label="渠道" />
        <el-table-column prop="preNotifyDays" label="预告(天)" width="120" />
        <el-table-column prop="releaseTime" label="发布时间" width="180" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }"><el-tag>{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="warning" :disabled="row.status !== 'DRAFT'" @click="act('submit', row)">提交审批</el-button>
            <el-button link type="primary" :disabled="!['SCHEDULED','APPROVED'].includes(row.status)" @click="act('publish', row)">发布</el-button>
            <el-button link type="danger" @click="act('revoke', row)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="新建发布计划" width="520px">
      <el-form label-width="100px">
        <el-form-item label="关联版本">
          <el-select v-model="form.versionId" placeholder="选择版本" style="width: 100%" filterable>
            <el-option v-for="v in versions" :key="v.id" :label="versionLabel(v)" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="推送人群">
          <el-select v-model="form.audienceId" placeholder="选择人群模板" style="width: 100%" filterable>
            <el-option v-for="a in audiences" :key="a.id" :label="`${a.name}（命中 ${a.hitCountCache ?? '—'}）`" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="推送渠道">
          <el-checkbox-group v-model="channels">
            <el-checkbox value="IN_APP">站内信</el-checkbox>
            <el-checkbox value="WECOM">企业微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="事前预告">
          <el-checkbox-group v-model="preDays">
            <el-checkbox :value="3">提前3天</el-checkbox>
            <el-checkbox :value="1">提前1天</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker v-model="form.releaseTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { releaseApi, versionApi, audienceApi } from '@/api'

const rows = ref<any[]>([])
const versions = ref<any[]>([])
const audiences = ref<any[]>([])
const visible = ref(false)
const form = ref<any>({})
const channels = ref<string[]>(['IN_APP', 'WECOM'])
const preDays = ref<number[]>([3, 1])

function versionLabel(v: any) {
  return `${v.versionNo}（${statusText(v.status)}）`
}

const statusMap: Record<string, string> = {
  DRAFT: '草稿', REVIEW: '审批中', APPROVED: '已审批', SCHEDULED: '定时待发', PUBLISHED: '已发布', REVOKED: '已撤回',
}
const statusText = (s: string) => statusMap[s] || s

async function load() {
  rows.value = await releaseApi.list()
}

async function openCreate() {
  form.value = {}
  channels.value = ['IN_APP', 'WECOM']
  preDays.value = [3, 1]
  // 加载可选版本（已审批/定时待发优先）与人群模板
  const vp: any = await versionApi.page({ current: 1, size: 100 })
  versions.value = vp.records
  audiences.value = await audienceApi.list()
  visible.value = true
}

async function save() {
  const payload = {
    ...form.value,
    channels: JSON.stringify(channels.value),
    preNotifyDays: JSON.stringify(preDays.value),
    scheduleEnabled: true,
  }
  await releaseApi.create(payload)
  ElMessage.success('已创建')
  visible.value = false
  load()
}

async function act(type: 'submit' | 'publish' | 'revoke', row: any) {
  await releaseApi[type](row.id)
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-end; }
</style>
