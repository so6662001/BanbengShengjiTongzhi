<template>
  <div>
    <div class="head">
      <div>
        <h2 class="page-title">服务器管理</h2>
        <p class="page-sub">一台服务器可部署多个产品；客户按服务器归属</p>
      </div>
      <el-button type="primary" @click="openEdit()">＋ 新增服务器</el-button>
    </div>

    <el-card>
      <el-table :data="rows">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="code" label="编码" width="140" />
        <el-table-column label="部署产品（多个）">
          <template #default="{ row }">
            <el-tag v-for="pid in row.productIds || []" :key="pid" size="small" style="margin-right: 6px">
              {{ productName(pid) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="env" label="环境" width="90" />
        <el-table-column prop="region" label="地域" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="服务器编辑" width="520px">
      <el-form label-width="92px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="环境">
          <el-radio-group v-model="form.env">
            <el-radio value="PROD">生产</el-radio>
            <el-radio value="TEST">测试</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地域"><el-input v-model="form.region" /></el-form-item>
        <el-form-item label="部署产品">
          <el-select v-model="productIds" multiple placeholder="可多选" style="width: 100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
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
import { productApi, serverApi } from '@/api'

const products = ref<any[]>([])
const rows = ref<any[]>([])
const visible = ref(false)
const form = ref<any>({ env: 'PROD' })
const productIds = ref<number[]>([])

const productName = (id: number) => products.value.find((p) => p.id === id)?.name || id

async function load() {
  const list: any = await serverApi.list()
  for (const s of list) {
    s.productIds = await serverApi.products(s.id)
  }
  rows.value = list
}

function openEdit(row?: any) {
  if (row) {
    form.value = { ...row }
    productIds.value = row.productIds || []
  } else {
    form.value = { env: 'PROD' }
    productIds.value = []
  }
  visible.value = true
}

async function save() {
  const payload = { server: form.value, productIds: productIds.value }
  if (form.value.id) await serverApi.update(payload)
  else await serverApi.create(payload)
  ElMessage.success('已保存')
  visible.value = false
  load()
}

onMounted(async () => {
  products.value = await productApi.list()
  load()
})
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-end; }
</style>
