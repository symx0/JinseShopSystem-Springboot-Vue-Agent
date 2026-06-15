<template>
  <div class="page-container">
    <div class="page-header">
      <h3>AI 模型配置</h3>
      <p class="page-desc">配置 AI 导购 Agent 使用的模型参数（配置存储在 Redis 中）</p>
    </div>

    <!-- 当前Agent状态 -->
    <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <span>📊 Agent 服务状态</span>
          <el-button size="small" @click="fetchHealth" :loading="healthLoading">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-descriptions :column="3" border size="small" v-if="healthData">
        <el-descriptions-item label="服务状态">
          <el-tag :type="healthData.status === 'ok' ? 'success' : 'danger'">
            {{ healthData.status === 'ok' ? '运行中' : '异常' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="MCP 连接">
          <el-tag :type="healthData.mcp_connected ? 'success' : 'warning'">
            {{ healthData.mcp_connected ? '已连接' : '未连接(降级模式)' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="LLM 配置">
          <el-tag :type="healthData.llm_configured ? 'success' : 'info'">
            {{ healthData.llm_configured ? '已配置' : '未配置(规则引擎)' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- API 配置 -->
    <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <span>🔑 API 配置</span>
          <div style="display: flex; gap: 8px;">
            <el-button size="small" @click="fetchConfig" :loading="llmModelsLoading">
              <el-icon><Refresh /></el-icon> 刷新模型列表
            </el-button>
          </div>
        </div>
      </template>

      <el-form :model="configForm" label-width="130px" style="max-width: 720px">
        <el-form-item label="Base URL">
          <el-input
            v-model="configForm.baseUrl"
            placeholder="API 调用地址"
            clearable
          >
            <template #prepend>
              <el-icon><Link /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">
            Ollama 地址格式: http://你的服务器IP:11434/v1 (本地用 localhost)
          </div>
        </el-form-item>

        <el-form-item label="API Key">
          <el-input
            v-model="configForm.apiKey"
            placeholder="输入 API Key（Ollama 可留空）"
            show-password
            clearable
          >
            <template #prepend>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="Model">
          <div style="display: flex; gap: 8px; width: 100%;">
            <el-select
              v-model="configForm.model"
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入模型名"
              style="flex: 1"
              :loading="llmModelsLoading"
            >
              <el-option
                v-for="m in llmModelOptions"
                :key="m.name"
                :label="m.label"
                :value="m.name"
              />
            </el-select>
            <el-button type="success" size="small" @click="switchLLM" :loading="switchingLLM">
              切换
            </el-button>
          </div>
          <div class="form-tip">可从下拉列表选择已拉取的模型，或手动输入新模型名后点击"切换"</div>
        </el-form-item>

        <el-form-item label="Temperature">
          <el-slider
            v-model="configForm.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            show-input
            style="width: 100%"
          />
          <div class="form-tip">0 = 精确稳定 ← → 2 = 创意发散，推荐 0.7</div>
        </el-form-item>

        <el-form-item label="Max Tokens">
          <el-input-number v-model="configForm.maxTokens" :min="256" :max="32768" :step="256" />
          <span class="form-tip" style="margin-left: 8px;">单次回复最大长度，默认 4096</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- RAG 嵌入模型配置 -->
    <el-card shadow="never" class="config-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>📚 RAG 嵌入模型</span>
          <div style="display: flex; gap: 8px;">
            <el-button size="small" @click="fetchConfig" :loading="embedModelsLoading">
              <el-icon><Refresh /></el-icon> 刷新模型列表
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        title="RAG 嵌入模型说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px;"
      >
        <p>嵌入模型用于 RAG 知识库的语义检索（BM25 + 向量双路混合）。启动时默认不加载，需在此配置嵌入模型后激活。</p>
        <p>使用 Ollama 本地嵌入模型。配置后自动构建向量索引。</p>
      </el-alert>

      <el-form :model="embeddingForm" label-width="130px" style="max-width: 720px">
        <el-form-item label="Base URL">
            <el-input
              v-model="embeddingForm.baseUrl"
              placeholder="嵌入模型 API 地址"
              clearable
            >
              <template #prepend>
                <el-icon><Link /></el-icon>
              </template>
            </el-input>
            <div class="form-tip">
              Ollama 地址: http://你的服务器IP:11434/v1 (与 LLM 的 Base URL 相同即可)
            </div>
          </el-form-item>

          <el-form-item label="API Key">
            <el-input
              v-model="embeddingForm.apiKey"
              placeholder="API Key（Ollama 可留空）"
              show-password
              clearable
            >
              <template #prepend>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="嵌入模型">
            <div style="display: flex; gap: 8px; width: 100%;">
              <el-select
                v-model="embeddingForm.model"
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入嵌入模型名"
                style="flex: 1"
                :loading="embedModelsLoading"
              >
                <el-option
                  v-for="m in embedModelOptions"
                  :key="m.name"
                  :label="m.label"
                  :value="m.name"
                />
              </el-select>
              <el-button type="success" size="small" @click="switchEmbedding" :loading="switchingEmbedding">
                切换
              </el-button>
            </div>
            <div class="form-tip">可从下拉列表选择已拉取的嵌入模型，或手动输入新模型名后点击"切换"</div>
          </el-form-item>

          <el-form-item label="检索数量 (top_k)">
            <el-slider
              v-model="embeddingForm.topK"
              :min="1"
              :max="10"
              :step="1"
              show-input
              style="width: 100%"
            />
            <div class="form-tip">RAG 检索返回的最大文档数，推荐 5</div>
          </el-form-item>
      </el-form>
    </el-card>

    <!-- 连接测试 -->
    <el-card shadow="never" class="config-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>🔌 连接测试</span>
        </div>
      </template>

      <div class="test-area">
        <el-button
          type="primary"
          :loading="testing"
          @click="testConnection"
          :icon="Connection"
        >
          {{ testing ? '测试中...' : '测试连接' }}
        </el-button>

        <div v-if="testResult !== null" class="test-result">
          <el-alert
            :title="testResult.llm?.success ? '✅ LLM 连接成功' : '❌ LLM 连接失败'"
            :type="testResult.llm?.success ? 'success' : 'error'"
            :description="testResult.llm?.message || ''"
            :closable="false"
            show-icon
            style="margin-bottom: 8px"
          />
          <el-alert
            :title="testResult.embedding?.success ? '✅ 嵌入模型连接成功' : '❌ 嵌入模型连接失败'"
            :type="testResult.embedding?.success ? 'success' : 'error'"
            :description="testResult.embedding?.message || ''"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </el-card>

    <!-- 知识库管理 -->
    <el-card shadow="never" class="config-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>📚 知识库文档管理</span>
          <el-button type="primary" size="small" @click="showUploadDialog" :icon="Upload">上传文档</el-button>
        </div>
      </template>

      <div v-loading="knowledgeLoading">
        <el-alert
          title="知识库说明"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px;"
        >
          <p>上传 txt 或 md 格式的花卉知识文档，Agent 将自动索引用于 RAG 检索。支持分类：鲜花养护、花语含义、场景选花、品种介绍、行业知识。</p>
        </el-alert>

        <div v-if="knowledgeData && knowledgeData.length" class="knowledge-categories">
          <div v-for="cat in knowledgeData" :key="cat.category" class="knowledge-category">
            <div class="category-header" @click="cat._collapsed = !cat._collapsed" style="cursor: pointer;">
              <span class="category-name">
                <el-icon :size="14" style="vertical-align: middle; margin-right: 4px;">
                  <ArrowRight v-if="cat._collapsed" />
                  <ArrowDown v-else />
                </el-icon>
                📁 {{ cat.category }}
              </span>
              <el-tag size="small">{{ cat.file_count }} 个文档</el-tag>
            </div>
            <div class="category-files" v-if="!cat._collapsed && cat.files && cat.files.length">
              <div v-for="file in cat.files" :key="file.filename" class="file-item">
                <span class="file-name">📄 {{ file.filename }}</span>
                <span class="file-size">{{ formatFileSize(file.size_bytes) }}</span>
                <el-button link size="small" type="danger" @click="deleteDoc(cat.category, file.filename)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <el-empty v-else-if="!cat._collapsed" description="空分类" :image-size="24" />
          </div>
        </div>
        <el-empty v-else description="暂无知识文档" />

        <div style="margin-top: 12px;">
          <el-button size="small" @click="fetchKnowledge" :loading="knowledgeLoading">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 上传知识文档弹窗 -->
    <el-dialog title="上传知识文档" v-model="uploadDialogVisible" width="560px" destroy-on-close>
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="uploadForm.category" placeholder="选择分类" style="width:100%">
            <el-option label="鲜花养护" value="鲜花养护" />
            <el-option label="花语含义" value="花语含义" />
            <el-option label="场景选花" value="场景选花" />
            <el-option label="品种介绍" value="品种介绍" />
            <el-option label="行业知识" value="行业知识" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传方式">
          <el-radio-group v-model="uploadForm.mode">
            <el-radio-button value="file">选择文件</el-radio-button>
            <el-radio-button value="folder">选择文件夹</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <!-- 单文件上传 -->
        <el-form-item v-if="uploadForm.mode === 'file'" label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
            accept=".txt,.md"
            drag
          >
            <el-icon class="el-icon--upload"><Upload /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处，或<em>点击选择文件</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">仅支持 txt / md 格式文件</div>
            </template>
          </el-upload>
        </el-form-item>
        <!-- 文件夹上传 -->
        <el-form-item v-if="uploadForm.mode === 'folder'" label="选择文件夹">
          <div class="folder-upload-area">
            <input
              ref="folderInputRef"
              type="file"
              webkitdirectory
              directory
              multiple
              style="display: none"
              @change="onFolderSelect"
            />
            <el-button @click="$refs.folderInputRef.click()">
              <el-icon><FolderOpened /></el-icon> 选择文件夹
            </el-button>
            <div v-if="uploadForm.folderFiles.length" class="folder-file-list">
              <el-tag type="success" style="margin-bottom: 8px;">
                已选择 {{ uploadForm.folderFiles.length }} 个 txt/md 文件
              </el-tag>
              <div class="folder-files-preview">
                <span v-for="(f, i) in uploadForm.folderFiles.slice(0, 10)" :key="i" class="folder-file-tag">
                  {{ f.name }}
                </span>
                <span v-if="uploadForm.folderFiles.length > 10" class="folder-file-tag more">
                  ...等共 {{ uploadForm.folderFiles.length }} 个文件
                </span>
              </div>
            </div>
            <div v-else class="el-upload__tip" style="margin-top: 8px;">
              选择包含 txt/md 文件的文件夹，将自动过滤并上传所有文档
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="uploadDoc"
          :loading="uploading"
          :disabled="uploadForm.mode === 'file' ? !uploadForm.file : !uploadForm.folderFiles.length"
        >
          {{ uploading ? '上传中...' : '上传' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Key, Link, Connection, Refresh, Upload, Delete, FolderOpened, Monitor, ArrowRight, ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'
import request from '@/utils/request'
import config from '@/config'

const testing = ref(false)
const testResult = ref(null)
const healthLoading = ref(false)
const healthData = ref(null)

// 模型列表
const llmModelsLoading = ref(false)
const embedModelsLoading = ref(false)
const llmModelOptions = ref([])
const embedModelOptions = ref([])
const switchingLLM = ref(false)
const switchingEmbedding = ref(false)

// 表单初始值（全部为空，由 Redis API 填充）
const configForm = reactive({
  apiKey: '',
  baseUrl: '',
  model: '',
  temperature: 0.7,
  maxTokens: 4096
})

const embeddingForm = reactive({
  baseUrl: '',
  apiKey: '',
  model: '',
  topK: 5
})

// 测试连接（调用Agent端，同时测试LLM和嵌入模型）
const testConnection = async () => {
  testing.value = true
  testResult.value = null
  try {
    const agentBase = config.agentBaseURL || '/agent'
    const payload = {
      llm_model: configForm.model || undefined,
      llm_base_url: configForm.baseUrl || undefined,
      embedding_model: embeddingForm.model || undefined,
      embedding_base_url: embeddingForm.baseUrl || undefined,
    }
    const res = await axios.post(`${agentBase}/config/test-connection`, payload, { timeout: 20000 })
    const data = res.data || {}
    const llm = data.llm || {}
    const emb = data.embedding || {}
    testResult.value = {
      success: data.success,
      message: `LLM: ${llm.message || '未知'} | 嵌入模型: ${emb.message || '未知'}`,
      llm: llm,
      embedding: emb
    }

    // 测试通过后自动应用当前表单配置（含 temperature、max_tokens、top_k）
    // 独立判断：LLM 通过就切换 LLM，嵌入通过就切换嵌入，不互相阻塞
    if (llm.success) {
      switchLLM().catch(() => {})
    }
    if (emb.success) {
      switchEmbedding().catch(() => {})
    }
  } catch (e) {
    testResult.value = {
      success: false,
      message: e.response?.data?.detail || e.message || '连接失败，请检查 Agent 服务是否启动',
      llm: { success: false, message: 'Agent服务未启动' },
      embedding: { success: false, message: 'Agent服务未启动' }
    }
  } finally {
    testing.value = false
  }
}

// 获取 Agent 健康状态
const fetchHealth = async () => {
  healthLoading.value = true
  try {
    const agentBase = config.agentBaseURL || '/agent'
    const res = await axios.get(`${agentBase}/health`)
    healthData.value = res.data
  } catch {
    healthData.value = null
  } finally {
    healthLoading.value = false
  }
}

// 从 Agent 端获取所有配置：当前模型 + Ollama 上实际安装的模型列表
const fetchConfig = async () => {
  llmModelsLoading.value = true
  embedModelsLoading.value = true
  try {
    const agentBase = config.agentBaseURL || '/agent'
    const res = await axios.get(`${agentBase}/config`)
    const data = res.data || {}
    console.log('[fetchConfig] Agent返回:', JSON.stringify(data.llm), JSON.stringify(data.embedding))

    // 当前 LLM 配置
    configForm.model = data.llm?.model || ''
    configForm.baseUrl = data.llm?.base_url || ''
    configForm.temperature = Number(data.llm?.temperature ?? 0.7)
    configForm.maxTokens = Number(data.llm?.max_tokens ?? 4096)

    // 当前 Embedding 配置
    embeddingForm.model = data.embedding?.model || ''
    embeddingForm.baseUrl = data.embedding?.base_url || ''
    embeddingForm.topK = Number(data.embedding?.top_k ?? 5)

    console.log('[fetchConfig] 表单已填充:', {
      temperature: configForm.temperature,
      maxTokens: configForm.maxTokens,
      topK: embeddingForm.topK
    })

    // 可选模型列表：从 Ollama /api/tags 获取
    const allModels = data.all_models || []
    llmModelOptions.value = (data.available_llm_models || []).map(name => {
      const m = allModels.find(x => x.name === name)
      return { name, label: m ? `${name} (${formatModelSize(m.size)})` : name }
    })
    embedModelOptions.value = (data.available_embed_models || []).map(name => {
      const m = allModels.find(x => x.name === name)
      return { name, label: m ? `${name} (${formatModelSize(m.size)})` : name }
    })

    return true
  } catch (e) {
    console.warn('获取Agent配置失败:', e.message)
    return false
  } finally {
    llmModelsLoading.value = false
    embedModelsLoading.value = false
  }
}

const formatModelSize = (bytes) => {
  if (!bytes) return ''
  if (bytes > 1e9) return (bytes / 1e9).toFixed(1) + ' GB'
  if (bytes > 1e6) return (bytes / 1e6).toFixed(1) + ' MB'
  return (bytes / 1e3).toFixed(0) + ' KB'
}

// 热切换 LLM 模型（调用 Agent 端接口，切换即时生效）
const switchLLM = async () => {
  if (!configForm.model) {
    ElMessage.warning('请选择或输入模型名称')
    return
  }
  switchingLLM.value = true
  try {
    const agentBase = config.agentBaseURL || '/agent'
    const payload = {
      model: configForm.model,
      base_url: configForm.baseUrl || undefined,
      temperature: configForm.temperature != null ? Number(configForm.temperature) : undefined,
      max_tokens: configForm.maxTokens != null ? Number(configForm.maxTokens) : undefined,
    }
    console.log('[switchLLM] 发送:', JSON.stringify(payload))
    await axios.post(`${agentBase}/config/switch-llm`, payload)
    ElMessage.success(`LLM 模型已切换为: ${configForm.model}`)
  } catch (e) {
    ElMessage.error('切换失败: ' + (e.response?.data?.detail || e.message))
  } finally {
    switchingLLM.value = false
  }
}

// 热切换 Embedding 模型（调用 Agent 端接口，切换即时生效）
const switchEmbedding = async () => {
  if (!embeddingForm.model) {
    ElMessage.warning('请选择或输入嵌入模型名称')
    return
  }
  switchingEmbedding.value = true
  try {
    const agentBase = config.agentBaseURL || '/agent'
    const payload = {
      model: embeddingForm.model,
      base_url: embeddingForm.baseUrl || undefined,
      top_k: embeddingForm.topK != null ? Number(embeddingForm.topK) : undefined,
    }
    console.log('[switchEmbedding] 发送:', JSON.stringify(payload))
    await axios.post(`${agentBase}/config/switch-embedding`, payload)
    ElMessage.success(`Embedding 模型已切换为: ${embeddingForm.model}`)
  } catch (e) {
    ElMessage.error('切换失败: ' + (e.response?.data?.detail || e.message))
  } finally {
    switchingEmbedding.value = false
  }
}

// ═════════════ 知识库管理 ═════════════

const knowledgeLoading = ref(false)
const knowledgeData = ref(null)
const uploadDialogVisible = ref(false)
const uploading = ref(false)
const uploadRef = ref(null)
const folderInputRef = ref(null)
const uploadForm = reactive({
  category: '鲜花养护',
  mode: 'file',
  file: null,
  folderFiles: []
})

const onFileChange = (uploadFile) => {
  uploadForm.file = uploadFile
}

const onFileRemove = () => {
  uploadForm.file = null
}

const onFolderSelect = (event) => {
  const allFiles = Array.from(event.target.files)
  // 只保留 txt 和 md 文件
  uploadForm.folderFiles = allFiles.filter(
    f => f.name.toLowerCase().endsWith('.txt') || f.name.toLowerCase().endsWith('.md')
  )
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  return (bytes / 1024).toFixed(1) + ' KB'
}

const _agentBase = () => config.agentBaseURL || 'http://localhost:8000'

const fetchKnowledge = async () => {
  knowledgeLoading.value = true
  try {
    const res = await axios.get(`${_agentBase()}/knowledge/documents`)
    if (res.data?.documents) {
      // 后端返回 { documents: [...], total: N }，按分类分组
      const grouped = {}
      for (const doc of res.data.documents) {
        const cat = doc.category || '未分类'
        if (!grouped[cat]) grouped[cat] = { category: cat, files: [], file_count: 0, _collapsed: false }
        grouped[cat].files.push({ filename: doc.filename, size_bytes: doc.size || 0 })
        grouped[cat].file_count++
      }
      knowledgeData.value = Object.values(grouped)
    }
  } catch {
    knowledgeData.value = null
  } finally {
    knowledgeLoading.value = false
  }
}

const showUploadDialog = () => {
  uploadForm.category = '鲜花养护'
  uploadForm.mode = 'file'
  uploadForm.file = null
  uploadForm.folderFiles = []
  uploadDialogVisible.value = true
}

const uploadDoc = async () => {
  if (uploadForm.mode === 'file') {
    // 单文件上传
    if (!uploadForm.file) {
      ElMessage.warning('请选择要上传的文件')
      return
    }
    uploading.value = true
    try {
      const formData = new FormData()
      formData.append('file', uploadForm.file.raw, uploadForm.file.name)
      formData.append('category', uploadForm.category)
      await axios.post(`${_agentBase()}/knowledge/upload`, formData)
      ElMessage.success('文档上传成功')
      uploadDialogVisible.value = false
      fetchKnowledge()
    } catch (e) {
      ElMessage.error('上传失败: ' + (e.response?.data?.detail || e.message))
    } finally {
      uploading.value = false
    }
  } else {
    // 文件夹批量上传
    if (!uploadForm.folderFiles.length) {
      ElMessage.warning('请选择包含 txt/md 文件的文件夹')
      return
    }
    uploading.value = true
    let successCount = 0
    let failCount = 0
    try {
      for (const f of uploadForm.folderFiles) {
        try {
          const formData = new FormData()
          formData.append('file', f, f.name)
          formData.append('category', uploadForm.category)
          const res = await axios.post(`${_agentBase()}/knowledge/upload`, formData)
          if (res.data?.success) successCount++
          else failCount++
        } catch {
          failCount++
        }
      }
      ElMessage.success(`批量上传完成: 成功 ${successCount} 个${failCount ? '，失败 ' + failCount + ' 个' : ''}`)
      uploadDialogVisible.value = false
      fetchKnowledge()
    } catch (e) {
      ElMessage.error('上传失败: ' + e.message)
    } finally {
      uploading.value = false
    }
  }
}

const deleteDoc = async (category, filename) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${category}/${filename}？`, '确认', { type: 'warning' })
  } catch { return }
  try {
    await axios.delete(`${_agentBase()}/knowledge/documents/${encodeURIComponent(category)}/${encodeURIComponent(filename)}`)
    ElMessage.success('文档已删除')
    fetchKnowledge()
  } catch (e) {
    ElMessage.error('删除失败: ' + (e.response?.data?.detail || e.message))
  }
}

onMounted(() => {
  fetchHealth()
  fetchConfig()
  fetchKnowledge()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h3 {
  font-size: 22px;
  font-weight: 700;
  color: #2D2320;
  margin: 0 0 4px;
}

.page-desc {
  font-size: 13px;
  color: #9B8B85;
  margin: 0;
}

.config-card {
  border-radius: 12px;
  border: 1px solid #EDE4DD;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
  color: #2D2320;
}

.form-tip {
  font-size: 12px;
  color: #9B8B85;
  margin-top: 4px;
}

.test-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 文件夹上传 */
.folder-upload-area {
  width: 100%;
}
.folder-file-list {
  margin-top: 8px;
}
.folder-files-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}
.folder-file-tag {
  font-size: 12px;
  color: #5D4A3A;
  background: #FBF9F7;
  border: 1px solid #EDE4DD;
  border-radius: 4px;
  padding: 2px 8px;
}
.folder-file-tag.more {
  color: #9B8B85;
  font-style: italic;
}

.test-result {
  max-width: 600px;
}

/* 知识库管理 */
.knowledge-categories {
  display: flex; flex-direction: column; gap: 16px;
}
.knowledge-category {
  border: 1px solid #EDE4DD; border-radius: 8px; padding: 12px;
}
.category-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;
}
.category-name { font-size: 14px; font-weight: 600; color: #5D4A3A; }
.category-files { display: flex; flex-direction: column; gap: 6px; }
.file-item {
  display: flex; align-items: center; gap: 8px; padding: 6px 8px;
  background: #FBF9F7; border-radius: 6px;
}
.file-name { flex: 1; font-size: 13px; color: #2D2320; }
.file-size { font-size: 11px; color: #9B8B85; }

:deep(.el-radio-group) {
  display: flex;
  gap: 12px;
}

:deep(.el-radio-button__inner) {
  border-radius: 8px !important;
  border: 1px solid #EDE4DD !important;
  padding: 12px 20px !important;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #8B4A50, #A06068) !important;
  border-color: #8B4A50 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(139,74,80,0.3);
}
</style>
