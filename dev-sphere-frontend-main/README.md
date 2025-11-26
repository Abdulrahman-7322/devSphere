# DevSphere 前端

Dev-Sphere IM 前端 是一个面向现代实时通信场景打造的轻量级即时聊天 Web 客户端，采用 Vue3 + Vite + TypeScript 实现，提供流畅的消息发送体验与多端一致的消息同步能力。项目内置私聊、群聊、消息状态展示、重发机制、本地消息队列、WebSocket 长连管理等核心能力，并对接后端的 Redis Stream + MySQL 可靠消息系统，实现类似企业级 IM 的交互体验。代码结构清晰、组件划分合理，适合作为即时通讯学习参考、企业项目脚手架或个人聊天应用的前端基础工程。

## 项目结构概览

- **基础配置**:
  - `index.html`: 项目的主 HTML 文件。
  - `package.json`: 包含项目的元数据和依赖项。
  - `vite.config.ts`: Vite 的配置文件。
  - `tsconfig.json`: TypeScript 的配置文件。
  - `.gitignore`: 指定 Git 忽略的文件和目录。

- **源代码**:
  - `src/main.ts`: 项目的入口文件。
  - `src/App.vue`: 根组件。
  - `src/router/index.ts`: 路由配置。
  - `src/style.css`: 全局样式文件。
  - `src/assets/`: 静态资源文件。
  - `src/components/`: Vue 组件。
  - `src/views/`: 页面视图组件。
  - `src/services/`: 服务层，处理网络请求和业务逻辑。
  - `src/stores/`: 状态管理模块。
  - `src/utils/`: 工具类函数。

## 核心功能

- **用户认证**: 登录和注册功能。
- **聊天功能**: 支持一对一聊天、群组聊天、添加好友、创建群组等功能。
- **WebSocket 通信**: 使用 `WebSocketService` 处理实时通信。
- **通知系统**: 提供通知功能。
- **响应式设计**: 使用 Tailwind CSS 实现响应式布局。

## 如何运行项目

1. **安装依赖**:
   ```bash
   npm install
   ```

2. **启动开发服务器**:
   ```bash
   npm run dev
   ```

3. **构建生产版本**:
   ```bash
   npm run build
   ```

4. **运行测试**:
   ```bash
   npm run test
   ```

5. **代码检查**:
   ```bash
   npm run lint
   ```

## 技术栈

- **Vue 3**: 使用 Vue 3 的 Composition API 和 `<script setup>` 语法。
- **TypeScript**: 提供类型安全。
- **Vite**: 快速的构建工具。
- **Tailwind CSS**: 实用优先的 CSS 框架。
- **Pinia**: Vue 的状态管理库。
- **WebSocket**: 实现实时通信。

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目。
2. 创建新分支。
3. 提交你的更改。
4. 创建 Pull Request。

## 许可证

本项目采用 MIT 许可证。详情请查看 [LICENSE](LICENSE) 文件。

---

该项目是一个现代化的前端模板，适合用于开发各种 Web 应用程序。欢迎查看并贡献代码！