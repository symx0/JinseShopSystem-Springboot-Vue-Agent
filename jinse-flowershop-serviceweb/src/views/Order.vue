<template>
  <div class="page-container">
    <div class="page-header">
      <h3>订单管理</h3>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="search-bar">
        <el-input v-model="search.number" placeholder="订单号" clearable style="width: 180px" />
        <el-input v-model="search.phone" placeholder="手机号" clearable style="width: 150px" />
        <el-input v-model="search.userId" placeholder="用户ID" clearable style="width: 120px" />
        <el-date-picker
          v-model="search.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 360px"
          @change="loadData"
        />
        <el-select v-model="search.status" placeholder="订单状态" clearable style="width: 140px" @change="loadData">
          <el-option label="待付款" :value="1" />
          <el-option label="待接单" :value="2" />
          <el-option label="已接单" :value="3" />
          <el-option label="派送中" :value="4" />
          <el-option label="已确认" :value="5" />
          <el-option label="已完成" :value="6" />
          <el-option label="已取消" :value="7" />
          <el-option label="退货申请中" :value="8" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
        <span class="delivery-days-wrap">
          <span class="delivery-days-label">发货天数：</span>
          <el-input-number v-model="deliveryDays" :min="1" :max="30" size="small" style="width: 120px" />
          <el-button size="small" type="primary" @click="saveDeliveryDays">保存</el-button>
        </span>
      </div>

      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="number" label="订单号" width="150" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="consignee" label="收货人" width="100" />
        <el-table-column prop="amount" label="金额（元）" width="110" />
        <el-table-column prop="status" label="状态" width="140">
          <template #default="{ row }">
            <div style="display:flex; flex-direction:column; gap:4px;">
              <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status, row.payStatus) }}</el-tag>
              <el-tag v-if="row.status === 8 && getPrevStatus(row)" :type="statusType(getPrevStatus(row))" size="small">{{ statusText(getPrevStatus(row)) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" link @click="confirmOrder(row)">接单</el-button>
            <el-button v-if="row.status === 3" size="small" type="warning" link @click="deliveryOrder(row)">送出</el-button>
            <el-button v-if="row.status === 5" size="small" type="success" link @click="completeOrder(row)">完成</el-button>
            <el-button v-if="row.status === 8" size="small" type="success" link @click="approveRefund(row)">同意退货</el-button>
            <el-button v-if="row.status === 8" size="small" type="danger" link @click="rejectRefund(row)">拒绝退货</el-button>
            <el-button v-if="row.status !== 6 && row.status !== 7 && row.status !== 8" size="small" type="danger" link @click="cancelOrder(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog title="订单详情" v-model="detailVisible" width="600px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号">{{ detail.number }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.status)" size="small">{{ statusText(detail.status, detail.payStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detail.consignee }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ detail.address }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detail.orderTime }}</el-descriptions-item>
        <el-descriptions-item label="配送方式">{{ detail.deliveryStatus === 1 ? '立即送出' : '预约送达' }}</el-descriptions-item>
        <el-descriptions-item label="预计送达">{{ detail.estimatedDeliveryTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>商品明细</el-divider>
      <el-table :data="detail.orderDetailList || []" border size="small">
        <el-table-column label="图片" width="70">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width:40px;height:40px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="flowerName" label="商品名称" />
        <el-table-column prop="number" label="数量" width="60" />
        <el-table-column prop="amount" label="金额" width="80" />
      </el-table>
      <el-divider>费用明细</el-divider>
      <table class="fee-table">
        <tr>
          <td class="fee-label">商品总额</td>
          <td class="fee-value">¥{{ ((detail.amount || 0) - (detail.deliveryFee || 0) - (detail.packAmount || 0)).toFixed(2) }}</td>
        </tr>
        <tr>
          <td class="fee-label">配送费</td>
          <td class="fee-value">{{ (detail.deliveryFee || 0) === 0 ? '免运费' : '¥' + (detail.deliveryFee || 0).toFixed(2) }}</td>
        </tr>
        <tr>
          <td class="fee-label">打包费</td>
          <td class="fee-value">{{ (detail.packAmount || 0) === 0 ? '免打包费' : '¥' + (detail.packAmount || 0).toFixed(2) }}</td>
        </tr>
        <tr class="fee-total">
          <td class="fee-label">应付金额</td>
          <td class="fee-value">¥{{ (detail.amount || 0).toFixed(2) }}</td>
        </tr>
      </table>
    </el-dialog>

    <!-- 拒绝退货弹窗 -->
    <el-dialog title="拒绝退货" v-model="rejectVisible" width="420px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="doReject">确定拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单弹窗 -->
    <el-dialog title="取消订单" v-model="cancelVisible" width="420px">
      <el-form label-width="80px">
        <el-form-item label="取消原因">
          <el-select v-model="cancelReason" placeholder="请选择取消原因" style="width: 100%">
            <el-option v-for="r in cancelReasons" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="cancelRemark" type="textarea" :rows="2" placeholder="可选，补充说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="danger" @click="doCancel">确定取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, shopApi } from '@/api'
import config from '@/config'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(config.adminPageSize)
const detailVisible = ref(false)
const detail = ref({})
const deliveryDays = ref(1)

const search = reactive({ number: '', phone: '', userId: '', timeRange: null, status: null })

const statusType = (s) => {
  const map = { 1: 'warning', 2: 'info', 3: '', 4: '', 5: 'primary', 6: 'success', 7: 'danger', 8: 'warning' }
  return map[s] || 'info'
}
const statusText = (s, payStatus) => {
  if (s === 7 && payStatus === 2) return '已退款'
  const map = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已确认', 6: '已完成', 7: '已取消', 8: '退货申请中' }
  return map[s] || '未知'
}

// 从 cancelReason 中解析退货前状态（格式：前状态|||原因）
const getPrevStatus = (row) => {
  if (!row.cancelReason || !row.cancelReason.includes('|||')) return null
  const parts = row.cancelReason.split('|||')
  const prev = parseInt(parts[0], 10)
  return isNaN(prev) ? null : prev
}

const loadData = async () => {
  const params = {
    page: page.value,
    pageSize: pageSize.value,
    number: search.number || undefined,
    phone: search.phone || undefined,
    userId: search.userId || undefined,
    beginTime: search.timeRange ? search.timeRange[0] : undefined,
    endTime: search.timeRange ? search.timeRange[1] : undefined,
    status: search.status || undefined
  }
  const res = await orderApi.page(params)
  if (res.code === 1) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const resetSearch = () => { search.number = ''; search.phone = ''; search.userId = ''; search.timeRange = null; search.status = null; loadData() }

const handleSizeChange = () => { page.value = 1; loadData() }

const showDetail = async (row) => {
  try {
    const res = await orderApi.detail(row.id)
    if (res.code === 1 && res.data) {
      detail.value = { ...res.data.order, orderDetailList: res.data.details || [] }
    } else {
      detail.value = row
    }
  } catch (e) {
    detail.value = row
  }
  detailVisible.value = true
}

const confirmOrder = async (row) => {
  await orderApi.confirm(row.id)
  ElMessage.success('已接单')
  loadData()
}

const deliveryOrder = async (row) => {
  await orderApi.delivery(row.id)
  ElMessage.success('已送出')
  loadData()
}

const completeOrder = async (row) => {
  await orderApi.complete(row.id)
  ElMessage.success('已完成订单')
  loadData()
}

const cancelReasons = [
  '库存不足',
  '商品已下架',
  '用户要求取消',
  '地址信息有误',
  '重复订单',
  '其他原因'
]

const cancelVisible = ref(false)
const cancelReason = ref('')
const cancelRemark = ref('')
const cancelTargetRow = ref(null)

const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTargetRow = ref(null)

const cancelOrder = (row) => {
  cancelTargetRow.value = row
  cancelReason.value = ''
  cancelRemark.value = ''
  cancelVisible.value = true
}

const doCancel = async () => {
  if (!cancelReason.value) {
    ElMessage.warning('请选择取消原因')
    return
  }
  const reason = cancelRemark.value ? `${cancelReason.value}：${cancelRemark.value}` : cancelReason.value
  await orderApi.cancel(cancelTargetRow.value.id, reason)
  ElMessage.success('已取消订单，如已付款将自动退款')
  cancelVisible.value = false
  loadData()
}

const approveRefund = async (row) => {
  try {
    await ElMessageBox.confirm('确认同意退货并退款？', '同意退货', { confirmButtonText: '确认退款', type: 'warning' })
    await orderApi.approveRefund(row.id)
    ElMessage.success('已同意退货，已退款')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '操作失败')
  }
}

const rejectRefund = (row) => {
  rejectTargetRow.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

const doReject = async () => {
  if (!rejectReason.value) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  try {
    await orderApi.rejectRefund({ id: rejectTargetRow.value.id, cancelReason: rejectReason.value })
    ElMessage.success('已拒绝退货申请')
    rejectVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const selectedRows = ref([])
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的订单')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个订单吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const ids = selectedRows.value.map(row => row.id)
    await orderApi.delete(ids)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

let refreshTimer = null
const handleVisibility = () => {
  if (document.hidden) {
    if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
  } else {
    if (!refreshTimer) {
      loadData()
      refreshTimer = setInterval(loadData, config.orderRefreshInterval)
    }
  }
}
onMounted(() => {
  loadData()
  refreshTimer = setInterval(loadData, config.orderRefreshInterval)
  document.addEventListener('visibilitychange', handleVisibility)
})
onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  document.removeEventListener('visibilitychange', handleVisibility)
})

const loadDeliveryDays = async () => {
  try {
    const res = await shopApi.getDeliveryDays()
    if (res.code === 1 && res.data != null) {
      deliveryDays.value = res.data
    }
  } catch (e) {}
}

const saveDeliveryDays = async () => {
  try {
    await shopApi.setDeliveryDays(deliveryDays.value)
    ElMessage.success(`发货天数已设置为 ${deliveryDays.value} 天`)
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

loadDeliveryDays()
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h3 { margin: 0; color: #3D2E28; font-size: 18px; }
.table-card { border-radius: 10px; border: 1px solid #EDE4DD; }
.search-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.delivery-days-wrap { margin-left: 20px; display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.delivery-days-label { font-size: 14px; color: #5D4A3A; white-space: nowrap; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.fee-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
.fee-table td { padding: 8px 12px; border-bottom: 1px solid #EBEEF5; font-size: 14px; }
.fee-label { color: #909399; width: 120px; }
.fee-value { text-align: right; color: #303133; }
.fee-total td { border-bottom: none; font-weight: 700; font-size: 15px; }
.fee-total .fee-value { color: #F56C6C; }
</style>