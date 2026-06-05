import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import config from '@/config'

const request = axios.create({
  baseURL: config.apiBaseURL,
  timeout: config.requestTimeout
})

request.interceptors.request.use(config => {
  if (!(config.data instanceof FormData)) {
    config.headers['Content-Type'] = 'application/json;charset=utf-8'
  }
  // 优先从内存(Pinia store)获取token，比localStorage更可靠
  try {
    const userStore = useUserStore()
    if (userStore.isLoggedIn && userStore.user.token) {
      config.headers['token'] = userStore.user.token
    }
  } catch (e) {
    // store未初始化时回退到localStorage
    try {
      const user = localStorage.getItem('user')
      if (user) {
        const parsed = JSON.parse(user)
        if (parsed.token) {
          config.headers['token'] = parsed.token
        }
      }
    } catch (e2) {}
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
    if (error.response && error.response.status === 401) {
      try {
        const userStore = useUserStore()
        if (userStore.isLoggedIn) {
          userStore.logout()
          ElMessage.warning('登录已过期，请重新登录')
        }
      } catch (e) {}
    }
    return Promise.reject(error)
  }
)

export default request