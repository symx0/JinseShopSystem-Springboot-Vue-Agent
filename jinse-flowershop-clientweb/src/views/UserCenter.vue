<template>
  <div class="user-center-page">
    <div class="page-header">
      <div class="user-avatar-wrap">
        <img v-if="userInfo?.avatar" :src="userInfo.avatar" class="user-avatar-img" />
        <div v-else class="user-avatar">👤</div>
      </div>
      <h2>{{ userInfo?.name || userInfo?.username || '用户' }}</h2>
      <p>欢迎回到锦瑟花店</p>
    </div>

    <div class="uc-content">
      <!-- 个人信息卡片 -->
      <div class="uc-card" v-if="userInfo">
        <div class="card-header">
          <h3>个人信息</h3>
          <el-button v-if="!editing" type="primary" size="small" round @click="startEdit">编辑</el-button>
          <div v-else class="edit-actions">
            <el-button size="small" round @click="cancelEdit">取消</el-button>
            <el-button type="primary" size="small" round :loading="saving" @click="saveEdit">保存</el-button>
          </div>
        </div>

        <!-- 展示模式 -->
        <template v-if="!editing">
          <div class="info-row">
            <span class="info-label">头像</span>
            <span>
              <img v-if="userInfo.avatar" :src="userInfo.avatar" class="info-avatar" />
              <span v-else>未设置</span>
            </span>
          </div>
          <div class="info-row">
            <span class="info-label">用户名</span>
            <span>{{ userInfo.username }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">姓名</span>
            <span>{{ userInfo.name || '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">手机号</span>
            <span>{{ userInfo.phone || '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">性别</span>
            <span>{{ userInfo.sex === '1' ? '男' : userInfo.sex === '0' ? '女' : '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">身份证号</span>
            <span>{{ userInfo.idNumber || '未设置' }}</span>
          </div>
        </template>

        <!-- 编辑模式 -->
        <template v-else>
          <el-form :model="editForm" label-width="80px" class="edit-form">
            <el-form-item label="头像">
              <div class="avatar-upload">
                <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar-preview" />
                <div v-else class="avatar-placeholder">点击上传</div>
                <input type="file" accept="image/*" class="avatar-file-input" @change="handleAvatarUpload" />
              </div>
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="editForm.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="editForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="editForm.sex">
                <el-radio label="1">男</el-radio>
                <el-radio label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="身份证号">
              <el-input v-model="editForm.idNumber" placeholder="请输入身份证号" />
            </el-form-item>
          </el-form>
        </template>
      </div>

      <!-- 地址簿管理 -->
      <div class="uc-card">
        <div class="card-header">
          <h3>收货地址</h3>
          <el-button type="primary" size="small" round @click="openAddressDialog(null)">新增地址</el-button>
        </div>
        <div v-if="addressList.length" class="address-list">
          <div v-for="addr in addressList" :key="addr.id" class="address-item" :class="{ default: addr.isDefault === 1 }">
            <div class="addr-main">
              <div class="addr-top">
                <span class="addr-name">{{ addr.consignee }}</span>
                <span class="addr-phone">{{ addr.phone }}</span>
                <el-tag v-if="addr.isDefault === 1" size="small" type="danger">默认</el-tag>
                <el-tag v-if="addr.label" size="small">{{ addr.label }}</el-tag>
              </div>
              <div class="addr-detail">{{ addr.provinceName }}{{ addr.cityName }}{{ addr.districtName }}{{ addr.detail }}</div>
            </div>
            <div class="addr-actions">
              <el-button v-if="addr.isDefault !== 1" link size="small" @click="setDefault(addr)">设为默认</el-button>
              <el-button link size="small" @click="openAddressDialog(addr)">编辑</el-button>
              <el-button link size="small" type="danger" @click="deleteAddress(addr.id)">删除</el-button>
            </div>
          </div>
        </div>
        <p v-else class="no-data">暂无收货地址</p>
      </div>

      <!-- 快捷操作 -->
      <div class="uc-card">
        <h3>快捷操作</h3>
        <div class="quick-actions">
          <div class="action-item" @click="$router.push('/order')">
            <el-icon :size="24"><Document /></el-icon>
            <span>我的订单</span>
          </div>
          <div class="action-item" @click="$router.push('/cart')">
            <el-icon :size="24"><ShoppingCart /></el-icon>
            <span>购物车</span>
          </div>
          <div class="action-item" @click="toggleComments">
            <el-icon :size="24"><ChatDotRound /></el-icon>
            <span>我的评论</span>
          </div>
          <div class="action-item" @click="pwdDialogVisible = true">
            <el-icon :size="24"><Lock /></el-icon>
            <span>修改密码</span>
          </div>
          <div class="action-item logout" @click="handleLogout">
            <el-icon :size="24"><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </div>
      </div>

      <!-- 我的评论 -->
      <div class="uc-card" v-if="showComments">
        <h3>我的评论</h3>
        <div v-if="myComments.length" class="comment-list">
          <div v-for="c in myComments" :key="c.id" class="comment-item">
            <router-link :to="`/flower/${c.flowerId}`" class="comment-flower-link">
              <img v-if="c.flowerImage" :src="c.flowerImage" :alt="c.flowerName" class="comment-flower-img" />
              <div v-else class="comment-flower-placeholder">🌸</div>
            </router-link>
            <div class="comment-body">
              <div class="comment-user-row">
                <img v-if="c.userAvatar" :src="c.userAvatar" class="comment-user-avatar" />
                <span class="comment-user-name">{{ c.userName || '我' }}</span>
                <el-button class="comment-delete-btn" link size="small" type="danger" @click="deleteComment(c.id)">删除</el-button>
              </div>
              <p class="comment-flower">{{ c.flowerName }}</p>
              <div class="comment-rating">
                <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= c.rating }">★</span>
              </div>
              <p class="comment-text">{{ c.content }}</p>
              <span class="comment-time">{{ formatTime(c.createTime) }}</span>
            </div>
          </div>
        </div>
        <p v-else class="no-data">暂无评论</p>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog title="修改密码" v-model="pwdDialogVisible" width="420px" :close-on-click-modal="false">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSaving" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog :title="addressForm.id ? '编辑地址' : '新增地址'" v-model="addressDialogVisible" width="500px" :close-on-click-modal="false">
      <el-form :model="addressForm" label-width="80px" :rules="addressRules" ref="addressFormRef">
        <div style="height: 20px;"></div>
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="addressForm.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="地区" prop="areaCode">
          <el-cascader
            v-model="addressForm.areaCode"
            :options="areaOptions"
            :props="{ value: 'code', label: 'name', children: 'children' }"
            placeholder="请选择省/市/区"
            style="width: 100%"
            @change="handleAreaChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" placeholder="请输入详细地址（街道、门牌号等）" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="addressForm.label" placeholder="请选择标签" clearable style="width: 100%">
            <el-option label="家" value="家" />
            <el-option label="公司" value="公司" />
            <el-option label="学校" value="学校" />
          </el-select>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSaving" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Document, ShoppingCart, ChatDotRound, SwitchButton, Lock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store'
import { userApi, addressApi, commentApi } from '@/api'
import areaData from 'china-area-data'

const userStore = useUserStore()
const userInfo = ref(null)
const myComments = ref([])
const showComments = ref(false)
const editing = ref(false)
const saving = ref(false)
const pwdSaving = ref(false)
const pwdDialogVisible = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const editForm = reactive({
  name: '',
  phone: '',
  sex: '',
  idNumber: '',
  avatar: ''
})

// 地址簿
const addressList = ref([])
const addressDialogVisible = ref(false)
const addressSaving = ref(false)
const addressFormRef = ref(null)

// 构建省市区级联数据
const areaOptions = computed(() => {
  const provinces = []
  const provData = areaData['86']
  if (!provData) return provinces
  for (const provinceCode in provData) {
    const province = { code: provinceCode, name: provData[provinceCode], children: [] }
    const cityData = areaData[provinceCode]
    if (cityData) {
      for (const cityCode in cityData) {
        const city = { code: cityCode, name: cityData[cityCode], children: [] }
        const districtData = areaData[cityCode]
        if (districtData) {
          for (const districtCode in districtData) {
            city.children.push({ code: districtCode, name: districtData[districtCode] })
          }
        }
        if (city.children.length) province.children.push(city)
      }
    }
    if (province.children.length) provinces.push(province)
  }
  return provinces
})

const addressForm = reactive({
  id: null,
  consignee: '',
  phone: '',
  provinceCode: '',
  provinceName: '',
  cityCode: '',
  cityName: '',
  districtCode: '',
  districtName: '',
  areaCode: [],
  detail: '',
  label: '',
  isDefault: 0
})

const phoneValidator = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号'))
  } else {
    callback()
  }
}

const addressRules = {
  consignee: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [{ required: true, validator: phoneValidator, trigger: 'blur' }],
  areaCode: [{ required: true, message: '请选择地区', trigger: 'change', type: 'array' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const handleAreaChange = (values) => {
  if (values && values.length === 3) {
    addressForm.provinceCode = values[0]
    addressForm.provinceName = areaData['86'][values[0]]
    addressForm.cityCode = values[1]
    addressForm.cityName = areaData[values[0]][values[1]]
    addressForm.districtCode = values[2]
    addressForm.districtName = areaData[values[1]][values[2]]
  }
}

const loadUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo()
    if (res.code === 1) {
      userInfo.value = res.data
    }
  } catch (e) {}
}

const startEdit = () => {
  editForm.name = userInfo.value?.name || ''
  editForm.phone = userInfo.value?.phone || ''
  editForm.sex = userInfo.value?.sex || ''
  editForm.idNumber = userInfo.value?.idNumber || ''
  editForm.avatar = userInfo.value?.avatar || ''
  editing.value = true
}

const handleAvatarUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await userApi.upload(formData)
    if (res.code === 1) {
      editForm.avatar = res.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error('上传失败')
    }
  } catch (err) {
    ElMessage.error('上传失败')
  }
}

const cancelEdit = () => {
  editing.value = false
}

const saveEdit = async () => {
  saving.value = true
  try {
    const res = await userApi.modifyInfo(editForm)
    if (res.code === 1) {
      ElMessage.success('修改成功')
      editing.value = false
      await loadUserInfo()
    }
  } catch (e) {
    ElMessage.error(e?.message || '修改失败')
  } finally {
    saving.value = false
  }
}

// 地址簿操作
const loadAddressList = async () => {
  try {
    const res = await addressApi.list()
    if (res.code === 1) {
      addressList.value = res.data || []
    }
  } catch (e) {}
}

const openAddressDialog = (addr) => {
  if (addr) {
    const areaCode = []
    if (addr.provinceCode) areaCode.push(addr.provinceCode)
    if (addr.cityCode) areaCode.push(addr.cityCode)
    if (addr.districtCode) areaCode.push(addr.districtCode)
    Object.assign(addressForm, {
      id: addr.id,
      consignee: addr.consignee || '',
      phone: addr.phone || '',
      provinceCode: addr.provinceCode || '',
      provinceName: addr.provinceName || '',
      cityCode: addr.cityCode || '',
      cityName: addr.cityName || '',
      districtCode: addr.districtCode || '',
      districtName: addr.districtName || '',
      areaCode: areaCode,
      detail: addr.detail || '',
      label: addr.label || '',
      isDefault: addr.isDefault || 0
    })
  } else {
    Object.assign(addressForm, {
      id: null,
      consignee: '',
      phone: '',
      provinceCode: '',
      provinceName: '',
      cityCode: '',
      cityName: '',
      districtCode: '',
      districtName: '',
      areaCode: [],
      detail: '',
      label: '',
      isDefault: 0
    })
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  if (!addressFormRef.value) return
  try {
    await addressFormRef.value.validate()
  } catch {
    return
  }
  addressSaving.value = true
  try {
    const form = { ...addressForm }
    delete form.areaCode
    let res
    if (addressForm.id) {
      res = await addressApi.update(form)
    } else {
      res = await addressApi.add(form)
    }
    if (res.code === 1) {
      ElMessage.success(addressForm.id ? '修改成功' : '添加成功')
      addressDialogVisible.value = false
      await loadAddressList()
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    addressSaving.value = false
  }
}

const setDefault = async (addr) => {
  try {
    const res = await addressApi.setDefault({ id: addr.id })
    if (res.code === 1) {
      ElMessage.success('已设为默认地址')
      await loadAddressList()
    }
  } catch (e) {}
}

const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该地址吗？', '提示', { type: 'warning' })
    const res = await addressApi.delete(id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      await loadAddressList()
    }
  } catch (e) {}
}

const loadComments = async () => {
  if (myComments.value.length) return
  try {
    const res = await userApi.listOwnComment()
    if (res.code === 1 && Array.isArray(res.data)) {
      myComments.value = res.data
    }
  } catch (e) {}
}

const toggleComments = () => {
  showComments.value = !showComments.value
  if (showComments.value) {
    loadComments()
  }
}

const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' })
    const res = await commentApi.delete(commentId)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      myComments.value = myComments.value.filter(c => c.id !== commentId)
    }
  } catch (e) {}
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleLogout = () => {
  userStore.logout()
}

const changePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (pwdForm.newPassword.length < 4) {
    ElMessage.warning('新密码至少4位')
    return
  }
  pwdSaving.value = true
  try {
    const res = await userApi.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    if (res.code === 1) {
      ElMessage.success('密码修改成功，请重新登录')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
      pwdDialogVisible.value = false
      userStore.logout()
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    pwdSaving.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  loadAddressList()
})
</script>

<style scoped>
.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.user-avatar-wrap {
  margin-bottom: 12px;
}

.user-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6B3A42, #8B4A50);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.user-avatar-img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
}

.info-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  vertical-align: middle;
}

.avatar-upload {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px dashed #D5C8C0;
  transition: border-color 0.3s;
}

.avatar-upload:hover {
  border-color: #8B4A50;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #9B8B85;
  background: #FBF9F7;
}

.avatar-file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #2D2320;
  margin: 0 0 4px;
}

.page-header p {
  font-size: 13px;
  color: #9B8B85;
  margin: 0;
}

.uc-content {
  max-width: 700px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.uc-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.uc-card h3 {
  font-size: 16px;
  color: #2D2320;
  margin: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.info-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #F5F0EC;
  font-size: 14px;
}

.info-row:last-child { border-bottom: none; }

.info-label {
  width: 80px;
  color: #9B8B85;
  flex-shrink: 0;
}

.edit-form {
  margin-top: 8px;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  padding: 14px;
  border: 1px solid #F0EBE7;
  border-radius: 10px;
  transition: all 0.3s;
}

.address-item.default {
  border-color: #8B4A50;
  background: #FDF8F8;
}

.address-item:hover {
  border-color: #C9A0A0;
}

.addr-main {
  margin-bottom: 8px;
}

.addr-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.addr-name {
  font-weight: 600;
  font-size: 14px;
  color: #2D2320;
}

.addr-phone {
  font-size: 13px;
  color: #7A6B65;
}

.addr-detail {
  font-size: 13px;
  color: #5D4A3A;
  line-height: 1.5;
}

.addr-actions {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #5D4A3A;
  font-size: 13px;
  padding: 12px 20px;
  border-radius: 10px;
  transition: all 0.3s;
}

.action-item:hover {
  background: #FBF9F7;
  color: #8B4A50;
}

.action-item.logout {
  color: #E8574A;
}

.action-item.logout:hover {
  background: #fef2f2;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #FBF9F7;
  border-radius: 12px;
}

.comment-flower-link {
  display: block;
  flex-shrink: 0;
  cursor: pointer;
}

.comment-flower-link:hover .comment-flower-img {
  opacity: 0.8;
}

.comment-flower-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  transition: opacity 0.2s;
}

.comment-flower-placeholder {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  background: #FBF9F7;
  border-radius: 8px;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-user-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-delete-btn {
  margin-left: auto;
}

.comment-user-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-user-name {
  font-size: 13px;
  color: #8B7B75;
}

.comment-flower {
  font-size: 15px;
  font-weight: 600;
  color: #8B4A50;
  margin: 0 0 6px;
}

.comment-rating {
  display: flex;
  gap: 2px;
  margin-bottom: 8px;
}

.star {
  font-size: 14px;
  color: #E0C8B0;
}

.star.filled {
  color: #D4A04A;
}

.comment-text {
  font-size: 14px;
  color: #3D2E28;
  margin: 0 0 6px;
}

.comment-time {
  font-size: 12px;
  color: #B5A39C;
}

.no-data {
  text-align: center;
  color: #B5A39C;
  padding: 20px 0;
}
</style>
