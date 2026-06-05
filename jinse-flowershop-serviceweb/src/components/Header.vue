<template>
  <div class="header-row">
    <div class="header-left">
      <el-icon class="collapse-btn" @click="$emit('toggle')">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="·" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="routeName && routeName !== 'Home'">{{ routeName }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <el-dropdown trigger="click">
        <div class="user-area">
          <el-avatar :size="34" icon="UserFilled" />
          <span class="user-name">{{ userName }}</span>
          <el-icon class="user-arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$router.push('/person')">
              <el-icon><User /></el-icon> 个人信息
            </el-dropdown-item>
            <el-dropdown-item @click="$router.push('/password')">
              <el-icon><Lock /></el-icon> 修改密码
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { employeeApi } from '@/api'
import { Fold, Expand, ArrowDown, User, Lock, SwitchButton } from '@element-plus/icons-vue'

const props = defineProps({
  isCollapse: Boolean
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const routeName = computed(() => route.name || '')
const userName = computed(() => userStore.manager?.name || '管理员')

const handleLogout = async () => {
  try {
    const res = await employeeApi.logout()
    if (res.code === 1) {
      ElMessage.success(res.data || '退出成功')
    }
    userStore.logout()
  } catch (e) {
    userStore.logout()
  }
}
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 22px;
  cursor: pointer;
  color: #6B5E58;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #8B5A3C;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: #8B7E75;
  font-weight: 400;
  font-size: 14px;
}

.breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: #8B5A3C;
}

.breadcrumb :deep(.el-breadcrumb__separator) {
  color: #C8BAB0;
  margin: 0 8px;
  font-weight: 300;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-area:hover {
  background: #F5F0EB;
}

.user-name {
  font-size: 14px;
  color: #5D4A3A;
  font-weight: 500;
}

.user-arrow {
  font-size: 12px;
  color: #A89890;
}
</style>