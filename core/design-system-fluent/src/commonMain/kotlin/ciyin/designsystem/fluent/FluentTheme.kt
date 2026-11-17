package ciyin.designsystem.fluent

import androidx.compose.runtime.Immutable
import io.github.composefluent.Colors
import io.github.composefluent.CornerRadius
import io.github.composefluent.Typography

/**
 * Fluent 原生主题规格，经 `NomiTheme.() -> FluentTheme` 映射交给实现安装。
 *
 * 与 `io.github.composefluent.FluentTheme` 同名但职责不同：本类型是可持久保存的纯数据，
 * 只描述本层要施加的原生值，不读取、也不发布组合环境。为 null 的组表示继承当前原生 Fluent 值，
 * 而不是把 null 解释成透明或默认值。
 *
 * @property isDark 是否使用深色原生基线，决定未指定色板时使用哪份原生色板。
 * @property colors 完整原生色板；null 回退到 [isDark] 对应的 `lightColors()` / `darkColors()`。
 * @property typography 完整八角色原生排版；null 继承当前原生 Fluent 排版。
 * @property cornerRadius 完整三档原生圆角；null 继承当前原生 Fluent 圆角。
 * @property useAcrylicPopup 弹层是否使用 acrylic 材质；默认启用，对应原生 FluentTheme 的默认值。
 * @property compactMode 是否使用紧凑密度；默认启用，对应原生 FluentTheme 的默认值。
 */
@Immutable
data class FluentTheme(
    val isDark: Boolean = false,
    val colors: Colors? = null,
    val typography: Typography? = null,
    val cornerRadius: CornerRadius? = null,
    val useAcrylicPopup: Boolean = true,
    val compactMode: Boolean = true,
)
