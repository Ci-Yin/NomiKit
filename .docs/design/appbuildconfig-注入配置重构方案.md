# AppBuildConfig 注入配置设计与实现

> 状态：已实现（主体重构已落地；已知行为缺口见“当前限制”）
> 范围：跨平台配置契约、Android / Desktop / iOS 配置生成、Desktop / iOS 入口注入与桌面身份映射。
> 相关模块：`:core:application`；构建逻辑：`buildSrc`。
> 最近核对：2026-11-10；源码基线 `e5de064a`，本次仅整理文档，未修改实现。
> 验证记录：源码静态核对；本次未运行 Gradle、单测或平台应用，不代表原方案验收全部通过。

本文记录设计取舍、实际实现入口和限制，不再维护实现代码副本或历史归档。文件名默认不加版本号；只有实际出现第二版方案时才区分版本。
转换前方案没有已提交的 Git 历史；按当前约定不保留原稿副本，不将其视为可恢复记录。

## 背景与目标

设计时，桌面应用身份通过模块构建脚本内联任务生成，配置入口偏向
Desktop。此次重构将生成职责移入独立约定插件，让运行时消费稳定的跨平台契约，平台字段仍留在平台源集。

已落地的主体能力：

- 公共契约提供应用 ID、版本名和字符串扩展配置，不让 Desktop、Android 专属字段进入 common。
- 平台配置对象及 `currentAppBuildConfigImpl` 的 actual 由构建逻辑生成；运行时不读取工程配置文件。
- `AppBuildConfig` companion 提供默认访问，`LocalAppBuildConfig` 提供 Compose 默认值与局部覆盖入口。
- Desktop / iOS 的 `runApplication` 接收并注入配置；Desktop 同时提供应用身份映射。
- 配置收集、前缀合并、必填项和桌面目录名配对校验集中在构建期。

非目标保持不变：不迁移旧数据目录，不引入 profile、YAML、JSON Schema 或
ClassGraph；不把产品配置变成公共强类型字段，不在本模块承担业务 DI，也不把生成配置当作密钥管理方案。

## 核心设计决策

| 决策                                           | 理由与边界                                                                                       |
|------------------------------------------------|--------------------------------------------------------------------------------------------------|
| common 只定义通用契约，平台对象分别生成        | 保持调用入口统一，不把平台专属字段带入 common                                                    |
| 独立 `app-build-config` 插件                   | 除常量生成外，还包含 expect/actual、配对校验、前缀配置合并与源集装配；不与通用常量生成器混为一体 |
| 构建期收集、运行时只读                         | 配置随产物发布，不依赖运行环境中的工程文件；配置变更需要重新构建                                 |
| companion 默认入口与 CompositionLocal 覆盖并存 | 非 Compose 调用可读默认配置，Compose 树可由入口传入替代实例；对象委托不等于类型转换，见下文限制  |
| 业务扩展使用字符串 Map                         | 暂不引入业务 schema；类型转换与语义校验由使用方承担                                              |

## 实现入口

模块声明见 [settings.gradle.kts](<../../settings.gradle.kts>)。阅读顺序为模块接入、公共契约、生成插件、运行入口。

| 职责                | 源码入口                                                                                                                        | 关键符号与说明                                                                                    |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| 模块接入            | [application 构建配置](<../../core/application/build.gradle.kts>)                                                               | 应用 `app-build-config`，声明包名与配置前缀                                                       |
| 公共配置契约        | [AppBuildConfig.kt](<../../core/application/src/commonMain/kotlin/ciyin/application/config/AppBuildConfig.kt>)                  | `AppBuildConfig`、`currentAppBuildConfigImpl`；仅含通用字段                                       |
| Compose 配置入口    | [LocalAppBuildConfig.kt](<../../core/application/src/commonMain/kotlin/ciyin/application/config/LocalAppBuildConfig.kt>)        | `LocalAppBuildConfig`；默认返回 companion                                                         |
| 配置生成及源集装配  | [app-build-config.gradle.kts](<../../buildSrc/src/main/kotlin/app-build-config.gradle.kts>)                                     | `AppBuildConfigExtension`、`GeneratePlatformAppBuildConfigTask`、`AppBuildConfigConventionPlugin` |
| 配置读取与优先级    | [properties.kt](<../../buildSrc/src/main/kotlin/properties.kt>)                                                                 | `getPropertyOrNull`、`getPropertiesByPrefix`                                                      |
| Desktop 身份映射    | [AppBuildConfig.desktop.kt](<../../core/application/src/desktopMain/kotlin/ciyin/application/config/AppBuildConfig.desktop.kt>) | `toDesktopAppInfo`；组织名分支依赖具体生成对象类型                                                |
| Desktop 运行入口    | [Application.desktop.kt](<../../core/application/src/desktopMain/kotlin/ciyin/application/Application.desktop.kt>)              | `runApplication`；注入配置，允许显式提供 `appInfo`                                                |
| iOS 运行入口        | [Application.ios.kt](<../../core/application/src/iosMain/kotlin/ciyin/application/Application.ios.kt>)                          | `runApplication`；注入配置与平台上下文                                                            |
| 真实 Desktop 调用方 | [main.kt](<../../app/desktop/src/main/kotlin/com/ciyin/app/main.kt>)                                                            | 使用默认参数调用 `runApplication`，不是自定义配置样例                                             |
| Android 根入口      | [MainActivity.kt](<../../app/android/src/main/kotlin/com/ciyin/app/MainActivity.kt>)                                            | 当前直接进入共享 `App`，未显式提供构建配置                                                        |

平台对象应追踪生成器，而非本机 `build/` 文件：插件仅在对应源集存在时注册
`generateDesktopAppBuildConfig`、`generateAndroidAppBuildConfig`、`generateIosAppBuildConfig`
，接入生成目录与编译依赖。未启用的平台不能据此宣称已编译验证。

## 配置与平台边界

配置读取顺序由 [properties.kt](<../../buildSrc/src/main/kotlin/properties.kt>) 定义：命令行 `-P`、JVM
system property、环境变量、本地属性、Gradle 属性文件、extra properties。扩展配置按相同优先级合并，去掉
`app.config.` 前缀并按键排序。这里不把 `findProperty` 与显式命令行属性等同。

| 配置                                        | 当前用途与约束                                      |
|---------------------------------------------|-----------------------------------------------------|
| `app.id`、`app.version.name`                | 公共身份与版本；构建期读取并校验非空                |
| `app.config.*`                              | 生成 `properties` Map，值为字符串；不要注入生产密钥 |
| `app.organization`、`app.name`              | 必须同时配置或同时省略；只生成到 Desktop 对象       |
| `android.namespace`、`android.version.code` | Android 生成对象字段，不进入 common 契约            |

- Desktop：生成对象提供 `organization`、`name`，`runApplication` 注入传入配置。目录选择仍受下文默认
  companion 映射缺口影响。
- Android：生成器提供配置与
  actual。当前 [MainActivity.kt](<../../app/android/src/main/kotlin/com/ciyin/app/MainActivity.kt>)
  和共享 [App.kt](<../../app/shared/src/commonMain/kotlin/com/ciyin/app/presentation/app/App.kt>)
  没有显式覆盖 `LocalAppBuildConfig`，读取时依赖其默认值；不能描述成已实现根树参数注入。
- iOS：生成对象目前仅含公共字段；运行入口已支持显式注入。源码存在不等于已在 macOS / iOS 验证。
- 生成插件当前只注册上述三个平台；本设计不声明 Web / Wasm 支持。

## 实施结果与原方案差异

| 原方案事项         | 当前结果                                                                                                       |
|--------------------|----------------------------------------------------------------------------------------------------------------|
| 抽离内联生成任务   | 模块构建脚本已改为应用独立插件，不再内联 `GenerateDesktopAppInfoTask`                                          |
| 固定字段与扩展配置 | 两者均已实现；“是否启用 Map”不再是未决项                                                                       |
| 属性读取顺序       | 使用 `gradle.startParameter.projectProperties` 明确取得命令行覆盖，不采用原方案笼统的 `-P / findProperty` 描述 |
| 平台配置生成       | 生成器为存在的源集注册任务；不是无条件生成所有平台                                                             |
| 根树注入开放问题   | Desktop / iOS 已有配置参数和 provider；Android 保留默认值回退，显式根注入尚未落地，是否需要该能力仍待后续决定  |
| 桌面目录优先级目标 | 组织名映射分支已存在，但默认 companion 调用链未达到原方案的组织名优先预期，见下节                              |

这些结论来自当前源码，仅保留已确认的设计理由，不追认未经记录的实现动机。

## 当前限制

### Desktop 默认身份映射的已知缺口

[Desktop 运行入口](<../../core/application/src/desktopMain/kotlin/ciyin/application/Application.desktop.kt>)
默认传入 `AppBuildConfig`
companion，而 [toDesktopAppInfo](<../../core/application/src/desktopMain/kotlin/ciyin/application/config/AppBuildConfig.desktop.kt>)
通过 `this as? DesktopAppBuildConfig` 判断具体生成对象。companion 只是委托公共接口，并不是该对象，因此默认调用会回退到
`AppInfo.ApplicationId(id)`，即使构建时配置了组织名与应用名，也不能据此承诺自动选中组织目录。

只有接收者为实际 `DesktopAppBuildConfig` 且组织名配对非空时，当前映射才进入组织名分支；显式 `appInfo`
则由调用方负责。此处记录的是静态调用链结论，本次未运行程序复现，也未修改代码。原方案该项验收仍需修复和回归，不能用“已实现”标签替代验收。

### 验证与资料缺口

- 尚未在 `core/application/src`、`buildSrc/src` 找到该能力的专用测试文件；本次不虚构测试入口。
- 本次没有运行平台构建、错误配置验证、生成内容检查或目录行为回归；文档中的验证入口不代表已通过记录。
- 模块目前没有独立
  README；接入工作流可参考 [app-build-config skill](<../../.agents/skills/app-build-config/SKILL.md>)
  。本次不扩展为整模块文档建设。

## 验证记录与后续入口

核对环境：2026-11-10，Windows 工作区，源码基线 `e5de064a`。

| 检查                                   | 本次结果                                               |
|----------------------------------------|--------------------------------------------------------|
| 方案目标与源码、构建逻辑、调用方对应   | 已静态核对；实现入口与已知缺口已记录                   |
| 本地文档链接与命名                     | 已检查；使用无版本号文件名，引用已同步，不保留归档副本 |
| Gradle 编译、单测、生成任务运行        | 未执行：本次仅转换文档与新增文档工作流                 |
| Android / Desktop 运行、iOS 构建与运行 | 未执行；不声明平台验收通过                             |

后续需要验证实现时，先依照 [构建指南](<../contributing/building.md>)
和 [测试指南](<../contributing/testing.md>) 确认平台开关与任务，再选择最窄检查：

- 配置生成：启用对应源集后运行 `:core:application:generateDesktopAppBuildConfig`
  或其它平台生成任务，检查字段隔离、前缀合并与转义。
- Desktop 编译：启用 Desktop 后运行 `:core:application:compileKotlinDesktop`；应用调用链变化时再补
  `:app:desktop:compileKotlin`。
- 配置校验：分别只提供组织名、只提供应用名，确认配对错误；测试中明确清空另一项，避免本机配置使反例失效。
- 行为回归：覆盖默认 companion、实际生成对象、自定义配置及显式 `appInfo` 的目录身份映射；补充优先级冲突和空字段用例。
- iOS 验证在具备 Apple 工具链的环境执行，不能由 Windows 静态检查替代。

执行后记录日期、命令、平台与实际结果；以上仅为后续入口，不是已执行声明。

## 后续演进

优先处理已知映射缺口并补测试。YAML、JSON Schema、Dev / Test / Prod profile、Android 显式根注入以及应用层
Koin properties 装配仍为独立评估项，不属于当前已实现能力的承诺。 返回[设计文档索引](<README.md>)。
