import axios from 'axios'
import { ElMessage } from 'element-plus'
import config from '@/config'

const request = axios.create({
  baseURL: config.apiBaseURL,
  timeout: config.requestTimeout
})

request.interceptors.request.use(config => {
  config.headers['Content-Type'] = 'application/json;charset=utf-8'
  const manager = localStorage.getItem('manager')
  if (manager) {
    const parsed = JSON.parse(manager)
    if (parsed.token) {
      config.headers['token'] = parsed.token
    }
  }
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (response.config.responseType === 'blob') {
      return res
    }
    if (typeof res === 'string') {
      return JSON.parse(res)
    }
    if (res.code !== 1) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    console.error('网络请求失败:', error)
    // 401 未授权，说明 token 无效，需要重新登录
    if (error.response && error.response.status === 401) {
      const userStore = require('@/store').useUserStore()
      userStore.logout()
    }
    return Promise.reject(error)
  }
)

export default request