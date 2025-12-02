# AI 面试官系统设计方案

## 1. 整体系统架构图

系统采用前后端分离架构，结合实时音视频流处理和即时通讯技术。

### 核心模块描述

1.  **前端交互层 (Frontend Layer)**
    *   **虚拟人驱动模块 (Avatar Driver)**: 负责渲染 3D/2D 虚拟人，接收服务端下发的动作/口型指令 (Viseme) 并实时驱动。
    *   **实时音视频模块 (RTC Client)**: 采集用户麦克风音频，播放 AI 语音，处理回声消除 (AEC) 和降噪 (NS)。
    *   **UI 交互模块**: 展示面试题目、实时字幕、倒计时、结束按钮等。

2.  **网关层 (Gateway Layer)**
    *   **WebSocket Gateway**: 维持长连接，处理实时信令（如 VAD 状态、打断信号、实时字幕）。
    *   **API Gateway**: 处理业务请求（开始面试、获取报告）。

3.  **核心业务服务 (Core Service - Java Spring Boot)**
    *   **面试编排引擎 (Interview Orchestrator)**: 管理面试状态机（开场 -> 提问 -> 等待回答 -> 评估 -> 下一题 -> 结束）。
    *   **对话管理 (Dialogue Manager)**: 维护上下文，调用 LLM 决策下一步动作。
    *   **评分系统 (Scoring System)**: 异步分析用户回答，生成多维度评分。

4.  **AI 能力层 (AI Service Layer)**
    *   **ASR 服务 (Speech-to-Text)**: 将用户语音流实时/流式转为文本。
    *   **LLM 服务 (Brain)**: 负责意图识别、答案评估、追问生成、情感分析。
    *   **TTS 服务 (Text-to-Speech)**: 将 LLM 生成的文本转为语音流。
    *   **Avatar 服务**: 生成口型数据 (Viseme/BlendShapes)。

## 2. 核心业务流程

1.  **初始化**: 用户进入房间 -> 建立 WebSocket 连接 -> 加载虚拟人资源。
2.  **开场**: 虚拟人主动打招呼（TTS 播放 + 动作） -> 介绍面试规则。
3.  **提问循环**:
    *   **AI 提问**: LLM 生成问题 -> TTS 合成语音 + 口型数据 -> 前端播放。
    *   **用户回答**: VAD 检测说话 -> 录音流式上传 -> ASR 转文本 -> 实时显示字幕。
    *   **回答结束**: VAD 检测静音或用户点击“回答完毕”。
    *   **评估与决策**: LLM 分析文本 -> 评分 + 决定（追问/下一题/结束）。
4.  **结束**: 所有问题问完 -> 生成总结语 -> 断开连接。
5.  **报告**: 异步生成详细面试报告（能力雷达图、优缺点、建议）。

## 3. 关键技术选型

| 模块 | 推荐方案 | 理由 |
| :--- | :--- | :--- |
| **虚拟人** | **Live2D** (轻量级) 或 **HeyGen API** (逼真) | Live2D 适合 Web 端低延迟交互；HeyGen 效果好但成本高延迟稍大。开发阶段推荐 **Live2D** 或 **Three.js (Ready Player Me)**。 |
| **TTS** | **Edge-TTS** (免费/开源) 或 **Azure TTS** | Edge-TTS 效果惊艳且免费；Azure 商业级稳定，支持 SSML 表情控制。 |
| **ASR** | **Azure Speech SDK** 或 **OpenAI Whisper** | Azure 支持流式识别，延迟极低；Whisper 准确率高。 |
| **LLM** | **GPT-4o** 或 **Qwen-Turbo (通义千问)** | GPT-4o 逻辑最强；Qwen 性价比高，中文理解极好。 |
| **后端** | **Java Spring Boot + Netty/WebSocket** | 现有架构兼容，高并发处理长连接。 |
| **前端** | **Vue 3 + WebRTC + PixiJS (Live2D)** | 现有技术栈，易于集成。 |

## 4. 接口设计

### REST API

1.  **开始面试**
    *   `POST /api/interview/start`
    *   入参: `{ "userId": "...", "jobId": "..." }`
    *   回参: `{ "interviewId": "...", "wsUrl": "wss://...", "firstQuestion": "..." }`

2.  **提交回答 (非流式兜底)**
    *   `POST /api/interview/{interviewId}/answer`
    *   入参: `MultipartFile audio` (或 text)
    *   回参: `{ "status": "processing" }`

3.  **获取面试报告**
    *   `GET /api/interview/{interviewId}/report`
    *   回参: `{ "score": 85, "dimensions": [...], "summary": "..." }`

### WebSocket 事件 (核心)

*   **Client -> Server**:
    *   `AUDIO_CHUNK`: 二进制音频流
    *   `VAD_START`: 检测到说话
    *   `VAD_END`: 检测到静音
    *   `INTERRUPT`: 用户打断 AI

*   **Server -> Client**:
    *   `TRANSCRIPT`: 实时转写的文本 (ASR 结果)
    *   `AI_RESPONSE_TEXT`: LLM 生成的文本流
    *   `AI_AUDIO_CHUNK`: TTS 音频流
    *   `AVATAR_VISEME`: 口型同步数据 (时间戳 + 嘴型参数)
    *   `INTERVIEW_STATUS`: 状态变更 (THINKING, SPEAKING, LISTENING)

## 5. 面试对话策略逻辑

### 行为树 (Behavior Tree) 设计

*   **Root**
    *   **Sequence: 开场**
        *   Action: 欢迎语
        *   Action: 确认设备正常
    *   **Loop: 面试主循环** (直到问题列表为空)
        *   **Selector: 提问策略**
            *   Condition: 上一题回答太短 -> **Action: 追问/深挖**
            *   Condition: 连续回答错误 -> **Action: 降低难度/提示**
            *   Condition: 正常 -> **Action: 下一题**
        *   Action: 生成问题
        *   Action: 等待回答 (VAD)
        *   Action: 评估回答
    *   **Sequence: 结束**
        *   Action: 感谢语
        *   Action: 生成报告

### 评分模型 (Prompt 示例)

```markdown
你是一位资深技术面试官。请根据候选人的回答进行评估。
问题: {current_question}
回答: {user_answer}

请输出 JSON 格式:
{
  "score": 0-10,
  "dimensions": {
    "logic": 0-10,
    "technical_depth": 0-10,
    "communication": 0-10
  },
  "feedback": "简短评价",
  "follow_up_needed": true/false,
  "follow_up_question": "如果需要追问，请生成问题"
}
```

## 6. 前端交互设计

### 虚拟人驱动方案 (Web)

1.  **Live2D 方案**:
    *   使用 `pixi-live2d-display` 库加载模型。
    *   **口型同步**: TTS 生成音频时，同时请求 Viseme 数据（或根据音频振幅模拟）。
    *   **映射**: 将 Viseme (A, E, I, O, U) 映射到 Live2D 的 `ParamMouthOpenY` 参数。

2.  **UI 布局**:
    *   **背景**: 简洁的办公室或科技感背景。
    *   **中央**: 虚拟人半身像。
    *   **底部**: 
        *   波形图 (Visualizer) 显示当前谁在说话。
        *   控制栏 (麦克风开关、结束面试)。
    *   **右侧/浮层**: 实时字幕 (Subtitle)，半透明显示。

### 关键交互细节

*   **打断机制**: 用户在 AI 说话时如果开始说话（VAD 触发），前端立即停止播放音频，发送 `INTERRUPT` 信号给后端，后端停止 TTS 生成，重新监听。
*   **延迟优化**: 使用流式传输 (Streaming)。LLM 生成一个字 -> TTS 生成一段音频 -> 前端收到立即播放（Buffer 缓冲策略）。
