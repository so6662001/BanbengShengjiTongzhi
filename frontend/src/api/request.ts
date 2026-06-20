import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: '/',
  timeout: 15000,
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (resp) => {
    const data = resp.data
    if (data && typeof data.code !== 'undefined') {
      if (data.code === 0) {
        return data.data
      }
      ElMessage.error(data.message || '请求失败')
      if (data.code === 2001) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    ElMessage.error(err.message || '网络错误')
    return Promise.reject(err)
  },
)

/**
 * 类型化包装：响应拦截器已把后端 Result.data 解包，
 * 故这里直接把返回声明为 Promise<T>，避免 AxiosResponse 类型噪音。
 */
const request = {
  get: <T = any>(url: string, config?: AxiosRequestConfig) =>
    instance.get(url, config) as unknown as Promise<T>,
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
    instance.post(url, data, config) as unknown as Promise<T>,
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
    instance.put(url, data, config) as unknown as Promise<T>,
  delete: <T = any>(url: string, config?: AxiosRequestConfig) =>
    instance.delete(url, config) as unknown as Promise<T>,
}

export default request
