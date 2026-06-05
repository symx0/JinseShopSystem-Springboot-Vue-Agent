<template>
  <div class="alipay-config">
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>支付模式</span>
        </div>
      </template>
      <div class="payment-mode-area">
        <div class="status-indicator">
          <div class="status-dot" :class="paymentMode === 1 ? 'alipay' : 'mock'"></div>
          <div>
            <div class="status-label">当前支付方式</div>
            <div class="status-value" :class="paymentMode === 1 ? 'alipay-text' : 'mock-text'">
              {{ paymentMode === 1 ? '支付宝支付 💳' : '模拟支付 🧪' }}
            </div>
          </div>
        </div>
        <div class="mode-action">
          <el-switch
            v-model="switchValue"
            :active-text="switchValue === 1 ? '支付宝支付' : '模拟支付'"
            :active-value="1"
            :inactive-value="0"
            active-color="#1677FF"
            inactive-color="#E6A23C"
            size="large"
            @change="handleToggleMode"
            :loading="modeLoading"
          />
        </div>
      </div>
      <el-alert
        v-if="paymentMode === 0"
        title="模拟支付模式已开启"
        type="warning"
        :closable="false"
        show-icon
        style="margin-top: 16px;"
      >
        <p>当前为模拟支付模式，用户付款时将直接跳过支付宝，订单状态自动变为已付款。此模式仅用于开发测试。</p>
      </el-alert>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>支付宝沙箱配置</span>
          <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px" style="max-width: 700px">
        <el-form-item label="AppID">
          <el-input v-model="form.appId" placeholder="支付宝沙箱应用AppID" />
        </el-form-item>

        <el-form-item label="应用私钥">
          <el-input
            v-model="form.privateKey"
            type="textarea"
            :rows="4"
            placeholder="请填写你的应用私钥（RSA2）"
          />
        </el-form-item>

        <el-form-item label="支付宝公钥">
          <el-input
            v-model="form.alipayPublicKey"
            type="textarea"
            :rows="4"
            placeholder="请填写你的支付宝公钥（RSA2）"
          />
        </el-form-item>

        <el-form-item label="回调地址">
          <el-input v-model="form.notifyUrl" placeholder="http://你的公网地址/user/order/payment/notify" />
        </el-form-item>

        <el-form-item label="返回地址">
          <el-input v-model="form.returnUrl" placeholder="http://localhost:5173/order" />
        </el-form-item>

        <el-form-item label="订单名称前缀">
          <el-input v-model="form.subjectPrefix" placeholder="如：锦瑟花店订单-" />
          <span style="margin-left:8px;font-size:13px;color:#9B8B85;">支付页面显示的商品名称前缀</span>
        </el-form-item>
      </el-form>

      <el-divider />
      <el-alert
        title="配置说明"
        type="info"
        :closable="false"
        show-icon
      >
        <p>1. 请在<a href="https://open.alipay.com/develop/sandbox/app" target="_blank">支付宝沙箱控制台</a>生成RSA2密钥</p>
        <p>2. 应用私钥需使用PKCS8格式</p>
        <p>3. 回调地址需为公网可访问地址，支付宝支付成功后会回调此地址</p>
        <p>4. 保存后无需重启服务，立即生效</p>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { shopApi } from '@/api'

const form = ref({
  appId: '',
  privateKey: '',
  alipayPublicKey: '',
  notifyUrl: '',
  returnUrl: '',
  subjectPrefix: '锦瑟花店订单-'
})

const saving = ref(false)
const paymentMode = ref(1)
const switchValue = ref(1)
const modeLoading = ref(false)

onMounted(async () => {
  try {
    const modeRes = await shopApi.getPaymentMode()
    if (modeRes.code === 1) {
      paymentMode.value = modeRes.data ?? 1
      switchValue.value = paymentMode.value
    }
  } catch (e) {}

  try {
    const res = await shopApi.getAlipayConfig()
    if (res.code === 1 && res.data) {
      form.value = {
        appId: res.data.appId || '',
        privateKey: res.data.privateKey || '',
        alipayPublicKey: res.data.alipayPublicKey || '',
        notifyUrl: res.data.notifyUrl || '',
        returnUrl: res.data.returnUrl || '',
        subjectPrefix: res.data.subjectPrefix || '锦瑟花店订单-'
      }
    }
  } catch (e) {
    ElMessage.error('获取配置失败')
  }
})

const saveConfig = async () => {
  if (!form.value.appId || !form.value.privateKey || !form.value.alipayPublicKey) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    const res = await shopApi.saveAlipayConfig(form.value)
    if (res.code === 1) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleToggleMode = async (val) => {
  modeLoading.value = true
  try {
    await shopApi.setPaymentMode(val)
    ElMessage.success(val === 1 ? '已切换为支付宝支付' : '已切换为模拟支付（测试模式）')
    paymentMode.value = val
  } catch (e) {
    switchValue.value = paymentMode.value
    ElMessage.error('切换失败')
  } finally {
    modeLoading.value = false
  }
}
</script>

<style scoped>
.alipay-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.el-alert p {
  margin: 4px 0;
  font-size: 13px;
  line-height: 1.6;
}

.el-alert a {
  color: #409EFF;
  text-decoration: none;
}

.payment-mode-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-dot.alipay {
  background: #1677FF;
  box-shadow: 0 0 8px rgba(22, 119, 255, 0.4);
}

.status-dot.mock {
  background: #E6A23C;
  box-shadow: 0 0 8px rgba(230, 162, 60, 0.4);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-label {
  font-size: 13px;
  color: #9B8B85;
  margin-bottom: 4px;
}

.status-value {
  font-size: 20px;
  font-weight: 600;
}

.alipay-text {
  color: #1677FF;
}

.mock-text {
  color: #E6A23C;
}

.mode-action {
  display: flex;
  align-items: center;
}
</style>
