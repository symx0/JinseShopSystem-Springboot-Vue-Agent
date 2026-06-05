import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'flowers', name: 'FlowerList', component: () => import('../views/FlowerList.vue') },
      { path: 'flower/:id', name: 'FlowerDetail', component: () => import('../views/FlowerDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/Cart.vue') },
      { path: 'order', name: 'Order', component: () => import('../views/Order.vue') },
      { path: 'checkout', name: 'Checkout', component: () => import('../views/Checkout.vue') },
      { path: 'activity', name: 'Activity', component: () => import('../views/Activity.vue') },
      { path: 'usercenter', name: 'UserCenter', component: () => import('../views/UserCenter.vue') }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')

  if (to.name === 'Login' && user) {
    try {
      const parsed = JSON.parse(user)
      if (parsed.token) {
        next('/')
        return
      }
    } catch (e) {}
  }

  const publicPages = ['Login', 'Home', 'FlowerList', 'FlowerDetail', 'Activity']
  if (!user && !publicPages.includes(to.name)) {
    next('/login')
    return
  }

  next()
})

export default router