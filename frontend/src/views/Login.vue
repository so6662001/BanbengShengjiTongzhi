<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <div class="title">🛰️ 版本更新通知中心</div>
      <div class="sub">后台管理端登录</div>
      <el-form :model="form" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large"
            :prefix-icon="Lock" show-password @keyup.enter="onLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="onLogin">
          登录
        </el-button>
      </el-form>
      <div class="hint">演示账号：admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const form = ref({ username: 'admin', password: 'admin123' })
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

async function onLogin() {
  loading.value = true
  try {
    await auth.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    /* 错误已由拦截器提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100vh; display: grid; place-items: center;
  background: linear-gradient(135deg, #3b5bfd, #7b5bff);
}
.login-card { width: 360px; border-radius: 14px; }
.title { font-size: 20px; font-weight: 700; text-align: center; }
.sub { color: #99a0b0; text-align: center; margin: 6px 0 20px; font-size: 13px; }
.hint { color: #99a0b0; font-size: 12px; text-align: center; margin-top: 12px; }
</style>
