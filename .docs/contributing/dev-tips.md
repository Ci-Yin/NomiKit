# 开发提示

## UI

### 预览 Compose UI

自 IntelliJ 2025.1 和 Android Studio 对应的新版本起，Compose UI 预览可以直接在 commonMain 中使用。
只需编写一个没有参数的 composable 并增加 `@Preview` 注解即可。

```kotlin
@Composable
fun MyUI(
    text: String
) {
    Text(text)
}

@Preview
@Composable
private fun PreviewMyUI() {
    MyUI("Hello, World!")
}
```

请将预览代码放在与主代码相同的文件中，并添加 `private` 修饰符(防止污染全局)。

> #### 如果你仍然使用旧版本 IDE (小于 2025.1)，请参考以下内容
> 因为项目目前支持 Android 和桌面 JVM 两个平台，预览也就分两个平台。
> 绝大部分 UI 代码在 Android 的桌面的效果是一模一样的，因此使用一个平台的预览即可。
>
> 在 common 中使用 `@Preview` 将进行桌面平台的预览，但桌面预览不支持可交互式预览 (Interactive Mode),
> 也不支持即时刷新，不推荐。
>
> 在开发时，通常建议在 `androidMain` 中编写预览代码。你可以通过 IDE 分屏功能将 Android 预览放到一边。
>

#### 查找已有页面的预览

用 IDE 查找想要预览的 Composable 的实现就能找到 (按住 Ctrl 点击函数名)。许多页面都有 Android
的预览。


