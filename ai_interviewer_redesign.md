# AI 面试官系统方案设计文档

## 一、系统整体目标与核心能力

### 1.1 整体目标
构建一个高可用、高并发、智能化的 AI 面试官系统，旨在通过人工智能技术实现对候选人的初步筛选和能力评估。系统应具备模拟真实面试场景的能力，能够进行多轮技术问答、深度追问、代码考察及行为分析，并生成客观、可解释的面试报告，从而降低企业招聘成本，提高招聘效率。

### 1.2 核心能力
*   **全双工语音交互**：支持毫秒级延迟的语音识别（ASR）与语音合成（TTS），提供接近真人的对话体验。
*   **深度 NLP 面试问答**：基于大模型（LLM）的动态问题生成，支持根据候选人简历和实时回答进行个性化提问与追问。
*   **多维评分体系**：结合规则引擎与大模型评估，对候选人的知识广度、深度、逻辑思维、表达能力及代码能力进行量化评分。
*   **多模态情绪检测**：通过音频声学特征（语调、语速）和视频面部表情分析候选人的紧张程度、自信度及情绪变化。
*   **多轮对话管理**：具备上下文记忆能力，能够引导面试流程，处理打断、插话及话题切换。

---

## 二、系统整体架构

### 2.1 整体架构图（逻辑视图）

系统采用微服务架构，分为接入层、网关层、核心业务层、AI 引擎层及数据支撑层。

1.  **面试客户端 (Client Layer)**
    *   **H5 / Web**: 基于 WebRTC 的浏览器端，支持 PC 及移动端访问。
    *   **小程序**: 微信/钉钉小程序，通过 WebSocket + RecorderManager 实现音频流传输。
    *   **App**: Native App，集成高性能音视频 SDK。

2.  **面试网关 (Gateway Layer)**
    *   **API Gateway**: 负责 RESTful 接口的路由、鉴权、限流（Nginx / Kong / Spring Cloud Gateway）。
    *   **WebSocket Gateway**: 负责长连接维护、信令交换及实时音频流转发（Netty / Go-Zero）。

3.  **面试控制服务 (Control Plane)**
    *   **会话管理器 (Session Manager)**: 维护面试状态机（准备、进行中、暂停、结束），管理上下文。
    *   **调度中心 (Dispatcher)**: 协调 ASR、LLM、TTS 及评分服务的调用时序。

4.  **AI 引擎层 (AI Engine Plane)**
    *   **ASR 服务**: 实时语音转文本（FunASR / Whisper）。
    *   **NLP 面试策略服务**: 提示词工程、对话管理、追问逻辑。
    *   **LLM 推理服务**: 基于 **Spring AI** 框架统一封装大模型接口，屏蔽底层模型差异（DeepSeek, OpenAI, Ollama）。
    *   **TTS 服务**: 文本转语音，支持流式输出及情感控制（CosyVoice / VITS）。
    *   **情绪分析服务**: 音视频流分析。

5.  **面试评分引擎 (Scoring Plane)**
    *   **实时评分**: 对单轮回答进行即时反馈。
    *   **综合评分**: 面试结束后生成总评报告。

6.  **数据服务 (Data Plane)**
    *   **PostgreSQL**: 核心业务库，存储用户、面试记录。
    *   **pgvector**: 基于 PostgreSQL 的向量扩展，通过 **Spring AI Vector Store** 进行操作，实现 RAG 检索。
    *   **Redis**: 缓存会话状态、热点题库、Token 桶。
    *   **OSS**: MinIO (自建) / 阿里云 OSS，存储面试录音、录像文件。

---

## 三、面试策略与评分引擎

### 3.1 面试策略引擎

#### 3.1.1 NLP 面试问题生成策略
*   **简历驱动**: 解析候选人简历（PDF/Word -> Text），提取关键词（技术栈、项目经历），结合岗位 JD 生成初始问题集。
*   **动态生成**: 根据上一轮回答的关键词和完整度，动态生成下一题。例如，候选人提到 "Redis 分布式锁"，下一题自动生成 "Redis 锁的死锁与续期问题"。

#### 3.1.2 深度追问算法
*   **STAR 原则引导**: 检测回答是否包含 Situation, Task, Action, Result。若缺失，则追问细节（e.g., "在这个项目中，你具体承担了什么职责？"）。
*   **深度探测**: 基于知识图谱或预设深度层级（L1-L5）。若候选人回答浅显（L1），则追问原理（L2/L3）；若回答深入，则通过或转向应用场景。

#### 3.1.3 视频/音频情绪检测
*   **音频**: 提取 MFCC 特征，分析语速、停顿、音调抖动，判断紧张/自信。
*   **视频**: 关键帧人脸检测，分析微表情（皱眉、眼神游离），判断专注度/欺骗倾向。

#### 3.1.4 行为分析
*   **表达逻辑**: 分析句法结构复杂度，连接词使用（因为...所以...，首先...其次...）。
*   **冗余检测**: 统计重复词频、无效语气词（嗯、啊、那个），判断是否啰嗦。
*   **跑题检测**: 计算回答内容与问题意图的语义相似度（Cosine Similarity），低于阈值则提示或打断。

### 3.2 评分引擎（生产级）

#### 3.2.1 评分指标体系
| 维度 | 权重 | 细分指标 |
| :--- | :--- | :--- |
| **专业知识** | 40% | 深度、广度、准确性、技术栈匹配度 |
| **逻辑思维** | 20% | 结构化表达、因果推理、问题拆解能力 |
| **工程能力** | 20% | 代码规范、系统设计、异常处理、性能优化意识 |
| **沟通表达** | 10% | 清晰度、流畅度、重点突出 |
| **综合素质** | 10% | 学习能力、抗压能力、热情度 |

#### 3.2.2 打分方法
采用 **"DeepSeek CoT 评分 + 规则修正"** 的混合模式：
1.  **LLM CoT (Chain of Thought)**: 输入问题、标准答案（参考）、候选人回答。Prompt 要求 DeepSeek 先分析优缺点，再给出分数（0-100）。
2.  **规则修正**:
    *   关键词命中率：核心关键词缺失扣分。
    *   反作弊扣分：检测到切屏、长时间沉默、声音不匹配等强制扣分。
3.  **多维加权**: 最终得分 = $\sum (维度分 \times 权重)$。

#### 3.2.3 可解释性输出
*   **优点**: "对 Redis 底层数据结构理解深刻，能准确描述跳表的实现。"
*   **缺点**: "在分布式事务方案中，未能清晰阐述 TCC 的异常处理机制。"
*   **建议**: "建议加强对高并发场景下数据一致性方案的学习。"

---

## 四、大模型服务设计

### 4.1 提示词工程 (Prompt Engineering)
*   **结构化封装**: 使用 **Spring AI PromptTemplate** 进行管理，支持参数替换和模板复用。
    ```java
    PromptTemplate promptTemplate = new PromptTemplate(interviewPromptResource);
    Prompt prompt = promptTemplate.create(Map.of("role", "Java面试官", "context", "..."));
    ```

### 4.2 缓存策略
*   **Context Cache**: 利用 Redis 或 **Spring AI ChatMemory** (Advisor) 组件，缓存最近 N 轮对话，避免 Token 溢出，同时保持对话连贯性。
*   **KV Cache**: 在推理侧（如 vLLM）启用 KV Cache 复用，加速首字生成时间（TTFT）。

### 4.3 模型选择与编排
*   **ASR 模型**: FunASR (Paraformer-Large-Streaming) - 高精度，支持流式。
*   **NLU/LLM**:
    *   **框架集成**: 使用 **Spring AI OpenAI** Starter 连接 DeepSeek (DeepSeek 兼容 OpenAI API)。
    *   **主模型**: **DeepSeek-V3 / DeepSeek-R1** (擅长代码生成、逻辑推理，性价比极高)。
    *   **辅模型**: Qwen-2.5-72B (自建部署，作为备用或处理特定领域知识)。
*   **TTS 模型**: CosyVoice / Edge-TTS (低成本方案)。
*   **编排**: 使用 **Spring AI** 的流式接口 (`Flux<ChatResponse>`) 结合 Reactor 进行异步编排：ASR -> 意图识别 -> (如果是闲聊 -> 小模型) / (如果是技术题 -> DeepSeek) -> TTS。

### 4.4 监控与日志
*   **ELK Stack**: 收集全链路日志（ASR 耗时、LLM Token 消耗、API 延迟）。
*   **Prometheus + Grafana**: 监控实时并发数、GPU 利用率、错误率。

---

## 五、技术方案

### 5.1 ASR 方案（语音转文本）
*   **选型**: **FunASR** (阿里开源)。
*   **理由**: 工业级开源，支持流式识别（Streaming），支持 VAD（语音活动检测），中文效果极佳。
*   **流式方案**:
    *   客户端通过 WebSocket 发送 PCM/Opus 音频帧。
    *   服务端 VAD 检测静音截断，送入 ASR 引擎。
    *   **延迟要求**: < 300ms。

### 5.2 TTS 方案（语音合成）
*   **选型**: **CosyVoice** 或 **VITS**。
*   **特性**: 支持情感标签（`<emotion type="serious">`），支持流式合成（LLM 生成一段文本，TTS 立即合成一段音频）。
*   **并发优化**: 预生成常用语（"请做个自我介绍"、"好的，下一题"）缓存为静态文件，减少实时推理压力。

### 5.3 WebRTC / WebSocket 通信设计
*   **信令通道 (WebSocket)**: 交换 SDP、Candidate、控制指令（开始、暂停、题目文本推送）。
*   **流媒体链路 (WebRTC)**:
    *   **浏览器 -> 面试官 (AI)**: 推送音频流（AudioTrack）和视频流（VideoTrack）。
    *   **AI -> 浏览器**: 推送 TTS 生成的音频流。
    *   **架构**: 使用 SFU (Selective Forwarding Unit) 架构（如 **Mediasoup** 或 **ZLMediaKit**），便于服务端录制和分析音视频流。
*   **抗弱网**: 开启 NACK (丢包重传), FEC (前向纠错), BWE (带宽估计)。

---

## 六、详细接口设计

### 6.1 REST API 示例

#### 6.1.1 开始面试
*   **URL**: `POST /api/interview/start`
*   **Request (JSON)**:
    ```json
    {
      "userId": "12345",
      "jobId": "67890",
      "resumeId": "resume_001",
      "config": {
        "mode": "voice", // voice, video, text
        "difficulty": "hard"
      }
    }
    ```
*   **Response**:
    ```json
    {
      "code": 200,
      "data": {
        "interviewId": "int_99999",
        "wsUrl": "wss://api.interview.com/ws/int_99999",
        "token": "eyJhbGciOi..."
      }
    }
    ```

#### 6.1.2 获取下一题 (Fallback / Manual)
*   **URL**: `POST /api/interview/question/next`
*   **Request**: `{"interviewId": "int_99999", "lastAnswer": "..."}`
*   **Response**:
    ```json
    {
      "code": 200,
      "data": {
        "questionId": "q_101",
        "text": "请解释一下 Spring Bean 的生命周期。",
        "type": "technical"
      }
    }
    ```

#### 6.1.3 提交评分
*   **URL**: `POST /api/interview/score`
*   **Request**: `{"interviewId": "int_99999"}` (触发异步结算)
*   **Response**: `{"code": 200, "message": "Scoring started"}`

### 6.2 WebSocket 协议示例

#### 6.2.1 消息格式
所有 WebSocket 消息遵循统一包结构：
```json
{
  "type": "event_type",
  "payload": { ... },
  "timestamp": 1700000000
}
```

#### 6.2.2 事件类型定义

| 方向 | Type | 说明 | Payload 示例 |
| :--- | :--- | :--- | :--- |
| C -> S | `JOIN` | 加入房间 | `{"token": "..."}` |
| C -> S | `AUDIO_CHUNK` | 音频数据包 | `{"data": "base64...", "seq": 1}` (若不用 WebRTC) |
| C -> S | `VAD_END` | 说话结束 | `{}` |
| S -> C | `QUESTION` | 推送问题文本 | `{"text": "请自我介绍", "qId": "1"}` |
| S -> C | `ASR_RESULT` | 实时转录结果 | `{"text": "我熟练掌握...", "isFinal": false}` |
| S -> C | `SCORE_UPDATE` | 实时评分反馈 | `{"score": 85, "comment": "回答流畅"}` |

---

## 七、数据库设计

### 7.1 用户表 (`users`)
| 字段 | 类型 | 索引 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK | 用户ID |
| username | VARCHAR(64) | | 用户名 |
| email | VARCHAR(128) | UK | 邮箱 |
| resume_url | VARCHAR(255)| | 默认简历地址 |

### 7.2 面试记录表 (`interviews`)
| 字段 | 类型 | 索引 | 说明 |
| :--- | :--- | :--- | :--- |
| id | UUID | PK | 面试ID (UUID) |
| user_id | BIGINT | IDX | 候选人ID |
| job_id | BIGINT | IDX | 岗位ID |
| start_time | TIMESTAMPTZ | | 开始时间 |
| end_time | TIMESTAMPTZ | | 结束时间 |
| status | SMALLINT | IDX | 0:准备, 1:进行中, 2:完成, 3:异常 |
| total_score | DECIMAL(5,2)| | 总分 |
| summary | TEXT | | 面试总结 |

### 7.3 面试问题表 (`questions`)
| 字段 | 类型 | 索引 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK | 问题ID |
| content | TEXT | | 问题内容 |
| embedding | VECTOR(1536)| | 问题向量 (pgvector) |
| category | VARCHAR(32) | IDX | 分类 (Java, Go, SystemDesign) |
| difficulty | SMALLINT | | 难度 (1-5) |
| standard_answer | TEXT | | 参考答案 |

### 7.4 问答日志表 (`interview_logs`)
| 字段 | 类型 | 索引 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK | 日志ID |
| interview_id | UUID | IDX | 关联面试ID |
| round | INT | | 轮次 (第几问) |
| question_text | TEXT | | 当时的问题 |
| answer_audio_url| VARCHAR(255)| | 回答录音地址 |
| answer_text | TEXT | | ASR转录文本 |
| score | DECIMAL(5,2)| | 单题得分 |
| evaluation | TEXT | | 单题评价 |

### 7.5 评分结果表 (`scores`)
| 字段 | 类型 | 索引 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK | |
| interview_id | UUID | IDX | |
| dimension | VARCHAR(32) | | 维度 (Knowledge, Logic...) |
| score | DECIMAL(5,2)| | 分数 |
| comment | TEXT | | 维度评语 |

---

## 八、扩展性与高可用设计

### 8.1 高可用 (High Availability)
*   **负载均衡**: Nginx 轮询转发 API 请求；WebSocket 集群使用 Hash Ring (一致性哈希) 保持会话粘性。
*   **熔断降级**: 当 LLM 服务超时 (>5s)，自动降级为本地规则库出题，或使用预生成的通用兜底话术。
*   **限流**: 针对 API Key 进行 Rate Limiting (Token Bucket 算法)，防止恶意刷量。

### 8.2 安全设计
*   **鉴权**: JWT (JSON Web Token) 双向验证。
*   **加密**: 全链路 HTTPS / WSS 加密；敏感数据 (手机号、简历) AES-256 加密存储。
*   **审计**: 记录所有 API 调用日志及后台操作日志。

### 8.3 扩展性
*   **多岗位支持**: 通过配置中心 (Nacos) 动态加载不同岗位的 Prompt 模板和题库。
*   **多语言**: 集成多语言 ASR/TTS 模型，Prompt 增加 `<language>en</language>` 约束。
*   **AI 简历解析**: 集成 OCR 及 NLP 实体抽取 (NER) 技术，自动提取技能树，生成知识图谱。
