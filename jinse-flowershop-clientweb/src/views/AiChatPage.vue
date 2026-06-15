<template>
  <div class="ai-page">
    <!-- 左侧会话列表 -->
    <div class="session-panel">
      <div class="session-header">
        <h3>💬 AI 智能导购</h3>
        <el-button type="primary" size="small" @click="createNewSession" :loading="creatingSession">
          <el-icon><Plus /></el-icon> 新对话
        </el-button>
      </div>
      <div class="session-list" v-loading="loadingSessions">
        <div
          v-for="sess in sessions"
          :key="sess.id"
          :class="['session-item', { active: activeId === String(sess.id) }]"
          @click="selectSession(sess)"
        >
          <div class="session-title">{{ sess.title }}</div>
          <div class="session-time">{{ formatTime(sess.updatedAt) }}</div>
          <div class="session-actions">
            <el-icon class="session-edit" :size="14" @click.stop="renameSession(sess)"><EditPen /></el-icon>
            <el-popconfirm
              title="确定删除此会话？"
              @confirm="deleteSession(sess.id)"
              @click.stop
            >
              <template #reference>
                <el-icon class="session-delete" :size="14" @click.stop><Delete /></el-icon>
              </template>
            </el-popconfirm>
          </div>
        </div>
        <el-empty v-if="!loadingSessions && sessions.length === 0" description="点击上方按钮开始对话" :image-size="48" />
      </div>
      <div class="session-footer">
        <el-tag size="small" :type="agentOnline ? 'success' : 'danger'" effect="dark">
          {{ agentOnline ? '🟢 Agent 在线' : '🔴 Agent 离线' }}
        </el-tag>
      </div>
    </div>

    <!-- 右侧聊天区 -->
    <div class="chat-panel">
      <!-- 顶部信息 -->
      <div class="chat-topbar" v-if="currentSessionId">
        <span class="current-session-title">{{ currentSessionTitle }}</span>
      </div>

      <!-- 消息区 -->
      <div class="chat-messages" ref="msgContainer">
        <!-- 欢迎页 -->
        <div class="chat-welcome" v-if="messages.length === 0">
          <div class="welcome-icon">🌸</div>
          <h2>您好！我是锦色花店的 AI 智能导购<strong>小锦</strong></h2>
          <p class="welcome-desc">
            我可以帮您根据需求推荐鲜花、搭配活动、构建订单。<br/>
            在左侧创建或选择一个会话开始对话吧~
          </p>
          <div class="quick-questions" v-if="currentSessionId">
            <span class="quick-tag" @click="sendQuick('我想买花送给女朋友，有什么推荐？')">🌹 送女朋友</span>
            <span class="quick-tag" @click="sendQuick('母亲节快到了，想给妈妈买花')">👩 送妈妈</span>
            <span class="quick-tag" @click="sendQuick('有没有促销活动？帮我搭配一个省钱方案')">💰 看活动</span>
            <span class="quick-tag" @click="sendQuick('玫瑰花花语和养护知识')">📖 花语养护</span>
            <span class="quick-tag" @click="sendQuick('我想装饰一下家里，有什么推荐？')">🏠 家居装饰</span>
            <span class="quick-tag" @click="sendQuick('新居乔迁，有什么合适的盆栽推荐？')">🏡 乔迁盆栽</span>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(msg, idx) in messages" :key="idx"
             :class="['chat-msg', msg.role === 'user' ? 'msg-user' : 'msg-bot']">
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
                    x{{ item.quantity }} = <span class="item-subtotal">¥{{ item.subtotal }}</span>
                  </div>
                  <div class="order-item-reason" v-if="item.reason">💬 {{ item.reason }}</div>
                </div>
              </div>
            </div>
            <div class="order-card-tips" v-if="msg.order.tips">💡 {{ msg.order.tips }}</div>
          </div>

          <!-- 操作按钮 -->
          <div v-if="msg.buttons && msg.buttons.length" class="action-buttons">
            <el-button v-for="btn in msg.buttons" :key="btn.action"
              :type="btn.type || 'default'" size="small" @click="handleAction(btn.action, msg)">
              {{ btn.text }}
            </el-button>
          </div>
        </div>

        <!-- 打字指示 -->
        <div v-if="typing" class="chat-msg msg-bot">
          <div class="msg-bubble typing-bubble">
            <span class="typing-dot"></span><span class="typing-dot"></span><span class="typing-dot"></span>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <el-input
          v-model="inputText"
          :placeholder="currentSessionId ? '描述您的需求...' : '请先在左侧创建或选择会话'"
          @keyup.enter="sendMessage"
          :disabled="typing || !currentSessionId"
          size="large"
        >
          <template #append>
            <el-button @click="sendMessage" :disabled="typing || !inputText.trim() || !currentSessionId"
              type="primary" :icon="Promotion" style="width:60px" />
          </template>
        </el-input>
      </div>
    </div>

    <!-- 订单预览弹窗 -->
    <el-dialog title="订单预览" v-model="orderDialogVisible" width="650px" :close-on-click-modal="false" destroy-on-close>
      <div class="order-preview" v-if="previewOrder && previewOrder.items">
        <div class="preview-header">
          <span>{{ previewOrder.scenario || '推荐方案' }}</span>
          <el-tag type="danger" size="large">¥{{ previewOrder.total_price }}</el-tag>
        </div>
        <el-table :data="previewOrder.items" border size="small" style="margin-top:12px">
          <el-table-column label="图片" width="70">
            <template #default="{ row }">
              <el-image v-if="row.image" :src="row.image" style="width:44px;height:44px;border-radius:6px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品">
            <template #default="{ row }">
              <span :class="{ 'unavailable-text': row.unavailable }">{{ row.name }}</span>
              <el-tag v-if="row.is_promo" size="small" type="danger" effect="dark" style="margin-left:4px">促销</el-tag>
              <el-tag v-if="row.unavailable" size="small" type="info" effect="dark" style="margin-left:4px">{{ row.unavailable_reason }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="90">
            <template #default="{ row }">¥{{ row.unit_price }}</template>
          </el-table-column>
          <el-table-column label="数量" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="0" :max="99" size="small" controls-position="right" @change="onQtyChange(row)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="90">
            <template #default="{ row }">¥{{ (row.unit_price * row.quantity).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <!-- 费用明细 -->
        <div class="fee-summary">
          <div class="fee-line"><span>商品合计</span><span>¥{{ itemsTotal }}</span></div>
          <div class="fee-line"><span>打包费</span><span>{{ packFee === 0 ? '免打包费' : '¥' + packFee.toFixed(2) }}</span></div>
          <div class="fee-line"><span>配送费</span><span>{{ deliveryFee === 0 ? '免配送费' : '¥' + deliveryFee.toFixed(2) }}</span></div>
          <div class="fee-line fee-total"><span>总计</span><span class="total-price">¥{{ computedTotal }}</span></div>
        </div>
      </div>
      <el-divider>收货信息</el-divider>
      <el-form :model="confirmForm" label-width="80px" size="small">
        <el-form-item label="收货地址">
          <el-select v-model="confirmForm.addressBookId" placeholder="请选择收货地址" style="width:100%">
            <el-option v-for="addr in addressList" :key="addr.id"
              :label="`${addr.consignee} ${addr.phone} ${addr.detail}`" :value="addr.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="confirmForm.remark" placeholder="如有特殊要求请备注" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">继续修改</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">
          ✅ 提交订单 (¥{{ computedTotal }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 支付弹窗（提交订单后弹出） -->
    <el-dialog title="订单已提交" v-model="payDialogVisible" width="420px" :close-on-click-modal="false">
      <div class="pay-dialog-body">
        <div class="pay-success-icon">✅</div>
        <p class="pay-order-info">订单编号：<strong>{{ submittedOrderId }}</strong></p>
        <p class="pay-order-amount">金额：<strong>¥{{ submittedOrderAmount }}</strong></p>
        <p class="pay-order-hint">订单已生成，请选择：</p>
      </div>
      <template #footer>
        <el-button @click="goToOrders">稍后支付</el-button>
        <el-button type="primary" @click="payNow">立即支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Promotion, EditPen } from '@element-plus/icons-vue'
import { agentApi, agentSessionApi, cartApi, orderApi, addressApi, shopApi, flowerApi } from '@/api'
import { useUserStore } from '@/store'

const router = useRouter()

// ── 状态 ──
const inputText = ref('')
const typing = ref(false)
const messages = ref([])
const msgContainer = ref(null)
const agentOnline = ref(false)
const userId = ref(null)

const sessions = ref([])
const loadingSessions = ref(false)
const creatingSession = ref(false)
const currentSessionId = ref(null)
const currentSessionTitle = ref('')
const activeId = computed(() => currentSessionId.value != null ? String(currentSessionId.value) : '')

const orderDialogVisible = ref(false)
const payDialogVisible = ref(false)
const submittedOrderId = ref('')
const submittedOrderAmount = ref('')
const previewOrder = ref(null)
const submitting = ref(false)
const addressList = ref([])
const confirmForm = ref({ addressBookId: null, remark: '' })

// 费用配置
const packFee = ref(0)
const deliveryFee = ref(0)

const itemsTotal = computed(() => {
  if (!previewOrder.value?.items) return '0.00'
  return previewOrder.value.items.filter(i => !i.unavailable).reduce((sum, i) => sum + i.unit_price * i.quantity, 0).toFixed(2)
})

const computedTotal = computed(() => {
  return (parseFloat(itemsTotal.value) + packFee.value + deliveryFee.value).toFixed(2)
})

// ── Agent Order 数据兼容 ──
// Agent返回 { items: [{flower_id, quantity}], scenario, summary, tips }
// 前端需要 { items: [{id, name, image, unit_price, original_price, is_promo, quantity, subtotal}], total_price, scenario, tips }
const enrichOrder = async (order) => {
  if (!order?.items?.length) return order

  // 检测是否为Agent格式（只有flower_id，没有name等详情字段）
  const firstItem = order.items[0]
  if (firstItem.name && firstItem.unit_price !== undefined) return order

  // 批量查询商品详情
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

  // 组装前端格式
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
    // 判断是否不可购买：售罄或限购已满
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

// ── 会话管理 ──

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
    if (res.code === 1 && Array.isArray(res.data)) {
      sessions.value = res.data
      if (sessions.value.length > 0) {
        if (!currentSessionId.value) {
          selectSession(sessions.value[0])
        }
      } else {
        await autoCreateSession()
      }
    } else {
      await autoCreateSession()
    }
  } catch (e) {
    console.error('加载会话失败:', e)
    await autoCreateSession()
  } finally {
    loadingSessions.value = false
  }
}

const autoCreateSession = async () => {
  creatingSession.value = true
  try {
    const res = await agentSessionApi.create('新会话')
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
    }
  } catch (e) {
    ElMessage.error('创建会话失败')
  } finally {
    creatingSession.value = false
  }
}

const selectSession = async (sess) => {
  currentSessionId.value = sess.id
  currentSessionTitle.value = sess.title
  messages.value = []
  messagesLoaded.value = false
  await loadHistory()
}

const deleteSession = async (sid) => {
  // 并行清理：数据库 + Agent会话
  const results = await Promise.allSettled([
    agentSessionApi.delete(sid),
    agentApi.resetSession(sid),
  ])
  const failed = results.filter(r => r.status === 'rejected')
  if (failed.length > 0) {
    console.warn('删除会话部分清理失败:', failed.length, '项')
  }
  sessions.value = sessions.value.filter(s => String(s.id) !== String(sid))
  if (String(currentSessionId.value) === String(sid)) {
    currentSessionId.value = null
    messages.value = []
    if (sessions.value.length > 0) selectSession(sessions.value[0])
  }
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
      if (String(currentSessionId.value) === String(sess.id)) {
        currentSessionTitle.value = newTitle.value.trim()
      }
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

const checkAgentHealth = async () => {
  try {
    const res = await agentApi.health()
    agentOnline.value = res.data?.status === 'ok'
  } catch { agentOnline.value = false }
}

const messagesLoaded = ref(false)

const loadHistory = async () => {
  if (!currentSessionId.value || messagesLoaded.value) return
  try {
    const sidStr = String(currentSessionId.value)
    const res = await agentApi.getHistory(sidStr, userId.value)
    const data = res.data
    if (data.has_history && data.messages?.length > 0) {
      messages.value = data.messages.map(m => ({ role: m.role, content: m.content, order: null, buttons: [] }))
      // 恢复当前订单到最近的 bot 消息并生成按钮
      if (data.order?.items?.length) {
        const enrichedOrder = await enrichOrder(data.order)
        const lastBot = [...messages.value].reverse().find(m => m.role === 'bot')
        if (lastBot) {
          const idx = messages.value.lastIndexOf(lastBot)
          if (idx >= 0) {
            messages.value[idx].order = enrichedOrder
            messages.value[idx].buttons = [
              { text: '📋 查看/修改订单', action: 'view_order', type: 'primary' },
              { text: '🔄 重新推荐', action: 'clear_order', type: 'default' }
            ]
          }
        }
      }
      await scrollToBottom()
    }
    messagesLoaded.value = true
  } catch {}
}

onMounted(() => {
  const userStore = useUserStore()
  userId.value = userStore.userId ? String(userStore.userId) : null
  checkAgentHealth()
  loadSessions()
})

// ── 消息 ──
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || typing.value || !currentSessionId.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  await scrollToBottom()

  // 流式消息占位
  const botMsg = { role: 'bot', content: '', order: null, buttons: [] }
  messages.value.push(botMsg)
  const botIdx = messages.value.length - 1

  typing.value = true
  try {
    const sidStr = String(currentSessionId.value)
    const baseUrl = agentApi.chatStreamUrl(sidStr, text, userId.value)
    const resp = await fetch(baseUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ session_id: sidStr, message: text, user_id: userId.value })
    })

    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      // 解析 SSE 事件
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          try {
            const data = JSON.parse(line.slice(6))
            if (data.type === 'token') {
              // 逐字符追加
              messages.value[botIdx].content += data.content
              await scrollToBottom()
            } else if (data.type === 'done') {
              if (data.order) {
                const enriched = await enrichOrder(data.order)
                messages.value[botIdx].order = enriched
              }
              if (data.action_buttons?.length) {
                messages.value[botIdx].buttons = data.action_buttons
              }
              // 有订单但无按钮 → 自动补充
              if (data.order?.items?.length && !messages.value[botIdx].buttons.length) {
                messages.value[botIdx].buttons = [
                  { text: '📋 查看/修改订单', action: 'view_order', type: 'primary' },
                  { text: '🔄 重新推荐', action: 'clear_order', type: 'default' }
                ]
              }
              if (data.current_step === 'confirm' && data.order) {
                openOrderPreview(await enrichOrder(JSON.parse(JSON.stringify(data.order))))
                await loadAddresses()
                setTimeout(() => { orderDialogVisible.value = true }, 500)
              }
            }
          } catch {}
        }
      }
    }
  } catch (e) {
    console.error('Agent流式请求失败:', e)
    // 流式失败 → 显示错误，让用户重试
    messages.value[botIdx].content = '抱歉，连接AI服务时出现问题 😅\n请稍后重试。'
  } finally {
    typing.value = false
    await scrollToBottom()
  }

  // 自动更新标题 & touch会话更新时间
  const userMsgs = messages.value.filter(m => m.role === 'user')
  if (userMsgs.length === 1) {
    const sess = sessions.value.find(s => String(s.id) === activeId.value)
    if (sess && (!sess.title || sess.title === '新对话' || sess.title === '新会话')) {
      const shortTitle = text.slice(0, 20) + (text.length > 20 ? '...' : '')
      sess.title = shortTitle
      currentSessionTitle.value = shortTitle
      try { await agentSessionApi.rename(sess.id, shortTitle) } catch {}
    }
  }
  // 更新会话活跃时间并重新排序
  try { await agentSessionApi.touch(currentSessionId.value) } catch {}
  const touchedSess = sessions.value.find(s => String(s.id) === activeId.value)
  if (touchedSess) {
    touchedSess.updatedAt = new Date().toISOString()
    sessions.value.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
  }
}

const sendQuick = (text) => { inputText.value = text; sendMessage() }

// ── 按钮 ──
const handleAction = async (action, msg) => {
  if (action === 'view_order' && msg.order) {
    // 查看/修改订单
    openOrderPreview(await enrichOrder(JSON.parse(JSON.stringify(msg.order))))
    await loadAddresses()
    orderDialogVisible.value = true
  }
  if (action === 'confirm_order' && msg.order) {
    // 确认下单 → 直接弹窗
    openOrderPreview(await enrichOrder(JSON.parse(JSON.stringify(msg.order))))
    await loadAddresses()
    orderDialogVisible.value = true
  }
  if (action === 'clear_order') {
    // 重新推荐 — 只清空订单，保留对话
    try { await ElMessageBox.confirm('确定要清空当前订单，重新推荐吗？', '确认', { type: 'warning' }) } catch { return }
    try {
      await agentApi.updateOrder(currentSessionId.value, { session_id: currentSessionId.value, action: 'clear' })
    } catch {}
    // 清除消息中所有订单卡片
    messages.value = messages.value.map(m => ({ ...m, order: null, buttons: [] }))
    messages.value.push({ role: 'bot', content: '已清空订单。请告诉我您的新需求，我重新为您推荐~ 🌸', order: null, buttons: [] })
    await scrollToBottom()
  }
}

// ── 订单弹窗 ──
const loadAddresses = async () => {
  try {
    const [addrRes, feeRes] = await Promise.all([
      addressApi.list(),
      shopApi.getFee()
    ])
    if (addrRes.code === 1 && Array.isArray(addrRes.data)) {
      addressList.value = addrRes.data
      const d = addrRes.data.find(a => a.isDefault === 1) || addrRes.data[0]
      if (d) confirmForm.value.addressBookId = d.id
    }
    // 加载费用配置
    if (feeRes?.data) {
      packFee.value = feeRes.data.packFee ?? 0
      // 配送费：满额免配送费
      const threshold = feeRes.data.deliveryFreeThreshold ?? 199
      const baseFee = feeRes.data.deliveryFee ?? 10
      deliveryFee.value = parseFloat(itemsTotal.value) >= threshold ? 0 : baseFee
    }
  } catch {}
}
const onQtyChange = async (item) => {
  if (item.quantity <= 0) {
    // 数量归零 → 延迟删除，避免el-input-number组件复用导致索引错乱
    await nextTick()
    const idx = previewOrder.value.items.indexOf(item)
    if (idx >= 0) removeOrderItemSilent(idx)
    return
  }
  previewOrder.value.total_price = computedTotal.value
}

const removeOrderItemSilent = async (index) => {
  const item = previewOrder.value.items[index]
  try {
    await agentApi.updateOrder(currentSessionId.value, { session_id: currentSessionId.value, action: 'remove', product_id: item.id })
  } catch {}
  previewOrder.value.items.splice(index, 1)
  previewOrder.value.total_price = computedTotal.value
  if (!previewOrder.value.items.length) {
    orderDialogVisible.value = false
    messages.value.push({ role: 'bot', content: '订单已清空~ 🌸', order: null, buttons: [] })
  }
}
const removeOrderItem = async (index) => {
  const item = previewOrder.value.items[index]
  try { await ElMessageBox.confirm(`确定删除「${item.name}」？`, '确认', { type: 'warning' }) } catch { return }
  await agentApi.updateOrder(currentSessionId.value, { session_id: currentSessionId.value, action: 'remove', product_id: item.id })
  previewOrder.value.items.splice(index, 1)
  previewOrder.value.total_price = computedTotal.value
  if (!previewOrder.value.items.length) {
    orderDialogVisible.value = false
    messages.value.push({ role: 'bot', content: '订单已清空~ 🌸', order: null, buttons: [] })
  }
}
const submitOrder = async () => {
  if (!confirmForm.value.addressBookId) { ElMessage.warning('请选择收货地址'); return }
  submitting.value = true
  try {
    await cartApi.clean()
    for (const item of previewOrder.value.items) {
      if (item.unavailable) continue
      await cartApi.add({ flowerId: item.id, number: item.quantity })
    }
    const submitRes = await orderApi.submit({
      addressBookId: confirmForm.value.addressBookId,
      payMethod: 1,
      packAmount: packFee.value,
      deliveryFee: deliveryFee.value,
      amount: parseFloat(computedTotal.value),
      deliveryStatus: 1,
      remark: confirmForm.value.remark || 'AI导购推荐方案'
    })
    if (submitRes.code === 1) {
      orderDialogVisible.value = false
      const oid = submitRes.data?.id || '-'
      submittedOrderId.value = String(oid)
      submittedOrderAmount.value = computedTotal.value
      messages.value.push({
        role: 'bot',
        content: `✅ 订单已提交！订单编号：${oid}，金额：¥${computedTotal.value}`,
        order: null, buttons: []
      })
      // 弹出支付选择
      payDialogVisible.value = true
    } else {
      ElMessage.error(submitRes.msg || '提交失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '提交失败')
  } finally { submitting.value = false }
}

const goToOrders = () => {
  payDialogVisible.value = false
  router.push('/order')
}

const payNow = async () => {
  payDialogVisible.value = false
  try {
    const modeRes = await shopApi.getPaymentMode()
    const paymentMode = modeRes.code === 1 ? (modeRes.data ?? 1) : 1
    if (paymentMode === 0) {
      const res = await orderApi.mockPayment(submittedOrderId.value)
      if (res.code === 1) {
        ElMessage.success('支付成功')
        router.push('/order')
      } else {
        ElMessage.error(res.msg || '支付失败')
      }
    } else {
      window.location.href = '/api/user/order/payment/page/' + submittedOrderId.value
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

// ── 辅助 ──
const renderMarkdown = (text) => {
  if (!text) return ''
  return text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/\n\n/g, '<br/><br/>').replace(/\n/g, '<br/>')
}
const scrollToBottom = async () => { await nextTick(); if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight }
</script>

<style scoped>
.ai-page { display: flex; height: calc(100vh - 64px - 80px); max-width: 1200px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 20px rgba(0,0,0,0.06); }

/* 左侧会话面板 */
.session-panel { width: 240px; min-width: 240px; background: #FBF9F7; border-right: 1px solid #EDE4DD; display: flex; flex-direction: column; }
.session-header { padding: 16px; border-bottom: 1px solid #EDE4DD; display: flex; justify-content: space-between; align-items: center; }
.session-header h3 { font-size: 15px; font-weight: 700; color: #5D4A3A; margin: 0; }
.session-list { flex: 1; overflow-y: auto; padding: 8px; }
.session-item { padding: 12px; border-radius: 8px; cursor: pointer; position: relative; margin-bottom: 3px; transition: all .2s; }
.session-item:hover { background: #F0EBE7; }
.session-item.active { background: #F2E8E5; border: 1px solid #D4A99A; }
.session-title { font-size: 13px; font-weight: 500; color: #2D2320; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-right: 20px; }
.session-time { font-size: 11px; color: #9B8B85; margin-top: 2px; }
.session-actions { position: absolute; right: 8px; top: 50%; transform: translateY(-50%); display: flex; gap: 6px; opacity: 0; transition: all .2s; }
.session-item:hover .session-actions { opacity: 1; }
.session-edit { color: #C8BAB0; transition: all .2s; }
.session-edit:hover { color: #409EFF; }
.session-delete { color: #C8BAB0; transition: all .2s; }
.session-delete:hover { color: #F56C6C; }
.session-footer { padding: 10px 16px; border-top: 1px solid #EDE4DD; text-align: center; }

/* 右侧聊天区 */
.chat-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.chat-topbar { padding: 12px 20px; border-bottom: 1px solid #EDE4DD; background: #FBF9F7; }
.current-session-title { font-size: 14px; font-weight: 600; color: #5D4A3A; }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px; background: #FBF9F7; }

.chat-welcome { text-align: center; padding: 60px 20px 20px; }
.welcome-icon { font-size: 64px; margin-bottom: 12px; }
.chat-welcome h2 { font-size: 20px; color: #5D4A3A; margin: 0 0 8px; font-weight: 400; }
.welcome-desc { color: #9B8B85; font-size: 14px; line-height: 1.8; margin: 0 0 24px; }
.quick-questions { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
.quick-tag { padding: 8px 18px; background: #F7F3F0; border: 1px solid #EDE4DD; border-radius: 20px; font-size: 13px; color: #8B4A50; cursor: pointer; transition: all .3s; }
.quick-tag:hover { background: #8B4A50; color: #fff; border-color: #8B4A50; }

.chat-msg { margin-bottom: 16px; display: flex; flex-direction: column; }
.msg-user { align-items: flex-end; }
.msg-bot { align-items: flex-start; }
.msg-user .msg-bubble { background: linear-gradient(135deg, #8B4A50, #A06068); color: #fff; border-radius: 16px 4px 16px 16px; max-width: 75%; }
.msg-bot .msg-bubble { background: #fff; border-radius: 4px 16px 16px 16px; border: 1px solid #EDE4DD; max-width: 90%; }
.msg-bubble { padding: 12px 16px; font-size: 14px; line-height: 1.7; word-break: break-word; }

.typing-bubble { display: flex; align-items: center; gap: 5px; padding: 14px 20px; }
.typing-dot { width: 7px; height: 7px; border-radius: 50%; background: #C8BAB0; animation: typingBounce 1.4s ease-in-out infinite; }
.typing-dot:nth-child(2) { animation-delay: .2s; }
.typing-dot:nth-child(3) { animation-delay: .4s; }
@keyframes typingBounce { 0%,60%,100%{transform:translateY(0)} 30%{transform:translateY(-8px)} }

/* 订单卡片 */
.order-card-inline { margin-top: 10px; background: #FFFAF5; border: 1px solid #F5E6D3; border-radius: 10px; padding: 12px; width: 85%; }
.order-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.order-card-title { font-weight: 600; color: #5D4A3A; font-size: 14px; }
.order-card-items { display: flex; flex-direction: column; gap: 8px; }
.order-card-item { display: flex; gap: 10px; padding: 8px; background: #fff; border-radius: 8px; }
.order-card-item.promo { border: 1px dashed #F56C6C; }
.order-card-item.unavailable { opacity: 0.5; background: #F5F5F5; }
.order-card-item.unavailable .order-item-name { color: #999; }
.order-card-item.unavailable .order-item-name .name-text { text-decoration: line-through; }
.order-card-item.unavailable .order-item-price { color: #BBB; }
.order-card-item.unavailable .order-item-img { filter: grayscale(1); }
.unavailable-text { color: #999; text-decoration: line-through; }
.order-item-img { width: 48px; height: 48px; border-radius: 8px; flex-shrink: 0; }
.order-item-info { flex: 1; min-width: 0; }
.order-item-name { font-size: 13px; font-weight: 600; color: #2D2320; display: flex; align-items: center; gap: 4px; }
.order-item-price { font-size: 12px; color: #8B4A50; margin-top: 2px; }
.unit-price { font-weight: 600; }
.original-price { text-decoration: line-through; color: #B5A39C; margin-left: 4px; font-size: 11px; }
.item-subtotal { font-weight: 600; color: #F56C6C; }
.order-item-reason { font-size: 11px; color: #9B8B85; margin-top: 2px; }
.order-card-tips { font-size: 12px; color: #8B4A50; margin-top: 8px; padding: 6px 8px; background: #FFF8F0; border-radius: 6px; }
.action-buttons { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; margin-left: 4px; }

.chat-input-area { padding: 14px 20px; border-top: 1px solid #EDE4DD; background: #fff; }

/* 弹窗 */
.preview-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; }
.preview-total { text-align: right; margin-top: 12px; font-size: 16px; }
.total-price { font-size: 22px; font-weight: 700; color: #F56C6C; }

/* 费用明细 */
.fee-summary { margin-top: 16px; padding: 12px 16px; background: #FBF9F7; border-radius: 8px; }
.fee-line { display: flex; justify-content: space-between; align-items: center; padding: 4px 0; font-size: 14px; color: #5D4A3A; }
.fee-line.fee-total { border-top: 1px solid #EDE4DD; margin-top: 4px; padding-top: 8px; font-weight: 700; font-size: 16px; }

/* 支付弹窗 */
.pay-dialog-body { text-align: center; padding: 12px 0; }
.pay-success-icon { font-size: 48px; margin-bottom: 12px; }
.pay-order-info { font-size: 15px; color: #2D2320; margin: 8px 0; }
.pay-order-amount { font-size: 20px; color: #F56C6C; margin: 8px 0; }
.pay-order-hint { font-size: 14px; color: #9B8B85; margin: 16px 0 0; }
</style>
