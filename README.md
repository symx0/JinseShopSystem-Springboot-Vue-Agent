# 锦色花店 (JinSe FlowerShop)

全栈鲜花电商平台，集成 AI 智能导购 Agent，支持从选购推荐到下单支付的全流程自动化。

***

<img width="1275" height="676" alt="Snipaste_2026-06-15_20-57-51" src="https://github.com/user-attachments/assets/3614db15-7a2d-4273-ad51-f9f5f7b63817" />


<img width="1275" height="676" alt="Snipaste_2026-06-15_20-53-37" src="https://github.com/user-attachments/assets/182d5ccd-03e6-4eb5-80cf-1117ffc47c38" />






## 项目架构

```
FlowerShop/
├── JinSe-FlowerShop-MangerSystem/   # Spring Boot 后端（Maven 多模块）
│   ├── jinse-pojo/                  # 实体类、DTO、VO
│   ├── jinse-common/                # 公共工具、异常、常量
│   └── jinse-server/                # 主应用（控制器、服务层、数据访问层）
├── jinse-flowershop-serviceweb/     # 管理端前端（Vue 3 + Element Plus）—— 端口 3000
├── jinse-flowershop-clientweb/      # 用户端前端（Vue 3 + Element Plus）—— 端口 3001
├── FlowerShopAgentSystem/
│   ├── FlowerShopAgent/             # AI 智能导购（Python FastAPI + LangGraph）—— 端口 8000
│   └── FlowerShopMCPService/        # MCP 工具服务（数据库查询 + RAG 知识检索）
└── image-resource/                  # 静态图片资源
```

### 系统交互流程

```
用户浏览器（端口 3001）
    │
    ├── 普通电商请求 → Nginx/Vite代理 → Spring Boot（端口 8080）→ MySQL + Redis
    │
    └── AI导购请求 → Vite代理 → FastAPI Agent（端口 8000）
                        │
                        ├── LangGraph 工作流编排
                        ├── Ollama LLM（LLM + 嵌入模型）
                        └── MCP Server（工具调用：数据库查询、RAG检索）
                              └── MySQL + ChromaDB（向量知识库）
```

***

## 技术栈

### 后端 (Spring Boot)

| 技术          | 版本     | 用途      |
| ----------- | ------ | ------- |
| Java        | 8+     | 开发语言    |
| Spring Boot | 2.7.3  | 主框架     |
| MyBatis     | 2.2.0  | ORM     |
| MySQL       | 8.0    | 主数据库    |
| Redis       | 6.x    | 缓存与会话管理 |
| Alipay SDK  | 4.38.0 | 支付宝沙箱支付 |

### AI Agent (Python FastAPI)

| 技术                           | 用途                                                  |
| ---------------------------- | --------------------------------------------------- |
| FastAPI                      | HTTP 服务框架                                           |
| LangGraph                    | Agent 工作流编排                                         |
| LangChain                    | LLM 调用封装                                            |
| Ollama                       | 本地 LLM (qwen3-coder:30b) 和嵌入模型 (qwen3-embedding:8b) |
| MCP (Model Context Protocol) | Agent 工具调用协议                                        |
| BGE Reranker (FlagEmbedding) | RAG 检索结果重排序                                         |
| ChromaDB                     | 向量知识库存储                                             |

### 前端 (Vue 3)

| 技术                      | 用途   |
| ----------------------- | ---- |
| Vue 3 (Composition API) | 前端框架 |

***

## 快速启动

### 前置依赖

- **MySQL 8.0** — 导入 `JinSe-FlowerShop-MangerSystem/jinse_flowershop.sql`
- **Redis** — 默认 `localhost:6379`
- **Ollama** — 部署 LLM 和嵌入模型（如无 GPU 可使用 CPU 模式）
- **Python 3.10+** (推荐 Conda 环境)
- **Maven 3.8+**
- **Node.js 18+**

### 1. 数据库初始化

```bash
# 创建数据库并导入
mysql -u root -p < JinSe-FlowerShop-MangerSystem/jinse_flowershop.sql
```

### 2. 启动 Spring Boot 后端（端口 8080）

```bash
cd JinSe-FlowerShop-MangerSystem/jinse-server

# 修改 src/main/resources/application-dev.yml 中的数据库和 Redis 连接信息
# 确保以下配置正确：
#   sky.datasource.host / port / database / username / password
#   sky.redis.host / port / password

# 构建并启动
mvn clean package -DskipTests
mvn spring-boot:run
```

### 3. 启动 MCP 工具服务

```bash
cd FlowerShopAgentSystem/FlowerShopMCPService

# 安装依赖
pip install -r requirements.txt

# 修改 config.yaml 中的 MySQL 连接信息和知识库路径
# 启动（stdio 模式，由 Agent 自动拉起）
python server.py

# 或 SSE 模式（调试用）
python server.py --sse
```

### 4. 启动 AI 智能导购 Agent（端口 8000）

```bash
cd FlowerShopAgentSystem/FlowerShopAgent

# 安装依赖
pip install -r requirements.txt

# 配置
cp .env.example .env          # 基本环境变量
# 编辑 config.yaml 设置 Ollama 地址和模型选择

# 启动
python -m app.main
# 服务地址: http://localhost:8000
```

> **模型热切换**: Agent 支持运行时通过管理端前端（AI配置页面）动态切换 LLM 和嵌入模型，无需重启服务。

### 5. 启动管理端前端（端口 3000）

```bash
cd jinse-flowershop-serviceweb

npm install
npm run dev
# 访问: http://localhost:3000
# 默认管理员账号: admin / admin
```

### 6. 启动用户端前端（端口 3001）

```bash
cd jinse-flowershop-clientweb

npm install
npm run dev
# 访问: http://localhost:3001
# 默认用户账号: 9527 / 10086
```

***

## 项目功能

### 用户端 (Client Web)

| 模块      | 功能                          |
| ------- | --------------------------- |
| 首页      | 鲜花展示、分类浏览、促销活动              |
| AI 智能导购 | 多轮对话式鲜花推荐、自动构建订单、一键下单       |
| 鲜花列表    | 按分类筛选、搜索、分页浏览               |
| 活动专区    | 查看进行中的促销活动及活动商品             |
| 购物车     | 添加/删除商品、修改数量                |
| 订单管理    | 查看订单、付款（支付宝/模拟支付）、确认收货、退货申请 |
| 用户中心    | 个人信息、收货地址管理、密码修改            |

### 管理端 (Admin Web)

| 模块    | 功能                          |
| ----- | --------------------------- |
| 仪表盘   | 数据概览、销售统计                   |
| AI 配置 | Agent 服务状态监控、LLM 模型热切换、参数调整 |
| 鲜花管理  | 商品 CRUD、上架/下架、活动商品筛选        |
| 分类管理  | 鲜花分类维护                      |
| 活动管理  | 促销活动创建、活动商品配置、限购库存管理        |
| 订单管理  | 订单查看、接单、派送、完成、退款处理          |
| 员工管理  | 员工账号管理                      |
| 用户管理  | 注册用户管理                      |
| 评论管理  | 用户评论审核                      |
| 报表统计  | ECharts 数据可视化（营业额、订单量、销量排行） |
| 店铺设置  | 营业状态、支付方式、配送费用配置            |
| 支付宝配置 | 沙箱环境参数配置                    |

### AI 智能导购 (Agent)

| 功能    | 说明                             |
| ----- | ------------------------------ |
| 意图识别  | 自动区分闲聊、知识问答、购物推荐三类意图           |
| 需求分析  | 提取用户的花色、预算、场景、收花人等需求           |
| 知识检索  | RAG 向量检索 + BGE 重排序，精准匹配鲜花和养护知识 |
| 商品推荐  | 结合数据库实时促销信息，推荐最优方案             |
| 订单构建  | 对话中自动生成订单，支持查看/修改/删除商品         |
| 幻觉检测  | 所有回复自动校验商品真实性和可购买性，清除敏感 ID 信息  |
| 模型热切换 | 运行时动态切换 LLM 和嵌入模型，无需重启         |
| 会话持久化 | 支持多会话管理，历史消息回溯                 |

### Agent 工作流 (LangGraph)

```
START → classify（意图分类）
          ├→ chitchat → hallucination_check → respond → summarize → END
          ├→ knowledge → hallucination_check → respond → summarize → END
          └→ shopping → extract_info → query_and_recommend
                          → hallucination_check → respond → summarize → END
```

所有路径均经过 `hallucination_check` 节点进行商品真实性验证和敏感信息清理。

***

## 配置说明

### Agent (FlowerShopAgent)

| 配置文件               | 说明                     |
| ------------------ | ---------------------- |
| `config.yaml`      | LLM、嵌入模型、MCP 服务、服务端口配置 |
| `.env`             | LLM API 密钥等环境变量        |
| `model_state.json` | 运行时模型状态持久化（自动生成）       |

关键配置项：

```yaml
llm:
  model: qwen3-coder:30b        # 大语言模型
  temperature: 0.7              # 生成温度
  max_tokens: 256               # 最大输出 token
embedding:
  model: qwen3-embedding:8b     # 嵌入模型
  top_k: 5                      # 重排序后保留的文档数
mcp_server:
  command: python               # MCP 服务启动命令
  module_path: server.py
```

### 后端 (Spring Boot)

| 配置文件                  | 说明                         |
| --------------------- | -------------------------- |
| `application.yml`     | 主配置                        |
| `application-dev.yml` | 开发环境（数据库、Redis、支付宝沙箱等敏感信息） |

### 前端 (Vue)

| 配置文件                        | 说明                   |
| --------------------------- | -------------------- |
| `src/config.js`             | API 基础路径、Agent 服务地址等 |
| `.env` / `.env.development` | 环境变量                 |

***

## 支付方式

- **模拟支付**: 通过 `/user/order/mock-payment/{orderId}` 接口直接标记已付款，用于测试
- **支付宝沙箱**: RSA2 签名，支持同步跳转 + 异步回调通知
- 支付模式可在管理端「店铺设置」中切换

***

## 设计模式

后端采用以下设计模式：

- **责任链模式**: 订单提交、活动校验、评论处理等业务流程通过责任链校验
- **AOP 切面**: 自动填充创建时间/更新时间/操作人等审计字段

***

## 项目结构速查

```
FlowerShopAgentSystem/FlowerShopAgent/app/
├── main.py                 # FastAPI 入口，路由注册
├── config.py               # 配置读取
├── model_manager.py        # 模型热切换管理
├── llm_factory.py          # LLM/嵌入/重排序工厂
├── mcp_client.py           # MCP 客户端
├── graph/
│   ├── main_graph.py       # 主图编排
│   ├── state.py            # Agent 状态定义
│   └── subgraphs/          # 子图模块
│       ├── classify_intent_subgraph.py  # 意图分类
│       ├── rag_subgraph.py             # RAG 检索
│       ├── recommend_subgraph.py       # 商品推荐
│       └── respond_subgraph.py         # 回复生成
├── memory/
│   └── session_store.py    # 会话持久化
└── models/
    └── schemas.py          # Pydantic 数据模型
```



---
感谢以下开源工作：
https://github.com/cuiyuestar/Blossom-FlowerShop
