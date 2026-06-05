<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="header-brand">
        <span class="brand-icon">🌸</span>
        <span class="brand-text">锦瑟花店</span>
      </router-link>
      <nav class="header-nav">
        <router-link to="/home" class="nav-link">首页</router-link>
        <router-link to="/flowers" class="nav-link">鲜花商城</router-link>
        <router-link to="/activity" class="nav-link">热门活动</router-link>
        <div class="nav-cart" @click="$router.push('/cart')">
          <el-badge :value="cartCount" :hidden="cartCount === 0" :max="99">
            <el-icon :size="22"><ShoppingCart /></el-icon>
          </el-badge>
        </div>
        <router-link v-if="userStore.isLoggedIn" to="/order" class="nav-link">
          <el-icon style="vertical-align:middle;margin-right:2px"><Document /></el-icon>我的订单
        </router-link>
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click">
            <div class="nav-user">
              <img v-if="userStore.user?.avatar" :src="userStore.user.avatar" class="nav-avatar" />
              <el-icon v-else :size="20"><UserFilled /></el-icon>
              <span class="user-name">{{ userStore.userName }}</span>
              <el-icon :size="14"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/usercenter')">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-login-btn">登录 / 注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, UserFilled, ArrowDown, User, Document, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { userApi, cartApi } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartCount = ref(0)

const loadCartCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await cartApi.list()
    if (res.code === 1 && Array.isArray(res.data)) {
      cartCount.value = res.data.reduce((sum, item) => sum + (item.number || 0), 0)
    }
  } catch (e) {}
}

const handleLogout = async () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await userApi.logout()
      if (res.code === 1) {
        ElMessage.success(res.data || '退出成功')
      }
    } catch (e) {}
    userStore.logout()
    cartCount.value = 0
  }).catch(() => {})
}

onMounted(() => {
  loadCartCount()
  refreshUserInfo()
  // 每5分钟心跳，保持会话活跃
  setInterval(() => {
    if (userStore.isLoggedIn) {
      refreshUserInfo()
    }
  }, 5 * 60 * 1000)
})

watch(() => route.path, () => {
  loadCartCount()
})

// 监听登录状态，如果被清除则跳转登录页
watch(() => userStore.isLoggedIn, (val) => {
  if (!val && route.path !== '/login') {
    router.push('/login')
  }
})

const refreshUserInfo = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await userApi.getUserInfo()
    if (res.code === 1 && res.data) {
      const user = { ...userStore.user, name: res.data.name, avatar: res.data.avatar }
      userStore.setUser(user)
    }
  } catch (e) {}
}
</script>

<style scoped>
.app-header {
  background: linear-gradient(135deg, #2D2320 0%, #4A3035 100%);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.brand-icon {
  font-size: 28px;
}

.brand-text {
  font-size: 22px;
  font-weight: 700;
  color: #E8B96B;
  letter-spacing: 4px;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  color: #D4C4B8;
  font-size: 15px;
  transition: color 0.3s;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #E8B96B;
  border-bottom-color: #E8B96B;
}

.nav-cart {
  cursor: pointer;
  color: #D4C4B8;
  padding: 6px;
  transition: color 0.3s;
}

.nav-cart:hover {
  color: #E8B96B;
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #D4C4B8;
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid rgba(232, 185, 107, 0.3);
  transition: all 0.3s;
}

.nav-user:hover {
  color: #E8B96B;
  border-color: #E8B96B;
}

.user-name {
  font-size: 14px;
}

.nav-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.nav-login-btn {
  color: #E8B96B;
  font-size: 14px;
  padding: 6px 18px;
  border: 1px solid #E8B96B;
  border-radius: 20px;
  transition: all 0.3s;
}

.nav-login-btn:hover {
  background: #E8B96B;
  color: #2D2320;
}
</style>