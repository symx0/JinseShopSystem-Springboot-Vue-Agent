<template>
  <div class="checkout-page">
    <div class="page-header">
      <h2>确认订单</h2>
    </div>

    <div class="checkout-layout" v-if="cartList.length">
      <div class="checkout-main">
        <div class="address-section">
          <div class="section-header">
            <h3>收货地址</h3>
            <el-button type="primary" size="small" @click="openAddressDialog()">新增地址</el-button>
          </div>
          <div v-if="addresses.length" class="address-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              :class="['address-card', { active: selectedAddress === addr.id }]"
              @click="selectedAddress = addr.id"
            >
              <div class="addr-contact">
                <strong>{{ addr.consignee }}</strong>
                <span>{{ addr.phone }}</span>
              </div>
              <p class="addr-detail">{{ addr.provinceName }}{{ addr.cityName }}{{ addr.districtName }} {{ addr.detail }}</p>
              <div class="addr-actions">
                <el-tag v-if="addr.isDefault" size="small" type="danger">默认</el-tag>
                <span class="addr-edit-btn" @click.stop="openAddressDialog(addr)">编辑</span>
                <span class="addr-del-btn" @click.stop="handleDeleteAddress(addr.id)">删除</span>
              </div>
            </div>
          </div>
          <div v-else class="no-address">暂无收货地址，请先添加地址</div>
        </div>

        <div class="order-items">
          <h3>商品清单</h3>
          <div v-for="item in cartList" :key="item.id" class="order-item">
            <img :src="item.image || 'https://via.placeholder.com/60x60?text=🌸'" class="order-item-img" />
            <div class="order-item-info">
              <span>{{ item.name }}</span>
              <span class="item-qty">×{{ item.number }}</span>
            </div>
            <span class="order-item-price">¥{{ (item.amount * item.number).toFixed(2) }}</span>
          </div>
        </div>

        <div class="delivery-section">
          <h3>配送方式</h3>
          <el-radio-group v-model="deliveryStatus" class="delivery-radio">
            <el-radio :value="1">立即送出</el-radio>
            <el-radio :value="0">选择送达时间</el-radio>
          </el-radio-group>
          <div v-if="deliveryStatus === 0" class="delivery-time-picker">
            <el-date-picker
              v-model="scheduledTime"
              type="datetime"
              placeholder="选择送达时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-hours="disabledHours"
              :disabled-minutes="disabledMinutes"
              style="width: 100%"
            />
          </div>
        </div>

        <div class="remark-section">
          <h3>订单备注</h3>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="2"
            placeholder="如有特殊要求，请在此备注（如配送时间、贺卡内容等）"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>

      <div class="checkout-sidebar">
        <div class="price-summary">
          <div class="price-row">
            <span>商品总价</span>
            <span>¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <div class="price-row">
            <span>配送费</span>
            <span>{{ actualDeliveryFee === 0 ? '免运费' : '¥' + actualDeliveryFee.toFixed(2) }}</span>
          </div>
          <div class="price-row">
            <span>打包费</span>
            <span>{{ feeConfig.packFee === 0 ? '免打包费' : '¥' + feeConfig.packFee.toFixed(2) }}</span>
          </div>
          <div class="price-row price-total">
            <span>应付金额</span>
            <span>¥{{ finalPrice.toFixed(2) }}</span>
          </div>
        </div>
        <el-button type="primary" size="large" class="submit-order-btn" :loading="submitting" @click="submitOrder">
          提交订单
        </el-button>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>购物车为空，请先添加商品</p>
      <el-button type="primary" @click="$router.push('/flowers')">去选购</el-button>
    </div>

    <!-- 地址编辑对话框 -->
    <el-dialog v-model="addressDialogVisible" :title="isEditAddress ? '编辑地址' : '新增地址'" width="500px">
      <el-form :model="addressForm" label-width="80px" :rules="addressRules" ref="addressFormRef">
        <div style="height: 20px;"></div>
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="addressForm.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="地区" prop="areaCode">
          <el-cascader
            v-model="addressForm.areaCode"
            :options="areaOptions"
            :props="{ value: 'code', label: 'name', children: 'children' }"
            placeholder="请选择省/市/区"
            style="width: 100%"
            @change="handleAreaChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" placeholder="请输入详细地址（街道、门牌号等）" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="addressForm.label" placeholder="请选择标签" clearable style="width: 100%">
            <el-option label="家" value="家" />
            <el-option label="公司" value="公司" />
            <el-option label="学校" value="学校" />
          </el-select>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>

    <!-- 付款弹窗 -->
    <el-dialog v-model="payDialogVisible" title="确认下单" width="400px" :close-on-click-modal="false" :close-on-press-escape="false">
      <div style="text-align:center;padding:10px 0">
        <el-icon style="font-size:48px;color:#67C23A;margin-bottom:12px"><ShoppingCart /></el-icon>
        <p style="font-size:16px;color:#2D2320;margin-bottom:8px">订单金额：¥{{ pendingOrderData ? pendingOrderData.amount : 0 }}</p>
      </div>
      <template #footer>
        <el-button @click="payLater" :loading="placingOrder">稍后支付</el-button>
        <el-button type="primary" :loading="placingOrder" @click.stop="goPay">立即支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { orderApi, cartApi, addressApi, shopApi } from '@/api'
import areaData from 'china-area-data'

const router = useRouter()
const cartList = ref([])
const addresses = ref([])
const selectedAddress = ref(null)
const submitting = ref(false)
const remark = ref('')
const deliveryStatus = ref(1)
const scheduledTime = ref(null)
const addressDialogVisible = ref(false)
const isEditAddress = ref(false)
const addressFormRef = ref(null)

const feeConfig = ref({
  deliveryFee: 10,
  deliveryFreeThreshold: 199,
  packFee: 0
})

// 构建省市区级联数据
const areaOptions = computed(() => {
  const provinces = []
  const provData = areaData['86']
  if (!provData) return provinces
  for (const provinceCode in provData) {
    const province = { code: provinceCode, name: provData[provinceCode], children: [] }
    const cityData = areaData[provinceCode]
    if (cityData) {
      for (const cityCode in cityData) {
        const city = { code: cityCode, name: cityData[cityCode], children: [] }
        const districtData = areaData[cityCode]
        if (districtData) {
          for (const districtCode in districtData) {
            city.children.push({ code: districtCode, name: districtData[districtCode] })
          }
        }
        if (city.children.length) province.children.push(city)
      }
    }
    if (province.children.length) provinces.push(province)
  }
  return provinces
})

const addressForm = ref({
  consignee: '',
  phone: '',
  provinceCode: '',
  provinceName: '',
  cityCode: '',
  cityName: '',
  districtCode: '',
  districtName: '',
  areaCode: [],
  detail: '',
  label: '',
  isDefault: 0
})

const phoneValidator = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号'))
  } else {
    callback()
  }
}

const addressRules = {
  consignee: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [{ required: true, validator: phoneValidator, trigger: 'blur' }],
  areaCode: [{ required: true, message: '请选择地区', trigger: 'change', type: 'array' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const handleAreaChange = (values) => {
  if (values && values.length === 3) {
    addressForm.value.provinceCode = values[0]
    addressForm.value.provinceName = areaData['86'][values[0]]
    addressForm.value.cityCode = values[1]
    addressForm.value.cityName = areaData[values[0]][values[1]]
    addressForm.value.districtCode = values[2]
    addressForm.value.districtName = areaData[values[1]][values[2]]
  }
}

const totalPrice = computed(() => {
  return cartList.value.reduce((sum, item) => sum + (item.amount || 0) * (item.number || 0), 0)
})

const actualDeliveryFee = computed(() => {
  if (feeConfig.value.deliveryFreeThreshold > 0 && totalPrice.value >= feeConfig.value.deliveryFreeThreshold) {
    return 0
  }
  return feeConfig.value.deliveryFee
})

const finalPrice = computed(() => {
  return totalPrice.value + actualDeliveryFee.value + feeConfig.value.packFee
})

const loadData = async () => {
  try {
    const [cartRes, addrRes, feeRes] = await Promise.all([cartApi.list(), addressApi.list(), shopApi.getFee()])
    if (cartRes.code === 1 && Array.isArray(cartRes.data)) {
      cartList.value = cartRes.data
    }
    if (addrRes.code === 1 && Array.isArray(addrRes.data)) {
      addresses.value = addrRes.data
      const defaultAddr = addrRes.data.find(a => a.isDefault)
      if (defaultAddr) selectedAddress.value = defaultAddr.id
      else if (addrRes.data.length) selectedAddress.value = addrRes.data[0].id
    }
    if (feeRes.code === 1 && feeRes.data) {
      feeConfig.value.deliveryFee = feeRes.data.deliveryFee ?? 10
      feeConfig.value.deliveryFreeThreshold = feeRes.data.deliveryFreeThreshold ?? 199
      feeConfig.value.packFee = feeRes.data.packFee ?? 0
    }
  } catch (e) {}
}

const openAddressDialog = (addr = null) => {
  if (addr) {
    isEditAddress.value = true
    const areaCode = []
    if (addr.provinceCode) areaCode.push(addr.provinceCode)
    if (addr.cityCode) areaCode.push(addr.cityCode)
    if (addr.districtCode) areaCode.push(addr.districtCode)
    addressForm.value = { ...addr, areaCode }
  } else {
    isEditAddress.value = false
    addressForm.value = {
      consignee: '',
      phone: '',
      provinceCode: '',
      provinceName: '',
      cityCode: '',
      cityName: '',
      districtCode: '',
      districtName: '',
      areaCode: [],
      detail: '',
      label: '',
      isDefault: 0
    }
  }
  addressDialogVisible.value = true
}

const handleSaveAddress = async () => {
  if (!addressFormRef.value) return
  try {
    await addressFormRef.value.validate()
  } catch {
    return
  }
  const form = { ...addressForm.value }
  delete form.areaCode
  try {
    if (isEditAddress.value) {
      await addressApi.update(form)
    } else {
      await addressApi.add(form)
    }
    ElMessage.success(isEditAddress.value ? '修改成功' : '添加成功')
    addressDialogVisible.value = false
    const addrRes = await addressApi.list()
    if (addrRes.code === 1 && Array.isArray(addrRes.data)) {
      addresses.value = addrRes.data
      const defaultAddr = addrRes.data.find(a => a.isDefault)
      if (defaultAddr) selectedAddress.value = defaultAddr.id
      else if (addrRes.data.length) selectedAddress.value = addrRes.data[0].id
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleDeleteAddress = async (id) => {
  try {
    await addressApi.delete(id)
    ElMessage.success('删除成功')
    const addrRes = await addressApi.list()
    if (addrRes.code === 1 && Array.isArray(addrRes.data)) {
      addresses.value = addrRes.data
      if (selectedAddress.value === id) {
        const defaultAddr = addrRes.data.find(a => a.isDefault)
        if (defaultAddr) selectedAddress.value = defaultAddr.id
        else if (addrRes.data.length) selectedAddress.value = addrRes.data[0].id
        else selectedAddress.value = null
      }
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const payDialogVisible = ref(false)
const pendingOrderId = ref(null)
const pendingDeliveryTime = ref('')
// 暂存订单数据，等待用户在支付弹窗中选择后才真正下单
const pendingOrderData = ref(null)
// 真正下单过程中的 loading（点"立即支付"/"稍后支付"后）
const placingOrder = ref(false)

const submitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (deliveryStatus.value === 0 && !scheduledTime.value) {
    ElMessage.warning('请选择送达时间')
    return
  }
  // 仅校验并准备订单数据，弹出支付确认弹窗，不执行下单
  const data = {
    addressBookId: selectedAddress.value,
    payMethod: 1,
    packAmount: feeConfig.value.packFee,
    deliveryFee: actualDeliveryFee.value,
    amount: finalPrice.value,
    deliveryStatus: deliveryStatus.value,
    remark: remark.value || ''
  }
  if (deliveryStatus.value === 0 && scheduledTime.value) {
    data.estimatedDeliveryTime = scheduledTime.value
  }
  pendingOrderData.value = data
  pendingOrderId.value = null
  pendingDeliveryTime.value = ''
  payDialogVisible.value = true
}

// 真正执行下单：调用后端 submit → 轮询结果 → 清空购物车
const placeOrder = async () => {
  if (!pendingOrderData.value) {
    throw new Error('订单信息异常')
  }
  const res = await orderApi.submit(pendingOrderData.value)
  if (res.code !== 1) {
    throw new Error(res.msg || '订单创建失败')
  }
  const correlationId = res.data
  const orderResult = await pollOrderResult(correlationId)
  if (!orderResult || !orderResult.id) {
    throw new Error('订单创建失败，请重试')
  }
  // 下单成功，清空购物车
  await cartApi.clean()
  pendingOrderId.value = orderResult.id
  pendingDeliveryTime.value = orderResult.estimatedDeliveryTime
  return orderResult
}

const goPay = async () => {
  if (placingOrder.value) return
  if (!pendingOrderData.value) {
    ElMessage.error('订单信息异常')
    payDialogVisible.value = false
    return
  }
  placingOrder.value = true
  try {
    // 先下单
    await placeOrder()
    payDialogVisible.value = false
    // 再发起支付
    const modeRes = await shopApi.getPaymentMode()
    const paymentMode = modeRes.code === 1 ? (modeRes.data ?? 1) : 1
    if (paymentMode === 0) {
      const res = await orderApi.mockPayment(pendingOrderId.value)
      if (res.code === 1) {
        ElMessage.success('支付成功')
        router.push('/order')
      } else {
        ElMessage.error(res.msg || '支付失败')
      }
    } else {
      window.location.href = '/api/user/order/payment/page/' + pendingOrderId.value
    }
  } catch (e) {
    ElMessage.error(e?.message || '下单失败')
    payDialogVisible.value = false
  } finally {
    placingOrder.value = false
  }
}

const payLater = async () => {
  if (placingOrder.value) return
  if (!pendingOrderData.value) {
    ElMessage.error('订单信息异常')
    payDialogVisible.value = false
    return
  }
  placingOrder.value = true
  try {
    const orderResult = await placeOrder()
    payDialogVisible.value = false
    const deliveryTime = orderResult.estimatedDeliveryTime
    ElMessage.success(`下单成功！付款后预计在${deliveryTime ? deliveryTime.replace('T', ' ').substring(0, 16) : '1天内'}内发货，请尽快付款`)
    router.push('/order')
  } catch (e) {
    ElMessage.error(e?.message || '下单失败')
    payDialogVisible.value = false
  } finally {
    placingOrder.value = false
  }
}

// 轮询获取异步下单结果
const pollOrderResult = async (correlationId, maxRetries = 30, interval = 1000) => {
  for (let i = 0; i < maxRetries; i++) {
    let result = null
    try {
      const res = await orderApi.getSubmitResult(correlationId)
      if (res.code === 1 && res.data) {
        result = res.data
      }
    } catch (e) {
      // 仅网络/请求异常才重试，最后一次抛出
      if (i === maxRetries - 1) throw e
    }
    // 业务结果判断放在 try-catch 外，避免业务错误被吞导致空等
    if (result) {
      // 下单成功
      if (typeof result === 'object' && result.id) {
        return result
      }
      // 业务错误（如购物车为空），立即抛出，不再轮询
      if (typeof result === 'object' && result.error) {
        throw new Error(result.error)
      }
    }
    // 仅 "processing" 或无数据时等待后重试
    await new Promise(resolve => setTimeout(resolve, interval))
  }
  throw new Error('订单处理超时，请稍后在订单列表中查看')
}

onMounted(() => {
  loadData()
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

.checkout-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

.address-section, .order-items {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 16px;
  color: #2D2320;
  margin: 0;
}

.address-section h3, .order-items h3 {
  font-size: 16px;
  color: #2D2320;
  margin: 0 0 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  padding: 14px;
  border: 2px solid #EDE4DD;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-card:hover { border-color: #C88A6E; }

.address-card.active {
  border-color: #8B4A50;
  background: rgba(139, 74, 80, 0.03);
}

.addr-contact { display: flex; gap: 12px; margin-bottom: 4px; font-size: 14px; }
.addr-detail { font-size: 13px; color: #5D4A3A; margin: 4px 0; }

.addr-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.addr-edit-btn {
  color: #409eff;
  cursor: pointer;
  font-size: 12px;
}

.addr-edit-btn:hover { color: #66b1ff; }

.addr-del-btn {
  color: #f56c6c;
  cursor: pointer;
  font-size: 12px;
}

.addr-del-btn:hover { color: #f78989; }

.no-address {
  color: #B5A39C;
  padding: 20px 0;
  text-align: center;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid #F5F0EC;
}

.order-item:last-child { border-bottom: none; }

.order-item-img {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
}

.order-item-info {
  flex: 1;
  font-size: 14px;
  color: #3D2E28;
}

.item-qty { color: #9B8B85; margin-left: 8px; }

.order-item-price {
  font-size: 16px;
  font-weight: 600;
  color: #8B4A50;
}

.checkout-sidebar {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  position: sticky;
  top: 84px;
}

.price-summary { margin-bottom: 20px; }

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 14px;
  color: #5D4A3A;
}

.price-total {
  border-top: 1px solid #EDE4DD;
  padding-top: 14px;
  font-size: 18px;
  font-weight: 700;
  color: #8B4A50;
}

.submit-order-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  border-radius: 8px;
}

.empty-state {
  text-align: center;
  padding: 80px;
  background: #fff;
  border-radius: 12px;
}

.empty-state p { color: #B5A39C; margin-bottom: 16px; }

.delivery-section, .remark-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.delivery-section h3, .remark-section h3 {
  font-size: 16px;
  color: #2D2320;
  margin: 0 0 12px;
}

.delivery-radio {
  display: flex;
  gap: 24px;
}

.delivery-time-picker {
  margin-top: 12px;
}
</style>