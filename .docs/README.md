# 项目文档

这里集中维护 NomiKit 的开发规范、构建测试说明、专题指南和长期设计方案。文档中的命令、模块名和 API
以当前源码、Gradle 配置与测试为准；示例未验证时会明确标注。

## 新人上手

- [环境与工具](contributing/setup.md)：IDE、JDK、Android SDK、iOS 前置条件。
- [构建与打包](contributing/building.md)：Android、iOS、Desktop 构建任务与限制。
- [测试与调试](contributing/testing.md)：运行应用、单元测试和 Android Instrumented Test。

## 开发规范

- [代码风格](contributing/code-style.md)：命名、可见性、资源、注释和测试命名。
- [分层与错误处理](contributing/layered.md)：Data / Domain / UI 边界、UseCase 和错误映射。
- [MVI 与 ViewModel](contributing/mvi.md)：State、Action、Effect 和 ViewModel 选择。
- [Kotlin Multiplatform](contributing/kmp.md)：源集结构与平台代码边界。
- [架构总览](contributing/architecture.md)：当前模块层级和依赖方向。
- [参与开发入口](contributing/README.md)：完整的贡献与参考资料索引。

## 专题指南

- [指南索引](guides/README.md)
- [FlowRedux2 状态机](guides/flow-redux.md)
- [KoinBoot 介绍](guides/koinboot-introduce.md)
- [KoinBoot 第三方库适配](guides/koinboot-third-party-library-adaptation-guide.md)

## 设计文档

- [设计文档索引](design/README.md)：长期方案、状态和验证记录。
- [KMP 平台层级参考](KMP-Platform-Structure.md)

## 历史参考

- [Android 开发命名规范（历史参考）](《Android 开发命名规范（Jetpack Compose + MVVM 架构）》.md)
- [Android Compose + MVVM 命名与架构规范手册（历史参考）](《Android Compose + MVVM
  命名与架构规范手册》.md)

## 文档维护

- 文档变更先核对 [仓库规则](../.agents/rules/AGENTS.md)。
- 设计状态和验证记录遵循 [design-docs skill](../.agents/skills/design-docs/SKILL.md)。
- 叶子 Gradle 模块的 README 与模块源码、Gradle 配置和测试保持同步。