<template>
  <div class="manage-container">
    <el-container style="height: 100vh">
      <el-aside :width="isCollapse ? '64px' : '220px'" class="manage-aside">
        <Aside :isCollapse="isCollapse" />
      </el-aside>
      <el-container>
        <el-header class="manage-header">
          <Header :isCollapse="isCollapse" @toggle="isCollapse = !isCollapse" />
        </el-header>
        <el-main class="manage-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Aside from '@/components/Aside.vue'
import Header from '@/components/Header.vue'

const isCollapse = ref(false)
</script>

<style scoped>
.manage-container {
  height: 100vh;
  overflow: hidden;
}

.manage-aside {
  background: linear-gradient(180deg, #2D2320 0%, #3D2E2B 50%, #2D2320 100%);
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
  transition: width 0.3s ease;
  overflow: hidden;
}

.manage-header {
  background: #FFFFFF;
  border-bottom: 1px solid #EDE4DD;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
}

.manage-main {
  background: #F7F3F0;
  padding: 20px;
  overflow-y: auto;
}
</style>