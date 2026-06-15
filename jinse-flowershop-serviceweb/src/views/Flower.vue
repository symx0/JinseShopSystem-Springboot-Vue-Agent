<template>
  <div class="page-container">
    <div class="page-header">
      <h3>鲜花管理</h3>
      <el-button type="primary" @click="openDialog()">新增鲜花</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="search-bar">
        <el-input v-model="search.name" placeholder="鲜花名称" clearable style="width: 160px" />
        <el-select v-model="search.categoryId" placeholder="分类" clearable style="width: 140px" @change="loadData">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="search.activityId" placeholder="活动" clearable style="width: 160px" @change="loadData">
          <el-option v-for="a in activities" :key="a.id" :label="a.content" :value="a.id" />
          <el-option label="非活动商品" :value="-1" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
        <el-button type="success" @click="handleBatchOnShelf" :disabled="selectedRows.length === 0">批量上架</el-button>
        <el-button type="warning" @click="handleBatchOffShelf" :disabled="selectedRows.length === 0">批量下架</el-button>
      </div>

      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="image" label="图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width:50px;height:50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="140" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格（元）" width="110" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="550px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入鲜花名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" :on-success="handleUploadSuccess" :show-file-list="false">
            <el-button size="small">上传图片</el-button>
          </el-upload>
          <el-image v-if="form.image" :src="form.image" style="width:80px;height:80px;margin-top:8px" fit="cover" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flowerApi, categoryApi, activityApi } from '@/api'
import config from '@/config'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(config.adminPageSize)
const categories = ref([])
const activities = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const editId = ref(null)

const search = reactive({ name: '', categoryId: null, activityId: null })
const form = reactive({ id: null, name: '', categoryId: null, price: 0, description: '', image: '' })

const dialogTitle = computed(() => editId.value ? '编辑鲜花' : '新增鲜花')
const uploadUrl = '/api/admin/common/upload'

const formRules = {
  name: [{ required: true, message: '请输入鲜花名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const loadData = async () => {
  const params = { page: page.value, pageSize: pageSize.value, name: search.name || undefined, categoryId: search.categoryId || undefined }
  if (search.activityId === -1) {
    params.noActivity = true
  } else {
    params.activityId = search.activityId || undefined
  }
  const res = await flowerApi.page(params)
  if (res.code === 1) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const loadCategories = async () => {
  const res = await categoryApi.list(1)
  if (res.code === 1) categories.value = res.data || []
}

const loadActivities = async () => {
  const res = await activityApi.page({ page: 1, pageSize: 1000 })
  if (res.code === 1) activities.value = res.data.records || []
}

const resetSearch = () => { search.name = ''; search.categoryId = null; search.activityId = null; loadData() }
const handleSizeChange = () => { page.value = 1; loadData() }
const uploadHeaders = { token: JSON.parse(localStorage.getItem('manager') || '{}').token || '' }

const openDialog = (row) => {
  editId.value = row?.id || null
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, categoryId: row.categoryId, price: row.price, description: row.description || '', image: row.image || '' })
  } else {
    Object.assign(form, { id: null, name: '', categoryId: null, price: 0, description: '', image: '' })
  }
  dialogVisible.value = true
}

const handleUploadSuccess = (res) => {
  if (res.code === 1) form.image = res.data
}

const handleSave = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (editId.value) {
      await flowerApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await flowerApi.save(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  })
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await flowerApi.setStatus(newStatus, row.id)
  ElMessage.success('操作成功')
  loadData()
}

const selectedRows = ref([])
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的鲜花')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个鲜花吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const ids = selectedRows.value.map(row => row.id)
    await flowerApi.delete(ids)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const handleDelete = async (row) => {
  await flowerApi.delete([row.id])
  ElMessage.success('删除成功')
  loadData()
}

const handleBatchOnShelf = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要上架的鲜花')
    return
  }
  const ids = selectedRows.value.map(row => row.id)
  await flowerApi.setStatusBatch(1, ids)
  ElMessage.success('批量上架成功')
  loadData()
}

const handleBatchOffShelf = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要下架的鲜花')
    return
  }
  const ids = selectedRows.value.map(row => row.id)
  await flowerApi.setStatusBatch(0, ids)
  ElMessage.success('批量下架成功')
  loadData()
}

onMounted(() => { loadCategories(); loadActivities(); loadData() })
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h3 { margin: 0; color: #3D2E28; font-size: 18px; }
.table-card { border-radius: 10px; border: 1px solid #EDE4DD; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>