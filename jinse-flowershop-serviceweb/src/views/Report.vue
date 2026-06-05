<template>
  <div class="page-container">
    <div class="page-header">
      <h3>数据统计</h3>
    </div>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="card-header">
            <h4 class="card-title">营业额统计</h4>
            <div class="card-controls">
              <el-date-picker
                v-model="turnoverRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                :shortcuts="dateShortcuts"
                size="small"
                @change="loadTurnover"
              />
            </div>
          </div>
          <div ref="turnoverChartRef" style="height: 300px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="card-header">
            <h4 class="card-title">用户统计</h4>
            <div class="card-controls">
              <el-date-picker
                v-model="userRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                :shortcuts="dateShortcuts"
                size="small"
                @change="loadUsers"
              />
            </div>
          </div>
          <div ref="userChartRef" style="height: 300px"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="card-header">
            <h4 class="card-title">订单统计</h4>
            <div class="card-controls">
              <el-date-picker
                v-model="orderRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                :shortcuts="dateShortcuts"
                size="small"
                @change="loadOrders"
              />
            </div>
          </div>
          <div ref="orderChartRef" style="height: 260px"></div>
          <div class="order-summary" v-if="orderSummary.totalOrderCount !== null">
            <span>订单总数：<b>{{ orderSummary.totalOrderCount }}</b></span>
            <span>有效订单：<b>{{ orderSummary.validOrderCount }}</b></span>
            <span>完成率：<b>{{ orderSummary.orderCompletionRate }}%</b></span>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="card-header">
            <h4 class="card-title">热销鲜花总榜</h4>
            <div class="card-controls">
              <el-date-picker
                v-model="topRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                :shortcuts="dateShortcuts"
                size="small"
                style="width: 240px"
                @change="loadTop"
              />
              <span class="top-label">前</span>
              <el-select v-model="topN" size="small" style="width: 80px" clearable placeholder="全部" @change="loadTop">
                <el-option :value="5" label="5" />
                <el-option :value="10" label="10" />
                <el-option :value="20" label="20" />
              </el-select>
              <span class="top-label">名</span>
            </div>
          </div>
          <div ref="topChartRef" style="height: 300px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { reportApi } from '@/api'

const dateShortcuts = [
  { text: '近7天', value: () => { const e = new Date(); const s = new Date(); s.setDate(s.getDate() - 6); return [s, e] } },
  { text: '近30天', value: () => { const e = new Date(); const s = new Date(); s.setDate(s.getDate() - 29); return [s, e] } },
  { text: '本月', value: () => { const e = new Date(); const s = new Date(e.getFullYear(), e.getMonth(), 1); return [s, e] } }
]

const getLastWeek = () => {
  const end = new Date()
  const begin = new Date()
  begin.setDate(begin.getDate() - 6)
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return [fmt(begin), fmt(end)]
}

const turnoverRange = ref(getLastWeek())
const userRange = ref(getLastWeek())
const orderRange = ref(getLastWeek())
const topRange = ref(null)
const topN = ref(null)
const orderSummary = ref({ totalOrderCount: null, validOrderCount: null, orderCompletionRate: null })

const turnoverChartRef = ref(null)
const userChartRef = ref(null)
const orderChartRef = ref(null)
const topChartRef = ref(null)

let chartInstances = {}

const getOrCreateChart = (key, refEl) => {
  if (!refEl.value) return null
  if (chartInstances[key]) {
    chartInstances[key].dispose()
  }
  const chart = echarts.init(refEl.value)
  chartInstances[key] = chart
  return chart
}

const handleResize = () => {
  Object.values(chartInstances).forEach(c => c && c.resize())
}

const extractRange = (rangeRef) => {
  if (rangeRef.value && rangeRef.value.length === 2) {
    return { begin: rangeRef.value[0], end: rangeRef.value[1] }
  }
  return null
}

const baseGrid = { left: '3%', right: '4%', bottom: '3%', top: '16px', containLabel: true }
const legendGrid = { left: '3%', right: '4%', bottom: '3%', top: '40px', containLabel: true }
const baseXAxis = { type: 'category', data: [], axisLine: { lineStyle: { color: '#E5DDD8' } }, axisTick: { show: false }, axisLabel: { color: '#9B8B85' } }
const baseYAxis = { type: 'value', splitLine: { lineStyle: { color: '#F5EDE6' } }, axisLabel: { color: '#9B8B85' } }
const baseTooltip = { trigger: 'axis', backgroundColor: '#fff', borderColor: '#EDE4DD', textStyle: { color: '#3D2E28' } }

const loadTurnover = async () => {
  const r = extractRange(turnoverRange)
  if (!r) return
  try {
    const res = await reportApi.turnover(r.begin, r.end)
    if (res && res.data) {
      const d = res.data
      const dates = d.dateList ? d.dateList.split(',') : []
      const vals = d.turnoverList ? d.turnoverList.split(',').map(Number) : []
      const chart = getOrCreateChart('turnover', turnoverChartRef)
      if (chart) {
        chart.setOption({
          tooltip: baseTooltip, grid: baseGrid,
          xAxis: { ...baseXAxis, data: dates },
          yAxis: baseYAxis,
          series: [{
            name: '营业额', type: 'bar', data: vals,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#8B4A50' }, { offset: 1, color: '#C88A6E' }
              ])
            },
            barWidth: 24,
            label: { show: true, position: 'top', color: '#6B5E58', fontSize: 11 }
          }]
        })
      }
    }
  } catch (e) {
    console.error('营业额统计失败', e)
    ElMessage.error('营业额统计加载失败')
  }
}

const loadUsers = async () => {
  const r = extractRange(userRange)
  if (!r) return
  try {
    const res = await reportApi.users(r.begin, r.end)
    if (res && res.data) {
      const d = res.data
      const dates = d.dateList ? d.dateList.split(',') : []
      const totals = d.totalUserList ? d.totalUserList.split(',').map(Number) : []
      const news = d.newUserList ? d.newUserList.split(',').map(Number) : []
      const chart = getOrCreateChart('user', userChartRef)
      if (chart) {
        chart.setOption({
          tooltip: baseTooltip, grid: legendGrid,
          legend: { top: '0px', textStyle: { color: '#6B5E58' } },
          xAxis: { ...baseXAxis, data: dates },
          yAxis: baseYAxis,
          series: [
            {
              name: '用户总量', type: 'line', data: totals, smooth: true,
              lineStyle: { color: '#8B4A50', width: 2 },
              itemStyle: { color: '#8B4A50' },
              areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(139,74,80,0.2)' }, { offset: 1, color: 'rgba(139,74,80,0.02)' }]) }
            },
            {
              name: '新增用户', type: 'line', data: news, smooth: true,
              lineStyle: { color: '#E8B96B', width: 2 },
              itemStyle: { color: '#E8B96B' },
              areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(232,185,107,0.2)' }, { offset: 1, color: 'rgba(232,185,107,0.02)' }]) }
            }
          ]
        })
      }
    }
  } catch (e) {
    console.error('用户统计失败', e)
    ElMessage.error('用户统计加载失败')
  }
}

const loadOrders = async () => {
  const r = extractRange(orderRange)
  if (!r) return
  try {
    const res = await reportApi.orders(r.begin, r.end)
    if (res && res.data) {
      const d = res.data
      const dates = d.dateList ? d.dateList.split(',') : []
      const orders = d.orderCountList ? d.orderCountList.split(',').map(Number) : []
      const validOrders = d.validOrderCountList ? d.validOrderCountList.split(',').map(Number) : []
      orderSummary.value = {
        totalOrderCount: d.totalOrderCount ?? 0,
        validOrderCount: d.validOrderCount ?? 0,
        orderCompletionRate: d.orderCompletionRate != null ? (d.orderCompletionRate * 100).toFixed(1) : '0.0'
      }
      const chart = getOrCreateChart('order', orderChartRef)
      if (chart) {
        chart.setOption({
          tooltip: baseTooltip, grid: legendGrid,
          legend: { top: '0px', textStyle: { color: '#6B5E58' } },
          xAxis: { ...baseXAxis, data: dates },
          yAxis: baseYAxis,
          series: [
            { name: '订单总数', type: 'bar', data: orders, itemStyle: { color: '#C88A6E' }, barWidth: 20 },
            { name: '有效订单', type: 'bar', data: validOrders, itemStyle: { color: '#6B8E6B' }, barWidth: 20 }
          ]
        })
      }
    }
  } catch (e) {
    console.error('订单统计失败', e)
    ElMessage.error('订单统计加载失败')
  }
}

const loadTop = async () => {
  try {
    const params = {}
    if (topN.value) {
      params.limit = topN.value
    }
    if (topRange.value && topRange.value.length === 2) {
      params.begin = topRange.value[0]
      params.end = topRange.value[1]
    }
    const res = await reportApi.top10(params)
    if (res && res.data) {
      const d = res.data
      const names = d.nameList ? d.nameList.split(',') : []
      const numbers = d.numberList ? d.numberList.split(',').map(Number) : []
      const max = numbers.length > 0 ? Math.max(...numbers) : 1
      const chart = getOrCreateChart('top', topChartRef)
      if (chart) {
        chart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#fff', borderColor: '#EDE4DD', textStyle: { color: '#3D2E28' } },
          grid: { left: '3%', right: '4%', bottom: '3%', top: '16px', containLabel: true },
          xAxis: { type: 'value', splitLine: { lineStyle: { color: '#F5EDE6' } }, axisLabel: { color: '#9B8B85' } },
          yAxis: { type: 'category', data: [...names].reverse(), axisLabel: { color: '#9B8B85' }, axisLine: { lineStyle: { color: '#E5DDD8' } }, axisTick: { show: false } },
          series: [{
            name: '销量', type: 'bar', data: [...numbers].reverse(),
            itemStyle: {
              color: (p) => {
                const ratio = (p.value || 0) / max
                const r = Math.round(220 - ratio * 130)
                const g = Math.round(80 + ratio * 100)
                const b = Math.round(55 + ratio * 25)
                return `rgb(${r},${g},${b})`
              }
            },
            barWidth: 18,
            label: { show: true, position: 'right', color: '#6B5E58', fontSize: 11 }
          }]
        })
      }
    }
  } catch (e) {
    console.error('热销统计失败', e)
    ElMessage.error('热销统计加载失败')
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  nextTick(() => {
    loadTurnover()
    loadUsers()
    loadOrders()
    loadTop()
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(chartInstances).forEach(c => c && c.dispose())
  chartInstances = {}
})
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { margin-bottom: 16px; }
.page-header h3 { margin: 0; color: #3D2E28; font-size: 18px; }
.chart-row { margin-bottom: 20px; }
.chart-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.card-title { margin: 0; font-size: 15px; color: #3D2E28; font-weight: 600; }
.card-controls { display: flex; align-items: center; gap: 12px; }
.top-label { font-size: 13px; color: #6B5E58; }
.order-summary { display: flex; justify-content: space-around; padding: 10px 0 0; border-top: 1px solid #F5EDE6; margin-top: 8px; font-size: 13px; color: #6B5E58; }
.order-summary b { color: #3D2E28; }
</style>
