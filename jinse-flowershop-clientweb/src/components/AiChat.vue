<template>
  <div class="ai-chat-wrapper" :class="{ expanded: isOpen }">
    <transition name="fade">
      <div v-if="isOpen" class="ai-chat-panel">
        <div class="chat-header">
          <div class="chat-header-left">
            <span class="chat-avatar">🤖</span>
            <span class="chat-title">AI 智能客服</span>
            <el-tag size="small" type="success" effect="dark">在线</el-tag>
          </div>
          <el-icon class="chat-close" :size="18" @click="isOpen = false"><Close /></el-icon>
        </div>
        <div class="chat-messages" ref="msgContainer">
          <div class="chat-welcome">
            <div class="welcome-avatar">🌸</div>
            <p>您好！我是锦瑟花店的AI智能客服小锦，请问有什么可以帮您的？</p>
            <div class="quick-questions">
              <span class="quick-tag" @click="sendQuick('有什么鲜花推荐？')">有什么鲜花推荐？</span>
              <span class="quick-tag" @click="sendQuick('今天的活动有哪些？')">今天的活动有哪些？</span>
              <span class="quick-tag" @click="sendQuick('如何下单购买？')">如何下单购买？</span>
              <span class="quick-tag" @click="sendQuick('配送范围和时间？')">配送范围和时间？</span>
            </div>
          </div>
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            :class="['chat-msg', msg.role === 'user' ? 'msg-user' : 'msg-bot']"
          >
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
          <div v-if="typing" class="chat-msg msg-bot">
            <div class="msg-bubble typing-bubble">
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <el-input
            v-model="inputText"
            placeholder="输入您的问题..."
            @keyup.enter="sendMessage"
            :disabled="typing"
          >
            <template #append>
              <el-button @click="sendMessage" :disabled="typing || !inputText.trim()">
                <el-icon><Promotion /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>
    <div v-if="!isOpen" class="ai-chat-bubble" @click="isOpen = true">
      <span class="bubble-icon">🤖</span>
      <span class="bubble-text">智能客服</span>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { Close, Promotion } from '@element-plus/icons-vue'

const isOpen = ref(false)
const inputText = ref('')
const typing = ref(false)
const messages = ref([])
const msgContainer = ref(null)

const knowledgeBase = {
  '推荐': '我们推荐您看看本季热销的🌹卡罗拉红玫瑰、💐戴安娜粉玫瑰和🌻向日葵花束，都是顾客评价最高的鲜花哦！',
  '活动': '目前我们有"浪漫520"特惠活动，全场玫瑰9折优惠；还有"母亲节感恩"专区，康乃馨花束限时特价。您可以到"热门活动"页面查看详情！',
  '下单': '下单非常简单哦：\n1️⃣ 在鲜花商城选择您喜欢的花束\n2️⃣ 加入购物车\n3️⃣ 点击结算，填写收货信息\n4️⃣ 在线支付即可\n下单后我们会尽快为您配送！',
  '配送': '我们支持全城配送，下单后通常2小时内送达。配送时间为每天早上8点到晚上8点。满199元免配送费哦！',
  '价格': '我们的鲜花价格从几十元到几百元不等，丰俭由人。具体的价格可以在鲜花商城页面查看，每款花束都明码标价~',
  '支付': '我们支持微信支付和支付宝支付，安全便捷。',
  '退货': '收到鲜花后如果发现质量问题，请在24小时内联系客服，我们承诺无条件退换！',
  '会员': '注册成为我们的会员可以享受积分累计、生日当月8折、会员日专属优惠等权益。每消费1元积1分，积分可兑换精美礼品！',
  '定制': '我们提供个性化花束定制服务！您可以在"鲜花商城"选择花材、颜色搭配和包装风格，打造专属于您的独一无二的花束。',
  '地址': '我们的实体店地址在花城大道520号，欢迎您到店选购！线上购买请填写正确的收货地址，我们会送货上门。'
}

const getReply = (question) => {
  const q = question.toLowerCase()
  for (const [keyword, reply] of Object.entries(knowledgeBase)) {
    if (q.includes(keyword)) {
      return reply
    }
  }
  return '感谢您的咨询！这个问题我暂时无法准确回答，建议您拨打客服热线 400-888-JINSE 或到店咨询，我们会有专业的花艺顾问为您服务哦 🌸'
}

const scrollToBottom = async () => {
  await nextTick()
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || typing.value) return
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  await scrollToBottom()

  typing.value = true
  await new Promise(r => setTimeout(r, 800 + Math.random() * 1200))
  typing.value = false

  const reply = getReply(text)
  messages.value.push({ role: 'bot', content: reply })
  await scrollToBottom()
}

const sendQuick = (text) => {
  inputText.value = text
  sendMessage()
}
</script>

<style scoped>
.ai-chat-wrapper {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 999;
}

.ai-chat-panel {
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.15), 0 0 0 1px rgba(232, 185, 107, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  background: linear-gradient(135deg, #6B3A42, #8B4A50);
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-avatar {
  font-size: 24px;
}

.chat-title {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}

.chat-close {
  color: rgba(255,255,255,0.7);
  cursor: pointer;
  transition: color 0.3s;
}

.chat-close:hover {
  color: #fff;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #FBF9F7;
}

.chat-welcome {
  text-align: center;
  padding: 10px 0 16px;
}

.welcome-avatar {
  font-size: 40px;
  margin-bottom: 8px;
}

.chat-welcome p {
  color: #5D4A3A;
  font-size: 14px;
  margin-bottom: 12px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-tag {
  padding: 4px 12px;
  background: #F7F3F0;
  border: 1px solid #EDE4DD;
  border-radius: 16px;
  font-size: 12px;
  color: #8B4A50;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-tag:hover {
  background: #8B4A50;
  color: #fff;
  border-color: #8B4A50;
}

.chat-msg {
  margin-bottom: 12px;
  display: flex;
}

.msg-user {
  justify-content: flex-end;
}

.msg-user .msg-bubble {
  background: linear-gradient(135deg, #8B4A50, #A06068);
  color: #fff;
  border-radius: 16px 4px 16px 16px;
}

.msg-bot .msg-bubble {
  background: #fff;
  border-radius: 4px 16px 16px 16px;
  border: 1px solid #EDE4DD;
  white-space: pre-line;
}

.msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 14px 18px;
}

.typing-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #C8BAB0;
  animation: typingBounce 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

.chat-input-area {
  padding: 12px 16px;
  border-top: 1px solid #EDE4DD;
  background: #fff;
}

.ai-chat-bubble {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6B3A42, #8B4A50);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(139, 74, 80, 0.3);
  transition: transform 0.3s;
  color: #fff;
  gap: 0;
}

.ai-chat-bubble:hover {
  transform: scale(1.08);
}

.bubble-icon {
  font-size: 24px;
  line-height: 1;
}

.bubble-text {
  font-size: 9px;
  color: #E8B96B;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>