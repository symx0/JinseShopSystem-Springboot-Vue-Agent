<template>
  <div class="page-container">
    <div class="page-header">
      <h3>修改密码</h3>
    </div>

    <el-card shadow="never" class="form-card">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px" style="max-width: 450px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChange">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api'
import { useUserStore } from '@/store'

const userStore = useUserStore()
const formRef = ref(null)

const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirmPass = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const formRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPass, trigger: 'blur' }
  ]
}

const handleChange = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const manager = userStore.manager
    if (!manager?.id) return
    await employeeApi.update({ id: manager.id, password: form.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
  })
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { margin-bottom: 16px; }
.page-header h3 { margin: 0; color: #3D2E28; font-size: 18px; }
.form-card { border-radius: 10px; border: 1px solid #EDE4DD; padding: 24px; }
</style>