<template>
  <div class="layout-wrapper">
    <AppHeader />
    <main class="layout-main">
      <router-view />
    </main>
    <AppFooter />
    <!-- 快捷浮动气泡（点击跳转到AI导购页面） -->
    <div class="ai-quick-bubble" @click="$router.push('/ai-assistant')" v-if="$route.path !== '/ai-assistant'">
      <span class="bubble-icon">🤖</span>
    </div>
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>

<style scoped>
.layout-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F7F3F0;
}

.layout-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

/* AI导购快捷入口 */
.ai-quick-bubble {
  position: fixed; right: 24px; bottom: 24px; z-index: 998;
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #6B3A42, #8B4A50);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; box-shadow: 0 4px 16px rgba(139,74,80,0.35);
  transition: transform 0.3s;
}
.ai-quick-bubble:hover { transform: scale(1.1); }
.bubble-icon { font-size: 26px; }
</style>