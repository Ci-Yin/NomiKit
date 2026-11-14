package ciyin.designsystem.component

import androidx.compose.runtime.Immutable

/**
 * 原生组件公开 API 可承载的覆盖与布局能力，用于展示提示及调用边界校验。
 *
 * @property buttonSize 是否支持显式按钮尺寸预设。
 * @property buttonShape 是否支持任意按钮形状。
 * @property buttonContentPadding 是否支持精确按钮内容内边距。
 * @property cardColors 是否支持非点击卡片的容器色与内容色覆盖。
 * @property cardBorder 是否支持非点击卡片移除或指定描边。
 * @property textAutoSize 是否支持文本自动字号缩放（TextAutoSize）。
 * @property navigationBar 是否支持底部导航栏布局。
 * @property navigationRail 是否支持侧边导航栏布局。
 * @property navigationDrawer 是否支持抽屉式导航布局；默认不支持，需由实现显式声明。
 * @property navigationItemModifier 是否支持导航项级修改符；不支持时实现必须拒绝非默认 Modifier。
 */
@Immutable
data class NomiComponentCapabilities(
    val buttonSize: Boolean = true,
    val buttonShape: Boolean = true,
    val buttonContentPadding: Boolean = true,
    val cardColors: Boolean = true,
    val cardBorder: Boolean = true,
    val textAutoSize: Boolean = true,
    val navigationBar: Boolean = true,
    val navigationRail: Boolean = true,
    val navigationDrawer: Boolean = false,
    val navigationItemModifier: Boolean = true,
)
