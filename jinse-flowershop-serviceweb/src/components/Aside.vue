<template>
  <div class="aside-container">
    <div class="logo-area">
      <span class="logo-icon">🌸</span>
      <span v-show="!isCollapse" class="logo-text">锦瑟花店</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      router
      class="aside-menu"
      background-color="transparent"
    >
      <el-menu-item index="/home" class="menu-item">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-sub-menu index="1" class="menu-item">
        <template #title>
          <el-icon><User /></el-icon>
          <span>人员管理</span>
        </template>
        <el-menu-item index="/employee" class="sub-menu-item">
          <el-icon><UserFilled /></el-icon>
          <span>员工管理</span>
        </el-menu-item>
      </el-sub-menu>
      <el-sub-menu index="2" class="menu-item">
        <template #title>
          <el-icon><ShoppingBag /></el-icon>
          <span>商品管理</span>
        </template>
        <el-menu-item index="/category" class="sub-menu-item">
          <el-icon><Grid /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/flower" class="sub-menu-item">
          <el-icon><Sunny /></el-icon>
          <span>花卉管理</span>
        </el-menu-item>
      </el-sub-menu>
      <el-menu-item index="/order" class="menu-item">
        <el-icon><Document /></el-icon>
        <span>订单管理</span>
      </el-menu-item>
      <el-menu-item index="/comment" class="menu-item">
        <el-icon><ChatDotSquare /></el-icon>
        <span>评论管理</span>
      </el-menu-item>
      <el-menu-item index="/report" class="menu-item">
        <el-icon><TrendCharts /></el-icon>
        <span>数据统计</span>
      </el-menu-item>
      <el-menu-item index="/activity" class="menu-item">
        <el-icon><Present /></el-icon>
        <span>活动管理</span>
      </el-menu-item>
      <el-menu-item index="/shop" class="menu-item">
        <el-icon><Shop /></el-icon>
        <span>店铺设置</span>
      </el-menu-item>
      <el-menu-item index="/alipay" class="menu-item">
        <el-icon><Wallet /></el-icon>
        <span>支付配置</span>
      </el-menu-item>
      <el-menu-item index="/ai-config" class="menu-item">
        <el-icon><Cpu /></el-icon>
        <span>AI 模型配置</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { House, User, UserFilled, ShoppingBag, Grid, Sunny, Document, ChatDotSquare, TrendCharts, Present, Shop, Wallet, Cpu } from '@element-plus/icons-vue'

const props = defineProps({
  isCollapse: Boolean
})

const route = useRoute()

const activeMenu = computed(() => route.path)
</script>

<style scoped>
.aside-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-icon {
  font-size: 24px;
  line-height: 1;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #F0DDCE;
  letter-spacing: 3px;
  white-space: nowrap;
}

.aside-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.aside-menu::-webkit-scrollbar {
  width: 4px;
}

.aside-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.aside-menu :deep(.el-menu-item),
.aside-menu :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 8px;
  color: #C8BAB0;
  font-size: 14px;
  transition: all 0.25s ease;
}

.aside-menu :deep(.el-menu-item:hover),
.aside-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(232, 185, 107, 0.12) !important;
  color: #E8B96B;
}

.aside-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(139, 90, 60, 0.4), rgba(200, 138, 110, 0.2)) !important;
  color: #E8B96B;
  font-weight: 600;
  border-right: 3px solid #E8B96B;
}

.aside-menu :deep(.el-icon) {
  font-size: 18px;
  color: inherit;
}

.aside-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 0 0 8px 8px;
}

.aside-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 56px !important;
  height: 42px;
  line-height: 42px;
  font-size: 13px;
}

.sub-menu-item :deep(.el-icon) {
  font-size: 16px;
}
</style>