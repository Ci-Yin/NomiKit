---
name: koinboot
description: Use NomiKit component/koin and the koin-boot-initializer Gradle convention plugin to add or maintain KoinBoot configuration properties, automatic Koin definitions, lifecycle extenders, and generated BootInitializer aggregation for Kotlin Multiplatform modules. Use when adapting a third-party library to KoinBoot, wiring runKoinBoot, adding KoinPropInstance/KoinAutoConfiguration/KoinLifecycleExtender/LibraryBootInitializer, configuring koinBootInitializer include/includes, or diagnosing generated AppBootInitializer, initializer naming/package, property mapping, override ordering, and generateKoinBootInitializer failures.
---

# KoinBoot 适配与使用

使用 `component/koin` 把类型安全配置、容器生命周期和可让位的默认依赖装配组合起来，再使用
`koin-boot-initializer` 约定插件为消费模块生成统一的 BootInitializer 入口。

## 先核对实时实现

开始修改前先读取当前工作区的 `.agents/rules/AGENTS.md`，再按任务范围读取：

- 理解设计目标与整体协作：`.docs/guides/koinboot-introduce.md`。
- 设计第三方库适配：`.docs/guides/koinboot-third-party-library-adaptation-guide.md`。
- 修改或排查聚合逻辑：`buildSrc/src/main/kotlin/koin-boot-initializer.gradle.kts`。
- 核对真实 API：`component/koin/src/commonMain/kotlin/ciyin/koin/` 及其 `configuration/`。
- 参考现有适配：`component/data-store`、`component/room` 及其对应 skill。

指南中的旧包名、旧类型名和“自动扫描依赖”表述可能落后于源码。发生冲突时，以当前源码、当前生成文件和可运行测试为准。

## 理解四层职责

按以下边界设计，不要把所有初始化逻辑都塞进一个 BootInitializer：

1. `KoinProperties`：保存扁平键值配置，并通过 `@KoinPropInstance` 映射成类型安全对象。
2. `KoinAutoConfiguration`：在 `ModulesLoading` 阶段提供默认 Koin 定义；用户已声明同类型或同限定符定义时应让位。
3. `KoinLifecycleExtender`：在容器特定阶段执行无法表示成 Koin 定义的全局初始化、启动或清理。
4. `KoinBootInitializer`：只负责把一个模块的自动配置和生命周期扩展器注册到 `KoinBootDSL`。

当前实际阶段顺序为：

```text
Starting -> Configuring -> PropertiesLoading -> ModulesLoading -> Ready -> Running
                                                                          |
                                                                       Stopping -> Stopped
```

`Configuring` 时使用 `context.properties` 读取 DSL 已收集的配置，不要假定业务 Koin 定义已经加载。
`ModulesLoading` 扩展器回调发生在模块加载动作之前；需要解析最终 Koin 实例时使用 `Ready` 或更晚阶段。
`Stopping` 扩展器在 `stopKoin()` 前执行，适合释放全局资源。

## 选择适配方式

先按行为选择最小机制：

- 只需向 Koin 提供客户端、工厂、存储或服务默认实现：使用 `KoinAutoConfiguration`。
- 需要调用第三方全局 `init/start/close/shutdown`，且操作依赖启动阶段：使用 `KoinLifecycleExtender`。
- 同时需要默认 DI 定义和全局生命周期：两者都实现，由同一个 BootInitializer 注册。
- 没有可配置项：不要为了形式新增 Properties 类。
- 存在平台实现差异：把公共契约留在 `commonMain`，通过现有平台抽象或 `expect/actual` 实现平台行为。

优先适配可复用能力。不要把产品账号、密钥、业务协议、应用资源或 `app/*` 实现下沉到公共模块。

## 定义类型安全配置

使用 `@KoinPropInstance("前缀")` 定义带默认值的配置对象。仅当调用方需要 DSL 代码提示时，再为稳定配置项提供
`KoinProperties` 扩展属性和键常量。新增 Kotlin 声明遵守项目的中文 KDoc 规则。

```kotlin
package ciyin.mylib

import ciyin.koin.KoinPropInstance
import ciyin.koin.KoinProperties

/**
 * MyLib 配置。
 *
 * @property enabled 是否启用 MyLib。
 * @property timeoutMillis 请求超时时间。
 */
@KoinPropInstance("mylib")
data class MyLibProperties(
    val enabled: Boolean = true,
    val timeoutMillis: Long = 30_000L,
) {
    /** MyLib 配置键。 */
    companion object {
        /** MyLib 启用状态属性键。 */
        const val MYLIB_ENABLED = "mylib.enabled"

        /** MyLib 超时时间属性键。 */
        const val MYLIB_TIMEOUT_MILLIS = "mylib.timeoutMillis"
    }
}

/** MyLib 启用状态。 */
var KoinProperties.mylib_enabled: Boolean
    get() = (this[MyLibProperties.MYLIB_ENABLED] as Boolean?)
        ?: MyLibProperties().enabled
    set(value) {
        MyLibProperties.MYLIB_ENABLED(value)
    }
```

遵守以下约束：

- 让属性键与 `preKey + 序列化字段名` 对齐；嵌套类型需要被单独读取时，也为该类型声明准确的 `@KoinPropInstance`。
- `asPropInstance` 在没有匹配键时返回 `null`，不会自动实例化默认对象；调用处显式回退到 `XxxProperties()`。
- 使用 `propertyInstance<XxxProperties>()` 或 `koin.getPropInstance<XxxProperties>()`，不要在适配代码中重复手写 Map 解析。
- 为配置提供安全默认值；密钥和环境专属值由消费方注入，不要写入公共模块。

## 实现自动配置

让业务模块先加载，再由自动配置检查最终是否缺少默认定义。对简单构造函数优先使用 `singleOf(::Type)`；只有构建器或
配置值无法直接注入时才使用显式 lambda。

```kotlin
package ciyin.mylib

import ciyin.koin.configuration.koinAutoConfiguration
import ciyin.koin.configuration.onMissInstances

/** MyLib 默认依赖装配。 */
internal val MyLibAutoConfiguration = koinAutoConfiguration(
    match = {
        propertyInstance<MyLibProperties>()?.enabled ?: MyLibProperties().enabled
    },
) {
    val properties = propertyInstance<MyLibProperties>() ?: MyLibProperties()

    module {
        onMissInstances<MyLibClient> {
            single<MyLibClient> {
                MyLibClient(timeoutMillis = properties.timeoutMillis)
            }
        }
    }
}
```

检查以下行为：

- 对每个可覆盖的默认类型或限定符使用 `onMissInstances`；检查类型必须与最终注册类型一致。
- 使用 `match` 控制整项自动配置是否生效，使用 `onExistProperties`、`onMissProperties`、`onEqProperty` 处理局部条件。
- 仅在自动配置之间确有依赖时设置 `order`；数字越小越先执行，不要依赖同序配置的偶然顺序。
- 不要覆盖用户定义或通过重复定义赌 Koin 的加载顺序。

## 实现生命周期扩展器

只在存在容器级副作用时增加扩展器，并在对称阶段释放资源。

```kotlin
package ciyin.mylib

import ciyin.koin.KoinBootContext
import ciyin.koin.KoinLifecycleExtender
import ciyin.koin.asPropInstance

/** 管理 MyLib 的容器生命周期。 */
internal class MyLibExtender : KoinLifecycleExtender {
    /** 在配置阶段启动 MyLib。 */
    override fun doConfiguring(context: KoinBootContext) {
        val properties = context.properties.asPropInstance<MyLibProperties>()
            ?: MyLibProperties()
        if (!properties.enabled) return

        MyLib.start(timeoutMillis = properties.timeoutMillis)
    }

    /** 在停止阶段释放 MyLib 资源。 */
    override fun doStopping(context: KoinBootContext) {
        MyLib.close()
    }
}
```

不要在早期阶段解析尚未加载的业务依赖。不要吞掉初始化异常；让 KoinBoot 报告失败阶段并中止启动。

## 暴露模块 BootInitializer

为可聚合模块在 `commonMain` 暴露一个顶层入口。当前生成器固定导入 `ciyin.<InitializerName>`，因此入口必须位于
`ciyin` 包，名称默认与 Gradle 项目名推导结果一致。

```kotlin
package ciyin

import ciyin.koin.KoinBootInitializer
import ciyin.mylib.MyLibAutoConfiguration
import ciyin.mylib.MyLibExtender

/** MyLib 的 KoinBoot 初始化入口。 */
val MyLibBootInitializer: KoinBootInitializer = {
    autoConfigurations(MyLibAutoConfiguration)
    extenders(MyLibExtender())
}
```

调用时使用 `MyLibBootInitializer()`。不要沿用旧文档中的 `MyLibBootInitializer(this)` 写法。

默认名称推导会按 `-` 和 `_` 拆分依赖名并把每段首字母大写，例如 `data-store` 推导为
`DataStoreBootInitializer`。缩写或实际名称不一致时，在消费方显式传入名称。

## 配置聚合插件

在需要统一入口的消费模块应用插件，并让同一组依赖同时进入 KMP 源集和聚合配置：

```kotlin
plugins {
    `multiplatform-lib-targets`
    `koin-boot-initializer`
}

val bootDependencies = listOf<Dependency>(
    projects.component.room,
    projects.component.dataStore,
)

kotlin {
    sourceSets.commonMain.dependencies {
        bootDependencies.forEach(::api)
    }
}

koinBootInitializer {
    includes(bootDependencies)
}
```

该插件不会扫描 classpath 中的 BootInitializer。它只把 `include/includes` 显式列出的依赖映射为初始化器名称，去重后生成
`build/generated/koin_boot_initializer/<GeneratedName>.kt`，并把目录接入 common 源集。禁止手改生成文件。

需要覆盖推导名称时使用：

```kotlin
koinBootInitializer {
    includes(
        projects.feature.sdwebui to "SdWebUiBootInitializer",
    )
}
```

`generatedPackage` 默认是 `ciyin.generated`，`generatedInitializerName` 默认是 `AppBootInitializer`，仅在消费方确有命名冲突时修改。
当前生成任务虽然接收 `packageName`，但生成 import 时仍固定使用 `ciyin`；不要依赖其他初始化器包名，除非先修改并验证插件实现。
`include(dependency)` 还要求 `dependency.group` 非空；依赖被遗漏时检查这一点，并可对 `ciyin` 包入口显式使用
`include("ciyin", "ExactBootInitializer")`。

在应用入口调用生成结果：

```kotlin
import ciyin.generated.AppBootInitializer
import ciyin.koin.runKoinBoot
import ciyin.mylib.mylib_enabled

/** 初始化应用依赖容器。 */
fun initKoin() {
    runKoinBoot {
        AppBootInitializer()
        properties {
            mylib_enabled = true
        }
        module {
            // 声明业务依赖。
        }
    }
}
```

## 测试与验证

适配模块至少覆盖：

- 未提供自定义定义时，默认实例可解析。
- 用户预先提供同类型或同限定符定义时，默认配置让位。
- 默认配置和自定义 Properties 都能得到预期行为。
- 使用生命周期扩展器时，阶段选择、禁用开关、启动失败和清理行为正确。
- 存在平台代码时，相关平台源码集可编译。

按改动范围运行最窄任务，例如：

```powershell
.\gradlew.bat :component:<module>:desktopTest --console=plain
.\gradlew.bat :app:shared:generateKoinBootInitializer --console=plain
.\gradlew.bat :app:shared:compileCommonMainKotlinMetadata --console=plain
```

生成后检查 `build/generated/koin_boot_initializer/` 中的包名、import、调用名称和顺序；生成成功不等于消费方编译成功，至少再编译一个真实消费目标。

## 故障定位

- `AppBootInitializer` 无法解析：确认消费模块已应用插件、import 与 `generatedPackage` 一致，并先运行生成任务。
- 生成文件缺少某模块：确认该依赖出现在 `include/includes`、`dependency.group` 非空，并检查去重后的名称。
- 生成文件中的具体 initializer 无法解析：对照模块的 `package ciyin` 和顶层名称；缩写不一致时显式覆盖名称。
- 用户实现没有覆盖默认实现：确认用户定义通过同一次 `runKoinBoot` 的 `modules/module` 注册，并检查类型与 Qualifier 是否完全一致。
- Properties 总是回退默认值：检查键、`@KoinPropInstance.preKey`、字段名和实际 DSL 写入值；无匹配键时返回 `null` 是预期行为。
- 生命周期阶段解析依赖失败：把依赖解析移到 `Ready` 或更晚阶段，或把它建模为自动配置定义。

完成修改后同步检查涉及模块自己的 `.agents/skills/<module>/SKILL.md`，避免 KoinBoot API、入口名称或验证命令与真实模块漂移。
