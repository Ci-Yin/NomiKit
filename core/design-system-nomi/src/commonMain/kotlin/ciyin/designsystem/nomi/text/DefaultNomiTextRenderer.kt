package ciyin.designsystem.nomi.text

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
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
import ciyin.designsystem.nomi.LocalNomiNativeContentColor
import ciyin.designsystem.nomi.LocalNomiNativeTextStyle
import ciyin.designsystem.theme.NomiTheme

/**
 * Nomi 默认文本渲染器。
 */
internal object DefaultNomiTextRenderer : NomiTextRenderer {
    /**
     * 将逐字段显式覆盖折入解析样式，保持未指定字段沿用 Nomi 基础排版与原生内容样式。
     *
     * @receiver 已合并 Nomi 基础排版、原生内容样式与显式 slot 增量的样式。
     * @param fontSize 显式字号；Unspecified 不覆盖样式。
     * @param fontStyle 显式字形；null 不覆盖样式。
     * @param fontWeight 显式字重；null 不覆盖样式。
     * @param fontFamily 显式字体族；null 不覆盖样式。
     * @param letterSpacing 显式字距；Unspecified 不覆盖样式。
     * @param textDecoration 显式文本装饰；null 不覆盖样式。
     * @param textAlign 显式对齐；null 不覆盖样式。
     * @param lineHeight 显式行高；Unspecified 不覆盖样式。
     * @return 合并逐字段覆盖后的样式，供 BasicText 使用。
     */
    private fun TextStyle.mergeFieldOverrides(
        fontSize: TextUnit,
        fontStyle: FontStyle?,
        fontWeight: FontWeight?,
        fontFamily: FontFamily?,
        letterSpacing: TextUnit,
        textDecoration: TextDecoration?,
        textAlign: TextAlign?,
        lineHeight: TextUnit,
    ): TextStyle = merge(
        TextStyle(
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
        ),
    )

    /**
     * 按既有优先级解析文本颜色：显式颜色、合并样式色、Nomi 原生内容色、textPrimary。
     *
     * @param color 公共入口解析后的颜色；Unspecified 触发回退，Transparent 不回退。
     * @param resolvedStyle 已合并 Nomi 基础排版与原生内容样式的样式。
     * @return 应用到 BasicText 样式的最终颜色。
     */
    @Composable
    private fun resolveColor(color: Color, resolvedStyle: TextStyle): Color = when {
        color != Color.Unspecified -> color
        resolvedStyle.color != Color.Unspecified -> resolvedStyle.color
        LocalNomiNativeContentColor.current != Color.Unspecified -> LocalNomiNativeContentColor.current
        else -> NomiTheme.colorScheme.textPrimary
    }

    /**
     * 使用 Compose Foundation 绘制纯文本，依次合并 Nomi bodyMedium、原生内容样式及调用样式。
     *
     * @param text 要绘制的文本。
     * @param modifier 应用于 BasicText 节点的修饰符。
     * @param style 已合并显式 slot 的样式增量；null 不覆盖 Nomi 基础排版。
     * @param color 显式颜色优先于合并样式色、Nomi 原生内容色及 textPrimary；Unspecified 触发回退，Transparent 不回退。
     * @param fontSize 显式字号；Unspecified 沿用合并样式。
     * @param fontStyle 显式字形；null 沿用合并样式。
     * @param fontWeight 显式字重；null 沿用合并样式。
     * @param fontFamily 显式字体族；null 沿用合并样式。
     * @param letterSpacing 显式字距；Unspecified 沿用合并样式。
     * @param textDecoration 显式文本装饰；null 沿用合并样式。
     * @param textAlign 显式对齐；null 沿用合并样式。
     * @param lineHeight 显式行高；Unspecified 沿用合并样式。
     * @param overflow 超出布局或行数限制时的显示方式。
     * @param softWrap 是否允许在软换行位置折行。
     * @param maxLines 最大显示行数，须不小于 minLines。
     * @param minLines 最少占用的文本行数，须大于零。
     * @param onTextLayout 布局完成回调，原样交给 BasicText。
     * @param autoSize 自动字号策略，原样交给 BasicText；null 保留固定字号。
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
        val baseStyle =
            NomiTheme.typography.bodyMedium.merge(LocalNomiNativeTextStyle.current).merge(style)
        val resolvedStyle = baseStyle.mergeFieldOverrides(
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
        )
        BasicText(
            text = text,
            modifier = modifier,
            style = resolvedStyle.copy(color = resolveColor(color, resolvedStyle)),
            onTextLayout = onTextLayout,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            autoSize = autoSize,
        )
    }

    /**
     * 使用 Compose Foundation 绘制富文本，内联内容映射原样交给 BasicText。
     *
     * @param text 要绘制的富文本。
     * @param modifier 应用于 BasicText 节点的修饰符。
     * @param style 已合并显式 slot 的样式增量；null 不覆盖 Nomi 基础排版。
     * @param color 显式颜色优先于合并样式色、Nomi 原生内容色及 textPrimary；Unspecified 触发回退，Transparent 不回退。
     * @param fontSize 显式字号；Unspecified 沿用合并样式。
     * @param fontStyle 显式字形；null 沿用合并样式。
     * @param fontWeight 显式字重；null 沿用合并样式。
     * @param fontFamily 显式字体族；null 沿用合并样式。
     * @param letterSpacing 显式字距；Unspecified 沿用合并样式。
     * @param textDecoration 显式文本装饰；null 沿用合并样式。
     * @param textAlign 显式对齐；null 沿用合并样式。
     * @param lineHeight 显式行高；Unspecified 沿用合并样式。
     * @param overflow 超出布局或行数限制时的显示方式。
     * @param softWrap 是否允许在软换行位置折行。
     * @param maxLines 最大显示行数，须不小于 minLines。
     * @param minLines 最少占用的文本行数，须大于零。
     * @param inlineContent 替换文本区段的内联组合映射，原样交给 BasicText。
     * @param onTextLayout 布局完成回调，原样交给 BasicText。
     * @param autoSize 自动字号策略，原样交给 BasicText；null 保留固定字号。
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
        val baseStyle =
            NomiTheme.typography.bodyMedium.merge(LocalNomiNativeTextStyle.current).merge(style)
        val resolvedStyle = baseStyle.mergeFieldOverrides(
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
        )
        BasicText(
            text = text,
            modifier = modifier,
            style = resolvedStyle.copy(color = resolveColor(color, resolvedStyle)),
            onTextLayout = onTextLayout,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            inlineContent = inlineContent,
            autoSize = autoSize,
        )
    }
}
