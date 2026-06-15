import request from '@/utils/request'

export const userApi = {
  login: (data) => request.post('/user/user/login', data),
  register: (data) => request.post('/user/user/register', data),
  logout: () => request.post('/user/user/logout'),
  getUserInfo: () => request.get('/user/user/getUserInfo'),
  modifyInfo: (data) => request.post('/user/user/modifyInfo', data),
  changePassword: (data) => request.put('/user/user/changePassword', data),
  listOwnComment: () => request.get('/user/user/list-own-comment'),
  likeComment: (commentId) => request.post(`/user/comment/like/${commentId}`),
  upload: (formData) => request.post('/user/common/upload', formData)
}

export const flowerApi = {
  getByCategoryId: (categoryId) => request.get('/user/flower/getByCategoryId', { params: { categoryId } }),
  page: (params) => request.get('/user/flower/page', { params }),
  getById: (id) => request.get(`/user/flower/id/${id}`),
  shop: () => request.get('/user/flower/shop')
}

export const categoryApi = {
  list: (type) => request.get('/user/category/list', { params: { type } })
}

export const activityApi = {
  page: (params) => request.get('/user/activity/page', { params }),
  getById: (id) => request.get(`/user/activity/${id}`),
  sale: (params) => request.get('/user/activity/sale', { params })
}

export const commentApi = {
  add: (data) => request.post('/user/comment/add', data),
  delete: (commentId) => request.delete(`/user/comment/${commentId}`),
  list: (data) => request.post('/user/comment/list', data)
}

export const cartApi = {
  add: (data) => request.post('/user/shoppingCart/add', data),
  sub: (data) => request.post('/user/shoppingCart/sub', data),
  list: () => request.get('/user/shoppingCart/list'),
  clean: () => request.delete('/user/shoppingCart/clean')
}

export const orderApi = {
  submit: (data) => request.post('/user/order/submit', data),
  list: () => request.get('/user/order/list'),
  detail: (orderId) => request.get(`/user/order/detail/${orderId}`),
  receipt: (orderId) => request.put(`/user/order/receipt/${orderId}`),
  refund: (orderId) => request.put(`/user/order/refund/${orderId}`),
  cancel: (orderId) => request.put(`/user/order/cancel/${orderId}`),
  checkPaymentStatus: (orderId) => request.get(`/user/order/payment/status/${orderId}`),
  mockPayment: (orderId) => request.put(`/user/order/mock-payment/${orderId}`)
}

export const addressApi = {
  list: () => request.get('/user/addressBook/list'),
  getById: (id) => request.get(`/user/addressBook/${id}`),
  add: (data) => request.post('/user/addressBook', data),
  update: (data) => request.put('/user/addressBook', data),
  setDefault: (data) => request.put('/user/addressBook/default', data),
  delete: (id) => request.delete('/user/addressBook', { params: { id } })
}

export const shopApi = {
  getStatus: () => request.get('/user/shop/status'),
  getPaymentMode: () => request.get('/user/shop/paymentMode'),
  getFee: () => request.get('/user/shop/fee')
}

// Agent智能导购API（直连FastAPI Agent服务）
import axios from 'axios'
import config from '@/config'

const agentRequest = axios.create({
  baseURL: config.agentBaseURL,
  timeout: 60000
})

export const agentApi = {
  chat: (sessionId, message, userId = null) => agentRequest.post('/chat', { session_id: sessionId, message, user_id: userId }),
  // 流式聊天：返回 EventSource 可用的 URL
  chatStreamUrl: (sessionId, message, userId) => {
    const base = agentRequest.defaults.baseURL || '/agent'
    const params = new URLSearchParams()
    // 对于 SSE，需要将参数通过 POST body 传递，但 EventSource 只支持 GET。
    // 改用 fetch + ReadableStream
    return `${base}/chat/stream`
  },
  getOrder: (sessionId) => agentRequest.get(`/order/${sessionId}`),
  updateOrder: (sessionId, data) => agentRequest.post(`/order/${sessionId}/update`, data),
  confirmOrder: (sessionId, data) => agentRequest.post(`/order/${sessionId}/confirm`, data),
  resetSession: (sessionId) => agentRequest.post(`/session/${sessionId}/reset`),
  getHistory: (sessionId, userId) => agentRequest.get(`/session/${sessionId}/history`, { params: { user_id: userId } }),
  health: () => agentRequest.get('/health')
}

// Agent会话管理API（通过Spring Boot后端，会话标识=DB自增主键）
export const agentSessionApi = {
  list: () => request.get('/user/agent/sessions'),
  create: (title) => request.post('/user/agent/session', { title }),
  rename: (sessionId, title) => request.put(`/user/agent/session/${sessionId}/title`, { title }),
  touch: (sessionId) => request.post(`/user/agent/session/${sessionId}/touch`),
  delete: (sessionId) => request.delete(`/user/agent/session/${sessionId}`)
}