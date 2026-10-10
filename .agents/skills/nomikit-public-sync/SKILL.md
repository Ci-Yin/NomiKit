---
name: nomikit-public-sync
description: Use when a downstream project changes code that may belong to NomiKit business, component, core, or feature modules and the change must be evaluated for public synchronization.
---

# NomiKit 公共改动回流

本 skill 用于评估并准备下游项目对 NomiKit
公共脚手架的回流。它只覆盖公共改动判定、隔离准备、最小移植和验证；产品功能实现、下游私有代码和远端发布仍由对应项目流程负责。

## 触发与硬边界

当下游改动命中或可能命中 NomiKit 的 `business/*`、`component/*`、`core/*` 或 `feature/*` 时使用本
skill。只修改下游 `app/*` 或下游专属模块的任务通常不触发，除非该改动暴露了公共层缺少可复用扩展点。

一次改动只有同时满足以下条件，才是 NomiKit 公共改动候选：

1. 文件属于 NomiKit 当前 `settings.gradle.kts` 声明的公共模块；以该文件为准，不凭目录名称猜测，也不在
   skill 中维护静态模块清单。
2. 实现不依赖下游 `app/*`、下游专属模块、产品名称、业务协议、账号配置、密钥、资源或数据，能够在 NomiKit
   中独立编译和测试。
3. 改动提供可复用能力、通用缺陷修复或公共 API 演进，而不是只服务单个产品的特例。

以下内容默认不回流：下游 `app/*`、产品文案和资源、账号与密钥、产品协议、私有数据、仅用于产品装配的配置，以及没有公共复用价值的适配代码。

## 证据采集

先在下游仓库收集事实，再决定是否准备回流：

1. 读取下游和 NomiKit 的 `.agents/rules/AGENTS.md`，并运行 `git status --short`；保留所有与任务无关的用户改动。
2. 查看 `git diff --name-status`、相关完整 diff、模块 `build.gradle.kts` 和 `settings.gradle.kts`
   ，确认路径属于哪个真实模块以及依赖方向。
3. 检查实现、测试、文档、skill 和构建配置是否引用下游 `app/*`、产品名、业务协议、资源、密钥或私有数据。必要时用
   `rg` 做候选扫描，再打开命中的上下文确认，不以词条扫描单独作结论。
4. 记录下游最小相关验证及其结果。没有完成下游验证时，不把回流候选标记为已验证。

如果三项公共性条件无法可靠判断，停止移植并报告需要澄清的证据，不要把疑似公共代码直接复制到 NomiKit。

## 公共改动回流流程

### 1. 识别上游并隔离工作区

按远端 URL `https://github.com/Ci-Yin/NomiKit.git` 识别 NomiKit，不依赖下游仓库中的远端名称。确认上游最新
`master` 后，从该基线创建隔离的 `agent/<downstream>-<topic>` 分支和 worktree，或使用等价的隔离目录。

如果 NomiKit 当前工作区不干净，必须使用隔离 worktree；不得把下游提交历史、下游私有文件或 NomiKit
工作区已有的无关未提交改动带入回流分支。分支或 worktree 创建属于本地准备，不代表获得了远端写入授权。

### 2. 移植最小完整改动

只移植使公共能力可独立使用所必需的完整集合：

- 公共实现和它需要的测试；
- 与该 API 或行为同步的公共文档和 `.agents/skills/**`；
- 必要的模块 `build.gradle.kts`、`buildSrc/**`、`gradle/libs.versions.toml` 或根构建配置。

不要为了让移植暂时通过而带入产品实现、私有资源、账号配置、密钥、下游 `app/*` 或无关格式化改动。若下游需求只是公共层缺少扩展点，应在
NomiKit 中提供通用抽象或扩展点，把产品实现留在下游。

### 3. 执行窄验证

在隔离的 NomiKit worktree 中，按改动范围运行最窄的相关验证：模块测试、模块编译或对应文档/skill
校验。优先使用项目现有 Gradle task 和 skill 校验工具，不用全量 `clean check` 替代针对性证据。

至少确认：

- 公共实现不依赖下游私有路径或产品术语；
- 必要测试和构建配置随实现完整移植；
- 相关模块能够独立编译或测试；
- `git diff --check` 通过，且 diff 只包含公共改动。

把结果分为：

- `verified`：有当前 worktree 中的命令输出或源码证据支持；
- `inferred`：根据依赖或结构推断，但缺少运行时/构建证据；
- `blocked`：受到网络、认证、工具、环境或缺失输入阻塞。

## 远端写入授权门

默认只完成本地分析、隔离准备、移植和验证。提交、推送、创建 GitHub Pull
Request、合并、启用自动合并，以及处理与本次公共改动无关的远端内容，都必须获得用户明确且持续的授权。

即使用户授权了下游实现，也不能把该授权扩展为提交下游工作区、合并 PR
或修改无关分支的授权。网络、认证、权限或验证失败时停止外部写入，保留本地证据并报告阻塞原因。

## 交付报告

完成本地准备后，报告以下内容：

1. 公共性判定：命中的模块、满足的条件、排除的下游专属内容；
2. 隔离信息：上游远端 URL、基线、分支/worktree 和实际移植文件；
3. 验证结果：按 `verified`、`inferred`、`blocked` 分类列出命令和证据；
4. 未执行的远端动作及其授权原因；
5. 仍需用户决定的事项，例如是否允许提交、推送或创建 PR。
