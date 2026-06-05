<template>
  <div class="page-container">
    <div class="page-header">
      <h3>员工管理</h3>
      <el-button type="primary" @click="openDialog()">新增员工</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="search-bar">
        <el-input v-model="search.name" placeholder="员工姓名" clearable style="width: 180px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
        <el-button type="success" @click="handleBatchEnable" :disabled="selectedRows.length === 0">批量启用</el-button>
        <el-button type="warning" @click="handleBatchDisable" :disabled="selectedRows.length === 0">批量禁用</el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="sex" label="性别" width="70">
          <template #default="{ row }">{{ row.sex === '1' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="idNumber" label="身份证号" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio value="1">男</el-radio>
            <el-radio value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="form.idNumber" placeholder="请输入身份证号" />
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
import { employeeApi } from '@/api'
import config from '@/config'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(config.adminPageSize)
const dialogVisible = ref(false)
const formRef = ref(null)
const editId = ref(null)

const search = reactive({ name: '' })
const form = reactive({ id: null, name: '', username: '', password: '', phone: '', sex: '1', idNumber: '' })

const dialogTitle = computed(() => editId.value ? '编辑员工' : '新增员工')

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

const loadData = async () => {
  const res = await employeeApi.page({ page: page.value, pageSize: pageSize.value, name: search.name || undefined })
  if (res.code === 1) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const resetSearch = () => {
  search.name = ''
  loadData()
}

const openDialog = (row) => {
  editId.value = row?.id || null
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, username: row.username, password: '', phone: row.phone, sex: row.sex, idNumber: row.idNumber })
  } else {
    Object.assign(form, { id: null, name: '', username: '', password: '', phone: '', sex: '1', idNumber: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (editId.value) {
      await employeeApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await employeeApi.save(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  })
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const res = await employeeApi.setStatus(newStatus, row.id)
  if (res.code === 1) {
    ElMessage.success('操作成功')
    loadData()
  }
}

const selectedRows = ref([])
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的员工')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个员工吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const ids = selectedRows.value.map(row => row.id)
    await employeeApi.delete(ids)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const handleDelete = async (row) => {
  await employeeApi.delete([row.id])
  ElMessage.success('删除成功')
  loadData()
}

const handleBatchEnable = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要启用的员工')
    return
  }
  const ids = selectedRows.value.map(row => row.id)
  await employeeApi.setStatusBatch(1, ids)
  ElMessage.success('批量启用成功')
  loadData()
}

const handleBatchDisable = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要禁用的员工')
    return
  }
  const ids = selectedRows.value.map(row => row.id)
  await employeeApi.setStatusBatch(0, ids)
  ElMessage.success('批量禁用成功')
  loadData()
}

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