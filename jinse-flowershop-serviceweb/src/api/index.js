import request from '@/utils/request'

export const employeeApi = {
  login: (data) => request.post('/admin/employee/login', data),
  logout: () => request.post('/admin/employee/logout'),
  page: (params) => request.get('/admin/employee/page', { params }),
  save: (data) => request.post('/admin/employee/save', data),
  getById: (id) => request.get(`/admin/employee/${id}`),
  update: (data) => request.put('/admin/employee', data),
  setStatus: (status, id) => request.post(`/admin/employee/status/${status}`, null, { params: { id } }),
  setStatusBatch: (status, ids) => request.post(`/admin/employee/status/batch/${status}`, null, { params: { ids: ids.join(',') } }),
  delete: (ids) => request.delete('/admin/employee', { params: { ids: ids.join(',') } })
}

export const categoryApi = {
  page: (params) => request.get('/admin/category/page', { params }),
  list: (type) => request.get('/admin/category/list', { params: { type } }),
  save: (data) => request.post('/admin/category', data),
  update: (data) => request.put('/admin/category', data),
  setStatus: (status, id) => request.post(`/admin/category/status/${status}`, null, { params: { id } }),
  setStatusBatch: (status, ids) => request.post(`/admin/category/status/batch/${status}`, null, { params: { ids: ids.join(',') } }),
  delete: (ids) => request.delete('/admin/category', { params: { ids: ids.join(',') } })
}

export const flowerApi = {
  page: (params) => request.get('/admin/flower/page', { params }),
  getById: (id) => request.get(`/admin/flower/${id}`),
  save: (data) => request.post('/admin/flower', data),
  update: (data) => request.put('/admin/flower', data),
  setStatus: (status, id) => request.put('/admin/flower', { id, status }),
  setStatusBatch: (status, ids) => request.post(`/admin/flower/status/batch/${status}`, null, { params: { ids: ids.join(',') } }),
  delete: (ids) => request.delete('/admin/flower', { params: { ids: ids.join(',') } }),
}

export const commonApi = {
  upload: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/admin/common/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export const activityApi = {
  page: (params) => request.get('/admin/activity/page', { params }),
  create: (data) => request.post('/admin/activity/create', data),
  update: (data) => request.put('/admin/activity', data),
  getById: (id) => request.get(`/admin/activity/${id}`),
  setSale: (data) => request.post('/admin/activity/set', data),
  salePage: (params) => request.get('/admin/activity/sale/page', { params }),
  deleteSale: (id, activityId, flowerId) => request.delete('/admin/activity/sale', { params: { id, activityId, flowerId } }),
  updateSale: (data) => request.put('/admin/activity/sale', data),
  setStatus: (status, id) => request.post(`/admin/activity/status/${status}`, null, { params: { id } }),
  setStatusBatch: (status, ids) => request.post(`/admin/activity/status/batch/${status}`, null, { params: { ids: ids.join(',') } }),
  delete: (ids) => request.delete('/admin/activity', { params: { ids: ids.join(',') } }),
  stats: (activityId) => request.get(`/admin/activity/stats/${activityId}`)
}

export const reportApi = {
  turnover: (begin, end) => request.get('/admin/report/turnoverStatistics', { params: { begin, end } }),
  users: (begin, end) => request.get('/admin/report/userStatistics', { params: { begin, end } }),
  orders: (begin, end) => request.get('/admin/report/ordersStatistics', { params: { begin, end } }),
  top10: (params) => request.get('/admin/report/top10', { params })
}

export const shopApi = {
  getStatus: () => request.get('/admin/shop/status'),
  setStatus: (status) => request.put(`/admin/shop/${status}`),
  getDeliveryDays: () => request.get('/admin/shop/deliveryDays'),
  setDeliveryDays: (days) => request.put('/admin/shop/deliveryDays', null, { params: { days } }),
  getAlipayConfig: () => request.get('/admin/shop/alipay/config'),
  saveAlipayConfig: (data) => request.put('/admin/shop/alipay/config', data),
  getPaymentMode: () => request.get('/admin/shop/paymentMode'),
  setPaymentMode: (mode) => request.put(`/admin/shop/paymentMode/${mode}`),
  getFee: () => request.get('/admin/shop/fee'),
  setFee: (data) => request.put('/admin/shop/fee', data)
}

export const userApi = {
  page: (params) => request.get('/admin/user/page', { params })
}

export const orderApi = {
  page: (params) => request.get('/admin/order/page', { params }),
  detail: (id) => request.get(`/admin/order/detail/${id}`),
  confirm: (id) => request.put('/admin/order/confirm', { id }),
  delivery: (id) => request.put('/admin/order/delivery', { id }),
  complete: (id) => request.put('/admin/order/complete', { id }),
  cancel: (id, cancelReason) => request.put('/admin/order/cancel', { id, cancelReason }),
  approveRefund: (id) => request.put(`/admin/order/approve-refund/${id}`),
  rejectRefund: (data) => request.put('/admin/order/reject-refund', data),
  delete: (ids) => request.delete('/admin/order', { params: { ids: ids.join(',') } })
}