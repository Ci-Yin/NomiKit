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
- 现有 skill、`.docs` 文档、样例调用方和最窄验证命令。

README 中的模块名、依赖方向、公共 API 和验证命令必须来自当前源码或构建配置，不根据目录名称推断。

## README 内容约定

模块 README 保持 30～80 行，作为目录入口，不复制完整 API 手册。至少包含用途、公共边界、源码布局、验证入口和相关资料。

推荐模板：

```md
# `:module:name`

## 用途

说明模块负责什么，以及明确不负责什么。

## 公共边界

- 可被哪些层依赖。
- 不得依赖哪些模块。
- 哪些实现应留在应用或产品层。

## 源码布局

- `src/commonMain`：
- `src/androidMain`：
- `src/desktopMain`：
- `src/iosMain`：

## 验证

```powershell
.\gradlew.bat :module:name:<narrow-task>
```

## 相关资料

- [Module skill](../../.agents/skills/<skill>/SKILL.md)
- [Architecture](../../.docs/contributing/architecture.md)

```

如果模块不支持某个平台，明确写出当前 source set 和限制，不为不存在的实现创建示例。

## 保持文档可维护

- 代码、`settings.gradle.kts`、`build.gradle.kts` 和测试是事实来源；README 与事实冲突时修 README，不改源码迁就文档。
- README 只写稳定的职责、边界和入口；易变的 API 细节、完整示例和排错流程放对应 skill 或集中式文档。
- 使用从模块目录出发的相对链接，并确认链接目标存在；不要留下具体产品标识、私有协议或下游配置。
- 修改公共 API、模块行为或构建配置时，检查 README 与对应 skill 是否需要同步。
- 聚合目录索引只列真实子模块；不要复制每个子模块的完整说明。

## 验证与交付

文档变更至少执行：

1. `git diff --check`；
2. 检查 README 中的模块路径、Gradle task、skill 和 `.docs` 链接；
3. 对照 `settings.gradle.kts`、`build.gradle.kts` 和源码复核公共边界；
4. 如果 README 同步了可运行示例或验证任务，再运行对应最窄 Gradle task。

交付报告说明新增或更新的文档、确认过的事实来源、运行的验证和仍未确认的内容。
