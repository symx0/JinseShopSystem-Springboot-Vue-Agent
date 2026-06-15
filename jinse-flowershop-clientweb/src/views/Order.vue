<template>
  <div class="order-page">
    <div class="page-header">
      <h2>我的订单</h2>
    </div>

    <div class="order-list" v-if="orders.length">
      <div v-for="order in displayedOrders" :key="order.id" class="order-card" :class="'status-border-' + order.status">
        <div class="order-header">
          <div>
            <span class="order-number">订单号: {{ order.number }}</span>
            <span class="order-price">¥{{ order.amount || 0 }}</span>
          </div>
          <span class="status-badge" :class="'badge-' + order.status">{{ order.statusText }}</span>
        </div>
        <div class="order-body">
          <div class="order-left">
            <div class="order-info">
              <p v-if="order.status === 8" style="color:#E6A23C;font-weight:600;">退货申请已提交，等待商家审核...</p>
              <p v-if="order.rejectionReason" style="color:#F56C6C;">退货申请被拒绝：{{ order.rejectionReason }}</p>
              <p v-if="order.status === 7 && order.cancelReason" style="color:#909399;">取消原因：{{ order.cancelReason }}</p>
              <p><span class="label">收货人：</span>{{ order.consignee || '-' }} <span style="margin-left:12px">{{ order.phone || '' }}</span></p>
              <p><span class="label">地址：</span>{{ order.address || '-' }}</p>
              <p v-if="order.estimatedDeliveryTime && order.status !== 1" class="delivery-estimate">
                <span class="label">付款后预计在</span><span class="highlight-time">{{ formatTime(order.estimatedDeliveryTime) }}</span><span class="label">内发货</span>
              </p>
              <p v-if="order.orderTime">
                <span class="label">下单时间：</span>{{ formatTime(order.orderTime) }}
              </p>
            </div>
          
            <div class="order-goods" v-if="order.items && order.items.length">
              <div class="goods-preview" v-for="item in order.items.slice(0, 3)" :key="item.id">
                <el-image v-if="item.image" :src="item.image" fit="cover" class="goods-img" />
                <span class="goods-name">{{ item.flowerName }}</span>
              </div>
              <span v-if="order.items.length > 3" class="goods-more">+{{ order.items.length - 3 }}</span>
            </div>
          </div>
          <div class="order-actions">
            <el-button size="small" @click="showDetail(order.id)">查看详情</el-button>
            <el-button v-if="order.status === 1" type="danger" size="small" @click="handlePayment(order.id)">去付款</el-button>
            <el-button v-if="order.status === 1" size="small" @click="handleCancel(order.id)">取消订单</el-button>
            <el-button v-if="order.status === 4" type="primary" size="small" @click="handleReceipt(order.id)">确认收货</el-button>
            <el-button v-if="order.status >= 2 && order.status <= 6" type="warning" size="small" @click="handleRefund(order.id)">申请退货</el-button>
          </div>
        </div>
      </div>
      <div v-if="orders.length > 3" class="more-orders">
        <el-button v-if="!showAll" type="primary" link @click="showAll = true">更多订单 ({{ orders.length - 3 }})</el-button>
        <el-button v-else type="info" link @click="showAll = false">收起订单</el-button>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无订单</p>
      <el-button type="primary" @click="$router.push('/flowers')">去选购鲜花</el-button>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" v-model="detailVisible" width="580px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号">{{ detailData.number || detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detailData.status)" size="small">{{ detailData.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detailData.consignee || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ detailData.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ detailData.amount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatTime(detailData.orderTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.estimatedDeliveryTime" label="预计发货">{{ formatTime(detailData.estimatedDeliveryTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.deliveryTime" label="送达时间">{{ formatTime(detailData.deliveryTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>商品明细</el-divider>
      <el-table :data="detailItems" border size="small">
        <el-table-column label="图片" width="70">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width:40px;height:40px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="flowerName" label="商品名称" />
        <el-table-column prop="number" label="数量" width="60" />
        <el-table-column prop="amount" label="金额" width="80" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, shopApi } from '@/api'

const orders = ref([])
const showAll = ref(false)
const displayedOrders = computed(() => showAll.value ? orders.value : orders.value.slice(0, 3))
const detailVisible = ref(false)
const detailData = ref({})
const detailItems = ref([])

const statusType = (s) => {
  const map = { 1: 'warning', 2: 'info', 3: '', 4: '', 5: 'primary', 6: 'success', 7: 'danger', 8: 'warning' }
  return map[s] || 'info'
}

const statusText = (s) => {
  const map = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已确认', 6: '已完成', 7: '已取消', 8: '退货申请中' }
  return map[s] || '未知'
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

const loadOrders = async () => {
  try {
    const res = await orderApi.list()
    if (res.code === 1 && Array.isArray(res.data)) {
      const list = res.data.map(o => ({
        ...o,
        statusText: statusText(o.status),
        items: []
      }))
      orders.value = list
      // 异步加载每个订单的商品预览
      list.forEach(async (o) => {
        try {
          const detail = await orderApi.detail(o.id)
          if (detail.code === 1 && Array.isArray(detail.data)) {
            o.items = detail.data
          }
        } catch (e) {}
      })
    }
  } catch (e) {}
}

const showDetail = async (orderId) => {
  const order = orders.value.find(o => o.id === orderId)
  if (!order) return
  detailData.value = { ...order, statusText: statusText(order.status) }
  try {
    const res = await orderApi.detail(orderId)
    detailItems.value = res.code === 1 ? res.data : []
  } catch (e) {
    detailItems.value = []
  }
  detailVisible.value = true
}

const handlePayment = async (orderId) => {
  try {
    const modeRes = await shopApi.getPaymentMode()
    const paymentMode = modeRes.code === 1 ? (modeRes.data ?? 1) : 1
    if (paymentMode === 0) {
      await ElMessageBox.confirm('确认支付该订单？', '模拟支付', { confirmButtonText: '确认支付', type: 'warning' })
      const res = await orderApi.mockPayment(orderId)
      if (res.code === 1) {
        ElMessage.success('支付成功')
        loadOrders()
      } else {
        ElMessage.error(res.msg || '支付失败')
      }
    } else {
      await ElMessageBox.confirm('确认支付该订单？', '支付宝支付', { confirmButtonText: '确认支付', type: 'warning' })
      window.location.href = '/api/user/order/payment/page/' + orderId
    }
  } catch (e) {
    // 用户取消
  }
}

const handleReceipt = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'warning' })
    await orderApi.receipt(orderId)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '操作失败')
  }
}

const handleCancel = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', { type: 'warning' })
    await orderApi.cancel(orderId)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '操作失败')
  }
}

const handleRefund = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要申请退货吗？', '申请退货', { type: 'warning' })
    await orderApi.refund(orderId)
    ElMessage.success('退货申请已提交')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '操作失败')
  }
}

let refreshTimer = null
onMounted(() => {
  loadOrders()
  refreshTimer = setInterval(loadOrders, 30000)
})
onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.page-header { text-align: center; margin-bottom: 28px; }
.page-header h2 { font-size: 26px; font-weight: 700; color: #2D2320; margin: 0; }
.order-list { display: flex; flex-direction: column; gap: 16px; }
.more-orders { text-align: center; padding: 8px 0; }
.order-card { background: #fff; border-radius: 12px; padding: 20px 24px; border-left: 4px solid #ddd; transition: border-color 0.3s; }
.order-card.status-border-1 { border-left-color: #E6A23C; }
.order-card.status-border-2 { border-left-color: #409EFF; }
.order-card.status-border-3 { border-left-color: #67C23A; }
.order-card.status-border-4 { border-left-color: #F56C6C; }
.order-card.status-border-5 { border-left-color: #909399; }
.order-card.status-border-6 { border-left-color: #67C23A; }
.order-card.status-border-7 { border-left-color: #C0C4CC; }
.order-card.status-border-8 { border-left-color: #E6A23C; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; padding-bottom: 10px; border-bottom: 1px solid #F5F0EC; }
.order-number { font-size: 14px; color: #5D4A3A; }
.order-price { font-size: 18px; font-weight: 600; color: #8B4A50; margin-left: 16px; }
.status-badge { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.badge-1 { background: #FDF6EC; color: #E6A23C; }
.badge-2 { background: #ECF5FF; color: #409EFF; }
.badge-3 { background: #F0F9EB; color: #67C23A; }
.badge-4 { background: #FEF0F0; color: #F56C6C; }
.badge-5 { background: #F4F4F5; color: #909399; }
.badge-6 { background: #F0F9EB; color: #67C23A; }
.badge-7 { background: #F4F4F5; color: #C0C4CC; text-decoration: line-through; }
.badge-8 { background: #FDF6EC; color: #E6A23C; }
.order-body { display: flex; justify-content: space-between; align-items: stretch; }
.order-left { display: flex; flex: 1; min-width: 0; }
.order-info { flex: 1; min-width: 0; }
.order-info p { font-size: 13px; color: #5D4A3A; margin: 4px 0; }
.order-info .label { color: #9B8B85; }
.delivery-estimate .highlight-time { color: #E6A23C; font-weight: 600; }
.order-goods { display: flex; flex-direction: column; gap: 6px; padding-left: 16px; margin-left: 16px; border-left: 1px solid #F5F0EC; flex-shrink: 0; justify-content: center; }
.goods-preview { display: flex; align-items: center; gap: 6px; }
.goods-img { width: 36px; height: 36px; border-radius: 6px; flex-shrink: 0; }
.goods-name { font-size: 12px; color: #5D4A3A; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 90px; }
.goods-more { font-size: 12px; color: #9B8B85; }
.order-actions { display: flex; flex-direction: column; gap: 8px; flex-shrink: 0; margin-left: 20px; justify-content: center; }
.empty-state { text-align: center; padding: 80px; background: #fff; border-radius: 12px; }
.empty-state p { color: #B5A39C; margin-bottom: 16px; }
</style>