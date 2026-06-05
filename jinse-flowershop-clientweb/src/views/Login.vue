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
        <p class="brand-subtitle">Jinse Flower Shop</p>
      </div>
      <el-tabs v-model="activeTab" class="login-tabs" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="auth-form">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入账号" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="auth-form">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入账号" size="large" />
            </el-form-item>
            <el-form-item prop="name">
              <el-input v-model="registerForm.name" placeholder="请输入姓名" size="large" />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input v-model="registerForm.phone" placeholder="请输入手机号" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" size="large" show-password />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">注 册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="login-footer">
        <router-link to="/home" class="back-home">← 返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', name: '', phone: '', password: '', confirmPassword: '' })

const loginRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 2, max: 20, message: '账号长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度在 5 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadSavedCredentials = () => {
  try {
    const savedData = localStorage.getItem('rememberMe')
    if (savedData) {
      const parsed = JSON.parse(savedData)
      if (parsed.username && parsed.password) {
        loginForm.username = parsed.username
        loginForm.password = parsed.password
        rememberMe.value = true
      }
    }
  } catch (e) {}
}

const saveCredentials = () => {
  if (rememberMe.value) {
    try {
      localStorage.setItem('rememberMe', JSON.stringify({
        username: loginForm.username,
        password: loginForm.password
      }))
    } catch (e) {}
  } else {
    try { localStorage.removeItem('rememberMe') } catch (e) {}
  }
}

onMounted(() => {
  loadSavedCredentials()
  const user = localStorage.getItem('user')
  if (user) {
    try {
      const parsed = JSON.parse(user)
      if (parsed.token) {
        router.push('/')
      }
    } catch (e) {}
  }
})

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await userApi.login(loginForm)
      if (res.code === 1) {
        saveCredentials()
        userStore.setUser(res.data)
        ElMessage.success('登录成功，欢迎光临锦瑟花店')
        router.push('/')
      }
    } catch (e) {
      ElMessage.error(e?.message || '登录失败，请检查账号密码')
    } finally {
      loading.value = false
    }
  })
}

const handleRegister = () => {
  registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await userApi.register({
        username: registerForm.username,
        password: registerForm.password,
        name: registerForm.name,
        phone: registerForm.phone
      })
      if (res.code === 1) {
        ElMessage.success('注册成功，请登录')
        activeTab.value = 'login'
        loginForm.username = registerForm.username
        loginForm.password = registerForm.password
        registerForm.username = ''
        registerForm.name = ''
        registerForm.phone = ''
        registerForm.password = ''
        registerForm.confirmPassword = ''
      }
    } catch (e) {
      ElMessage.error(e?.message || '注册失败，请重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
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

.shape-1 { width: 600px; height: 600px; top: -200px; right: -150px; animation: float 8s ease-in-out infinite; }
.shape-2 { width: 400px; height: 400px; bottom: -100px; left: -100px; animation: float 10s ease-in-out infinite reverse; }
.shape-3 { width: 300px; height: 300px; top: 50%; left: 60%; animation: float 6s ease-in-out infinite; }

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 40px 36px 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(232, 185, 107, 0.2);
  position: relative;
  z-index: 1;
}

.login-brand {
  text-align: center;
  margin-bottom: 24px;
}

.brand-icon { font-size: 42px; margin-bottom: 4px; }
.brand-title { font-size: 26px; font-weight: 700; color: #2D2320; margin: 0 0 4px; letter-spacing: 4px; }
.brand-subtitle { font-size: 13px; color: #9B8B85; margin: 0; letter-spacing: 1px; }

.login-tabs :deep(.el-tabs__item) {
  color: #9B8B85;
  font-size: 15px;
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: #8B4A50;
}

.login-tabs :deep(.el-tabs__active-bar) {
  background-color: #8B4A50;
}

.auth-form {
  margin-top: 8px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #E5DDD8 inset;
}

.auth-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #C88A6E inset; }
.auth-form :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #8B5A3C inset, 0 0 0 4px rgba(139, 90, 60, 0.1); }

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 6px;
  border-radius: 8px;
  background: linear-gradient(135deg, #6B3A42, #8B4A50);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #7B4A52, #9B5A60);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(139, 74, 80, 0.4);
}

.login-footer {
  text-align: center;
  margin-top: 12px;
}

.back-home {
  font-size: 13px;
  color: #B5A39C;
  transition: color 0.3s;
}

.back-home:hover {
  color: #8B4A50;
}
</style>