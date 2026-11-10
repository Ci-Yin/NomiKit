---
name: app-build-config
description: 使用 NomiKit core/application 模块下 ciyin.application.config 包的跨平台应用构建配置（AppBuildConfig 接口、LocalAppBuildConfig CompositionLocal、平台生成对象 Desktop/Android/iOS AppBuildConfig、AppBuildConfig.toDesktopAppInfo()）。Use when 用户在 NomiKit 项目中需要读取应用 ID/版本名、运行时配置 Map、平台 namespace/versionCode、桌面 organization/name、把构建配置映射为桌面 AppInfo、在测试中覆盖 AppBuildConfig，或维护 core/application/config 包内代码。
---

# 应用构建配置 使用指南

`ciyin.application.config` 提供 NomiKit
的跨平台构建配置注入层。所有运行时可见的应用身份、版本名与额外运行时配置都先在编译期生成到平台源集，运行时通过统一接口消费。

## 模块边界

- 公共契约：`AppBuildConfig` 接口（`id`、`versionName`、`properties`），位于 `commonMain`。
- 平台注入：`currentAppBuildConfigImpl` 是 `commonMain` 声明的 `expect val`，由 `desktopMain` /
  `androidMain` / `iosMain` 提供 `actual`，指向当前平台生成对象。
- 默认访问：`AppBuildConfig` companion 通过 `by currentAppBuildConfigImpl` 委托，调用方写
  `AppBuildConfig.id` 即可拿到当前平台值。
- Compose 注入：`LocalAppBuildConfig` 使用 `staticCompositionLocalOf`，默认回退到 `AppBuildConfig`
  companion；运行入口一般会再覆盖为传入的实例。
- 平台字段：Desktop 的 `organization` / `name`，Android 的 `namespace` / `versionCode` 只存在于各自平台源集生成的
  `XxxAppBuildConfig` 对象中，不进入 common 接口。
- Desktop 映射：`AppBuildConfig.toDesktopAppInfo()`（`desktopMain` 扩展函数）把构建配置翻译成
  `core/system` 的 `AppInfo`，供 `AppFolderResolver` 使用。

## 接口与契约

```kotlin
package ciyin.application.config

interface AppBuildConfig {
    val id: String
    val versionName: String
    val properties: Map<String, String>

    companion object : AppBuildConfig by currentAppBuildConfigImpl
}

internal expect val currentAppBuildConfigImpl: AppBuildConfig
```

注意事项：

- 不要在业务代码里手动实现 `currentAppBuildConfigImpl`，它由 `buildSrc` 的 `app-build-config`
  插件按平台生成。
- `properties` 是 `Map<String, String>`，key 已经是去掉 `app.config.` 前缀后的形式（例如
  `example.enabled`）。
- 需要访问平台特有字段时，先 `as?` 转成具体平台对象（`as? DesktopAppBuildConfig`、
  `as? AndroidAppBuildConfig`），common 代码不要直接引用平台字段。

## Compose 注入

```kotlin
package ciyin.application.config

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppBuildConfig = staticCompositionLocalOf<AppBuildConfig> {
    AppBuildConfig
}
```

注意事项：

- `staticCompositionLocalOf` 表示值变化频率低；高频变化请改用普通 `compositionLocalOf`，避免重建整棵子树。
- 平台根 Compose 入口应在 `CompositionLocalProvider(LocalAppBuildConfig provides appBuildConfig)`
  内提供实例；非根节点访问会回退到 `AppBuildConfig` companion。
- UI 组件读取配置统一通过 `LocalAppBuildConfig.current`，不要在 `@Composable` 里直接读
  companion；companion 仅用于测试和非 Compose 上下文。

## 平台生成对象

每个平台源集生成一个 `object`，实现 `AppBuildConfig` 并把平台特有字段放在各自源集。

### desktopMain

```kotlin
object DesktopAppBuildConfig : AppBuildConfig {
    override val id: String = "com.ciyin.nomikit"
    override val versionName: String = "1.0.0"
    override val properties: Map<String, String> = emptyMap()

    val organization: String? = null
    val name: String? = null
}

internal actual val currentAppBuildConfigImpl: AppBuildConfig = DesktopAppBuildConfig
```

### androidMain

```kotlin
object AndroidAppBuildConfig : AppBuildConfig {
    override val id: String = "com.ciyin.nomikit"
    override val versionName: String = "1.0.0"
    override val properties: Map<String, String> = emptyMap()

    val namespace: String = "com.ciyin.app"
    val versionCode: Int = 100000
}

internal actual val currentAppBuildConfigImpl: AppBuildConfig = AndroidAppBuildConfig
```

### iosMain

```kotlin
object IosAppBuildConfig : AppBuildConfig {
    override val id: String = "com.ciyin.nomikit"
    override val versionName: String = "1.0.0"
    override val properties: Map<String, String> = emptyMap()
}

internal actual val currentAppBuildConfigImpl: AppBuildConfig = IosAppBuildConfig
```

注意事项：

- 平台字段（`organization` / `name` / `namespace` / `versionCode`）只能出现在对应平台的生成对象里；后续新增平台字段时同样只放到对应源集。
- 不要在 `commonMain` 中通过 `as?` 之外的方式访问平台字段；平台入口、Compose 根节点等需要平台字段的代码应该位于该平台源集。
- `app.organization` 和 `app.name` 必须同时配置或同时省略，错误信息保持中文。

## Desktop 映射 AppInfo

`AppBuildConfig.toDesktopAppInfo()` 是 `desktopMain` 的扩展函数，把构建配置翻译成
`ciyin.system.storage.AppInfo`：

```kotlin
fun AppBuildConfig.toDesktopAppInfo(): AppInfo {
    val desktopBuildConfig = this as? DesktopAppBuildConfig
    return if (
        desktopBuildConfig != null &&
        !desktopBuildConfig.organization.isNullOrBlank() &&
        !desktopBuildConfig.name.isNullOrBlank()
    ) {
        AppInfo.OrganizationName(
            qualifier = id.substringBefore('.'),
            organization = desktopBuildConfig.organization,
            name = desktopBuildConfig.name,
        )
    } else {
        AppInfo.ApplicationId(id)
    }
}
```

映射规则：

- 仅当接收者为实际 `DesktopAppBuildConfig` 且组织名、应用名同时非空时，返回
  `AppInfo.OrganizationName(qualifier, organization, name)`；`qualifier` 取 `id` 的首段。
- 其它情况回退到 `AppInfo.ApplicationId(id)`；构建期只配置一个目录名会被配对校验拒绝。
- 已知缺口：默认 `runApplication` 传入的是 `AppBuildConfig` companion，不是生成对象，
  因此当前默认映射不会因配置组织名而进入组织名分支。设计与源码依据见
  [AppBuildConfig 实现说明](<../../../.docs/design/appbuildconfig-注入配置重构方案.md>)
  ；不要把委托误认为具体类型。
- 仅 `desktopMain` 调用此扩展；其它平台源集直接使用平台自带的目录解析逻辑。

## 运行时使用

UI 组件统一从 CompositionLocal 读取：

```kotlin
@Composable
fun AppHeader() {
    val buildConfig = LocalAppBuildConfig.current
    Text("v${buildConfig.versionName} (${buildConfig.id})")
}
```

测试中覆盖构建配置：

```kotlin
runApplication(
    createApplication = ::DesktopApplication,
    appBuildConfig = object : AppBuildConfig {
        override val id: String = "com.ciyin.test"
        override val versionName: String = "0.0.1"
        override val properties: Map<String, String> = mapOf("example.enabled" to "true")
    },
) {
    App()
}
```

读取 `properties`：

```kotlin
val enabled = LocalAppBuildConfig.current.properties["example.enabled"] == "true"
```

注意事项：

- `properties` 值永远是 `String`，业务侧需要自行转换类型；常见转换模式：布尔用 `"true"` / `"false"`、数字用
  `toIntOrNull()`。
- 不要把生产密钥写进 `app.config.*` 注入 `properties`，生成结果会进入产物。
- 默认场景下（非测试）业务层不要 new `AppBuildConfig`；直接读 `LocalAppBuildConfig.current` 或
  `AppBuildConfig` companion。

## 与其他模块的边界

- `core/system`：桌面目录解析依赖 `AppInfo`，由 `AppBuildConfig.toDesktopAppInfo()` 产生映射。
- `core/platform`：`LocalContext` 与 `LocalAppBuildConfig` 由平台入口一起注入；UI 根节点同时消费两者。
- `app/shared`：业务初始化可以读 `AppBuildConfig.properties`，但 KoinBoot 等 DI 注册不放在本模块。
- `buildSrc`：负责生成平台对象的 Gradle 任务和属性读取优先级；本 skill 不覆盖插件实现细节。

## 修改注意

- 新增跨平台公共字段：扩展 `AppBuildConfig` 接口并在三个平台源集的生成对象中实现对应属性；属性值由
  Gradle 任务负责注入。
- 新增平台特有字段：仅在对应平台的 `XxxAppBuildConfig` 对象中添加，不要进入 common。
- 修改 `AppBuildConfig.toDesktopAppInfo()` 时保持组织名配对的语义，错误信息使用中文。
- 修改本模块后优先运行：

```powershell
.\gradlew.bat :core:application:compileCommonMainKotlinMetadata --console=plain
```

涉及平台入口时按改动范围补充：

```powershell
.\gradlew.bat :core:application:compileKotlinDesktop --console=plain
.\gradlew.bat :core:application:compileDebugKotlinAndroid --console=plain
```
