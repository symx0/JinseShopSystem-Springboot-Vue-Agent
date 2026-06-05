<template>
  <div class="page-container">
    <div class="page-header">
      <h3>用户管理</h3>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="search-bar">
        <el-input v-model="search.name" placeholder="用户姓名" clearable style="width: 180px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="sex" label="性别" width="70">
          <template #default="{ row }">{{ row.sex === '1' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :size="36" :src="row.avatar" />
            <el-avatar v-else :size="36" icon="UserFilled" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '@/api'
import config from '@/config'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(config.adminPageSize)
const search = reactive({ name: '' })

const loadData = async () => {
  const res = await userApi.page({ page: page.value, pageSize: pageSize.value, name: search.name || undefined })
  if (res.code === 1) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const resetSearch = () => { search.name = ''; loadData() }

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