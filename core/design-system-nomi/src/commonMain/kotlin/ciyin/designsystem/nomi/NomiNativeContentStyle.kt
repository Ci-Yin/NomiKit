package ciyin.designsystem.nomi

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * 当前组合子树中 Nomi 主题派生的原生内容色；缺省为 Unspecified，由 Nomi 主题或组件提供。
 * 不能作为其他设计系统的显式覆盖。
 */
internal val LocalNomiNativeContentColor = compositionLocalOf { Color.Unspecified }

/**
 * 当前组合子树中 Nomi 主题派生的正文排版；缺省为 TextStyle.Default，由 Nomi 主题或组件提供。
 * 显式跨系统 slot 使用独立中立上下文。
 */
internal val LocalNomiNativeTextStyle = compositionLocalOf { TextStyle.Default }
