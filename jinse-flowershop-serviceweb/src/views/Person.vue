<template>
  <div class="page-container">
    <div class="page-header">
      <h3>个人信息</h3>
    </div>

    <el-card shadow="never" class="form-card">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px" style="max-width: 500px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="form.username" disabled />
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
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api'
import { useUserStore } from '@/store'

const userStore = useUserStore()
const formRef = ref(null)

const form = reactive({ id: null, name: '', username: '', phone: '', sex: '1', idNumber: '' })

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const loadData = async () => {
  const manager = userStore.manager
  if (!manager?.id) return
  const res = await employeeApi.getById(manager.id)
  if (res.code === 1) {
    Object.assign(form, res.data)
  }
}

const handleSave = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    await employeeApi.update(form)
    ElMessage.success('保存成功')
    const updated = { ...userStore.manager, ...form }
    userStore.setManager(updated)
  })
}

onMounted(loadData)
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { margin-bottom: 16px; }
.page-header h3 { margin: 0; color: #3D2E28; font-size: 18px; }
.form-card { border-radius: 10px; border: 1px solid #EDE4DD; padding: 24px; }
</style>