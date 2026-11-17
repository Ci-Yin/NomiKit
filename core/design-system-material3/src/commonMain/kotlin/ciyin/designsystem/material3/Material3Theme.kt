package ciyin.designsystem.material3

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable

/**
 * 一次 Material 3 映射的结果，描述安装到 MaterialTheme 的原生组。
 *
 * 可空组为 null 时：色板取 Material 3 原生默认，排版与形状继承当前 MaterialTheme；
 * 因此映射函数决定哪些中立组参与原生主题，未参与的中立组不影响原生视觉。
 *
 * @property isDark 是否使用深色原生基线，决定未显式提供色板时的默认值。
 * @property colorScheme 完整原生色板；null 时按 [isDark] 选择 Material 默认色板。
 * @property typography 完整原生排版；null 时继承当前 MaterialTheme 的排版。
 * @property shapes 完整原生形状；null 时继承当前 MaterialTheme 的形状。
 */
@Immutable
data class Material3Theme(
    val isDark: Boolean = false,
    val colorScheme: ColorScheme? = null,
    val typography: Typography? = null,
    val shapes: Shapes? = null,
)
