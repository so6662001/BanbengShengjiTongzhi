import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: '数据看板', component: () => import('@/views/Dashboard.vue') },
      { path: 'version', name: '版本管理', component: () => import('@/views/Version.vue') },
      { path: 'server', name: '服务器管理', component: () => import('@/views/Server.vue') },
      { path: 'audience', name: '客户分层', component: () => import('@/views/Audience.vue') },
      { path: 'release', name: '发布流程', component: () => import('@/views/Release.vue') },
      { path: 'approval', name: '审批中心', component: () => import('@/views/Approval.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    return '/login'
  }
  return true
})

export default router
