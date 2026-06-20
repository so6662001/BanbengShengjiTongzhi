<template>
  <div>
    <div class="head">
      <div>
        <h2 class="page-title">客户管理</h2>
        <p class="page-sub">客户资料、套餐/行业、企微/邮箱/手机，以及「产品-服务器」归属维护</p>
      </div>
      <el-button type="primary" @click="openEdit()">＋ 新增客户</el-button>
    </div>

    <el-card style="margin-bottom: 14px">
      <el-input v-model="keyword" placeholder="🔍 搜索客户名称" style="width: 240px" @keyup.enter="load" clearable />
      <el-button style="margin-left: 8px" @click="load">查询</el-button>
    </el-card>

    <el-card>
      <el-table :data="rows">
        <el-table-column prop="name" label="客户名称" />
        <el-table-column prop="industry" label="行业" width="120" />
        <el-table-column label="套餐" width="100">
          <template #default="{ row }">{{ packageText(row.packageLevel) }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="wecomUserIds" label="企微" width="120" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openBinding(row)">归属维护</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="current"
        @current-change="load" style="margin-top: 14px; justify-content: flex-end" />
    </el-card>

    <!-- 新增/编辑客户 -->
    <el-dialog v-model="editVisible" title="客户编辑" width="520px">
      <el-form label-width="92px">
        <el-form-item label="客户名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="行业"><el-input v-model="form.industry" placeholder="如 商贸零售" /></el-form-item>
        <el-form-item label="套餐">
          <el-select v-model="form.packageLevel" placeholder="选择套餐" style="width: 100%">
            <el-option label="旗舰版" value="FLAGSHIP" />
            <el-option label="专业版" value="PRO" />
            <el-option label="标准版" value="STANDARD" />
            <el-option label="基础版" value="BASIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="企微接收人"><el-input v-model="form.wecomUserIds" placeholder="userid，逗号分隔" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 归属维护 -->
    <el-dialog v-model="bindingVisible" :title="`归属维护 · ${current_.name || ''}`" width="560px">
      <el-table :data="bindings" size="small" style="margin-bottom: 12px">
        <el-table-column label="产品" width="160">
          <template #default="{ row }">{{ productName(row.productId) }}</template>
        </el-table-column>
        <el-table-column label="服务器" width="180">
          <template #default="{ row }">{{ serverName(row.serverNodeId) }}</template>
        </el-table-column>
        <el-table-column prop="currentVersion" label="当前版本" />
      </el-table>
      <el-divider>新增 / 更新归属</el-divider>
      <el-form :inline="true">
        <el-form-item label="产品">
          <el-select v-model="bindForm.productId" placeholder="产品" style="width: 140px">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务器">
          <el-select v-model="bindForm.serverNodeId" placeholder="服务器" style="width: 160px">
            <el-option v-for="s in servers" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前版本">
          <el-input v-model="bindForm.currentVersion" placeholder="v3.1.9" style="width: 120px" />
        </el-form-item>
        <el-button type="primary" @click="saveBinding">保存归属</el-button>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { customerApi, productApi, serverApi } from '@/api'

const rows = ref<any[]>([])
const total = ref(0)
const current = ref(1)
const size = 10
const keyword = ref('')
const products = ref<any[]>([])
const servers = ref<any[]>([])

const editVisible = ref(false)
const form = ref<any>({})

const bindingVisible = ref(false)
const current_ = ref<any>({})
const bindings = ref<any[]>([])
const bindForm = ref<any>({})

const packageMap: Record<string, string> = { FLAGSHIP: '旗舰版', PRO: '专业版', STANDARD: '标准版', BASIC: '基础版' }
const packageText = (p: string) => packageMap[p] || p || '—'
const productName = (id: number) => products.value.find((p) => p.id === id)?.name || id
const serverName = (id: number) => servers.value.find((s) => s.id === id)?.name || id

async function load() {
  const res: any = await customerApi.page({ keyword: keyword.value, current: current.value, size })
  rows.value = res.records
  total.value = res.total
}

function openEdit(row?: any) {
  form.value = row ? { ...row } : {}
  editVisible.value = true
}

async function save() {
  if (form.value.id) await customerApi.update(form.value)
  else await customerApi.create(form.value)
  ElMessage.success('已保存')
  editVisible.value = false
  load()
}

async function openBinding(row: any) {
  current_.value = row
  bindForm.value = { customerId: row.id }
  bindings.value = await customerApi.products(row.id)
  bindingVisible.value = true
}

async function saveBinding() {
  await customerApi.upsertBinding({ ...bindForm.value, customerId: current_.value.id })
  ElMessage.success('归属已保存')
  bindings.value = await customerApi.products(current_.value.id)
}

onMounted(async () => {
  products.value = await productApi.list()
  servers.value = await serverApi.list()
  load()
})
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-end; }
</style>
