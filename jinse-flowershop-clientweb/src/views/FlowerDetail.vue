<template>
  <div class="flower-detail-page" v-loading="loading">
    <div class="breadcrumb">
      <router-link :to="backLink">← 返回{{ backText }}</router-link>
    </div>

    <div class="detail-main" v-if="flower">
      <div class="detail-image">
        <img :src="flower.image || 'https://via.placeholder.com/500x500?text=🌸'" :alt="flower.name" />
        <span v-if="flower.promo" class="detail-sale-badge">{{ flower.activityContent || '促销' }}</span>
      </div>
      <div class="detail-info">
        <h1 class="detail-name">{{ flower.name }}</h1>
        <p class="detail-category" v-if="flower.categoryName">{{ flower.categoryName }}</p>
        <div class="detail-price-row">
          <span v-if="flower.promo" class="detail-price promo-price">¥{{ flower.discountPrice }}</span>
          <span v-if="flower.promo" class="detail-original">¥{{ flower.originalPrice }}</span>
          <span v-if="!flower.promo" class="detail-price">¥{{ flower.price }}</span>
        </div>
        <div v-if="flower.promo" class="detail-promo-meta">
          <span v-if="flower.stock !== undefined && flower.stock > 0" class="promo-stock">仅剩{{ flower.stock }}件</span>
          <span v-if="flower.limitPer" class="promo-limit">限购{{ flower.limitPer }}件/人</span>
        </div>
        <p class="detail-desc">{{ flower.description || '暂无描述信息' }}</p>

        <div class="detail-actions">
          <el-input-number v-model="quantity" :min="1" :max="99" size="large" />
          <el-button type="primary" size="large" class="cart-btn" @click="addToCart">
            <el-icon><ShoppingCart /></el-icon>加入购物车
          </el-button>
        </div>
      </div>
    </div>

    <div class="comment-section" v-if="flower">
      <h3 class="comment-title">用户评价 ({{ commentTotal }})</h3>
      <div class="comment-list" v-if="comments.length">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <img v-if="c.userAvatar" :src="c.userAvatar" class="comment-avatar" />
          <div v-else class="comment-avatar-placeholder">👤</div>
          <div class="comment-body">
            <div class="comment-user">{{ c.userName || '匿名用户' }}
              <el-rate v-if="c.rating" :model-value="c.rating" disabled size="small" style="margin-left:8px;--el-rate-icon-size:12px;" />
            </div>
            <p class="comment-content">{{ c.content }}</p>
            <div class="comment-meta">
              <span>{{ c.createTime }}</span>
              <button
                :class="['like-btn', { liked: c.isLike === 1 }]"
                @click="likeComment(c)"
              >
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
                <span>{{ c.likeCount || 0 }}</span>
              </button>
              <button v-if="isOwnComment(c)" class="delete-btn" @click="deleteComment(c)">删除</button>
            </div>
          </div>
        </div>
        <div v-if="loadingMore" class="loading-more">加载中...</div>
        <div v-else-if="!hasMore && comments.length" class="no-more">没有更多评论了</div>
      </div>
      <div v-else class="no-comment">暂无评论，快来发表第一条评价吧~</div>

      <div class="add-comment" v-if="userStore.isLoggedIn">
        <div class="comment-rating-row">
          <span class="rating-label">评分：</span>
          <el-rate v-model="commentRating" :colors="['#F56C6C','#E6A23C','#F56C6C']" show-text :texts="['很差','一般','还行','不错','很好']" />
        </div>
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="3"
          placeholder="写下您对这款鲜花的评价..."
        />
        <el-button type="primary" class="comment-submit" @click="submitComment">发表评价</el-button>
      </div>
      <div v-else class="comment-login-hint">
        <router-link to="/login">登录</router-link>后即可发表评论
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { flowerApi, cartApi, commentApi, userApi } from '@/api'
import { useUserStore } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const backLink = computed(() => {
  const from = route.query.from
  if (from === 'activity') {
    return { name: 'Activity' }
  }
  return { name: 'FlowerList' }
})

const backText = computed(() => {
  const from = route.query.from
  if (from === 'activity') {
    return '热门活动'
  }
  return '鲜花商城'
})

const flower = ref(null)
const loading = ref(false)
const quantity = ref(1)
const comments = ref([])
const commentTotal = ref(0)
const commentText = ref('')
const commentRating = ref(5)
const commentPage = ref(1)
const commentPageSize = 5
const hasMore = ref(true)
const loadingMore = ref(false)

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await flowerApi.getById(route.params.id)
    if (res.code === 1) {
      flower.value = res.data
    }
  } catch (e) {
    ElMessage.error('加载鲜花详情失败')
  } finally {
    loading.value = false
  }
}

const loadComments = async (reset = false) => {
  if (loadingMore.value) return
  loadingMore.value = true
  try {
    const page = reset ? 1 : commentPage.value
    const res = await commentApi.list({ flowerId: route.params.id, page, pageSize: commentPageSize })
    if (res.code === 1 && res.data) {
      const records = res.data.records || res.data || []
      commentTotal.value = res.data.total || 0
      if (reset) {
        comments.value = records
        commentPage.value = 1
      } else {
        comments.value.push(...records)
      }
      hasMore.value = comments.value.length < commentTotal.value
      if (records.length > 0) commentPage.value = page + 1
    }
  } catch (e) {
    // ignore
  } finally {
    loadingMore.value = false
  }
}

const onCommentScroll = (e) => {
  const el = e.target
  if (!hasMore.value || loadingMore.value) return
  if (el.scrollHeight - el.scrollTop - el.clientHeight < 60) {
    loadComments()
  }
}

const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再添加购物车')
    return
  }
  try {
    const res = await cartApi.add({ flowerId: flower.value.id, number: quantity.value })
    if (res.code === 1) {
      ElMessage.success(`已将「${flower.value.name}」×${quantity.value} 加入购物车`)
    }
  } catch (e) {
    ElMessage.error(e?.message || '添加购物车失败')
  }
}

const submitComment = async () => {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    const res = await commentApi.add({
      flowerId: route.params.id,
      content: commentText.value,
      rating: commentRating.value
    })
    if (res.code === 1) {
      ElMessage.success('评论发表成功')
      commentText.value = ''
      commentRating.value = 5
      loadComments(true)
    }
  } catch (e) {
    ElMessage.error(e?.message || '评论发表失败')
  }
}

const likeComment = async (comment) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  try {
    await userApi.likeComment(comment.id)
    if (comment.isLike === 1) {
      comment.isLike = 0
      comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
    } else {
      comment.isLike = 1
      comment.likeCount = (comment.likeCount || 0) + 1
    }
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const isOwnComment = (comment) => {
  if (!userStore.isLoggedIn) return false
  return comment.userId === userStore.user?.id
}

const deleteComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' })
    const res = await commentApi.delete(comment.id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      comments.value = comments.value.filter(c => c.id !== comment.id)
      commentTotal.value--
    }
  } catch (e) {}
}

onMounted(() => {
  loadDetail()
  loadComments(true)
  const el = document.querySelector('.comment-list')
  if (el) el.addEventListener('scroll', onCommentScroll)
})

onUnmounted(() => {
  const el = document.querySelector('.comment-list')
  if (el) el.removeEventListener('scroll', onCommentScroll)
})
</script>

<style scoped>
.breadcrumb {
  margin-bottom: 24px;
}

.breadcrumb a {
  color: #8B4A50;
  font-size: 14px;
}

.detail-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 40px;
}

.detail-image {
  border-radius: 12px;
  overflow: hidden;
  background: #FBF9F7;
  position: relative;
}

.detail-sale-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #ff4d4f, #e63946);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 14px;
  border-radius: 12px;
  letter-spacing: 1px;
  box-shadow: 0 2px 6px rgba(230, 57, 70, 0.3);
}

.detail-image img {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.detail-name {
  font-size: 26px;
  font-weight: 700;
  color: #2D2320;
  margin: 0 0 8px;
}

.detail-category {
  font-size: 13px;
  color: #9B8B85;
  margin: 0 0 16px;
}

.detail-price-row {
  margin-bottom: 20px;
}

.detail-price {
  font-size: 32px;
  font-weight: 700;
  color: #8B4A50;
}

.detail-price.promo-price {
  color: #E8574A;
}

.detail-original {
  font-size: 16px;
  color: #B5A39C;
  text-decoration: line-through;
  margin-left: 8px;
}

.detail-promo-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 13px;
}

.promo-stock {
  color: #E8574A;
  font-weight: 500;
}

.promo-limit {
  color: #8B4A50;
  font-weight: 500;
  background: #fdf6f6;
  padding: 2px 8px;
  border-radius: 4px;
}

.detail-desc {
  font-size: 15px;
  color: #5D4A3A;
  line-height: 1.8;
  margin-bottom: 28px;
}

.detail-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.cart-btn {
  padding: 12px 32px;
  font-size: 15px;
  border-radius: 8px;
}

.comment-section {
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
}

.comment-title {
  font-size: 18px;
  color: #2D2320;
  margin: 0 0 20px;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #F5F0EC;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-avatar-placeholder {
  font-size: 28px;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F7F3F0;
  border-radius: 50%;
}

.comment-body { flex: 1; }

.comment-list {
  max-height: 500px;
  overflow-y: auto;
}

.comment-user {
  font-size: 14px;
  font-weight: 600;
  color: #5D4A3A;
  margin-bottom: 6px;
}

.comment-content {
  font-size: 14px;
  color: #3D2E28;
  line-height: 1.6;
  margin: 0 0 8px;
}

.comment-meta {
  font-size: 12px;
  color: #B5A39C;
  display: flex;
  align-items: center;
  gap: 16px;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: 1px solid #e8ddd6;
  border-radius: 20px;
  background: #fff;
  color: #B5A39C;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s;
  line-height: 1;
}

.like-btn:hover {
  border-color: #E8574A;
  color: #E8574A;
  background: #fef7f6;
}

.like-btn.liked {
  border-color: #E8574A;
  color: #E8574A;
  background: #fef1f0;
}

.like-btn.liked svg {
  animation: like-pop 0.3s ease;
}

.delete-btn {
  padding: 4px 12px;
  border: 1px solid #e8ddd6;
  border-radius: 20px;
  background: #fff;
  color: #B5A39C;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s;
  line-height: 1;
}

.delete-btn:hover {
  border-color: #F56C6C;
  color: #F56C6C;
  background: #fef0f0;
}

@keyframes like-pop {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.no-comment {
  text-align: center;
  color: #B5A39C;
  padding: 30px 0;
}

.loading-more {
  text-align: center;
  color: #B5A39C;
  padding: 16px 0;
  font-size: 13px;
}

.no-more {
  text-align: center;
  color: #CCC;
  padding: 16px 0;
  font-size: 13px;
}

.add-comment {
  margin-top: 24px;
}

.comment-rating-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.rating-label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
  white-space: nowrap;
}

.comment-submit {
  margin-top: 12px;
}

.comment-login-hint {
  margin-top: 24px;
  text-align: center;
  color: #9B8B85;
  font-size: 14px;
}

.comment-login-hint a {
  color: #8B4A50;
}
</style>