<template>
  <div>
    <h2 class="page-title">客户分层圈选</h2>
    <p class="page-sub">套餐 ∩ 行业 ∩ 产品 ∩ 服务器，实时命中数，可保存人群模板</p>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header>圈选条件</template>
          <el-form label-width="92px">
            <el-form-item label="使用产品">
              <el-select v-model="cond.productId" placeholder="选择产品" @change="onProduct">
                <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="客户套餐">
              <el-checkbox-group v-model="cond.packages">
                <el-checkbox value="FLAGSHIP">旗舰版</el-checkbox>
                <el-checkbox value="PRO">专业版</el-checkbox>
                <el-checkbox value="STANDARD">标准版</el-checkbox>
                <el-checkbox value="BASIC">基础版</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="客户行业">
              <el-checkbox-group v-model="cond.industries">
                <el-checkbox value="商贸零售">商贸零售</el-checkbox>
                <el-checkbox value="制造业">制造业</el-checkbox>
                <el-checkbox value="物流仓储">物流仓储</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="归属服务器">
              <el-select v-model="cond.serverNodeIds" multiple placeholder="可多选" style="width: 100%">
                <el-option v-for="s in servers" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="待升级版本">
              <el-input v-model="cond.versionBelow" placeholder="如 v3.2.0（仅命中低于该版本的客户，可空）" />
            </el-form-item>
            <el-button type="primary" @click="doPreview">圈选预览</el-button>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>命中预览</template>
          <div style="text-align: center; padding: 16px 0">
            <div class="stat-label">符合条件客户数</div>
            <div style="font-size: 44px; font-weight: 800; color: var(--primary)">{{ hit.hitCount }}</div>
          </div>
          <el-divider />
          <div v-for="(v, k) in hit.industryDistribution" :key="k" class="dist-row">
            <span>{{ k }}</span><b>{{ v }}</b>
          </div>
          <el-divider />
          <el-input v-model="audienceName" placeholder="人群模板名称" style="margin-bottom: 10px" />
          <el-button type="primary" :disabled="!hit.hitCount" @click="saveTemplate">保存为人群模板</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header>已保存人群模板</template>
      <el-table :data="templates" size="small">
        <el-table-column prop="name" label="模板名称" />
        <el-table-column prop="hitCountCache" label="命中客户" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { audienceApi, productApi, serverApi } from '@/api'

const products = ref<any[]>([])
const servers = ref<any[]>([])
const cond = ref<any>({ packages: [], industries: [], serverNodeIds: [] })
const hit = ref<any>({ hitCount: 0, industryDistribution: {} })
const audienceName = ref('')
const templates = ref<any[]>([])

function onProduct() { /* 可按产品过滤服务器，演示从略 */ }

async function doPreview() {
  if (!cond.value.productId) {
    ElMessage.warning('请先选择产品')
    return
  }
  hit.value = await audienceApi.preview(cond.value)
}

async function saveTemplate() {
  if (!audienceName.value) {
    ElMessage.warning('请输入模板名称')
    return
  }
  await audienceApi.save({ name: audienceName.value, condition: cond.value })
  ElMessage.success('已保存')
  loadTemplates()
}

async function loadTemplates() {
  templates.value = await audienceApi.list()
}

onMounted(async () => {
  products.value = await productApi.list()
  servers.value = await serverApi.list()
  loadTemplates()
})
</script>

<style scoped>
.dist-row { display: flex; justify-content: space-between; padding: 4px 0; color: #5a6273; }
</style>
