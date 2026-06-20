<template>
  <div>
    <h2 class="page-title">审批中心</h2>
    <p class="page-sub">按产品线多级审批，文案审批全程留痕</p>

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card>
          <template #header>待我审批</template>
          <el-table :data="todoList">
            <el-table-column prop="bizType" label="类型" width="130">
              <template #default="{ row }">{{ row.bizType === 'VERSION' ? '版本文案' : '发布计划' }}</template>
            </el-table-column>
            <el-table-column prop="bizId" label="业务ID" width="100" />
            <el-table-column prop="nodeLevel" label="层级" width="80" />
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button link type="success" @click="approve(row, true)">通过</el-button>
                <el-button link type="danger" @click="approve(row, false)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!todoList.length" description="暂无待办" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>各产品线审批流</template>
          <el-table :data="flows" size="small">
            <el-table-column prop="name" label="审批流" />
            <el-table-column prop="productId" label="产品ID" width="100" />
            <el-table-column prop="enabled" label="启用" width="80">
              <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="passing ? '通过审批' : '驳回审批'" width="420px">
      <el-input v-model="comment" type="textarea" :rows="3" placeholder="审批意见" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :type="passing ? 'success' : 'danger'" @click="confirm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { approvalApi } from '@/api'

const todoList = ref<any[]>([])
const flows = ref<any[]>([])
const dialogVisible = ref(false)
const passing = ref(true)
const comment = ref('')
const currentRow = ref<any>(null)

async function load() {
  todoList.value = await approvalApi.todo()
  flows.value = await approvalApi.flows()
}

function approve(row: any, pass: boolean) {
  currentRow.value = row
  passing.value = pass
  comment.value = ''
  dialogVisible.value = true
}

async function confirm() {
  await approvalApi.approve(currentRow.value.id, { pass: passing.value, comment: comment.value })
  ElMessage.success('已处理')
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>
