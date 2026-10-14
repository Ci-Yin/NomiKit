---
name: module-docs
description: Use when creating or maintaining module-level README documentation, deciding whether a nested module directory needs documentation, or separating README content from agent rules and task skills.
---

# 模块文档

本 skill 用于建立和维护模块目录附近的开发文档。它负责判断文档位置、收集模块事实、编写 README
和校验链接；不替代源码、Gradle 配置、测试或具体模块 skill。

## 适用范围

使用本 skill 的场景：

- 为有独立 `build.gradle.kts` 的叶子模块创建或维护 `README.md`；
- 判断聚合目录或复杂模块内部目录是否需要 README；
- 判断内容应该放 README、`SKILL.md` 还是模块级 `AGENTS.md`；
- 将集中式架构文档转换为模块入口说明；
- 检查模块文档是否与源码、Gradle 配置、测试任务和现有 skill 一致。

不要因为普通 Kotlin package、单个 source set 或单个类就机械新增 README。

## 文档位置决策

按以下顺序确认目录边界：

1. 读取根 `settings.gradle.kts`，确认目录是否对应真实 Gradle project。
2. 读取目标目录的 `build.gradle.kts`、源码布局和测试 source set，确认它是叶子模块、聚合目录还是普通
   package。
3. 读取现有模块 skill 和相关集中式文档，避免重复维护同一份 API 或架构说明。

选择规则：

- **叶子 Gradle 模块**：在模块根目录维护 `README.md`。
- **聚合目录**：维护简短索引，链接到子模块 README，不重复描述实现细节。
- **复杂模块内部目录**：只有当目录形成独立概念边界、拥有独立验证方式或由开发者单独进入时才创建 README。
- **模块独有硬约束**：只有必须自动作用于该目录全部文件的规则才使用模块级 `AGENTS.md`。
- **AI 工作流**：触发条件、实现步骤、排错和验证策略放在对应 `SKILL.md`。

## 收集模块事实

写 README 前必须检查：

- `settings.gradle.kts` 中的 project path 和实际目录；
- `build.gradle.kts` 的 API/implementation 依赖、平台 target 和测试任务；
- `src/commonMain`、`src/commonTest` 及平台 source set 的实际布局；
- public/internal API 的职责和模块边界；
- 现有 skill、`.docs` 文档和样例调用方。

README 中的模块名、依赖方向、公共 API、平台说明和接入示例必须来自当前源码或构建配置，不根据目录名称推断。

## README 内容与深度

模块 README 是开发者进入模块后的第一份说明，应该能回答“这个模块解决什么问题、从哪里开始、如何接入、哪些边界不能越过”。
不要用重复解释或完整 API 清单凑长度，也不要把 README 写成专用 skill 或集中式架构文档的副本。

按目录类型控制信息密度：

- **聚合目录索引**：约 30～60 行，只列真实子模块、职责入口和子模块 README 链接。
- **普通叶子模块**：约 60～110 行，覆盖职责、边界、源码布局、入口和相关资料。
- **多平台或公共 API 较多的模块**：约 120～220 行，补充最小接入、平台差异、扩展边界和常见误用。

行数只是复杂度参考；内容应以开发者首次接入所需的信息为准。

复杂公共模块推荐使用以下结构：

```md
# `:module:name`

## 用途与非目标

说明模块解决的问题、适用场景，以及明确不负责的产品语义、页面编排或应用组装。

## 模块位置与依赖边界

说明模块位于 `core`、`component`、`feature` 或 `business` 哪一层，允许哪些上层依赖，禁止依赖哪些模块。

## 从哪里开始

列出 2～5 个稳定的公共入口、入口文件及职责。入口必须来自当前源码，避免罗列内部实现细节。

## 最小接入路径

提供一个基于源码或测试确认过的短调用示例，或用调用链说明初始化、装配、调用和结果消费过程。

## 源码布局

- 按功能域说明 `src/commonMain`、平台 source set、测试目录以及 schema、资源或生成配置目录。
- 只记录当前存在且对开发者有帮助的目录，不为不存在的平台创建空说明。

## 平台差异与生命周期

说明 Android、Desktop、iOS、Web 等平台是否支持，哪些 API 使用 `expect/actual`，以及资源、线程、生命周期或权限边界。

## 依赖与扩展边界

说明关键 `api`/`implementation` 依赖、Koin 或其他装配入口、可扩展位置，以及哪些实现必须留在应用层。

## 限制与常见误用

记录源码和专用 skill 已确认的限制，例如迁移策略、错误模型、平台能力缺失、生命周期要求，以及不能复制公共实现的场景。

## 相关资料

- [Module skill](../../.agents/skills/<skill>/SKILL.md)
- [Architecture](../../.docs/contributing/architecture.md)

```

如果模块不支持某个平台，明确写出当前 source set
和限制，不为不存在的实现创建示例。复杂模块至少要有稳定入口、最小接入路径、平台差异、扩展边界和真实限制；简单模块不需要为了模板完整而添加空章节。

## 保持文档可维护

- 代码、`settings.gradle.kts`、`build.gradle.kts` 和测试是事实来源；README 与事实冲突时修
  README，不改源码迁就文档。
- README 写稳定的职责、边界、入口、平台限制和短接入示例；易变的 API 细节、完整教程和排错流程放对应 skill
  或集中式文档。
- 使用从模块目录出发的相对链接，并确认链接目标存在；不要留下具体产品标识、私有协议或下游配置。
- 修改公共 API、模块行为或构建配置时，检查 README 与对应 skill 是否需要同步。
- 聚合目录索引只列真实子模块；不要复制每个子模块的完整说明。

## 一致性检查与交付

生成或维护 README 前后，按以下清单检查：

- 对照 `settings.gradle.kts`、模块 `build.gradle.kts`、源码和测试确认模块路径、依赖、source set、入口 API
  和平台说明；
- 确认 README 中的接入示例、入口文件、skill 和 `.docs` 相对链接真实存在；
- 检查没有把应用层产品语义写成公共模块职责，也没有把内部实现误写成稳定公共 API；
- README 与事实冲突时，以源码、Gradle 配置和测试为准修正文档。

交付报告说明新增或更新的文档、确认过的事实来源、未确认的内容以及需要后续同步的 skill。
