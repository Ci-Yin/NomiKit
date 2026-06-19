---
name: ai-integrate
description: Use the feature/ai-integrate Kotlin Multiplatform module as 将具体 AI 引擎实现装配成可直接调用的聚合入口。Covers ciyin.ai.integrate.chat.AiChatIntegrate / ChatEngineConfig / ChatEngineSpec / IntegrateChatEngineIds, ciyin.ai.integrate.image.AiImageIntegrate / ImageEngineConfig / ImageEngineSpec / IntegrateImageEngineIds, engines 动态注册, 默认模型补齐, 模型列表去重, 生图重试/降级, SD WebUI 与 OpenAI 兼容聊天端点装配。
---

# feature/ai-integrate 模块协作指南

`feature/ai-integrate` 把 **具体 AI 引擎实现** 装配成可直接调用的聚合入口。当前包含：

- `ciyin.ai.integrate.chat.*`：聊天聚合，默认装配 OpenAI 兼容 `ChatEngine`。
- `ciyin.ai.integrate.image.*`：生图聚合，默认装配 SD WebUI `ImageEngine`。

定位：**不是**厂商无关抽象（那是 `ai-core`），也不是业务数据层；本模块负责 **config → engine** 的薄装配、
引擎注册选择、默认模型补齐与模型列表聚合。具体 HTTP / SDK 行为仍由 `ai-xxx-engine` 模块承担。

## 触发场景

任意一项命中即按本 skill 处理：

- 用户提到 `ai-integrate`、`AiChatIntegrate`、`AiImageIntegrate`
- 用户提到 `ChatEngineConfig`、`ImageEngineConfig`、`ChatEngineSpec`、`ImageEngineSpec`
- 用户要在应用或 sample 中注册 / 覆盖 OpenAI 兼容聊天端点、SD WebUI 地址或默认模型
- 用户要新增聚合支持的聊天或生图后端
- 用户排查本模块 `build.gradle.kts`、构建或 `commonTest` 失败

不命中则忽略本 skill。

## 模块速览

```
feature/ai-integrate/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/ciyin/ai/integrate/
    │   ├── chat/
    │   │   ├── AiChatIntegrate.kt
    │   │   ├── AiChatIntegrateFactory.kt
    │   │   ├── ChatEngineConfig.kt
    │   │   ├── ChatEngineSpec.kt
    │   │   └── IntegrateChatEngineIds.kt
    │   └── image/
    │       ├── AiImageIntegrate.kt
    │       ├── AiImageIntegrateFactory.kt
    │       ├── ImageEngineConfig.kt
    │       ├── ImageEngineSpec.kt
    │       ├── IntegrateImageEngineIds.kt
    │       ├── FallbackPolicy.kt
    │       ├── AiInvocationListener.kt
    │       ├── InvocationMetadata.kt
    │       ├── IntegrateImageDefaults.kt
    │       ├── IntegrateEnginePreferences.kt
    │       ├── HttpOriginParse.kt
    │       └── internal/
    │           ├── Fallbacks.kt
    │           └── InvocationIds.kt
    └── commonTest/kotlin/ciyin/ai/integrate/
        ├── chat/AiChatIntegrateTest.kt
        └── image/
            ├── AiImageIntegrateTest.kt
            ├── ImageEngineConfigMergeTest.kt
            └── ParseHttpOriginTest.kt
```

## 依赖（Gradle）

`api(projects.feature.aiCore)`：聊天 / 生图请求、事件、引擎接口、注册表等契约向上暴露。

`implementation(projects.feature.aiChatOpenaiEngine)`：对内装配 OpenAI 兼容聊天引擎。

`api(projects.feature.aiImageSdwebuiEngine)`：对内装配默认 SD WebUI 引擎，同时让业务只依赖
`ai-integrate` 即可通过 `AiImageIntegrate.engine<SdWebUiImageEngine>()` 使用厂商专属能力。

在 `build.gradle.kts` 中引用模块时仅使用 `projects.feature.aiIntegrate`（或各子项目约定名），禁止
`project(":...")` 字符串形式。

## 核心 API

### 聊天入口

调用方优先使用 `ciyin.ai.integrate.chat.AiChatIntegrate(...)`：

- `fun AiChatIntegrate()`：创建空注册表，之后通过 `engines(...)` 注入端点。
- `fun AiChatIntegrate(engineConfigs: List<ChatEngineConfig>)`：构造后即可调用。
- `suspend fun engines(configs: List<ChatEngineConfig>)`：按 `EngineId` 合并默认配置与覆盖配置，并重建运行时。
- `fun stream(request, spec)`：解析 `ChatEngineSpec`，`Explicit.model` 优先；没有显式模型时请求模型优先，再用配置 `defaultModel` 补齐。
- `suspend fun models(spec)`：按 spec 限定引擎后拉取模型，并按模型名小写去重。

首个聊天后端为 `ChatEngineConfig.OpenAiCompatible`，由 `OpenAiChatEngineConfig` 装配。

### 生图入口

调用方优先使用 `ciyin.ai.integrate.image.AiImageIntegrate()` 工厂获取 `AiImageIntegrate` 接口；模块内默认实现为
`DefaultAiImageIntegrate`：

- 内置 `IntegrateImageDefaults.sdWebUiLocalhost()` 作为本机 SD WebUI 基线。
- `suspend fun engines(configs: List<ImageEngineConfig>)`：按 `ImageEngineConfig` sealed 子类类型合并默认配置与覆盖配置。
- `fun generate(request, spec)`：解析 `ImageEngineSpec`，`Explicit.model` 优先；没有显式模型时请求模型优先，再补齐配置默认模型，并通过 `collectWithFallback` 执行重试 / 降级。
- `suspend fun models(spec)`：按 spec 限定引擎后拉取模型，并按模型名小写去重。
- `suspend fun <T : ImageEngine> engine()`：按具体引擎类型获取当前已注册的第一个匹配实例；没有匹配时抛出不支持能力异常。

### 配置与 EngineId

- 聊天后端：新增 `ChatEngineConfig` 子类、稳定 `IntegrateChatEngineIds` 常量，并在 `AiChatIntegrateFactory` 装配为 `ChatEngine`。
- 生图后端：新增 `ImageEngineConfig` 子类、稳定 `IntegrateImageEngineIds` 常量，并在 `AiImageIntegrateFactory` 装配为 `ImageEngine`。
- 不要把 host / port / model 编进内置默认 ID；业务 demo 可以按自身缓存需要生成实例 ID。
- `apiKey` 等敏感字段仅作数据载体，禁止在本模块内打印日志或调试输出。

## 扩展新后端（检查清单）

- 新增配置 sealed 子类和稳定 `EngineId`。
- 在对应 Factory 的 `when (config)` 分支中构造目标 `ChatEngine` 或 `ImageEngine`。
- 如果需要默认路由，调整默认配置表或偏好实现。
- 为合并规则与装配增加 `commonTest`，至少覆盖装配、默认模型补齐与模型列表行为。

## 与 ai-core / 引擎实现的边界

- **请求/事件模型**：始终使用 `ciyin.ai.core.chat.*` 与 `ciyin.ai.core.image.*`。
- **聊天策略**：默认只做薄路由和默认模型补齐，不做自动重试或备用引擎降级。
- **生图策略**：由 `ImageEngineSpec`、`IntegrateEnginePreferences` 与 `internal/Fallbacks.kt` 执行重试 / 降级。
- **具体 HTTP/SDK**：由 `ai-xxx-engine` 模块实现；聚合层只做 **config → engine** 的薄装配。

## 相关 skill

- 厂商无关契约与模型：`ai-core`
- OpenAI 兼容聊天引擎：`ai-chat-openai-engine`
- SD WebUI 引擎字段与 mapper：`ai-image-sdwebui-engine`
