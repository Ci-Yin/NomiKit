---
name: core-lang
description: Use the core/lang Kotlin Multiplatform small language-extension helper module (package ciyin.lang). Covers String.format, regex match helpers, isChinese/isHttp, Long/Int formatUnit, decimals/calPct, MutableCollection.toggle, ArrayDeque stack helpers, list replace/sub/filter2/findIndex/contains, numberList, ifZero, and Any.unit. Use when 用户要在 NomiKit 中使用基础字符串、数字、集合扩展，或排查 core/lang 行为。
---

# core/lang 使用指南

`core/lang` 放置少量业务无关的 Kotlin 基础扩展，包名为 `ciyin.lang`。优先使用 Kotlin 标准库；只有项目已有 helper 能表达意图时再引入本模块。

## 字符串 helper

```kotlin
val text = String.format("Price: %.2f", 12.345)
val id = "post-123".match("\\d+")
val groups = "A:42".matchGroup("(\\w+):(\\d+)")
val hasChinese = "标题".isChinese()
val isUrl = "https://example.com".isHttp()
```

注意事项：

- `String.format` 是项目自实现的多平台格式化，只支持 `%s`、`%d`、`%f`、`%.nf`、`%%`，不是 JVM 完整 formatter。
- `%d` / `%f` 转换失败会给默认值 `0` / `0.0`，不要用于需要严格校验的解析。
- `match()` 有捕获组时返回第一个捕获组；无捕获组时返回整个匹配；无匹配返回空字符串。
- `isChinese()` 只覆盖常用 CJK 范围，不等价于完整 Unicode 汉字识别。

## 数字 helper

```kotlin
val compact = 12345L.formatUnit(decimal = 1, isChinese = true)
val rounded = decimals(3.14159, decimal = 2)
val fallback = count.ifZero { 1 }
```

注意事项：

- `Long.formatUnit` 支持英文千进位和中文万进位单位。
- `Int.formatUnit()` 使用默认参数转成 `Long.formatUnit()`。
- `calPct(a, b, c)` 没有处理 `a == b` 的零区间，调用前自行保证范围有效。

## 集合 helper

```kotlin
selected.toggle(id)
items.replace(old, new)
items.subAll(newItems)
val index = items.findIndex { it.id == id }
```

注意事项：

- `MutableCollection.toggle` 依赖 `add()` 返回值；对 `Set` 是切换语义，对 `MutableList` 通常会直接追加而不是移除，使用前确认集合类型。
- `sub` / `subAll` 会先 `clear()` 再添加，适合“替换列表内容”，不适合保留旧引用语义的复杂状态。
- `replace(index, value)` 在 `index == -1` 时返回 `false`。
- `filter2` 会原地清空再添加满足条件的元素；不可变集合不要用它。

## 修改注意

- 这里是基础层，不要加入业务命名、业务模型或 UI 依赖。
- 新增 helper 前先确认标准库是否已足够，避免制造难发现的别名。
- 修改本模块后优先运行 `.\gradlew.bat :core:lang:compileCommonMainKotlinMetadata --console=plain`。
