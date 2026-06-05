<template>
  <div class="cart-page">
    <div class="page-header">
      <h2>我的购物车</h2>
    </div>

    <div class="cart-content" v-if="!loading && cartList.length">
      <div class="cart-list">
        <div v-for="item in cartList" :key="item.id" class="cart-item">
          <div class="cart-item-image">
            <img :src="item.image || 'https://via.placeholder.com/100x100?text=🌸'" />
          </div>
          <div class="cart-item-info">
            <h4>{{ item.name }}</h4>
          </div>
          <div class="cart-item-price">¥{{ item.amount }}</div>
          <div class="cart-item-qty">
            <el-button :icon="Minus" size="small" circle @click="handleSub(item)" />
            <span class="qty-num">{{ item.number }}</span>
            <el-button :icon="Plus" size="small" circle @click="handleAdd(item)" />
          </div>
          <div class="cart-item-total">¥{{ (item.amount * item.number).toFixed(2) }}</div>
        </div>
      </div>

      <div class="cart-summary">
        <div class="summary-row">
          <span>商品共计</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="summary-actions">
          <el-button @click="handleClean" type="danger" plain>清空购物车</el-button>
          <el-button type="primary" size="large" @click="$router.push('/checkout')">去结算</el-button>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-cart">
      <div class="empty-icon">🛒</div>
      <p>购物车是空的</p>
      <el-button type="primary" @click="$router.push('/flowers')">去逛逛鲜花商城</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Minus, Plus } from '@element-plus/icons-vue'
import { cartApi } from '@/api'

const cartList = ref([])
const loading = ref(false)

const totalPrice = computed(() => {
  return cartList.value.reduce((sum, item) => sum + (item.amount || 0) * (item.number || 0), 0)
})

const loadCart = async () => {
  loading.value = true
  try {
    const res = await cartApi.list()
    if (res.code === 1 && Array.isArray(res.data)) {
      cartList.value = res.data
    }
  } catch (e) {} finally {
    loading.value = false
  }
}

const handleClean = async () => {
  try {
    await cartApi.clean()
    cartList.value = []
  } catch (e) {}
}

const handleAdd = async (item) => {
  try {
    await cartApi.add({ flowerId: item.flowerId })
    item.number++
  } catch (e) {}
}

const handleSub = async (item) => {
  try {
    await cartApi.sub({ flowerId: item.flowerId })
    if (item.number <= 1) {
      cartList.value = cartList.value.filter(c => c.id !== item.id)
    } else {
      item.number--
    }
  } catch (e) {}
}

onMounted(() => {
  loadCart()
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
  margin: 0;
}

.cart-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.cart-item {
  display: grid;
  grid-template-columns: 100px 2fr 1fr 1fr 1fr;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #F5F0EC;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #FBF9F7;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cart-item-info h4 {
  font-size: 15px;
  color: #2D2320;
  margin: 0;
}

.cart-item-price {
  font-size: 16px;
  font-weight: 600;
  color: #5D4A3A;
}

.cart-item-qty {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-num {
  font-size: 16px;
  font-weight: 600;
  min-width: 24px;
  text-align: center;
  color: #2D2320;
}

.cart-item-total {
  font-size: 18px;
  font-weight: 700;
  color: #8B4A50;
  text-align: right;
}

.cart-summary {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-top: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #F5F0EC;
}

.summary-row span:first-child {
  font-size: 15px;
  color: #5D4A3A;
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: #8B4A50;
}

.summary-actions {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding-top: 16px;
}

.empty-cart {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 12px;
}

.empty-icon { font-size: 60px; margin-bottom: 16px; }
.empty-cart p { color: #B5A39C; margin-bottom: 20px; }
</style>