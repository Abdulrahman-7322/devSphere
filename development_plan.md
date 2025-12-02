# AI 面试官系统开发计划 (Development Plan)

基于《AI 面试官系统方案设计文档》，制定以下分阶段开发计划。本项目预计开发周期为 **12 周 (3 个月)**，分为基础设施、核心 AI 服务、业务逻辑、客户端开发、评分系统及测试上线六个阶段。

## 阶段一：基础设施与环境搭建 (Phase 1: Infrastructure)
**周期**: 第 1 周
**目标**: 完成基础中间件搭建，初始化项目脚手架，打通数据库与存储。

### 1.1 中间件部署 (DevOps)
- [ ] **PostgreSQL + pgvector**: 部署 PG 16+，安装 pgvector 插件，创建数据库 `ai_interviewer`。
- [ ] **Redis**: 部署 Redis Cluster 或哨兵模式，配置密码与持久化。
- [ ] **MinIO / OSS**: 搭建 MinIO 服务，创建 `interview-audio`, `interview-video` Bucket，配置访问策略。
- [ ] **Nacos / Config**: (可选) 搭建配置中心，管理多环境配置。

### 1.2 后端脚手架搭建 (Backend)
- [ ] **Spring Boot 初始化**: 创建 Spring Boot 3.x 项目，集成 `spring-boot-starter-web`, `mybatis-plus` / `jpa`。
- [ ] **Spring AI 集成**: 引入 `spring-ai-openai-spring-boot-starter`，配置 DeepSeek API Key 和 Base URL。
- [ ] **数据库设计**: 执行 SQL 脚本，创建 `users`, `interviews`, `questions`, `interview_logs`, `scores` 表。
- [ ] **基础模块**: 实现用户登录 (JWT)、文件上传 (MinIO SDK)、统一异常处理、Swagger/Knife4j 文档。

---

## 阶段二：核心 AI 引擎集成 (Phase 2: AI Core Integration)
**周期**: 第 2-3 周
**目标**: 打通 ASR、LLM、TTS 链路，实现"听得见、想得快、说得出"。

### 2.1 ASR 服务搭建 (Audio Service)
- [ ] **FunASR 部署**: 使用 Docker 部署 FunASR 实时语音转写服务 (Paraformer-Streaming)。
- [ ] **WebSocket Client**: 后端实现 WebSocket 客户端，连接 FunASR 服务，测试音频流发送与文本接收。
- [ ] **VAD 调试**: 调整 VAD (Voice Activity Detection) 阈值，确保静音截断准确。

### 2.2 LLM 与 RAG 实现 (Intelligence Service)
- [ ] **PromptTemplate 封装**: 使用 Spring AI 定义面试官角色、追问逻辑的 Prompt 模板。
- [ ] **Vector Store**: 实现题目向量化 (Embedding)，存入 pgvector。
- [ ] **RAG 检索**: 编写 Service，根据用户回答关键词检索相关技术点或标准答案。
- [ ] **DeepSeek 调试**: 测试 DeepSeek-V3 的流式响应 (`Flux<ChatResponse>`)，确保首字延迟 (TTFT) < 1s。

### 2.3 TTS 服务搭建 (Speech Service)
- [ ] **CosyVoice / Edge-TTS**: 部署 TTS 服务，暴露 HTTP/WS 接口。
- [ ] **流式合成**: 实现 LLM 输出文本流 -> TTS 输入流的管道处理，实现"边想边说"。

---

## 阶段三：面试业务逻辑与信令 (Phase 3: Business Logic)
**周期**: 第 4-6 周
**目标**: 实现完整的面试状态机与实时交互逻辑。

### 3.1 面试状态机 (State Machine)
- [ ] **会话管理**: 设计 `InterviewSession` 类，管理状态 (READY, ASKING, LISTENING, PROCESSING, FINISHED)。
- [ ] **流程控制**: 实现 "开始面试" -> "出题" -> "监听回答" -> "VAD截断" -> "STT" -> "LLM思考" -> "TTS播报" -> "下一题" 的闭环。

### 3.2 WebSocket 信令网关 (Gateway)
- [ ] **信令定义**: 定义 JSON 协议 (`JOIN`, `OFFER`, `ANSWER`, `CANDIDATE`, `AUDIO_FRAME`, `TEXT_PUSH`)。
- [ ] **Netty / Spring WebSocket**: 实现 WebSocket 服务端，处理高并发连接，维护 Session 映射。
- [ ] **音频流转发**: 实现从客户端接收二进制音频流，异步转发给 ASR 服务。

### 3.3 题目生成策略 (Strategy)
- [ ] **简历解析**: 集成 PDF 解析工具，提取技能关键词。
- [ ] **动态出题**: 编写算法，根据当前轮次和历史回答质量，决定是"追问"、"换题"还是"结束"。

---

## 阶段四：客户端开发 (Phase 4: Client Development)
**周期**: 第 7-9 周
**目标**: 开发用户侧界面，实现音视频采集与 WebRTC 通信。

### 4.1 前端框架 (Frontend)
- [ ] **项目初始化**: React / Vue3 + TypeScript + TailwindCSS。
- [ ] **UI 开发**: 登录页、设备检测页 (麦克风/摄像头测试)、面试主界面 (数字人/波形图展示)。

### 4.2 WebRTC 与音频处理 (Media)
- [ ] **WebRTC 接入**: 集成 `socket.io-client` 或原生 WebSocket，实现 SDP 交换。
- [ ] **AudioWorklet**: 使用 AudioWorkletProcessor 采集 PCM 数据，降采样至 16k，通过 WebSocket 发送。
- [ ] **音频播放**: 接收服务端下发的 TTS 音频流 (Blob/ArrayBuffer) 并通过 AudioContext 播放，处理队列缓冲，防止卡顿。

### 4.3 异常处理 (Robustness)
- [ ] **断线重连**: 实现 WebSocket 断开自动重连机制。
- [ ] **网络状态提示**: 监测丢包率和 RTT，弱网环境下提示用户。

---

## 阶段五：评分引擎与报表 (Phase 5: Scoring & Reporting)
**周期**: 第 10 周
**目标**: 实现面试后的自动化评分与报告生成。

### 5.1 异步评分服务 (Scoring)
- [ ] **评分任务调度**: 面试结束后，通过 MQ 触发评分任务。
- [ ] **维度打分**: 遍历 `interview_logs`，调用 DeepSeek 对每道题进行维度打分 (知识、逻辑、表达)。
- [ ] **总评生成**: 汇总各题得分，生成雷达图数据和文字总结。

### 5.2 报表展示 (Dashboard)
- [ ] **面试报告页**: 展示总分、雷达图、详细问答记录、AI 建议。
- [ ] **管理员后台**: 查看所有面试记录，导出 Excel/PDF。

---

## 阶段六：测试与部署 (Phase 6: Testing & Deployment)
**周期**: 第 11-12 周
**目标**: 性能调优，安全加固，正式上线。

### 6.1 性能测试 (Performance)
- [ ] **压力测试**: 使用 JMeter / Python 脚本模拟 100+ 并发 WebSocket 连接，观察 CPU/内存和延迟。
- [ ] **延迟优化**: 优化 ASR -> LLM -> TTS 的全链路耗时，目标端到端延迟 < 1.5s。

### 6.2 安全与监控 (Security & Ops)
- [ ] **鉴权**: 确保所有接口和 WS 连接均需 JWT 验证。
- [ ] **监控**: 配置 Prometheus + Grafana，监控在线人数、API 错误率、Token 消耗。

### 6.3 部署上线 (Release)
- [ ] **CI/CD**: 配置 Jenkins / GitHub Actions 自动化构建镜像。
- [ ] **K8s/Docker Compose**: 编写编排文件，一键部署所有服务。
