import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'Manage',
    component: () => import('../views/Manage.vue'),
    redirect: '/home',
    children: [
      { path: 'home',     name: 'Home',     component: () => import('../views/Home.vue') },
      { path: 'person',   name: 'Person',   component: () => import('../views/Person.vue') },
      { path: 'password', name: 'Password', component: () => import('../views/Password.vue') },
      { path: 'employee', name: 'Employee', component: () => import('../views/Employee.vue') },
      { path: 'category', name: 'Category', component: () => import('../views/Category.vue') },
      { path: 'flower',   name: 'Flower',   component: () => import('../views/Flower.vue') },
      { path: 'order',    name: 'Order',    component: () => import('../views/Order.vue') },
      { path: 'comment',  name: 'Comment',  component: () => import('../views/Comment.vue') },
      { path: 'report',   name: 'Report',   component: () => import('../views/Report.vue') },
      { path: 'activity', name: 'Activity', component: () => import('../views/Activity.vue') },
      { path: 'shop',     name: 'Shop',     component: () => import('../views/Shop.vue') },
      { path: 'alipay',   name: 'Alipay',   component: () => import('../views/AlipayConfig.vue') },
      { path: 'ai-config', name: 'AIConfig', component: () => import('../views/AIConfig.vue') }
    ]
  },
  {
    path: '/404',
    name: '404',
    component: () => import('../views/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  localStorage.setItem('currentPathName', to.name)
  const manager = localStorage.getItem('manager')

  // 检查 manager 是否有效（不是 mock 数据，并且有 token）
  let isValidManager = false
  if (manager) {
    try {
      const parsed = JSON.parse(manager)
      isValidManager = !!parsed.token && !parsed.token?.includes('mock')
    } catch (e) {
      isValidManager = false
    }
  }

  if (to.name === 'Login' && isValidManager) {
    next('/')
    return
  }

  if (to.name !== 'Login' && !isValidManager) {
    // 清理无效数据
    localStorage.removeItem('manager')
    localStorage.removeItem('menus')
    next('/login')
    return
  }

  next()
})

export default router