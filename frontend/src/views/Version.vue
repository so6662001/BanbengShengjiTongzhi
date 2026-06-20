<template>
  <div>
    <div class="head">
      <div>
        <h2 class="page-title">版本管理</h2>
        <p class="page-sub">多产品多版本，更新内容按 新增/优化/修复 结构化录入 + 版本说明</p>
      </div>
      <el-button type="primary" @click="openEdit()">＋ 新建版本</el-button>
    </div>

    <el-card style="margin-bottom: 14px">
      <el-select v-model="filterProductId" placeholder="全部产品" clearable style="width: 200px" @change="load">
        <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
    </el-card>

    <el-card>
      <el-table :data="rows">
        <el-table-column label="产品" width="140">
          <template #default="{ row }">{{ productName(row.productId) }}</template>
        </el-table-column>
        <el-table-column prop="versionNo" label="版本号" width="120" />
        <el-table-column prop="description" label="版本说明" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }"><el-tag>{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="preview(row)">预览</el-button>
            <el-button link type="warning" :disabled="row.status !== 'DRAFT'" @click="submit(row)">提交审批</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="current"
        @current-change="load" style="margin-top: 14px; justify-content: flex-end" />
    </el-card>

    <el-dialog v-model="editVisible" title="版本编辑" width="640px">
      <el-form label-width="92px">
        <el-form-item label="产品线">
          <el-select v-model="form.productId" placeholder="选择产品">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号"><el-input v-model="form.versionNo" placeholder="v3.2.0" /></el-form-item>
        <el-form-item label="发布类型">
          <el-radio-group v-model="form.releaseType">
            <el-radio value="RELEASE">正式发布</el-radio>
            <el-radio value="GRAY">灰度发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="版本说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="本次版本整体概述（展示在弹窗顶部与日志页）" />
        </el-form-item>
        <el-form-item label="更新条目">
          <div style="width: 100%">
            <div v-for="(it, i) in form.items" :key="i" class="item-row">
              <el-select v-model="it.category" style="width: 110px">
                <el-option label="新增" value="ADD" />
                <el-option label="优化" value="OPTIMIZE" />
                <el-option label="修复" value="FIX" />
              </el-select>
              <el-input v-model="it.title" placeholder="条目标题" />
              <el-button link type="danger" @click="form.items.splice(i, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="addItem('ADD')">＋ 新增</el-button>
            <el-button size="small" @click="addItem('OPTIMIZE')">⤴ 优化</el-button>
            <el-button size="small" @click="addItem('FIX')">🔧 修复</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="弹窗预览" width="480px">
      <h3>{{ previewData?.versionNo }} 更新说明</h3>
      <p style="color: #5a6273">{{ previewData?.description }}</p>
      <div v-for="(it, i) in previewData?.items || []" :key="i" style="margin: 6px 0">
        <el-tag size="small" :type="tagType(it.category)">{{ it.categoryDesc }}</el-tag>
        <span style="margin-left: 8px">{{ it.title }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { productApi, versionApi } from '@/api'

const products = ref<any[]>([])
const rows = ref<any[]>([])
const total = ref(0)
const current = ref(1)
const size = 10
const filterProductId = ref<number | undefined>()

const editVisible = ref(false)
const form = ref<any>({ items: [] })
const previewVisible = ref(false)
const previewData = ref<any>(null)

const statusMap: Record<string, string> = {
  DRAFT: '草稿', REVIEW: '审批中', APPROVED: '已审批', SCHEDULED: '定时待发', PUBLISHED: '已发布', REVOKED: '已撤回',
}
const statusText = (s: string) => statusMap[s] || s
const productName = (id: number) => products.value.find((p) => p.id === id)?.name || id
const tagType = (c: string) => (c === 'ADD' ? 'success' : c === 'OPTIMIZE' ? 'primary' : 'warning')

async function load() {
  const res: any = await versionApi.page({ productId: filterProductId.value, current: current.value, size })
  rows.value = res.records
  total.value = res.total
}

function openEdit(row?: any) {
  if (row) {
    form.value = { ...row, items: [] }
    versionApi.items(row.id).then((items: any) => (form.value.items = items))
  } else {
    form.value = { releaseType: 'RELEASE', items: [] }
  }
  editVisible.value = true
}

function addItem(category: string) {
  form.value.items.push({ category, title: '' })
}

async function save() {
  await versionApi.save(form.value)
  ElMessage.success('已保存')
  editVisible.value = false
  load()
}

async function submit(row: any) {
  await versionApi.submit(row.id)
  ElMessage.success('已提交审批')
  load()
}

async function preview(row: any) {
  previewData.value = await versionApi.preview(row.id)
  previewVisible.value = true
}

onMounted(async () => {
  products.value = await productApi.list()
  load()
})
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-end; }
.item-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.item-row .el-input { flex: 1; }
</style>
