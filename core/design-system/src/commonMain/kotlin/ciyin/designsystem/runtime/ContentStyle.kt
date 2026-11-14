package ciyin.designsystem.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * 显式内容 slot 颜色；Unspecified 留给当前原生环境解析。
 */
val LocalNomiContentColor = compositionLocalOf { Color.Unspecified }

/**
 * 显式内容 slot 排版增量；不能在组件派发前填入统一正文排版。
 */
val LocalNomiTextStyle = compositionLocalOf { TextStyle.Default }

/**
 * 在当前 slot 内覆盖指定的颜色和排版，未指定值继承父级，退出内容作用域后恢复父环境。
 *
 * @param color 显式内容色；Unspecified 继承父 slot 颜色，Transparent 明确覆盖为透明。
 * @param textStyle 合并到父 slot 文本样式的增量；TextStyle.Default 不主动填入正文默认值。
 * @param content 在新内容样式环境中组合的子树；该函数不维护业务状态。
 */
@Composable
fun ProvideNomiContentStyle(
    color: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalNomiContentColor provides if (color == Color.Unspecified) LocalNomiContentColor.current else color,
        LocalNomiTextStyle provides LocalNomiTextStyle.current.merge(textStyle),
        content = content,
    )
}
