<template>
  <div class="activity-page">
    <div class="page-header">
      <h2>热门活动</h2>
      <p>限时优惠，精彩不容错过</p>
    </div>

    <!-- 上方：活动列表 -->
    <div class="activity-section">
      <div class="activity-tabs" v-loading="loading">
        <div
          v-for="act in activities"
          :key="act.id"
          :class="['activity-tab', { active: selectedActivity?.id === act.id }]"
          @click="selectActivity(act)"
        >
          <img v-if="act.bestsellerImage" :src="act.bestsellerImage" class="tab-img" />
          <div v-else class="tab-img-placeholder">🌸</div>
          <span class="tab-name">{{ act.content }}</span>
        </div>
      </div>
    </div>

    <!-- 下方：促销商品 -->
    <div class="sale-section">
      <div class="sale-header" v-if="selectedActivity">
        <h3>{{ selectedActivity.content }}</h3>
        <span class="sale-time" v-if="selectedActivity.startTime">{{ formatDate(selectedActivity.startTime) }} - {{ formatDate(selectedActivity.endTime) }}</span>
      </div>

      <div class="sale-grid" v-if="selectedActivity" v-loading="saleLoading">
        <div
          v-for="s in saleList"
          :key="s.name"
          :class="['sale-card', { 'sold-out-card': s.stock !== undefined && s.stock <= 0 }]"
          @click="goToDetail(s)"
        >
          <div class="sale-image">
            <img :src="s.image" :alt="s.name" />
            <span class="sale-badge">促销</span>
          </div>
          <div class="sale-info">
            <h4 class="sale-name">{{ s.name }}</h4>
            <p class="sale-desc">{{ s.description }}</p>
            <div class="sale-meta">
              <span class="sale-stock" v-if="s.stock !== undefined">仅剩{{ s.stock }}件</span>
              <span class="sale-limit" v-if="selectedActivity && selectedActivity.limitPer">限购{{ selectedActivity.limitPer }}件/人</span>
            </div>
            <div class="sale-bottom">
              <div class="sale-price">
                <span class="sale-discount">¥{{ s.discountPrice }}</span>
                <span class="sale-original">¥{{ s.originalPrice }}</span>
              </div>
              <el-button type="primary" size="small" round @click.stop="addToCart(s)" :disabled="s.stock !== undefined && s.stock <= 0">
                {{ s.stock !== undefined && s.stock <= 0 ? '已售罄' : '加入购物车' }}
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="!saleLoading && saleList.length === 0" class="empty-state">
          <p>暂无促销商品</p>
        </div>
      </div>

      <div v-else class="empty-hint">
        <p>请点击上方活动查看促销商品</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { activityApi, cartApi } from '@/api'

const router = useRouter()
const activities = ref([])
const loading = ref(false)
const selectedActivity = ref(null)
const saleList = ref([])
const saleLoading = ref(false)

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await activityApi.page({ page: 1, pageSize: 20 })
    if (res.code === 1 && res.data && Array.isArray(res.data.records)) {
      activities.value = res.data.records
      // 默认选中第一个活动
      if (activities.value.length > 0) {
        selectActivity(activities.value[0])
      }
    }
  } catch (e) {} finally {
    loading.value = false
  }
}

const selectActivity = async (act) => {
  selectedActivity.value = act
  saleLoading.value = true
  saleList.value = []
  try {
    const res = await activityApi.sale({ activityId: act.id, page: 1, pageSize: 50 })
    if (res.code === 1 && Array.isArray(res.data)) {
      saleList.value = res.data
    }
  } catch (e) {
    saleList.value = []
  } finally {
    saleLoading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace(/\s\d{2}:\d{2}:\d{2}/, '')
}

const addToCart = async (sale) => {
  try {
    const res = await cartApi.add({ flowerId: sale.flowerId })
    if (res.code === 1) {
      ElMessage.success('已加入购物车')
    }
  } catch (e) {
    // 响应拦截器已统一处理错误提示，此处不再重复弹出
  }
}

const goToDetail = (sale) => {
  if (sale.flowerId) {
    router.push({ name: 'FlowerDetail', params: { id: sale.flowerId }, query: { from: 'activity' } })
  }
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.page-header {
  text-align: center;
  margin-bottom: 28px;
}

.page-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #2D2320;
  margin: 0 0 6px;
}

.page-header p {
  font-size: 14px;
  color: #9B8B85;
  margin: 0;
}

/* ========== 上方活动区 ========== */
.activity-section {
  margin-bottom: 32px;
}

.activity-tabs {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.activity-tab {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 12px 16px;
  border-radius: 12px;
  background: #fff;
  border: 2px solid transparent;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  min-width: 140px;
}

.activity-tab:hover {
  border-color: #d4a0a5;
}

.activity-tab.active {
  border-color: #8B4A50;
  background: #fdf6f6;
}

.tab-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 8px;
}

.tab-img-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #fce4ec, #f8bbd0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
}

.tab-name {
  font-size: 14px;
  font-weight: 600;
  color: #2D2320;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

/* ========== 下方促销商品区 ========== */
.sale-section {
  min-height: 300px;
}

.sale-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #EDE4DD;
}

.sale-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #2D2320;
  margin: 0;
}

.sale-time {
  font-size: 13px;
  color: #B5A39C;
}

.sale-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 200px;
}

.sale-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
}

.sale-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(139, 74, 80, 0.12);
}

.sale-card.sold-out-card {
  opacity: 0.55;
  pointer-events: none;
}

.sale-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: #FBF9F7;
  position: relative;
}

.sale-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.sale-card:hover .sale-image img {
  transform: scale(1.05);
}

.sale-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #E8574A, #F0736A);
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
}

.sale-info {
  padding: 16px;
}

.sale-name {
  font-size: 16px;
  font-weight: 600;
  color: #2D2320;
  margin: 0 0 6px;
}

.sale-desc {
  font-size: 12px;
  color: #B5A39C;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sale-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  font-size: 12px;
}

.sale-stock {
  color: #E8574A;
  font-weight: 500;
}

.sale-limit {
  color: #8B4A50;
  font-weight: 500;
  background: #fdf6f6;
  padding: 1px 6px;
  border-radius: 4px;
}

.sale-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sale-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.sale-discount {
  font-size: 20px;
  font-weight: 700;
  color: #E8574A;
}

.sale-original {
  font-size: 13px;
  color: #B5A39C;
  text-decoration: line-through;
}

.empty-state,
.empty-hint {
  text-align: center;
  padding: 60px;
  color: #B5A39C;
  font-size: 15px;
}
</style>
