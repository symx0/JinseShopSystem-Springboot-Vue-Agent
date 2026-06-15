<template>
  <div class="ai-chat-wrapper" :class="{ expanded: isOpen }">
    <transition name="fade">
      <div v-if="isOpen" class="ai-chat-outer">
        <!-- 会话侧边栏 -->
        <div class="session-sidebar" :class="{ collapsed: sidebarCollapsed }">
          <div class="sidebar-header">
            <span class="sidebar-title">💬 会话</span>
            <el-button link size="small" type="primary" @click="createNewSession" :disabled="creatingSession">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
          <div class="session-list" v-loading="loadingSessions">
            <div
              v-for="sess in sessions"
              :key="sess.id"
              :class="['session-item', { active: activeId === String(sess.id) }]"
              @click="selectSession(sess)"
            >
              <div class="session-item-title">{{ sess.title }}</div>
              <div class="session-item-time">{{ formatTime(sess.updatedAt) }}</div>
              <div class="session-item-actions">
                <el-icon class="session-edit" :size="14" @click.stop="renameSession(sess)"><EditPen /></el-icon>
                <el-popconfirm
                  title="确定删除此会话？"
                  confirm-button-text="删除"
                  cancel-button-text="取消"
                  @confirm="deleteSession(sess.id)"
                  @click.stop
                >
                  <template #reference>
                    <el-icon class="session-delete" :size="14" @click.stop><Delete /></el-icon>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <el-empty v-if="!loadingSessions && sessions.length === 0" description="暂无会话" :image-size="40" />
          </div>
        </div>

        <!-- 侧边栏折叠按钮 -->
        <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon :size="12"><ArrowLeft v-if="!sidebarCollapsed" /><ArrowRight v-else /></el-icon>
        </div>

        <!-- 聊天面板 -->
        <div class="ai-chat-panel">
          <!-- 头部 -->
          <div class="chat-header">
            <div class="chat-header-left">
              <span class="chat-avatar">🤖</span>
              <span class="chat-title">AI 智能导购</span>
              <el-tag size="small" :type="agentOnline ? 'success' : 'warning'" effect="dark">
                {{ agentOnline ? '在线' : '离线' }}
              </el-tag>
            </div>
            <div class="chat-header-right">
              <el-icon class="chat-close" :size="18" @click="isOpen = false"><Close /></el-icon>
            </div>
          </div>

          <!-- 消息区域 -->
          <div class="chat-messages" ref="msgContainer">
            <!-- 欢迎语 -->
            <div class="chat-welcome" v-if="messages.length === 0">
              <div class="welcome-avatar">🌸</div>
              <p>您好！我是锦瑟花店的AI智能导购<strong>小锦</strong>~</p>
              <p class="welcome-desc">我可以根据您的需求推荐鲜花、搭配活动、构建订单</p>
              <div class="quick-questions">
                <span class="quick-tag" @click="sendQuick('我想买花送给女朋友，有什么推荐？')">🌹 送女朋友</span>
                <span class="quick-tag" @click="sendQuick('母亲节快到了，想给妈妈买花')">👩 送妈妈</span>
                <span class="quick-tag" @click="sendQuick('有没有促销活动？帮我搭配一个省钱方案')">💰 看活动</span>
                <span class="quick-tag" @click="sendQuick('我想装饰一下家里，有什么推荐？')">🏠 家居装饰</span>
              </div>
            </div>

            <!-- 消息列表 -->
            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              :class="['chat-msg', msg.role === 'user' ? 'msg-user' : 'msg-bot']"
            >
              <!-- 思考链路 -->
              <div v-if="msg.thinkingChain && msg.thinkingChain.length" class="thinking-chain">
                <div
                  v-for="(step, si) in msg.thinkingChain"
                  :key="si"
                  class="thinking-step"
                >
                  {{ step }}
                </div>
              </div>

              <div class="msg-bubble" v-html="renderMarkdown(msg.content)"></div>

              <!-- 订单卡片 -->
              <div v-if="msg.order && msg.order.items && msg.order.items.length" class="order-card-inline">
                <div class="order-card-header">
                  <span class="order-card-title">📋 {{ msg.order.scenario || '推荐方案' }}</span>
                  <el-tag size="small" type="danger">总价 ¥{{ msg.order.total_price }}</el-tag>
                </div>
                <div class="order-card-items">
                  <div v-for="item in msg.order.items" :key="item.id" class="order-card-item" :class="{ promo: item.is_promo, unavailable: item.unavailable }">
                    <el-image v-if="item.image" :src="item.image" fit="cover" class="order-item-img" />
                    <div class="order-item-info">
                      <div class="order-item-name">
                        <span class="name-text">{{ item.name }}</span>
                        <el-tag v-if="item.is_promo" size="small" type="danger" effect="dark">🔥促销</el-tag>
                        <el-tag v-if="item.unavailable" size="small" type="info" effect="dark">{{ item.unavailable_reason }}</el-tag>
                      </div>
                      <div class="order-item-price">
                        <span class="unit-price">¥{{ item.unit_price }}</span>
                        <span v-if="item.original_price" class="original-price">¥{{ item.original_price }}</span>
                        <span class="item-qty">x{{ item.quantity }}</span>
                        <span class="item-subtotal">= ¥{{ item.subtotal }}</span>
                      </div>
                      <div class="order-item-reason" v-if="item.reason">💬 {{ item.reason }}</div>
                    </div>
                  </div>
                </div>
                <div class="order-card-tips" v-if="msg.order.tips">💡 {{ msg.order.tips }}</div>
              </div>

              <!-- 操作按钮 -->
              <div v-if="msg.buttons && msg.buttons.length" class="action-buttons">
                <el-button
                  v-for="btn in msg.buttons"
                  :key="btn.action"
                  :type="btn.type || 'default'"
                  size="small"
                  @click="handleAction(btn.action, msg)"
                >
                  {{ btn.text }}
                </el-button>
              </div>
            </div>

            <!-- 打字指示器 -->
            <div v-if="typing" class="chat-msg msg-bot">
              <div class="msg-bubble typing-bubble">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="chat-input-area">
            <el-input
              v-model="inputText"
              placeholder="描述您的需求，如：想买花送给女朋友，预算200左右..."
              @keyup.enter="sendMessage"
              :disabled="typing || !sessionId"
            >
              <template #append>
                <el-button @click="sendMessage" :disabled="typing || !inputText.trim() || !sessionId" type="primary">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </transition>

    <!-- 浮动气泡 -->
    <div v-if="!isOpen" class="ai-chat-bubble" @click="openChat">
      <span class="bubble-icon">🤖</span>
      <span class="bubble-text">智能导购</span>
    </div>

    <!-- 订单预览弹窗 -->
    <el-dialog
      title="订单预览"
      v-model="orderDialogVisible"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="order-preview" v-if="previewOrder && previewOrder.items">
        <div class="preview-header">
          <span>{{ previewOrder.scenario || '推荐方案' }}</span>
          <el-tag type="danger" size="large">¥{{ previewOrder.total_price }}</el-tag>
        </div>
        <el-table :data="previewOrder.items" border size="small" style="margin-top: 12px">
          <el-table-column label="图片" width="70">
            <template #default="{ row }">
              <el-image v-if="row.image" :src="row.image" style="width:44px;height:44px;border-radius:6px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品">
            <template #default="{ row }">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.is_promo" size="small" type="danger" effect="dark" style="margin-left:4px">促销</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="90">
            <template #default="{ row }">
              <span :class="{ 'line-through text-gray': row.original_price }">¥{{ row.unit_price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="140">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="99"
                size="small"
                controls-position="right"
                @change="onQtyChange(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="90">
            <template #default="{ row }">
              <span style="font-weight:600">¥{{ (row.unit_price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="preview-total">
          总计: <span class="total-price">¥{{ computedTotal }}</span>
        </div>
      </div>

      <!-- 收货信息 -->
      <el-divider>收货信息</el-divider>
      <el-form :model="confirmForm" label-width="80px" size="small">
        <el-form-item label="收货地址">
          <el-select v-model="confirmForm.addressBookId" placeholder="请选择收货地址" style="width:100%">
            <el-option
              v-for="addr in addressList"
              :key="addr.id"
              :label="`${addr.consignee} ${addr.phone} ${addr.detail}`"
              :value="addr.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="confirmForm.payMethod">
            <el-radio :label="2">支付宝</el-radio>
            <el-radio :label="1">微信支付</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="confirmForm.remark" placeholder="如有特殊要求请备注" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="orderDialogVisible = false">继续修改</el-button>
        <el-button @click="submitOrder(true)" :loading="submitting">⏰ 稍后付款</el-button>
        <el-button type="primary" @click="submitOrder(false)" :loading="submitting">
          ✅ 确认下单 (¥{{ computedTotal }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close, Promotion, Plus, Delete, ArrowLeft, ArrowRight, EditPen } from '@element-plus/icons-vue'
import { agentApi, agentSessionApi, cartApi, orderApi, addressApi, flowerApi } from '@/api'
import { useUserStore } from '@/store'

// ═════════════ 状态 ═════════════

const isOpen = ref(false)
const inputText = ref('')
const typing = ref(false)
const messages = ref([])
const msgContainer = ref(null)
const sessionId = ref('')
const agentOnline = ref(false)
const userId = ref(null)

// 会话侧边栏
const sidebarCollapsed = ref(false)
const sessions = ref([])
const loadingSessions = ref(false)
const creatingSession = ref(false)
const currentSessionId = ref(null)

// 当前选中的会话ID（字符串形式，用于比较）
const activeId = computed(() => currentSessionId.value != null ? String(currentSessionId.value) : '')

// 订单预览弹窗
const orderDialogVisible = ref(false)
const previewOrder = ref(null)
const submitting = ref(false)
const addressList = ref([])
const confirmForm = ref({
  addressBookId: null,
  payMethod: 2,
  remark: ''
})

// ═════════════ 计算属性 ═════════════

const computedTotal = computed(() => {
  if (!previewOrder.value?.items) return 0
  return previewOrder.value.items.reduce((sum, item) => sum + item.unit_price * item.quantity, 0).toFixed(2)
})

// ═════════════ Agent Order 数据兼容 ═════════════
// Agent返回 { items: [{flower_id, quantity}], scenario, summary, tips }
// 前端需要 { items: [{id, name, image, unit_price, original_price, is_promo, quantity, subtotal}], total_price, scenario, tips }
const enrichOrder = async (order) => {
  if (!order?.items?.length) return order

  const firstItem = order.items[0]
  if (firstItem.name && firstItem.unit_price !== undefined) return order

  const detailMap = {}
  await Promise.all(order.items.map(async (item) => {
    const fid = item.flower_id || item.id
    if (!fid || detailMap[fid]) return
    try {
      const res = await flowerApi.getById(fid)
      if (res.code === 1 && res.data) {
        detailMap[fid] = res.data
      }
    } catch (e) {
      console.warn(`查询商品#${fid}详情失败:`, e)
    }
  }))

  let totalPrice = 0
  const enrichedItems = order.items.map(item => {
    const fid = item.flower_id || item.id
    const detail = detailMap[fid] || {}
    const unitPrice = detail.discountPrice ? parseFloat(detail.discountPrice) : (detail.price ? parseFloat(detail.price) : 0)
    const originalPrice = detail.originalPrice ? parseFloat(detail.originalPrice) : null
    const isPromo = detail.promo === true || detail.discountPrice != null
    const subtotal = unitPrice * item.quantity
    const stock = detail.stock != null ? detail.stock : null
    const limitPer = detail.limitPer != null ? detail.limitPer : null
    const purchasedCount = detail.purchasedCount || 0
    const soldOut = stock !== null && stock <= 0
    const limitReached = isPromo && limitPer !== null && purchasedCount >= limitPer
    const unavailable = soldOut || limitReached
    if (!unavailable) totalPrice += subtotal
    return {
      id: fid,
      flower_id: fid,
      name: detail.name || `商品#${fid}`,
      image: detail.image || '',
      unit_price: unitPrice,
      original_price: originalPrice,
      is_promo: isPromo,
      quantity: item.quantity,
      subtotal: subtotal.toFixed(2),
      stock: stock,
      limit_per: limitPer,
      purchased_count: purchasedCount,
      unavailable: unavailable,
      unavailable_reason: soldOut ? '已售罄' : (limitReached ? '限购已满' : ''),
      activityContent: detail.activityContent || ''
    }
  })

  return {
    ...order,
    items: enrichedItems,
    total_price: totalPrice.toFixed(2)
  }
}

// ═════════════ 会话管理 ═════════════

// 打开订单预览弹窗前，过滤掉售罄商品
const openOrderPreview = (order) => {
  const filtered = { ...order, items: order.items.filter(i => !(i.stock !== null && i.stock <= 0)) }
  const total = filtered.items.filter(i => !i.unavailable).reduce((sum, i) => sum + i.unit_price * i.quantity, 0)
  filtered.total_price = total.toFixed(2)
  previewOrder.value = filtered
}

const loadSessions = async () => {
  loadingSessions.value = true
  try {
    const res = await agentSessionApi.list()
    console.log('loadSessions response:', JSON.stringify(res?.data?.slice?.(0, 2)))
    if (res.code === 1 && Array.isArray(res.data)) {
      sessions.value = res.data
      if (sessions.value.length > 0) {
        // 有会话：选中最新的
        selectSession(sessions.value[0])
      } else {
        // 无会话：自动创建
        await autoCreateSession()
      }
    } else {
      // 接口异常，也尝试自动创建
      await autoCreateSession()
    }
  } catch (e) {
    console.error('加载会话列表失败:', e)
    await autoCreateSession()
  } finally {
    loadingSessions.value = false
  }
}

const autoCreateSession = async () => {
  creatingSession.value = true
  try {
    const res = await agentSessionApi.create('新会话')
    console.log('autoCreateSession response:', JSON.stringify(res?.data))
    if (res.code === 1 && res.data) {
      sessions.value.unshift(res.data)
      selectSession(res.data)
    }
  } catch (e) {
    console.error('自动创建会话失败:', e)
  } finally {
    creatingSession.value = false
  }
}

const createNewSession = async () => {
  creatingSession.value = true
  try {
    const res = await agentSessionApi.create('新会话')
    if (res.code === 1 && res.data) {
      sessions.value.unshift(res.data)
      selectSession(res.data)
      ElMessage.success('已创建新会话')
    }
  } catch (e) {
    ElMessage.error('创建会话失败')
  } finally {
    creatingSession.value = false
  }
}

const selectSession = async (sess) => {
  if (!sess || sess.id == null) {
    console.error('selectSession: 无效的会话对象', sess)
    return
  }
  const sid = String(sess.id)
  console.log('selectSession:', sess.id, '→ sid:', sid)
  currentSessionId.value = sess.id
  sessionId.value = sid
  messages.value = []
  messagesLoaded.value = false
  // 异步加载历史，不阻塞
  loadHistory().catch(e => console.warn('加载历史消息失败:', e))
}

const deleteSession = async (sid) => {
  if (sid == null || String(sid) === 'undefined') {
    console.error('deleteSession: 无效的会话ID', sid)
    return
  }
  const sidStr = String(sid)
  // 并行清理：数据库 + Agent会话
  const tasks = [agentSessionApi.delete(sid)]
  tasks.push(agentApi.resetSession(sidStr).catch(() => {}))
  await Promise.allSettled(tasks)

  // 从列表中移除（用字符串比较避免 Number vs String 不一致）
  sessions.value = sessions.value.filter(s => String(s.id) !== sidStr)
  if (activeId.value === sidStr) {
    currentSessionId.value = null
    sessionId.value = ''
    messages.value = []
    messagesLoaded.value = false
    if (sessions.value.length > 0) {
      selectSession(sessions.value[0])
    } else {
      await autoCreateSession()
    }
  }
  ElMessage.success('会话已删除')
}

const renameSession = async (sess) => {
  try {
    const newTitle = await ElMessageBox.prompt('请输入新标题', '重命名会话', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: sess.title
    })
    if (newTitle && newTitle.value && newTitle.value.trim()) {
      await agentSessionApi.rename(sess.id, newTitle.value.trim())
      sess.title = newTitle.value.trim()
      ElMessage.success('重命名成功')
    }
  } catch {
    // 用户取消
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// ═════════════ 初始化 ═════════════

const checkAgentHealth = async () => {
  try {
    const res = await agentApi.health()
    agentOnline.value = res.data?.status === 'ok'
  } catch {
    agentOnline.value = false
  }
}

const messagesLoaded = ref(false)

const loadHistory = async () => {
  if (!sessionId.value || messagesLoaded.value) return
  try {
    const res = await agentApi.getHistory(sessionId.value, userId.value)
    const data = res.data
    if (data.has_history && data.messages && data.messages.length > 0) {
      messages.value = data.messages.map(msg => ({
        role: msg.role,
        content: msg.content,
        order: null,
        buttons: []
      }))
      if (data.order && data.order.items && data.order.items.length) {
        const enrichedOrder = await enrichOrder(data.order)
        const reversed = [...messages.value].reverse()
        const lastBotIdx = reversed.findIndex(m => m.role === 'bot')
        if (lastBotIdx >= 0) {
          messages.value[messages.value.length - 1 - lastBotIdx].order = enrichedOrder
        }
      }
      await scrollToBottom()
    }
    messagesLoaded.value = true
  } catch {
    messagesLoaded.value = true  // 即使失败也标记已加载，避免重复请求
  }
}

onMounted(() => {
  const userStore = useUserStore()
  userId.value = userStore.userId ? String(userStore.userId) : null
  checkAgentHealth()
})

// 打开聊天 → 加载用户会话
const openChat = async () => {
  isOpen.value = true
  if (!agentOnline.value) checkAgentHealth()
  const userStore = useUserStore()
  userId.value = userStore.userId ? String(userStore.userId) : null
  // 每次打开都重新加载会话列表
  currentSessionId.value = null
  sessionId.value = ''
  messages.value = []
  messagesLoaded.value = false
  await loadSessions()
}

// ═════════════ 核心：发送消息 ═════════════

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || typing.value) return
  if (!sessionId.value) {
    ElMessage.warning('请先创建一个会话')
    return
  }

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  await scrollToBottom()

  typing.value = true
  try {
    const res = await agentApi.chat(sessionId.value, text, userId.value)
    const data = res.data

    messages.value.push({
      role: 'bot',
      content: data.reply,
      order: await enrichOrder(data.order),
      buttons: data.action_buttons || [],
      currentStep: data.current_step,
      thinkingChain: data.thinking_chain || []
    })

    // 首次发送消息时，将标题从"新会话"更新为用户的第一句话
    if (messages.value.filter(m => m.role === 'user').length === 1 &&
        currentSessionId.value != null) {
      const sess = sessions.value.find(s => String(s.id) === activeId.value)
      if (sess && sess.title === '新会话') {
        const shortTitle = text.slice(0, 20) + (text.length > 20 ? '...' : '')
        try {
          await agentSessionApi.rename(sess.id, shortTitle)
          sess.title = shortTitle
        } catch (e) {
          console.warn('更新会话标题失败:', e)
        }
      }
    }

    // 更新会话活跃时间并重新排序
    try { await agentSessionApi.touch(currentSessionId.value) } catch {}
    const touchedSess = sessions.value.find(s => String(s.id) === activeId.value)
    if (touchedSess) {
      touchedSess.updatedAt = new Date().toISOString()
      sessions.value.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
    }

    // 如果Agent要求确认订单，自动弹出订单预览
    if (data.current_step === 'confirm' && data.order) {
      openOrderPreview(await enrichOrder(JSON.parse(JSON.stringify(data.order))))
      await loadAddresses()
      setTimeout(() => { orderDialogVisible.value = true }, 500)
    }
  } catch (e) {
    console.error('Agent请求失败:', e)
    agentOnline.value = false
    messages.value.push({
      role: 'bot',
      content: '抱歉，AI导购服务暂时不可用 😅\n\n请稍后再试，或拨打客服热线 400-888-JINSE',
      order: null,
      buttons: []
    })
  } finally {
    typing.value = false
    await scrollToBottom()
  }
}

const sendQuick = (text) => {
  inputText.value = text
  sendMessage()
}

// ═════════════ 按钮操作 ═════════════

const handleAction = async (action, msg) => {
  switch (action) {
    case 'view_order':
    case 'modify_order':
    case 'confirm_order':
      if (msg.order) {
        openOrderPreview(await enrichOrder(JSON.parse(JSON.stringify(msg.order))))
        await loadAddresses()
        orderDialogVisible.value = true
      }
      break

    case 'clear_order':
      try {
        await ElMessageBox.confirm('确定要清空当前订单吗？', '确认', { type: 'warning' })
        await agentApi.updateOrder(sessionId.value, { session_id: sessionId.value, action: 'clear' })
        messages.value.push({ role: 'bot', content: '已清空订单。请告诉我您新的需求~ 🌸', order: null, buttons: [] })
        await scrollToBottom()
      } catch {}
      break

    default:
      break
  }
}

// ═════════════ 订单预览弹窗操作 ═════════════

const loadAddresses = async () => {
  try {
    const res = await addressApi.list()
    if (res.code === 1 && Array.isArray(res.data)) {
      addressList.value = res.data
      const defaultAddr = res.data.find(a => a.isDefault === 1)
      if (defaultAddr) confirmForm.value.addressBookId = defaultAddr.id
      else if (res.data.length > 0) confirmForm.value.addressBookId = res.data[0].id
    }
  } catch {}
}

const onQtyChange = async (item) => {
  if (item.quantity <= 0) {
    await nextTick()
    const idx = previewOrder.value.items.indexOf(item)
    if (idx >= 0) {
      previewOrder.value.items.splice(idx, 1)
      previewOrder.value.total_price = computedTotal.value
      if (!previewOrder.value.items.length) {
        orderDialogVisible.value = false
        messages.value.push({ role: 'bot', content: '订单已清空，需要我重新为您推荐吗？ 🌸', order: null, buttons: [] })
      }
    }
    return
  }
  item.subtotal = (item.unit_price * item.quantity).toFixed(2)
  previewOrder.value.total_price = computedTotal.value
}

const removeOrderItem = async (index) => {
  const item = previewOrder.value.items[index]
  try {
    await ElMessageBox.confirm(`确定要删除「${item.name}」吗？`, '确认删除', { type: 'warning' })
    previewOrder.value.items.splice(index, 1)
    previewOrder.value.total_price = computedTotal.value

    await agentApi.updateOrder(sessionId.value, {
      session_id: sessionId.value,
      action: 'remove',
      product_id: item.id
    })

    if (previewOrder.value.items.length === 0) {
      orderDialogVisible.value = false
      messages.value.push({ role: 'bot', content: '订单已清空，需要我重新为您推荐吗？ 🌸', order: null, buttons: [] })
    }
  } catch {}
}

// ═════════════ 提交订单 ═════════════

const submitOrder = async (payLater = false) => {
  if (!confirmForm.value.addressBookId) {
    ElMessage.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    // 1. 清空购物车
    await cartApi.clean()

    // 2. 逐一添加商品到购物车
    for (const item of previewOrder.value.items) {
      await cartApi.add({
        flowerId: item.id,
        number: item.quantity
      })
    }

    // 3. 提交订单
    const submitRes = await orderApi.submit({
      addressBookId: confirmForm.value.addressBookId,
      payMethod: confirmForm.value.payMethod,
      remark: confirmForm.value.remark || (payLater ? 'AI导购推荐（稍后付款）' : 'AI导购推荐方案'),
      deliveryStatus: 1,
      amount: parseFloat(computedTotal.value)
    })

    if (submitRes.code === 1) {
      orderDialogVisible.value = false
      const orderId = submitRes.data?.id || submitRes.data?.orderId || '-'

      if (payLater) {
        ElMessage.success('订单已保存，请稍后完成付款！')
        messages.value.push({
          role: 'bot',
          content: `⏰ 订单已保存！\n\n订单ID：${orderId}\n金额：¥${computedTotal.value}\n\n您可以在「我的订单」页面查看并完成付款。`,
          order: previewOrder.value,
          buttons: []
        })
      } else {
        ElMessage.success('订单提交成功！')
        messages.value.push({
          role: 'bot',
          content: `✅ 订单已成功提交！\n\n订单ID：${orderId}\n金额：¥${computedTotal.value}\n\n您可以在「我的订单」页面查看订单详情和进行支付。`,
          order: previewOrder.value,
          buttons: []
        })
      }

      // 重置Agent会话
      try { await agentApi.resetSession(sessionId.value) } catch {}
    } else {
      ElMessage.error(submitRes.msg || '订单提交失败')
    }
  } catch (e) {
    console.error('提交订单失败:', e)
    ElMessage.error(e?.response?.data?.msg || e?.message || '订单提交失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// ═════════════ 辅助 ═════════════

const renderMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n\n/g, '<br/><br/>')
    .replace(/\n/g, '<br/>')
    .replace(/• /g, '•&nbsp;')
}

const scrollToBottom = async () => {
  await nextTick()
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
}

// 弹窗关闭时清理
watch(orderDialogVisible, (val) => {
  if (!val && previewOrder.value) {
    previewOrder.value.total_price = computedTotal.value
  }
})
</script>

<style scoped>
.ai-chat-wrapper { position: fixed; right: 20px; bottom: 20px; z-index: 999; }

/* 外层容器：侧边栏 + 聊天面板 */
.ai-chat-outer {
  display: flex; width: 660px; height: 620px; background: #fff;
  border-radius: 16px; box-shadow: 0 8px 40px rgba(0,0,0,0.15), 0 0 0 1px rgba(232,185,107,0.2);
  overflow: hidden;
}

/* 会话侧边栏 */
.session-sidebar {
  width: 180px; min-width: 180px; background: #FBF9F7;
  border-right: 1px solid #EDE4DD; display: flex; flex-direction: column;
  transition: width 0.3s, min-width 0.3s;
}
.session-sidebar.collapsed { width: 0; min-width: 0; overflow: hidden; border-right: none; }

.sidebar-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px; border-bottom: 1px solid #EDE4DD;
}
.sidebar-title { font-size: 13px; font-weight: 600; color: #5D4A3A; }

.sidebar-toggle {
  width: 18px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; background: #FBF9F7; border-left: 1px solid #EDE4DD;
  color: #9B8B85; transition: background 0.2s;
}
.sidebar-toggle:hover { background: #F5EDE8; color: #8B4A50; }

.session-list { flex: 1; overflow-y: auto; padding: 6px; }

.session-item {
  padding: 10px 12px; border-radius: 8px; cursor: pointer; position: relative;
  margin-bottom: 4px; transition: all 0.2s;
}
.session-item:hover { background: #F0EBE7; }
.session-item.active { background: #F2E8E5; border: 1px solid #D4A99A; }

.session-item-title { font-size: 13px; font-weight: 500; color: #2D2320; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 110px; }
.session-item-time { font-size: 11px; color: #9B8B85; margin-top: 2px; }
.session-item-actions { position: absolute; right: 8px; top: 50%; transform: translateY(-50%); display: flex; gap: 6px; align-items: center; opacity: 0; transition: opacity 0.2s; }
.session-item:hover .session-item-actions { opacity: 1; }
.session-edit { color: #A89B90; cursor: pointer; }
.session-edit:hover { color: #7C6B5E; }
.session-delete { color: #C8BAB0; }
.session-delete:hover { color: #F56C6C; }

/* 聊天面板 */
.ai-chat-panel {
  flex: 1; display: flex; flex-direction: column; overflow: hidden;
}

.chat-header {
  background: linear-gradient(135deg, #6B3A42, #8B4A50);
  padding: 10px 16px; display: flex; align-items: center; justify-content: space-between;
}
.chat-header-left { display: flex; align-items: center; gap: 8px; }
.chat-header-right { display: flex; align-items: center; gap: 8px; }
.chat-avatar { font-size: 22px; }
.chat-title { color: #fff; font-size: 14px; font-weight: 600; }
.chat-close { color: rgba(255,255,255,0.7); cursor: pointer; }

.chat-messages { flex: 1; overflow-y: auto; padding: 14px; background: #FBF9F7; }

.chat-welcome { text-align: center; padding: 10px 0 14px; }
.welcome-avatar { font-size: 40px; margin-bottom: 6px; }
.chat-welcome p { color: #5D4A3A; font-size: 13px; margin: 3px 0; }
.welcome-desc { color: #9B8B85 !important; font-size: 12px !important; }
.quick-questions { display: flex; flex-wrap: wrap; gap: 6px; justify-content: center; margin-top: 10px; }
.quick-tag {
  padding: 5px 12px; background: #F7F3F0; border: 1px solid #EDE4DD;
  border-radius: 16px; font-size: 11px; color: #8B4A50; cursor: pointer; transition: all 0.3s;
}
.quick-tag:hover { background: #8B4A50; color: #fff; border-color: #8B4A50; }

.chat-msg { margin-bottom: 12px; display: flex; flex-direction: column; }
.msg-user { align-items: flex-end; }
.msg-bot { align-items: flex-start; }
.msg-user .msg-bubble {
  background: linear-gradient(135deg, #8B4A50, #A06068); color: #fff;
  border-radius: 14px 4px 14px 14px; max-width: 85%;
}
.msg-bot .msg-bubble {
  background: #fff; border-radius: 4px 14px 14px 14px; border: 1px solid #EDE4DD;
  max-width: 95%;
}
.msg-bubble { padding: 9px 12px; font-size: 13px; line-height: 1.7; word-break: break-word; }

.typing-bubble { display: flex; align-items: center; gap: 4px; padding: 12px 16px; }
.typing-dot { width: 6px; height: 6px; border-radius: 50%; background: #C8BAB0; animation: typingBounce 1.4s ease-in-out infinite; }
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typingBounce { 0%,60%,100%{transform:translateY(0)} 30%{transform:translateY(-7px)} }

/* 订单卡片 */
.order-card-inline {
  margin-top: 8px; background: #FFFAF5; border: 1px solid #F5E6D3;
  border-radius: 10px; padding: 10px; width: 90%;
}
.order-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.order-card-title { font-weight: 600; color: #5D4A3A; font-size: 13px; }
.order-card-items { display: flex; flex-direction: column; gap: 6px; }
.order-card-item { display: flex; gap: 8px; padding: 6px; background: #fff; border-radius: 8px; }
.order-card-item.promo { border: 1px dashed #F56C6C; }
.order-card-item.unavailable { opacity: 0.5; background: #F5F5F5; }
.order-card-item.unavailable .order-item-name { color: #999; }
.order-card-item.unavailable .order-item-name .name-text { text-decoration: line-through; }
.order-card-item.unavailable .order-item-price { color: #BBB; }
.order-card-item.unavailable .order-item-img { filter: grayscale(1); }
.order-item-img { width: 44px; height: 44px; border-radius: 6px; flex-shrink: 0; }
.order-item-info { flex: 1; min-width: 0; }
.order-item-name { font-size: 12px; font-weight: 600; color: #2D2320; display: flex; align-items: center; gap: 4px; }
.order-item-price { font-size: 11px; color: #8B4A50; margin-top: 2px; }
.unit-price { font-weight: 600; }
.original-price { text-decoration: line-through; color: #B5A39C; margin-left: 4px; font-size: 10px; }
.item-qty { margin-left: 6px; color: #5D4A3A; }
.item-subtotal { font-weight: 600; color: #F56C6C; margin-left: 3px; }
.order-item-reason { font-size: 10px; color: #9B8B85; margin-top: 2px; }
.order-card-tips { font-size: 11px; color: #8B4A50; margin-top: 6px; padding: 5px 6px; background: #FFF8F0; border-radius: 6px; }

/* 操作按钮 */
.action-buttons { display: flex; flex-wrap: wrap; gap: 5px; margin-top: 6px; margin-left: 3px; }

/* 思考链路 */
.thinking-chain {
  margin-bottom: 6px;
  padding: 6px 10px;
  background: rgba(107, 58, 66, 0.06);
  border-radius: 8px;
  border-left: 3px solid #C9A87C;
  max-width: 85%;
}
.thinking-step {
  font-size: 12px;
  color: #9B8B85;
  line-height: 1.6;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.thinking-step + .thinking-step {
  margin-top: 2px;
}

.chat-input-area { padding: 8px 12px; border-top: 1px solid #EDE4DD; background: #fff; }

/* 浮动气泡 */
.ai-chat-bubble {
  width: 60px; height: 60px; border-radius: 50%;
  background: linear-gradient(135deg, #6B3A42, #8B4A50);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; box-shadow: 0 4px 16px rgba(139,74,80,0.35);
  transition: transform 0.3s; color: #fff; gap: 0;
}
.ai-chat-bubble:hover { transform: scale(1.08); }
.bubble-icon { font-size: 24px; line-height: 1; }
.bubble-text { font-size: 8px; color: #E8B96B; }

/* 弹窗 */
.order-preview {}
.preview-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; }
.preview-total { text-align: right; margin-top: 12px; font-size: 16px; }
.total-price { font-size: 22px; font-weight: 700; color: #F56C6C; }
.line-through { text-decoration: line-through; }
.text-gray { color: #B5A39C; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
