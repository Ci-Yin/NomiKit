---
name: public-capability-reuse
description: Use when a task may reuse, extend, or replace a cross-module, cross-platform, or shared capability such as permissions, files, networking, storage, sharing, platform adapters, or UI foundation.
---

# 公共能力复用

本 skill 用于决定一项能力应直接复用、扩展公共模块、增加应用层薄适配，还是保留为产品私有实现。它负责公共边界和证据收集，不替代具体模块
skill、源码、Gradle 配置或测试。

## 触发边界

以下任务使用本 skill：

- 权限、文件、网络、存储、分享、平台适配或 UI 基建等可能已有公共抽象的任务；
- 需要判断能力应放在 `app`、`feature`、`component` 还是 `core`；
- 不确定现有 API 是否覆盖需求；
- 需要新增公共 API，或修复可能影响多个产品的通用问题。

以下任务通常不触发：

- 明确的单页面局部实现；
- 只服务一个产品且没有公共复用价值的产品语义；
- 已经明确命中特定模块 skill，且只是简单调用，不涉及复用边界判断。

命中特定模块时仍需读取对应 skill，例如 `file-picker`、`feature-media-directory`、`core-platform` 或
`core-ui-foundation`。本 skill 只补充公共归属判断，不替代这些模块的实现规则。

## 事实采集

按以下顺序收集证据：

1. 读取 `settings.gradle.kts`，确认当前项目真实模块和 project path。
2. 检查目标模块的 `build.gradle.kts`、依赖方向、平台 target 和测试 source set。
3. 按语义搜索 `feature/*`、`component/*`、`core/*` 的源码，不只搜索预计的函数名。
4. 读取命中的模块 skill 和相关 `.docs` 文档。
5. 打开候选 API 的实现和至少一个真实调用方，确认参数、状态归属、生命周期和错误边界。
6. 检查受影响的 `commonMain`、平台 source set、测试和现有调用方，确认哪些平台需要变化。

候选 API、真实调用方、依赖方向、平台边界和测试证据必须来自当前工作区；不要根据模块名称或记忆下结论。

## 决策矩阵

| 情况                    | 处理            |
|-----------------------|---------------|
| 已有 API 完整覆盖需求         | 直接复用          |
| 现有能力缺少通用扩展点，且多个调用方会受益 | 扩展原公共模块       |
| 只有产品语义、生命周期或依赖组装不同    | 在 `app` 增加薄适配 |
| 只服务单产品且没有公共价值         | 保留在应用层，不下沉    |

应用层适配只承载产品语义、生命周期和依赖组装，不能复制公共模块的权限、文件、平台调用、错误映射或生命周期实现。

```kotlin
// 公共模块：提供跨产品能力
interface MediaDirectorySession {
    suspend fun requestAccess(): MediaDirectoryAccess
}

// 应用层：只负责产品语义和装配
class GalleryDirectoryAdapter(
    private val session: MediaDirectorySession,
) {
    suspend fun openGallery(): GallerySelection {
        return session.requestAccess().toGallerySelection()
    }
}
```

## 复用与扩展约束

- 已有公共 API 满足需求时，禁止创建同义封装。
- 已有公共抽象覆盖场景时，禁止在 `app/*` 复制实现。
- 只有在已有抽象不满足且原因明确时，才允许直接调用底层平台 API；实现必须放在正确的平台边界内。
- 可复用缺陷修复和公共 API 演进放回原公共模块，并同步测试、构建配置和对应 skill。
- 公共模块不得依赖 `app/*`、产品资源、账号配置、产品专属协议或产品数据。
- UI 任务仍必须遵循 `core-ui-foundation` 和 `core-material` 的专用规则。
- 下游改动回流另行遵循 `nomikit-public-sync`，本 skill 不重复 worktree、提交或 PR 流程。

## 验证与交付

根据最终决策运行最窄验证：

- **复用已有 API**：运行调用方模块的编译或测试；
- **扩展公共模块**：运行公共模块测试、调用方编译和对应 skill 校验；
- **应用层薄适配**：运行应用模块编译或受影响平台测试；
- **平台实现变化**：只验证受影响的平台 target，不要求无关平台全量验证。

交付报告必须说明：

- 搜索过的候选公共 API 和真实调用方；
- 最终选择：复用、公共扩展、应用适配或保持产品私有；
- 公共边界和受影响平台；
- 存在候选能力但未复用时的具体原因；
- 测试、编译、skill 校验和未验证项。
