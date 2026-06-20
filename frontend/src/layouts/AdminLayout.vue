<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" class="sidebar">
      <div class="brand">🛰️ 版本通知中心</div>
      <el-menu :default-active="route.path" router background-color="#131a2b" text-color="#aeb6cc"
        active-text-color="#fff">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>数据看板</span></el-menu-item>
        <el-menu-item index="/version"><el-icon><Files /></el-icon><span>版本管理</span></el-menu-item>
        <el-menu-item index="/server"><el-icon><Monitor /></el-icon><span>服务器管理</span></el-menu-item>
        <el-menu-item index="/customer"><el-icon><User /></el-icon><span>客户管理</span></el-menu-item>
        <el-menu-item index="/audience"><el-icon><Aim /></el-icon><span>客户分层</span></el-menu-item>
        <el-menu-item index="/release"><el-icon><Promotion /></el-icon><span>发布流程</span></el-menu-item>
        <el-menu-item index="/approval"><el-icon><Select /></el-icon><span>审批中心</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <span class="crumb">版本更新通知工具 / <b>{{ route.name }}</b></span>
        <el-dropdown @command="onCommand">
          <span class="user">{{ auth.nickname || '管理员' }} <el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

function onCommand(cmd: string) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.sidebar { background: #131a2b; color: #fff; }
.brand { color: #fff; font-weight: 700; font-size: 16px; padding: 18px 20px; }
.sidebar :deep(.el-menu) { border-right: none; }
.topbar {
  background: #fff; border-bottom: 1px solid #e9ecf2; display: flex;
  align-items: center; justify-content: space-between;
}
.crumb { color: #99a0b0; font-size: 13px; }
.crumb b { color: #1f2533; }
.user { cursor: pointer; color: #5a6273; }
.el-main { background: #f5f6fa; }
</style>
