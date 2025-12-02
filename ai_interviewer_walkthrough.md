# AI 面试官系统验证指南

本指南将帮助你运行和验证刚刚实现的 AI 面试官功能。

## 1. 后端验证

### 1.1 检查代码
确保以下文件已正确创建/修改：
- `devSphere-chat/src/main/java/com/shutu/devSphere/model/enums/ws/WSReqTypeEnum.java` (新增 `AI_INTERVIEW`)
- `devSphere-chat/src/main/java/com/shutu/devSphere/model/vo/ws/request/AIInterviewReq.java` (新增 DTO)
- `devSphere-chat/src/main/java/com/shutu/devSphere/model/vo/ws/response/AIInterviewResp.java` (新增 DTO)
- `devSphere-chat/src/main/java/com/shutu/devSphere/service/AIInterviewService.java` (新增接口)
- `devSphere-chat/src/main/java/com/shutu/devSphere/service/impl/AIInterviewServiceImpl.java` (新增实现)
- `devSphere-chat/src/main/java/com/shutu/devSphere/websocket/service/serviceImpl/WebSocketServiceImpl.java` (注入 Service)
- `devSphere-chat/src/main/java/com/shutu/devSphere/websocket/Handler/WebSocketServerHandler.java` (路由逻辑)

### 1.2 启动服务
运行 `devSphere-chat` 服务。如果遇到编译错误，请检查 Maven 依赖是否完整（特别是 Hutool 和 Lombok）。

## 2. 前端验证

### 2.1 检查代码
确保以下文件已正确创建/修改：
- `dev-sphere-frontend-main/src/views/interview/AIInterviewer.vue`
- `dev-sphere-frontend-main/src/views/interview/components/Avatar.vue`
- `dev-sphere-frontend-main/src/views/interview/components/AudioVisualizer.vue`
- `dev-sphere-frontend-main/src/router/index.ts` (新增 `/ai-interview` 路由)

### 2.2 安装依赖 (可选)
虽然目前代码使用了简单的 CSS 动画代替 Live2D，但如果你想后续集成 Live2D，请运行：
```bash
npm install pixi.js pixi-live2d-display
```

### 2.3 启动前端
```bash
cd dev-sphere-frontend-main
npm run dev
```

## 3. 功能测试流程

1.  **登录系统**: 确保你已登录 devSphere 系统（获取 Token）。
2.  **访问页面**: 在浏览器地址栏输入 `http://localhost:5173/ai-interview` (端口视本地配置而定)。
3.  **连接测试**:
    *   页面加载后，控制台应显示 "Connected to AI Interviewer"。
    *   你应该能看到 AI 面试官的头像。
    *   字幕应显示 "你好，我是你的AI面试官..."。
4.  **交互测试**:
    *   点击底部的麦克风按钮。
    *   状态应变为 "LISTENING"。
    *   再次点击麦克风（模拟说话结束）。
    *   状态应变为 "THINKING" -> "SPEAKING"。
    *   字幕应更新为 AI 的下一句回复。

## 4. 后续完善建议

*   **接入真实 AI**: 修改 `AIInterviewServiceImpl.java`，接入 OpenAI/Azure 的 API。
*   **接入真实 TTS**: 集成 Edge-TTS 或 Azure TTS 生成真实语音流。
*   **接入真实 ASR**: 集成 Whisper 或 Azure Speech SDK 进行实时语音转文字。
*   **优化 Avatar**: 替换 CSS 动画为 Live2D 模型驱动。
