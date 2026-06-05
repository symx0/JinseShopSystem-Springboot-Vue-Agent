<template>
  <div class="layout-wrapper">
    <AppHeader />
    <main class="layout-main">
      <router-view />
    </main>
    <AppFooter />
    <AiChat />
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import AiChat from '@/components/AiChat.vue'
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
</style>