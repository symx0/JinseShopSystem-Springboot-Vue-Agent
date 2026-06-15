<template>
  <div class="page-container">
    <div class="page-header">
      <h3>分类管理</h3>
      <el-button type="primary" @click="openDialog()">新增分类</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="search-bar">
        <el-input v-model="search.name" placeholder="分类名称" clearable style="width: 180px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
        <el-button type="success" @click="handleBatchEnable" :disabled="selectedRows.length === 0">批量启用</el-button>
        <el-button type="warning" @click="handleBatchDisable" :disabled="selectedRows.length === 0">批量禁用</el-button>
      </div>

      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="分类名称" width="150" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="image" label="图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width:40px;height:40px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
import { categoryApi, commonApi } from '@/api'
import config from '@/config'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(config.adminPageSize)
const dialogVisible = ref(false)
const formRef = ref(null)
const editId = ref(null)

const search = reactive({ name: '' })
const form = reactive({ id: null, name: '', sort: 0, image: '' })

const dialogTitle = computed(() => editId.value ? '编辑分类' : '新增分类')
const uploadUrl = '/api/admin/common/upload'

const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const loadData = async () => {
  const res = await categoryApi.page({ page: page.value, pageSize: pageSize.value, name: search.name || undefined })
  if (res.code === 1) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const resetSearch = () => { search.name = ''; loadData() }
const handleSizeChange = () => { page.value = 1; loadData() }

const openDialog = (row) => {
  editId.value = row?.id || null
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, sort: row.sort, image: row.image || '' })
  } else {
    Object.assign(form, { id: null, name: '', sort: 0, image: '' })
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
      await categoryApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await categoryApi.save(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  })
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await categoryApi.setStatus(newStatus, row.id)
  ElMessage.success('操作成功')
  loadData()
}

const handleDelete = async (id) => {
  await categoryApi.delete([id])
  ElMessage.success('删除成功')
  loadData()
}

const handleBatchEnable = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要启用的分类')
    return
  }
  const ids = selectedRows.value.map(row => row.id)
  await categoryApi.setStatusBatch(1, ids)
  ElMessage.success('批量启用成功')
  loadData()
}

const handleBatchDisable = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要禁用的分类')
    return
  }
  const ids = selectedRows.value.map(row => row.id)
  await categoryApi.setStatusBatch(0, ids)
  ElMessage.success('批量禁用成功')
  loadData()
}

const selectedRows = ref([])
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的分类')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个分类吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const ids = selectedRows.value.map(row => row.id)
    await categoryApi.delete(ids)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const uploadHeaders = { token: JSON.parse(localStorage.getItem('manager') || '{}').token || '' }

onMounted(loadData)
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h3 { margin: 0; color: #3D2E28; font-size: 18px; }
.table-card { border-radius: 10px; border: 1px solid #EDE4DD; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>