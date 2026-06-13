---
name: core-testing
description: Use the core/testing Kotlin Multiplatform testing helper module (package ciyin.testing). Covers DynamicTest/DynamicTestsResult, dynamicTest, runDynamicTests overloads and builder, readTestResourceAsString/readTestResourceAsByteArray, DisabledOnAndroid, DisabledOnNative, EnabledOnlyOnDesktop, and Sample. Use when 用户要在 NomiKit 中写 KMP 单测、动态测试、读取测试资源、做平台条件测试或排查 core/testing。
---

# core/testing 使用指南

`core/testing` 提供 NomiKit KMP 测试辅助 API，包名是 `ciyin.testing`。它面向测试代码使用，不放生产逻辑。

## 动态测试

```kotlin
@TestFactory
fun cases(): DynamicTestsResult = runDynamicTests {
    add("case 1") {
        assertEquals(1, 1)
    }
}
```

注意事项：

- `DynamicTest` / `DynamicTestsResult` 是 expect/actual；JVM actual 对接 JUnit 5。
- `runDynamicTests(...)` 的返回值必须从标注 `@TestFactory` 的函数返回，否则部分平台不会执行动态测试。
- builder 的 `add(displayName, action)` 适合生成多组命名用例。

## 测试资源

```kotlin
val text = this.readTestResourceAsString("fixtures/sample.json")
val bytes = this.readTestResourceAsByteArray("fixtures/image.png")
```

注意事项：

- `readTestResourceAsString` / `readTestResourceAsByteArray` 是平台 actual，实现路径与资源打包方式有关。
- 路径按测试资源根目录传入，不要写绝对路径。
- 大二进制资源优先用 ByteArray 入口。

## 平台条件与 sample

```kotlin
@DisabledOnNative
class ParserJvmOnlyTest

@Sample
fun usageSample() { ... }
```

注意事项：

- `DisabledOnAndroid`、`DisabledOnNative`、`EnabledOnlyOnDesktop` 是 optional expectation，用于跨平台条件测试。
- `Sample` 是 source retention 标记，主要服务 KDoc `@sample` 与 suppress unused。

## 修改注意

- 新增测试 helper 时保持测试专用边界，不要被生产模块依赖来实现运行时逻辑。
- 修改本模块后优先运行 `.\gradlew.bat :core:testing:compileCommonMainKotlinMetadata --console=plain`。
