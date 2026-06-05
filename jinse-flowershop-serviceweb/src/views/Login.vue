<template>
  <div class="login-wrapper">
    <div class="login-bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon">🌸</div>
        <h1 class="brand-title">锦瑟花店</h1>
        <p class="brand-subtitle">Jinse Flower Shop · 后台管理系统</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入账号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <div class="remember-password">
            <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>锦瑟无端五十弦，一弦一柱思华年</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

// 加载时检查是否有保存的密码
const loadSavedCredentials = () => {
  const savedData = localStorage.getItem('rememberMe')
  if (savedData) {
    try {
      const parsed = JSON.parse(savedData)
      if (parsed.username && parsed.password) {
        loginForm.username = parsed.username
        loginForm.password = parsed.password
        rememberMe.value = true
      }
    } catch (e) {
      console.error('加载记住的密码失败:', e)
    }
  }
}

// 保存或清除密码
const saveCredentials = () => {
  if (rememberMe.value) {
    localStorage.setItem('rememberMe', JSON.stringify({
      username: loginForm.username,
      password: loginForm.password
    }))
  } else {
    localStorage.removeItem('rememberMe')
  }
}

// 清理旧的登录数据
onMounted(() => {
  loadSavedCredentials()
  
  const oldManager = localStorage.getItem('manager')
  if (oldManager) {
    try {
      const parsed = JSON.parse(oldManager)
      // 如果是模拟登录的旧数据，或者没有 token 的旧数据，清理掉
      if (parsed.token?.includes('mock') || !parsed.token) {
        userStore.logout()
      }
    } catch (e) {
      userStore.logout()
    }
  }
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 2, max: 20, message: '账号长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度在 5 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await employeeApi.login(loginForm)
      if (res.code === 1) {
        saveCredentials()
        userStore.setManager(res.data)
        ElMessage.success('登录成功，欢迎回来')
        router.push('/')
      }
    } catch (e) {
      ElMessage.error(e?.message || '登录失败，请检查账号密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-wrapper {
  height: 100vh;
  background: linear-gradient(135deg, #2D2320 0%, #4A3035 40%, #6B3A42 70%, #8B4A50 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(232, 185, 107, 0.08);
}

.shape-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -150px;
  animation: float 8s ease-in-out infinite;
}

.shape-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -100px;
  animation: float 10s ease-in-out infinite reverse;
}

.shape-3 {
  width: 300px;
  height: 300px;
  top: 50%;
  left: 60%;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 48px 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(232, 185, 107, 0.2);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

.login-brand {
  text-align: center;
  margin-bottom: 36px;
}

.brand-icon {
  font-size: 48px;
  margin-bottom: 8px;
  filter: drop-shadow(0 2px 4px rgba(139, 74, 80, 0.3));
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  color: #2D2320;
  margin: 0 0 6px;
  letter-spacing: 4px;
}

.brand-subtitle {
  font-size: 13px;
  color: #9B8B85;
  margin: 0;
  letter-spacing: 1px;
}

.login-form {
  margin-top: 8px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #E5DDD8 inset;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C88A6E inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #8B5A3C inset, 0 0 0 4px rgba(139, 90, 60, 0.1);
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  letter-spacing: 6px;
  border-radius: 8px;
  background: linear-gradient(135deg, #6B3A42, #8B4A50);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #7B4A52, #9B5A60);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(139, 74, 80, 0.4);
}

.remember-password {
  display: flex;
  justify-content: flex-start;
  width: 100%;
}

.remember-password :deep(.el-checkbox__label) {
  color: #5D4A3A;
  font-size: 14px;
}

.remember-password :deep(.el-checkbox__inner:hover) {
  border-color: #8B4A50;
}

.remember-password :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #8B4A50;
  border-color: #8B4A50;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: #B5A39C;
  letter-spacing: 2px;
}
</style>