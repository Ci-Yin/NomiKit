# 中文 Git 提交信息提示词

```text
请根据当前代码变更生成一条中文 Git 提交信息，格式遵循 Conventional Commits。

输出格式：

<type>(<scope>): <中文摘要>

- <变更点 1>
- <变更点 2>
- <变更点 3>

Co-Authored-By: <模型名称>

要求：
1. type 只能从以下类型中选择：
   - feat：提交新功能
   - fix：修复 bug
   - docs：只修改文档
   - style：调整代码格式，未修改代码逻辑
   - refactor：非构建类代码重构，既没修复 bug 也没有添加新功能
   - perf：性能优化，提高性能的代码更改
   - test：添加或修改测试
   - chore：构建流程、构建工具或依赖库变更，例如 Gradle、build.gradle.kts、libs.versions.toml
2. scope 使用受影响的模块名，例如：
   app-shared、core-ui-foundation、feature-ai-core、buildSrc、gradle 等。
3. 中文摘要使用现在时态，简明扼要，不以句号结尾。
4. 正文使用中文无序列表，概括关键变更点。
5. 正文每条以动词开头，例如：新增、修复、调整、移除、配置、优化、补充。
6. 如果变更较小，可以输出 1-3 条正文；如果变更较多，最多输出 5 条正文。
7. 如果变更包含多个互不相关的目的，请拆分为多条提交信息建议。
8. 如果提交内容包含 AI 生成或 AI 协作修改的代码，必须在提交信息末尾追加模型信息，格式为 `Co-Authored-By: <模型名称>`，例如 `Co-Authored-By: GPT 5.5`。
9. 如果提交内容不是 AI 生成或 AI 协作修改的代码，不要追加 `Co-Authored-By`。
10. 只输出提交信息，不要输出解释。
```

示例：

```text
chore(gradle): 配置 Gradle 守护进程 JVM 工具链属性

- 为不同操作系统和架构配置 JetBrains JDK 21 下载链接
- 添加 FREE_BSD、LINUX、MAC_OS、UNIX 和 WINDOWS 平台支持
- 设置 AARCH64 和 X86_64 架构对应的工具链 URL
- 指定工具链供应商为 JETBRAINS
- 设定工具链版本为 21

Co-Authored-By: GPT 5.5
```
