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