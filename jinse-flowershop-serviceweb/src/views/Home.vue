<template>
  <div class="home-dashboard">
    <div class="welcome-card">
      <div class="welcome-content">
        <h2 class="welcome-title">欢迎回来，{{ userName }}</h2>
        <p class="welcome-desc">今天是 {{ today }}，祝您工作愉快 🌸</p>
      </div>
      <div class="welcome-illustration">💐</div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ borderTopColor: stat.color }">
          <div class="stat-icon" :style="{ background: stat.bg }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value">{{ stat.value }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <div class="chart-card">
          <h3 class="card-title">营业额统计</h3>
          <div ref="turnoverChartRef" style="height: 320px"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <h3 class="card-title">快速操作</h3>
          <div class="quick-actions">
            <div class="action-item" v-for="act in quickActions" :key="act.label" @click="$router.push(act.path)">
              <div class="action-icon" :style="{ background: act.bg }">
                <el-icon :size="20"><component :is="act.icon" /></el-icon>
              </div>
              <span>{{ act.label }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, markRaw } from 'vue'
import * as echarts from 'echarts'
import { reportApi } from '@/api'
import { useUserStore } from '@/store'
import { Money, Document, UserFilled, CircleCheck, Van, Grid, Avatar, DataAnalysis } from '@element-plus/icons-vue'

const userStore = useUserStore()
const userName = userStore.manager?.name || '管理员'

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

const stats = ref([
  { label: '营业额', value: '¥ 8,650', icon: markRaw(Money), color: '#8B4A50', bg: 'rgba(139,74,80,0.1)' },
  { label: '订单数', value: '128', icon: markRaw(Document), color: '#C88A6E', bg: 'rgba(200,138,110,0.1)' },
  { label: '用户数', value: '356', icon: markRaw(UserFilled), color: '#E8B96B', bg: 'rgba(232,185,107,0.1)' },
  { label: '有效订单', value: '115', icon: markRaw(CircleCheck), color: '#6B8E6B', bg: 'rgba(107,142,107,0.1)' }
])

const quickActions = [
  { label: '鲜花管理', icon: markRaw(Van), path: '/flower', bg: 'rgba(139,74,80,0.1)' },
  { label: '分类管理', icon: markRaw(Grid), path: '/category', bg: 'rgba(200,138,110,0.1)' },
  { label: '员工管理', icon: markRaw(Avatar), path: '/employee', bg: 'rgba(232,185,107,0.1)' },
  { label: '数据报表', icon: markRaw(DataAnalysis), path: '/report', bg: 'rgba(107,142,107,0.1)' }
]

const turnoverChartRef = ref(null)

const initChart = () => {
  if (!turnoverChartRef.value) return
  const chart = echarts.init(turnoverChartRef.value)
  
  // 模拟数据
  const mockDateList = []
  const mockTurnoverList = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date(Date.now() - i * 86400000)
    mockDateList.push(`${date.getMonth() + 1}/${date.getDate()}`)
    mockTurnoverList.push(800 + Math.floor(Math.random() * 1000))
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#EDE4DD',
      textStyle: { color: '#3D2E28' },
      axisPointer: { lineStyle: { color: '#EDE4DD' } }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '16px', containLabel: true },
    xAxis: {
      type: 'category',
      data: mockDateList,
      axisLine: { lineStyle: { color: '#E5DDD8' } },
      axisTick: { show: false },
      axisLabel: { color: '#9B8B85' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F5EDE6' } },
      axisLabel: { color: '#9B8B85' }
    },
    series: [{
      name: '营业额',
      type: 'line',
      smooth: true,
      data: mockTurnoverList,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#8B4A50', width: 2 },
      itemStyle: { color: '#8B4A50' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(139,74,80,0.25)' },
          { offset: 1, color: 'rgba(139,74,80,0.02)' }
        ])
      }
    }]
  }
  chart.setOption(option)
  
  // 计算并更新统计
  const total = mockTurnoverList.reduce((a, b) => a + b, 0)
  stats.value[0].value = '¥ ' + total.toLocaleString()

  // 尝试从后端获取真实数据
  const begin = new Date(Date.now() - 6 * 86400000).toISOString().slice(0, 10)
  const end = new Date().toISOString().slice(0, 10)
  reportApi.turnover(begin, end).then(res => {
    if (res.code === 1 && res.data) {
      const d = res.data
      const dates = d.dateList ? d.dateList.split(',') : []
      const vals = d.turnoverList ? d.turnoverList.split(',').map(Number) : []
      if (dates.length && vals.length) {
        chart.setOption({
          xAxis: { data: dates },
          series: [{ data: vals }]
        })
        const realTotal = vals.reduce((a, b) => a + b, 0)
        stats.value[0].value = '¥ ' + realTotal.toLocaleString()
      }
    }
  }).catch(() => {})

  Promise.all([
    reportApi.orders(begin, end),
    reportApi.users(begin, end)
  ]).then(([orderRes, userRes]) => {
    if (orderRes.code === 1 && orderRes.data) {
      stats.value[1].value = String(orderRes.data.totalOrderCount ?? stats.value[1].value)
      stats.value[3].value = String(orderRes.data.validOrderCount ?? stats.value[3].value)
    }
    if (userRes.code === 1 && userRes.data) {
      const totalUserList = userRes.data.totalUserList ? userRes.data.totalUserList.split(',').map(Number) : []
      if (totalUserList.length) {
        stats.value[2].value = String(totalUserList[totalUserList.length - 1])
      }
    }
  }).catch(() => {})

  window.addEventListener('resize', () => chart.resize())
}

onMounted(() => {
  nextTick(initChart)
})
</script>

<style scoped>
.home-dashboard { max-width: 1400px; }

.welcome-card {
  background: linear-gradient(135deg, #8B4A50, #C88A6E);
  border-radius: 12px;
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: #fff;
}

.welcome-title { margin: 0 0 6px; font-size: 22px; font-weight: 600; }
.welcome-desc { margin: 0; font-size: 14px; opacity: 0.85; }
.welcome-illustration { font-size: 48px; }

.stats-row { margin-bottom: 20px; }

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-top: 3px solid #8B4A50;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }

.stat-icon {
  width: 52px; height: 52px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #8B4A50;
}

.stat-label { font-size: 13px; color: #9B8B85; margin-bottom: 4px; }
.stat-value { font-size: 22px; font-weight: 700; color: #3D2E28; }

.chart-row { margin-bottom: 20px; }

.chart-card {
  background: #fff; border-radius: 10px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  height: 100%;
}

.card-title { margin: 0 0 16px; font-size: 16px; color: #3D2E28; font-weight: 600; }

.quick-actions { display: flex; flex-wrap: wrap; gap: 12px; }
.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 16px; border-radius: 10px; cursor: pointer;
  transition: background 0.2s; width: calc(50% - 6px);
  background: #F7F3F0; border: 1px solid #EDE4DD;
}
.action-item:hover { background: #EDE4DD; }

.action-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #8B4A50;
}

.action-item span { font-size: 13px; color: #5D4A3A; font-weight: 500; }
</style>