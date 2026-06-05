<template>
  <div class="page-container">
    <div class="page-header">
      <h3>评论管理</h3>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="search-bar">
        <el-input v-model="search.content" placeholder="评论内容" clearable style="width: 200px" />
        <el-input v-model="search.userName" placeholder="用户名" clearable style="width: 150px" />
        <el-input v-model="search.flowerName" placeholder="花束名称" clearable style="width: 150px" />
        <el-input v-model="search.flowerId" placeholder="花束ID" clearable style="width: 120px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
      </div>

      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.flowerImage" :src="row.flowerImage" style="width:60px;height:60px;border-radius:4px;" fit="cover" />
            <span v-else style="color:#999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="flowerId" label="花束ID" width="90" />
        <el-table-column prop="userName" label="用户名" width="120">
          <template #default="{ row }">{{ row.userName || '匿名用户' }}</template>
        </el-table-column>
        <el-table-column prop="flowerName" label="花束" width="140">
          <template #default="{ row }">{{ row.flowerName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled size="small" style="--el-rate-icon-size:14px;" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="likeCount" label="点赞" width="70" />
        <el-table-column prop="createTime" label="评论时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该评论吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const search = ref({ content: '', userName: '', flowerName: '', flowerId: '' })
const selectedIds = ref([])

const loadData = async () => {
  try {
    const res = await request.post('/admin/comment/page', {
      page: page.value,
      pageSize: pageSize.value,
      content: search.value.content,
      userName: search.value.userName,
      flowerName: search.value.flowerName,
      flowerId: search.value.flowerId ? Number(search.value.flowerId) : undefined
    })
    if (res.code === 1) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载评论列表失败')
  }
}

const resetSearch = () => {
  search.value = { content: '', userName: '', flowerName: '', flowerId: '' }
  loadData()
}

const handleSizeChange = () => {
  page.value = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleDelete = async (id) => {
  try {
    const res = await request.delete('/admin/comment', { params: { ids: String(id) } })
    if (res.code === 1) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    const res = await request.delete('/admin/comment', { params: { ids: selectedIds.value.join(',') } })
    if (res.code === 1) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header h3 {
  margin: 0 0 16px;
  font-size: 20px;
  color: #2D2320;
}

.table-card {
  border-radius: 12px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
