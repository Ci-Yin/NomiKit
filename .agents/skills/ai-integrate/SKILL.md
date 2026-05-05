---
name: ai-integrate
description: Use the feature/ai-integrate Kotlin Multiplatform module (package ciyin.ai.integrate.image) as 将具体 AI 引擎实现装配进 ai-facade 的聚合层。Covers AiImageIntegrate、engines 动态注册、ImageEngineConfig sealed 配置、IntegrateImageEngineIds 稳定 EngineId、mergeEngineConfigsWithDefaults 按子类合并、IntegrateEnginePreferences 默认路由、AiImageIntegrate() 工厂与 defaultSdWebUiImageEngine、parseHttpOrigin 解析 baseUrl。Use when 用户提到 ai-integrate、AiImageIntegrate、生图聚合、ImageEngineConfig、IntegrateImageEngineIds、engines 覆盖 SD WebUI 地址、扩展聚合层新后端、或排查本模块构建与单测。
---

# feature/ai-integrate 模块协作指南

`feature/ai-integrate` 把 **具体引擎实现**（当前首版为 SD WebUI）装配成 `ai-core` 的 [ImageEngine]
，并在内部委托 `ai-facade` 的 [DefaultAiImage] 完成路由、降级与观测。Kotlin 源码包名为
`ciyin.ai.integrate.image.*`，仅 `commonMain` + `commonTest`。

定位：**不是**厂商无关抽象（那是 `ai-core`），**不是**业务唯一入口接口（那是 `ai-facade` 的 `AiChat` /
`AiImage`）；本模块负责 **「把选中的引擎 jar 接上线」** 与 **「用统一配置模型描述各后端连接参数」**。

首版仅 **生图** 聚合；Chat 聚合若后续出现，应在同 skill 下增补包路径与类型名。

## 触发场景

任意一项命中即按本 skill 处理：

- 用户提到 `ai-integrate`、`AiImageIntegrate`、`ImageEngineConfig`、`IntegrateImageEngineIds`
- 用户提到 `engines` 动态切换生图后端、`parseHttpOrigin`、`IntegrateImageDefaults`
- 用户要在应用或 sample 中 **注册/覆盖** SD WebUI 根地址、默认模型，而不直接散落 `SdWebUiImageEngine`
  构造参数
- 用户要 **新增一种** 聚合支持的生图后端（新 `ImageEngineConfig` 子类 + 装配函数 + 稳定 `EngineId`）
- 用户排查本模块 `build.gradle.kts`、构建或 `commonTest` 失败

不命中则忽略本 skill。

## 模块速览

```
feature/ai-integrate/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/ciyin/ai/integrate/image/
    │   ├── AiImageIntegrate.kt           # 聚合入口：engines / generate / models；内建 Mutex + Runtime
    │   ├── AiImageIntegrateFactory.kt    # fun AiImageIntegrate() 默认工厂 + SD WebUI 装配
    │   ├── ImageEngineConfig.kt          # sealed：共通字段 + 各后端子类（首版 SdWebUi）
    │   ├── IntegrateImageEngineIds.kt    # 聚合层固定 EngineId（与 URL 解耦）
    │   ├── IntegrateImageDefaults.kt     # 内置基线配置（如本机 7860）
    │   ├── IntegrateEnginePreferences.kt # internal：默认 ImageEngineSpec 指向 sdWebUi
    │   └── HttpOriginParse.kt            # internal：baseUrl → (host, port, useHttps)
    └── commonTest/kotlin/ciyin/ai/integrate/image/
        ├── AiImageIntegrateTest.kt
        ├── ImageEngineConfigMergeTest.kt
        └── ParseHttpOriginTest.kt
```

## 依赖（Gradle）

`api(projects.feature.aiCore)`：`ImageEngine` / `ImageRequest` / 注册表等契约向上暴露。

`implementation(projects.feature.aiFacade)`、`implementation(projects.feature.aiImageSdwebuiEngine)`
：对内装配与默认 SD WebUI 引擎，**不**要求业务模块再依赖具体 engine——业务只需依赖 `ai-integrate`
（若仅走聚合入口）。

在 `build.gradle.kts` 中引用模块时仅使用 `projects.feature.aiIntegrate`（或各子项目约定名），禁止
`project(":...")` 字符串形式。

## 核心 API

### 对外默认入口

调用方优先使用同包顶层工厂 **`AiImageIntegrate()`**：注入
`IntegrateImageDefaults.sdWebUiLocalhost()`、`IntegrateEnginePreferences`、以及
`defaultSdWebUiImageEngine`（由 `ImageEngineConfig.SdWebUi` + `parseHttpOrigin` 构造
`SdWebUiImageEngine`）。

### AiImageIntegrate 实例行为

- **`suspend fun engines(configs: List<ImageEngineConfig>)`**：将构造时传入的 **默认配置表** 与本次
  `configs` 按 **sealed 子类类型（KClass）** 合并：先应用默认列表（同类后者覆盖前者），再应用 `configs`
  覆盖同类槽位；随后重建 `DefaultImageEngineRegistry`、`ImageEngineSelector` 与 `DefaultAiImage`
  运行时快照。
- **`fun generate(request, spec)`**：解析 `spec`、选择目标 `EngineId`、在 `model == null` 时用对应
  `ImageEngineConfig.defaultModel` 补全，再 **`emitAll` 委托** `DefaultAiImage.generate`（语义与
  Facade 一致）。
- **`suspend fun models()`**：委托 `DefaultAiImage.models()`。

自定义默认基线、偏好实现或 `buildImageEngine` 时：使用 **`internal` 三参构造**（需在测试或 `app` 模块通过
`internal` 可见性约定暴露，例如与 `ai-integrate` 同编译单元或 `@VisibleForTesting` 模块策略）。三参为：
`defaultEngineConfigs`、`preferences`、`buildImageEngine`。

### ImageEngineConfig 与 EngineId

- 每新增一种后端：在 `ImageEngineConfig` 增加 **sealed 子类**，在 `IntegrateImageEngineIds` 增加 **稳定
  ** `EngineId` 常量（**不要**把 host/port 编进 id）。
- `apiKey` 等敏感字段仅作数据载体；**禁止**在本模块内打印日志或调试输出。

## 扩展新后端（检查清单）

1. 在 `IntegrateImageEngineIds` 增加新常量。
2. 在 `ImageEngineConfig` 增加子类，并实现 `engineId` / `baseUrl`（或该后端所需字段）/ `apiKey` /
   `defaultModel` 等契约字段。
3. 在 `AiImageIntegrateFactory`（或单独装配函数）的 `when (config)` 分支中构造对应 `ImageEngine`。
4. 若默认路由应指向新引擎，调整或替换 `EnginePreferences` 实现（首版为 internal 的
   `IntegrateEnginePreferences`）。
5. 为合并规则与装配增加 `commonTest`（参考 `ImageEngineConfigMergeTest`、`AiImageIntegrateTest`）。

## 与 ai-core / ai-facade 的边界

- **请求/事件模型**：始终使用 `ciyin.ai.core.image.*`（如 `ImageRequest`、`ImageEvent`）。
- **路由与降级语义**：由聚合层内部的 `DefaultAiImage` 执行；修改降级策略应改 `ai-facade` 或注入自定义
  `EnginePreferences`，而非在聚合层复制调度逻辑。
- **具体 HTTP/SDK**：由 `ai-xxx-engine` 模块实现；聚合层只做 **config → engine** 的薄装配（当前 SD
  WebUI 见 `AiImageIntegrateFactory.kt`）。

## 相关 skill

- 厂商无关契约与模型：`ai-core`
- `DefaultAiImage`、 `ImageEngineSpec`、`EnginePreferences`：`ai-facade`
- SD WebUI 引擎字段与 mapper：`ai-image-sdwebui-engine`
