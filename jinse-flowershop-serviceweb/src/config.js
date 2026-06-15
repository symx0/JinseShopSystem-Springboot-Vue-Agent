// 全局配置
export default {
  // API 基础路径
  apiBaseURL: '/api',
  // Agent服务地址（通过vite代理转发，避免跨域）
  agentBaseURL: '/agent',
  // 请求超时时间（毫秒）
  requestTimeout: 30000,
  // 订单页面自动刷新间隔（毫秒）
  orderRefreshInterval: 10000,
  // 管理端列表每页数量
  adminPageSize: 10
}
