<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">锦瑟花店</h1>
        <p class="hero-desc">锦瑟无端五十弦，一弦一柱思华年。<br/>用鲜花传递每一份真挚的情感</p>
        <el-button size="large" class="hero-btn" @click="$router.push('/flowers')">立刻选花 →</el-button>
      </div>
      <div class="hero-decor">💐</div>
    </section>

    <section class="section" v-if="categories.length">
      <div class="section-header">
        <h2 class="section-title">鲜花分类</h2>
        <p class="section-subtitle">精选花材，总有您喜欢的那一款</p>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-card"
          @click="$router.push(`/flowers?categoryId=${cat.id}`)"
        >
          <div class="category-icon">{{ catIcons[cat.id % catIcons.length] }}</div>
          <span class="category-name">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <section class="section" v-if="activities.length">
      <div class="section-header">
        <h2 class="section-title">热门活动</h2>
        <p class="section-subtitle">限时优惠，精彩不容错过</p>
      </div>
      <div class="activity-list">
        <div
          v-for="act in activities"
          :key="act.id"
          class="activity-card"
          @click="$router.push('/activity')"
        >
          <img v-if="act.bestsellerImage" :src="act.bestsellerImage" class="activity-img" />
          <div v-else class="activity-img-placeholder">🌸</div>
          <div class="activity-tag">🔥 热门</div>
          <p class="activity-desc">{{ act.content }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { categoryApi, activityApi } from '@/api'

const categories = ref([])
const activities = ref([])
const catIcons = ['🌹', '💐', '🌻', '🌷', '💐', '🌸', '🌺', '🌼']

const loadCategories = async () => {
  try {
    const res = await categoryApi.list(2)
    if (res.code === 1 && Array.isArray(res.data)) {
      categories.value = res.data
    }
  } catch (e) {}
}

const loadActivities = async () => {
  try {
    const res = await activityApi.page({ page: 1, pageSize: 4 })
    if (res.code === 1 && res.data && Array.isArray(res.data.records)) {
      activities.value = res.data.records
    }
  } catch (e) {}
}

onMounted(() => {
  loadCategories()
  loadActivities()
})
</script>

<style scoped>
.hero-section {
  background: linear-gradient(135deg, #2D2320 0%, #4A3035 50%, #6B3A42 100%);
  border-radius: 16px;
  padding: 60px 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40px;
  position: relative;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: #E8B96B;
  letter-spacing: 8px;
  margin: 0 0 12px;
}

.hero-desc {
  font-size: 16px;
  color: #D4C4B8;
  line-height: 1.8;
  margin: 0 0 28px;
}

.hero-btn {
  background: #E8B96B;
  border-color: #E8B96B;
  color: #2D2320;
  font-weight: 600;
  border-radius: 24px;
  padding: 12px 32px;
  font-size: 15px;
}

.hero-btn:hover {
  background: #F0CD85;
  border-color: #F0CD85;
}

.hero-decor {
  font-size: 120px;
  opacity: 0.3;
  position: absolute;
  right: 40px;
  top: 50%;
  transform: translateY(-50%);
}

.section {
  margin-bottom: 48px;
}

.section-header {
  text-align: center;
  margin-bottom: 28px;
}

.section-title {
  font-size: 26px;
  font-weight: 700;
  color: #2D2320;
  margin: 0 0 8px;
  letter-spacing: 3px;
}

.section-subtitle {
  font-size: 14px;
  color: #9B8B85;
  margin: 0;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(139, 74, 80, 0.15);
}

.category-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.category-name {
  font-size: 15px;
  color: #5D4A3A;
  font-weight: 500;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.activity-img {
  width: 100%;
  aspect-ratio: 4/3;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 16px;
}

.activity-img-placeholder {
  width: 100%;
  aspect-ratio: 4/3;
  border-radius: 8px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #fce4ec, #f8bbd0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(139, 74, 80, 0.12);
}

.activity-tag {
  position: absolute;
  top: -10px;
  right: 16px;
  background: linear-gradient(135deg, #E8574A, #F0736A);
  color: #fff;
  font-size: 12px;
  padding: 3px 12px;
  border-radius: 12px;
}

.activity-name {
  font-size: 18px;
  color: #2D2320;
  margin: 0 0 10px;
}

.activity-desc {
  font-size: 14px;
  color: #9B8B85;
  margin: 0;
  line-height: 1.6;
}
</style>