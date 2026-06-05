<template>
  <div class="page-container">
    <div class="page-header">
      <h3>店铺管理</h3>
    </div>

    <el-card shadow="never" class="shop-card">
      <div class="shop-status-area">
        <div class="status-indicator">
          <div class="status-dot" :class="shopStatus === 1 ? 'open' : 'closed'"></div>
          <div>
            <div class="status-label">当前状态</div>
            <div class="status-value" :class="shopStatus === 1 ? 'open-text' : 'closed-text'">
              {{ shopStatus === 1 ? '营业中 🌸' : '已打烊 🌙' }}
            </div>
          </div>
        </div>
        <div class="shop-action">
          <el-switch
            v-model="switchValue"
            :active-text="switchValue ? '营业中' : '已打烊'"
            :active-value="1"
            :inactive-value="0"
            active-color="#6B8E6B"
            inactive-color="#9B8B85"
            size="large"
            @change="handleToggle"
            :loading="loading"
          />
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="shop-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>费用配置</span>
          <el-button type="primary" size="small" @click="saveFeeConfig" :loading="feeSaving">保存配置</el-button>
        </div>
      </template>
      <el-form :model="feeForm" label-width="120px" style="max-width: 500px">
        <el-form-item label="配送费">
          <el-input-number v-model="feeForm.deliveryFee" :min="0" :max="999" :step="1" />
          <span class="fee-unit">元</span>
        </el-form-item>
        <el-form-item label="免运费阈值">
          <el-input-number v-model="feeForm.deliveryFreeThreshold" :min="0" :max="99999" :step="1" />
          <span class="fee-unit">元（订单满此金额免配送费，0表示不免运费）</span>
        </el-form-item>
        <el-form-item label="打包费">
          <el-input-number v-model="feeForm.packFee" :min="0" :max="999" :step="1" />
          <span class="fee-unit">元（0表示免打包费）</span>
        </el-form-item>
      </el-form>
      <el-divider />
      <el-alert
        title="配置说明"
        type="info"
        :closable="false"
        show-icon
      >
        <p>1. 配送费：每笔订单收取的配送费用</p>
        <p>2. 免运费阈值：订单商品总金额达到此值时免收配送费，设为0则不免运费</p>
        <p>3. 打包费：每笔订单收取的包装费用，设为0表示免打包费</p>
        <p>4. 保存后立即生效，客户端结算页面将自动更新</p>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { shopApi } from '@/api'

const shopStatus = ref(0)
const switchValue = ref(0)
const loading = ref(false)
const feeSaving = ref(false)

const feeForm = ref({
  deliveryFee: 10,
  deliveryFreeThreshold: 199,
  packFee: 0
})

const loadStatus = async () => {
  const res = await shopApi.getStatus()
  if (res.code === 1) {
    shopStatus.value = res.data ?? 0
    switchValue.value = shopStatus.value
  }
}

const loadFeeConfig = async () => {
  try {
    const res = await shopApi.getFee()
    if (res.code === 1 && res.data) {
      feeForm.value.deliveryFee = res.data.deliveryFee ?? 10
      feeForm.value.deliveryFreeThreshold = res.data.deliveryFreeThreshold ?? 199
      feeForm.value.packFee = res.data.packFee ?? 0
    }
  } catch (e) {}
}

const handleToggle = async (val) => {
  loading.value = true
  try {
    await shopApi.setStatus(val)
    ElMessage.success(val === 1 ? '店铺已设为营业中' : '店铺已打烊')
    shopStatus.value = val
  } catch (e) {
    switchValue.value = shopStatus.value
  } finally {
    loading.value = false
  }
}

const saveFeeConfig = async () => {
  feeSaving.value = true
  try {
    const res = await shopApi.setFee(feeForm.value)
    if (res.code === 1) {
      ElMessage.success('费用配置保存成功')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    feeSaving.value = false
  }
}

onMounted(() => {
  loadStatus()
  loadFeeConfig()
})
</script>

<style scoped>
.page-container { max-width: 800px; }
.page-header { margin-bottom: 16px; }
.page-header h3 { margin: 0; color: #3D2E28; font-size: 18px; }

.shop-card { border-radius: 10px; border: 1px solid #EDE4DD; }

.shop-status-area {
  display: flex; justify-content: space-between; align-items: center;
  padding: 24px;
}

.status-indicator { display: flex; align-items: center; gap: 16px; }

.status-dot {
  width: 16px; height: 16px; border-radius: 50%;
  animation: pulse 2s infinite;
}
.status-dot.open { background: #6B8E6B; box-shadow: 0 0 8px rgba(107,142,107,0.4); }
.status-dot.closed { background: #9B8B85; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-label { font-size: 13px; color: #9B8B85; margin-bottom: 4px; }
.status-value { font-size: 20px; font-weight: 600; }
.open-text { color: #6B8E6B; }
.closed-text { color: #9B8B85; }

.shop-action { display: flex; align-items: center; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.fee-unit {
  margin-left: 8px;
  font-size: 13px;
  color: #9B8B85;
}

.el-alert p {
  margin: 4px 0;
  font-size: 13px;
  line-height: 1.6;
}
</style>
