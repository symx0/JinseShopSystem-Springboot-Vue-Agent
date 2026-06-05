<template>
  <div class="page-container">
    <div class="page-header">
      <h3>热门活动</h3>
      <el-button type="primary" @click="handleAdd">创建活动</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="search-bar">
        <el-input v-model="search.name" placeholder="活动内容" clearable style="width: 180px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
        <el-button type="success" @click="handleBatchStatus(1)" :disabled="selectedRows.length === 0">批量启用</el-button>
        <el-button type="info" @click="handleBatchStatus(2)" :disabled="selectedRows.length === 0">批量结束</el-button>
      </div>

      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="content" label="活动内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="limitPer" label="限购数量" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleManageProducts(row)">商品</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleSetStatus(1, row.id)">启用</el-button>
            <el-button v-if="row.status === 0" type="info" link size="small" @click="handleSetStatus(2, row.id)">结束</el-button>
            <el-button v-if="row.status === 1" type="info" link size="small" @click="handleSetStatus(2, row.id)">结束</el-button>
            <el-button v-if="row.status === 2" type="success" link size="small" @click="handleSetStatus(0, row.id)">重置</el-button>
            <el-button type="danger" link size="small" @click="handleSingleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 创建/编辑活动对话框 -->
    <el-dialog :title="isEdit ? '编辑活动' : '创建活动'" v-model="dialogVisible" width="550px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="活动内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入活动内容" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="限购数量" prop="limitPer">
          <el-input-number v-model="form.limitPer" :min="1" :max="99" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">{{ isEdit ? '保存修改' : '确定' }}</el-button>
      </template>
    </el-dialog>

    <!-- 活动商品管理对话框 -->
    <el-dialog title="管理活动商品" v-model="saleDialogVisible" width="960px" destroy-on-close @opened="loadSaleData">
      <!-- 销售统计 -->
      <div class="stats-bar" v-if="activityStats">
        <div class="stats-item">
          <span class="stats-label">总销量</span>
          <span class="stats-value">{{ activityStats.totalSale || 0 }} 件</span>
        </div>
        <div class="stats-item">
          <span class="stats-label">总销售额</span>
          <span class="stats-value">¥{{ activityStats.totalRevenue || 0 }}</span>
        </div>
      </div>
      <el-table :data="saleTableData" border stripe>
        <el-table-column prop="flowerName" label="鲜花名称" min-width="140">
          <template #default="{ row }">
            <template v-if="row._isAddRow">
              <el-select v-model="saleForm.flowerId" placeholder="选择鲜花" filterable size="small" style="width: 100%" @change="onFlowerSelect">
                <el-option v-for="f in normalFlowerList" :key="f.id" :label="f.name + ' (¥' + f.price + ')'" :value="f.id" />
              </el-select>
            </template>
            <template v-else>{{ row.flowerName }}</template>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            <template v-if="row._isAddRow">
              <el-input-number v-model="saleForm.originalPrice" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" />
            </template>
            <template v-else-if="editingSaleId === row.id">
              <el-input-number v-model="row.originalPrice" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" />
            </template>
            <template v-else>{{ row.originalPrice }}</template>
          </template>
        </el-table-column>
        <el-table-column prop="discountPrice" label="优惠价" width="100">
          <template #default="{ row }">
            <template v-if="row._isAddRow">
              <el-input-number v-model="saleForm.discountPrice" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" />
            </template>
            <template v-else-if="editingSaleId === row.id">
              <el-input-number v-model="row.discountPrice" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" />
            </template>
            <template v-else>{{ row.discountPrice }}</template>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80">
          <template #default="{ row }">
            <template v-if="row._isAddRow">
              <el-input-number v-model="saleForm.stock" :min="0" size="small" controls-position="right" style="width: 100%" />
            </template>
            <template v-else-if="editingSaleId === row.id">
              <el-input-number v-model="row.stock" :min="0" size="small" controls-position="right" style="width: 80px" />
            </template>
            <template v-else>{{ row.stock }}</template>
          </template>
        </el-table-column>
        <el-table-column prop="sale" label="已售" width="80">
          <template #default="{ row }">
            <template v-if="row._isAddRow">--</template>
            <template v-else>{{ row.sale }}</template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <template v-if="row._isAddRow">
              <el-button type="primary" size="small" @click="handleAddSale">添加</el-button>
            </template>
            <template v-else-if="editingSaleId === row.id">
              <el-button type="success" link size="small" @click="handleSaveEditSale(row)">保存</el-button>
              <el-button type="info" link size="small" @click="handleCancelEditSale(row)">取消</el-button>
            </template>
            <template v-else>
              <el-button type="primary" link size="small" @click="handleEditSale(row)">修改</el-button>
              <el-button type="danger" link size="small" @click="handleDeleteSale(row)">移除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 10px; color: #999; font-size: 12px;">共 {{ saleTotal }} 条</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { activityApi, flowerApi } from '@/api'
import config from '@/config'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(config.adminPageSize)
const dialogVisible = ref(false)
const formRef = ref(null)
const editId = ref(null)
const isEdit = ref(false)

const form = reactive({ content: '', startTime: '', endTime: '', limitPer: 1 })
const search = reactive({ name: '' })

const formRules = {
  content: [{ required: true, message: '请输入活动内容', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// 商品管理相关
const saleDialogVisible = ref(false)
const currentActivityId = ref(null)
const saleTableData = ref([])
const saleTotal = ref(0)
const flowerList = ref([])
const normalFlowerList = computed(() => flowerList.value.filter(f => !f.name.startsWith('[促销]')))
const saleForm = reactive({ flowerId: null, originalPrice: 0, discountPrice: 0, stock: 0 })
const editingSaleId = ref(null)
const originalSaleData = ref(null)
const activityStats = ref(null)

const statusType = (s) => s === 1 ? 'success' : s === 2 ? 'info' : 'warning'
const statusText = (s) => s === 1 ? '进行中' : s === 2 ? '已结束' : '未开始'

const loadData = async () => {
  const res = await activityApi.page({ page: page.value, pageSize: pageSize.value, name: search.name || undefined })
  if (res.code === 1) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const resetSearch = () => { search.name = ''; page.value = 1; loadData() }

const resetForm = () => {
  form.content = ''
  form.startTime = ''
  form.endTime = ''
  form.limitPer = 1
  editId.value = null
  isEdit.value = false
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  isEdit.value = true
  dialogVisible.value = true
  const res = await activityApi.getById(row.id)
  if (res.code === 1) {
    const d = res.data
    editId.value = d.id
    form.content = d.content
    form.startTime = d.startTime
    form.endTime = d.endTime
    form.limitPer = d.limitPer
  }
}

const handleSave = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (isEdit.value) {
      await activityApi.update({ id: editId.value, ...form })
      ElMessage.success('修改成功')
    } else {
      await activityApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  })
}

const selectedRows = ref([])
const handleSelectionChange = (rows) => { selectedRows.value = rows }

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) return ElMessage.warning('请选择要删除的活动')
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个活动吗？`, '确认删除', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    await activityApi.delete(selectedRows.value.map(r => r.id))
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const handleSingleDelete = async (row) => {
  await activityApi.delete([row.id])
  ElMessage.success('删除成功')
  loadData()
}

const handleSetStatus = async (status, id) => {
  await activityApi.setStatus(status, id)
  ElMessage.success('状态修改成功')
  loadData()
}

const handleBatchStatus = async (status) => {
  if (selectedRows.value.length === 0) return ElMessage.warning('请先选择活动')
  await activityApi.setStatusBatch(status, selectedRows.value.map(r => r.id))
  ElMessage.success('批量修改状态成功')
  loadData()
}

// 商品管理
const loadFlowerList = async () => {
  try {
    const res = await flowerApi.page({ page: 1, pageSize: 999 })
    if (res.code === 1) flowerList.value = (res.data && res.data.records) || []
  } catch (e) { /* ignore */ }
}

const loadSaleData = async () => {
  // 先刷新鲜花列表，确保新增的促销鲜花也在其中
  await loadFlowerList()
  const res = await activityApi.salePage({ page: 1, pageSize: 100, activityId: currentActivityId.value })
  if (res.code === 1) {
    const records = res.data.records || []
    // 补充鲜花名称
    const nameMap = {}
    flowerList.value.forEach(f => { nameMap[f.id] = f.name })
    saleTableData.value = records.map(r => ({ ...r, flowerName: nameMap[r.flowerId] || '未知鲜花' }))
    // 末尾添加一行"添加行"
    saleTableData.value.push({ _isAddRow: true })
    saleTotal.value = res.data.total || 0
  }
  // 加载销售统计
  try {
    const statsRes = await activityApi.stats(currentActivityId.value)
    if (statsRes.code === 1) {
      activityStats.value = statsRes.data
    }
  } catch (e) { /* ignore */ }
}

const handleManageProducts = async (row) => {
  currentActivityId.value = row.id
  saleForm.flowerId = null
  saleForm.originalPrice = 0
  saleForm.discountPrice = 0
  saleForm.stock = 0
  await loadFlowerList()
  saleDialogVisible.value = true
}

const onFlowerSelect = (flowerId) => {
  const flower = flowerList.value.find(f => f.id === flowerId)
  if (flower && flower.price != null) {
    saleForm.originalPrice = flower.price
    saleForm.discountPrice = flower.price
  }
}

const handleAddSale = async () => {
  if (!saleForm.flowerId) return ElMessage.warning('请选择鲜花')
  const flower = flowerList.value.find(f => f.id === saleForm.flowerId)
  await activityApi.setSale({
    activityId: currentActivityId.value,
    flowerId: saleForm.flowerId,
    originalPrice: saleForm.originalPrice,
    discountPrice: saleForm.discountPrice,
    stock: saleForm.stock,
    sale: 0,
    flowerName: flower?.name || '',
    categoryId: flower?.categoryId || null,
    flowerImage: flower?.image || '',
    flowerDescription: flower?.description || ''
  })
  ElMessage.success('添加成功')
  saleForm.flowerId = null
  saleForm.originalPrice = 0
  saleForm.discountPrice = 0
  saleForm.stock = 0
  loadSaleData()
}

const handleDeleteSale = async (row) => {
  await activityApi.deleteSale(row.id, row.activityId, row.flowerId)
  ElMessage.success('移除成功')
  loadSaleData()
}

const handleEditSale = (row) => {
  editingSaleId.value = row.id
  originalSaleData.value = {
    originalPrice: row.originalPrice,
    discountPrice: row.discountPrice,
    stock: row.stock
  }
}

const handleSaveEditSale = async (row) => {
  await activityApi.updateSale({
    id: row.id,
    activityId: row.activityId,
    flowerId: row.flowerId,
    originalPrice: row.originalPrice,
    discountPrice: row.discountPrice,
    stock: row.stock,
    sale: row.sale,
    flowerName: row.flowerName || row.name?.replace(/^\[促销\]/, '') || '',
    flowerImage: row.flowerImage || row.image || '',
    flowerDescription: row.flowerDescription || row.description || ''
  })
  ElMessage.success('修改成功')
  editingSaleId.value = null
  originalSaleData.value = null
}

const handleCancelEditSale = (row) => {
  if (originalSaleData.value) {
    row.originalPrice = originalSaleData.value.originalPrice
    row.discountPrice = originalSaleData.value.discountPrice
    row.stock = originalSaleData.value.stock
  }
  editingSaleId.value = null
  originalSaleData.value = null
}

onMounted(loadData)
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h3 { margin: 0; color: #3D2E28; font-size: 18px; }
.table-card { border-radius: 10px; border: 1px solid #EDE4DD; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.stats-bar { display: flex; gap: 24px; margin-bottom: 16px; padding: 12px 16px; background: #fdf6f6; border-radius: 8px; }
.stats-item { display: flex; flex-direction: column; align-items: center; }
.stats-label { font-size: 12px; color: #999; margin-bottom: 4px; }
.stats-value { font-size: 18px; font-weight: 700; color: #E8574A; }
</style>