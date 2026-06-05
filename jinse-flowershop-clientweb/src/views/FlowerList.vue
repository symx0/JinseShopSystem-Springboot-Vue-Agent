<template>
  <div class="flower-list-page">
    <div class="page-header">
      <h2>鲜花商城</h2>
      <p>精选优质花材，用鲜花传递心意</p>
    </div>

    <div class="filter-bar">
      <div class="filter-left">
        <span
          v-for="cat in categories"
          :key="cat.id"
          :class="['filter-tag', { active: currentCategory === cat.id }]"
          @click="changeCategory(cat.id)"
        >{{ cat.name }}</span>
      </div>
      <div class="filter-right">
        <span
          :class="['promo-filter-btn', { active: promoFilter === true }]"
          @click="togglePromoFilter"
        >仅看促销</span>
      </div>
    </div>

    <div class="flower-grid" v-loading="loading">
      <div
        v-for="flower in filteredList"
        :key="flower.id"
        :class="['flower-card', { 'promo-card': flower.promo, 'sold-out-card': flower.promo && flower.stock !== undefined && flower.stock <= 0 }]"
        @click="$router.push({ name: 'FlowerDetail', params: { id: flower.id }, query: { from: 'flowers' } })"
      >
        <div class="flower-image">
          <img :src="flower.image || 'https://via.placeholder.com/280x280?text=🌸'" :alt="flower.name" />
          <span v-if="flower.promo" class="sale-badge">{{ flower.activityContent || '促销' }}</span>
          <span v-if="flower.promo && flower.stock !== undefined && flower.stock <= 0" class="sold-out-badge">已售罄</span>
        </div>
        <div class="flower-info">
          <h4 class="flower-name">{{ flower.promo ? flower.name : flower.name }}</h4>
          <p class="flower-desc">{{ flower.description || '精美鲜花花束' }}</p>
          <div v-if="flower.promo" class="promo-meta">
            <span class="promo-stock" v-if="flower.stock !== undefined && flower.stock > 0">仅剩{{ flower.stock }}件</span>
            <span class="promo-limit" v-if="flower.limitPer">限购{{ flower.limitPer }}件/人</span>
          </div>
          <div class="flower-bottom">
            <div class="price-group">
              <span v-if="flower.promo" class="flower-price promo-price">¥{{ flower.discountPrice }}</span>
              <span v-if="flower.promo" class="flower-original">¥{{ flower.originalPrice }}</span>
              <span v-if="!flower.promo" class="flower-price">¥{{ flower.price }}</span>
            </div>
            <el-button
              v-if="flower.promo"
              type="primary"
              size="default"
              round
              class="cart-btn promo-cart-btn"
              :disabled="flower.stock !== undefined && flower.stock <= 0"
              @click.stop="addToCart(flower)"
            >
              {{ flower.stock !== undefined && flower.stock <= 0 ? '已售罄' : '加入购物车' }}
            </el-button>
            <el-button
              v-else
              type="primary"
              size="default"
              round
              class="cart-btn"
              @click.stop="addToCart(flower)"
            >
              <el-icon><ShoppingCart /></el-icon>加入购物车
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="!loading && filteredList.length === 0" class="empty-state">
        <p>暂无鲜花数据</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { flowerApi, categoryApi, cartApi } from '@/api'
import { useUserStore } from '@/store'

const route = useRoute()
const userStore = useUserStore()

const categories = ref([])
const flowerList = ref([])
const loading = ref(false)
const currentCategory = ref(null)
const promoFilter = ref(null)

const filteredList = computed(() => {
  let list = flowerList.value
  if (promoFilter.value === true) {
    list = list.filter(f => f.promo)
  }
  if (currentCategory.value) {
    list = list.filter(f => f.categoryId === currentCategory.value)
  }
  return list
})

const loadCategories = async () => {
  try {
    const res = await categoryApi.list(2)
    if (res.code === 1 && Array.isArray(res.data)) {
      categories.value = res.data
    }
  } catch (e) {}
}

const loadFlowers = async () => {
  loading.value = true
  try {
    const res = await flowerApi.shop()
    if (res.code === 1 && Array.isArray(res.data)) {
      flowerList.value = res.data
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const changeCategory = (catId) => {
  if (currentCategory.value === catId) {
    currentCategory.value = null
  } else {
    currentCategory.value = catId
  }
}

const togglePromoFilter = () => {
  promoFilter.value = promoFilter.value === true ? null : true
}

const addToCart = async (flower) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再添加购物车')
    return
  }
  try {
    const res = await cartApi.add({ flowerId: flower.id })
    if (res.code === 1) {
      ElMessage.success(`已将「${flower.name}」加入购物车`)
    }
  } catch (e) {
    ElMessage.error(e?.message || '添加购物车失败')
  }
}

onMounted(() => {
  const catId = route.query.categoryId
  if (catId) {
    currentCategory.value = Number(catId)
  }
  loadCategories()
  loadFlowers()
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

.filter-bar {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.filter-left {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-right {
  display: flex;
  gap: 10px;
}

.filter-tag {
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 14px;
  background: #fff;
  color: #5D4A3A;
  cursor: pointer;
  border: 1px solid #EDE4DD;
  transition: all 0.3s;
}

.filter-tag:hover {
  border-color: #8B4A50;
  color: #8B4A50;
}

.filter-tag.active {
  background: #8B4A50;
  color: #fff;
  border-color: #8B4A50;
}

.promo-filter-btn {
  padding: 8px 24px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  background: #fff;
  color: #E8574A;
  cursor: pointer;
  border: 2px solid #E8574A;
  transition: all 0.3s;
}

.promo-filter-btn:hover {
  background: #fef2f2;
}

.promo-filter-btn.active {
  background: linear-gradient(135deg, #E8574A, #F0736A);
  color: #fff;
  border-color: #E8574A;
  box-shadow: 0 2px 8px rgba(232, 87, 74, 0.25);
}

.flower-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 300px;
}

.flower-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.flower-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(139, 74, 80, 0.12);
}

.flower-card.promo-card {
  border: 2px solid #fce4ec;
}

.flower-card.promo-card:hover {
  border-color: #E8574A;
  box-shadow: 0 12px 28px rgba(232, 87, 74, 0.15);
}

.flower-card.sold-out-card {
  opacity: 0.55;
  pointer-events: none;
}

.flower-card.sold-out-card .sold-out-badge {
  pointer-events: auto;
}

.flower-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: #FBF9F7;
  position: relative;
}

.sale-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #ff4d4f, #e63946);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
  letter-spacing: 1px;
  box-shadow: 0 2px 6px rgba(230, 57, 70, 0.3);
}

.sold-out-badge {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  padding: 8px 24px;
  border-radius: 8px;
  letter-spacing: 2px;
  pointer-events: none;
}

.flower-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.flower-card:hover .flower-image img {
  transform: scale(1.05);
}

.flower-info {
  padding: 16px;
}

.flower-name {
  font-size: 16px;
  font-weight: 600;
  color: #2D2320;
  margin: 0 0 6px;
}

.flower-desc {
  font-size: 12px;
  color: #B5A39C;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.promo-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  font-size: 12px;
}

.promo-stock {
  color: #E8574A;
  font-weight: 500;
}

.promo-limit {
  color: #8B4A50;
  font-weight: 500;
  background: #fdf6f6;
  padding: 1px 6px;
  border-radius: 4px;
}

.flower-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-group {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.flower-price {
  font-size: 20px;
  font-weight: 700;
  color: #8B4A50;
}

.flower-price.promo-price {
  color: #E8574A;
}

.flower-original {
  font-size: 13px;
  color: #B5A39C;
  text-decoration: line-through;
}

.cart-btn {
  padding: 10px 20px !important;
  font-size: 14px !important;
}

.promo-cart-btn {
  background: linear-gradient(135deg, #E8574A, #F0736A) !important;
  border-color: #E8574A !important;
}

.promo-cart-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #d44a3e, #e0655c) !important;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px;
  color: #B5A39C;
  font-size: 15px;
}
</style>
