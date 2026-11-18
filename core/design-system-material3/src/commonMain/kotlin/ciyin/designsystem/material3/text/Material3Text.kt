package ciyin.designsystem.material3.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as NativeText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import ciyin.designsystem.component.text.NomiTextRenderer
import ciyin.designsystem.component.text.NomiTextSpec

/**
 * 原生文字适配，不把中立正文排版强加给原生 slot。
 */
object Material3TextRenderer : NomiTextRenderer {
    /**
     * 无显式样式时使用原生 LocalTextStyle，逐字段覆盖只作用于已指定字段。
     *
     * @param text 待显示的纯文本。
     * @param modifier 原生 Text 的布局与语义修饰符。
     * @param style null 保留原生 LocalTextStyle；非 null 按字段合并。
     * @param color 已由核心解析的颜色；Unspecified 留给原生样式和内容色，透明色仍显式生效。
     * @param fontSize 显式字号；Unspecified 沿用 style 与原生环境。
     * @param fontStyle 显式字形；null 沿用 style 与原生环境。
     * @param fontWeight 显式字重；null 沿用 style 与原生环境。
     * @param fontFamily 显式字体族；null 沿用 style 与原生环境。
     * @param letterSpacing 显式字距；Unspecified 沿用 style 与原生环境。
     * @param textDecoration 显式文本装饰；null 沿用 style 与原生环境。
     * @param textAlign 显式对齐；null 沿用 style 与原生环境。
     * @param lineHeight 显式行高；Unspecified 沿用 style 与原生环境。
     * @param overflow 文本超出可用空间或行数时的显示方式。
     * @param softWrap 是否允许按宽度自动换行。
     * @param maxLines 最大显示行数，交由原生 Text 校验。
     * @param minLines 最小占用行数，交由原生 Text 校验。
     * @param onTextLayout 布局完成回调，原样交给原生 Text。
     * @param autoSize 自动字号策略，原样交给原生 Text；null 保留固定字号。
     * @param nativeSpec 文本专属参数；当前无实现，原样接受并忽略。
     */
    @Composable
    override fun Text(
        text: String,
        modifier: Modifier,
        style: TextStyle?,
        color: Color,
        fontSize: TextUnit,
        fontStyle: FontStyle?,
        fontWeight: FontWeight?,
        fontFamily: FontFamily?,
        letterSpacing: TextUnit,
        textDecoration: TextDecoration?,
        textAlign: TextAlign?,
        lineHeight: TextUnit,
        overflow: TextOverflow,
        softWrap: Boolean,
        maxLines: Int,
        minLines: Int,
        onTextLayout: ((TextLayoutResult) -> Unit)?,
        autoSize: TextAutoSize?,
        nativeSpec: NomiTextSpec?,
    ) {
        NativeText(
            text = text,
            modifier = modifier,
            style = LocalTextStyle.current.merge(style ?: TextStyle.Default),
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
            autoSize = autoSize,
        )
    }

    /**
     * 富文本逐字段透传原生 Text，内联内容映射原样交给原生布局。
     *
     * @param text 待显示的富文本。
     * @param modifier 原生 Text 的布局与语义修饰符。
     * @param style null 保留原生 LocalTextStyle；非 null 按字段合并。
     * @param color 已由核心解析的颜色；Unspecified 留给原生样式和内容色，透明色仍显式生效。
     * @param fontSize 显式字号；Unspecified 沿用 style 与原生环境。
     * @param fontStyle 显式字形；null 沿用 style 与原生环境。
     * @param fontWeight 显式字重；null 沿用 style 与原生环境。
     * @param fontFamily 显式字体族；null 沿用 style 与原生环境。
     * @param letterSpacing 显式字距；Unspecified 沿用 style 与原生环境。
     * @param textDecoration 显式文本装饰；null 沿用 style 与原生环境。
     * @param textAlign 显式对齐；null 沿用 style 与原生环境。
     * @param lineHeight 显式行高；Unspecified 沿用 style 与原生环境。
     * @param overflow 文本超出可用空间或行数时的显示方式。
     * @param softWrap 是否允许按宽度自动换行。
     * @param maxLines 最大显示行数，交由原生 Text 校验。
     * @param minLines 最小占用行数，交由原生 Text 校验。
     * @param inlineContent 替换文本区段的内联组合映射，原样交给原生 Text。
     * @param onTextLayout 布局完成回调，原样交给原生 Text。
     * @param autoSize 自动字号策略，原样交给原生 Text；null 保留固定字号。
     * @param nativeSpec 文本专属参数；当前无实现，原样接受并忽略。
     */
    @Composable
    override fun Text(
        text: AnnotatedString,
        modifier: Modifier,
        style: TextStyle?,
        color: Color,
        fontSize: TextUnit,
        fontStyle: FontStyle?,
        fontWeight: FontWeight?,
        fontFamily: FontFamily?,
        letterSpacing: TextUnit,
        textDecoration: TextDecoration?,
        textAlign: TextAlign?,
        lineHeight: TextUnit,
        overflow: TextOverflow,
        softWrap: Boolean,
        maxLines: Int,
        minLines: Int,
        inlineContent: Map<String, InlineTextContent>,
        onTextLayout: ((TextLayoutResult) -> Unit)?,
        autoSize: TextAutoSize?,
        nativeSpec: NomiTextSpec?,
    ) {
        NativeText(
            text = text,
            modifier = modifier,
            style = LocalTextStyle.current.merge(style ?: TextStyle.Default),
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            inlineContent = inlineContent,
            onTextLayout = onTextLayout ?: {},
            autoSize = autoSize,
        )
    }
}
