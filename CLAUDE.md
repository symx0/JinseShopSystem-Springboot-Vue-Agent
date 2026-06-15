# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

锦色花店 (JinSe FlowerShop) — a full-stack flower shop e-commerce platform with an AI-powered shopping guide agent.

## Monorepo Structure

```
FlowerShop/
├── JinSe-FlowerShop-MangerSystem/   # Spring Boot backend (Maven multi-module)
│   ├── jinse-pojo/                  # Entities, DTOs, VOs, Page objects
│   ├── jinse-common/                # Shared utils, framework patterns, exceptions, constants
│   └── jinse-server/                # Main application (controllers, services, mappers, config)
├── jinse-flowershop-serviceweb/     # Admin management frontend (Vue 3 + Element Plus) — port 3000
├── jinse-flowershop-clientweb/      # Customer storefront frontend (Vue 3 + Element Plus) — port 3001
├── FlowerShopAgentSystem/
│   ├── FlowerShopAgent/             # AI shopping guide (Python FastAPI + LangGraph) — port 8000
│   └── FlowerShopMCPService/        # MCP service (skeleton, not yet implemented)
└── image-resource/                  # Static image assets
```

## Backend: JinSe-FlowerShop-MangerSystem

**Stack:** Java, Spring Boot 2.7.3, MyBatis, MySQL (`jinse_flowershop`), Redis, Druid, JWT, Knife4j (Swagger)

### Build & Run

```bash
# Build (from JinSe-FlowerShop-MangerSystem/)
mvn clean package -DskipTests

# Run (from jinse-server/)
mvn spring-boot:run
# Or run the Application.java main class directly in IDE

# Server starts on port 8080
# API docs: http://localhost:8080/doc.html
```

### Module Dependency Chain
`jinse-server` → depends on → `jinse-common` + `jinse-pojo`

### Package Architecture (jinse-server)

| Package | Purpose |
|---------|---------|
| `controller/admin/` | Admin API endpoints (Employee, Flower, Order, Category, Activity, Report, Shop, Comment) |
| `controller/user/` | Customer-facing API (Order, ShoppingCart, AddressBook, User, Category, Flower, Activity, Comment) |
| `service/` + `service/impl/` | Business logic layer |
| `mapper/` | MyBatis mapper interfaces (XML in `resources/mapper/`) |
| `handler/filter/` | Chain-of-responsibility filters for business rule validation |
| `config/` | Spring configuration (WebMvc, Redis, OSS, WebSocket, Alipay) |
| `interceptor/` | JWT interceptors (`JwtTokenAdminInterceptor`, `JwtTokenUserInterceptor`) |
| `aspect/` | AOP aspects (`AutoFillAspect` — auto-fills createTime/updateTime/createUser/updateUser on INSERT/UPDATE) |
| `task/` | Scheduled tasks (`OrderTask`, `ActivityTask`, `WebSocketTask`) |

### Key Design Patterns

1. **Chain of Responsibility** — Filters auto-registered via `AbstractChainHandler` interface. Each handler declares a `mark()` (chain group ID) and an `Ordered` priority. `AbstractChainContext` (a `CommandLineRunner`) collects all beans of type `AbstractChainHandler` at startup and executes them in order when triggered. Used for order submission, activity creation, comment processing, flower queries. See `OrderChainMarkEnum`, `ActivityChainMarkEnum`, etc.

2. **Strategy Pattern** — `AbstractStrategyChoose` collects all `AbstractExecuteStrategy` beans. Strategies are selected by `mark()` (a string key) at runtime via `chooseAndExecute()` / `chooseAndExecuteResp()`.

### API Authentication
- JWT tokens: `admin-secret-key` and `user-secret-key` (both default to `"jinse"`)
- Admin interceptor protects `/admin/**` except login and Swagger resources
- User interceptor protects `/user/**` except login, register, payment callbacks, and public browse endpoints
- Request header: `token` (Bearer-style, though config name is just `token`)

### Configuration
- Active profile: `dev` (set in `application.yml`)
- Sensitive values in `application-dev.yml` (datasource, OSS, Redis, WeChat, Alipay)
- Placeholder pattern: `${sky.*}` properties resolved from `application-dev.yml`
- `allow-circular-references: true` is set — be aware of potential circular DI

### Payment Integration
- Alipay sandbox (RSA2 signing) with synchronous return + async notify callbacks
- Mock payment endpoint (`/user/order/mock-payment/{orderId}`) for testing without Alipay
- Payment status query endpoint for compensating lost async callbacks
- cpolar tunnel maps `localhost:8080` to public URLs for sandbox callbacks

## Frontend: jinse-flowershop-serviceweb (Admin)

**Stack:** Vue 3, Vite, Element Plus, Pinia, Vue Router, ECharts, Axios

```bash
cd jinse-flowershop-serviceweb
npm install
npm run dev          # starts on port 3000
npm run build        # production build
```

Pages: Login, Home (dashboard), Employee, Category, Flower, Order, Comment, Report (ECharts), Activity, Shop settings, Alipay config

## Frontend: jinse-flowershop-clientweb (Customer)

**Stack:** Vue 3, Vite, Element Plus, Pinia, Vue Router, Axios

```bash
cd jinse-flowershop-clientweb
npm install
npm run dev          # starts on port 3001
npm run build        # production build
```

Pages: Home, FlowerList, FlowerDetail, Cart, Checkout, Order, Activity, UserCenter, Login
Includes an `AiChat.vue` component for the AI shopping guide integration.

## Agent System: FlowerShopAgent

**Stack:** Python, FastAPI, LangGraph, LangChain, OpenAI-compatible API

```bash
cd FlowerShopAgentSystem/FlowerShopAgent
pip install -r requirements.txt
cp .env.example .env     # configure LLM_API_KEY, LLM_BASE_URL, LLM_MODEL
python -m app.main        # starts on port 8000
```

### LangGraph Workflow
```
analyze (需求分析) → clarify (追问澄清)
                         ├── need_more_info? → generate (回复追问) → END
                         └── info sufficient → recommend (商品推荐) → generate (回复方案) → END
```

- `analyze_need`: Keyword/scenario matching against knowledge base, with optional LLM refinement
- `clarify_need`: Determines if more user input is needed (low scenario confidence, missing recipient/budget)
- `recommend_products`: Knowledge base candidate retrieval with LLM ranking fallback
- `generate_response`: Formats final reply (natural language if LLM available, template otherwise)

### Knowledge Base
- `data/knowledge/products.json` — Product catalog
- `data/knowledge/scenarios.json` — Purchase scenarios with keywords and tips
- LLM is optional — system falls back to rule-based matching when no API key is configured

### Agent API Endpoints
- `POST /chat` — Main conversation endpoint
- `POST /session/{session_id}/reset` — Reset a session
- `GET /health` — Health check with product/scenario/session counts

## Key Development Notes

- The `.gitignore` excludes `application-dev.yml`, but it IS committed (contains real credentials — be careful)
- `备忘录.txt` is in `.gitignore` (contains secrets) but still exists in the working tree
- The project uses `fastjson` (not Jackson by default), but has a custom `JacksonObjectMapper` registered as the primary MVC message converter
- Backend uses `commons-lang` (version 2.6) in parent POM, but `jinse-server` also depends on `commons-lang3`
- DTO fields are validated through chain filters, not `@Valid`/Bean Validation annotations
