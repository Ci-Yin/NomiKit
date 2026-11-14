package ciyin.designsystem.runtime

import androidx.compose.runtime.Immutable
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.DarkMode

/**
 * 原生主题建立前的稀疏请求；不提前生成其他设计系统的默认令牌。
 *
 * @property requestedMode 调用方请求的明暗模式，保留 System 等原始意图。
 * @property isDark 主题边界已解析的实际深色状态，供适配器选择原生默认值。
 * @property overrides 合并后的显式覆盖；空配置表示没有额外指定视觉组。
 */
@Immutable
data class NomiThemeRequest(
    val requestedMode: DarkMode,
    val isDark: Boolean,
    val overrides: NomiThemeOverrides = NomiThemeOverrides(),
)
