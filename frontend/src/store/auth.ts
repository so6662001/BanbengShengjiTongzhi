import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    nickname: localStorage.getItem('nickname') || '',
    roles: localStorage.getItem('roles') || '',
  }),
  getters: {
    isLogin: (s) => !!s.token,
  },
  actions: {
    async login(username: string, password: string) {
      const data: any = await authApi.login({ username, password })
      this.token = data.token
      this.nickname = data.nickname
      this.roles = data.roles
      localStorage.setItem('token', data.token)
      localStorage.setItem('nickname', data.nickname || '')
      localStorage.setItem('roles', data.roles || '')
    },
    logout() {
      this.token = ''
      this.nickname = ''
      this.roles = ''
      localStorage.removeItem('token')
      localStorage.removeItem('nickname')
      localStorage.removeItem('roles')
    },
  },
})
