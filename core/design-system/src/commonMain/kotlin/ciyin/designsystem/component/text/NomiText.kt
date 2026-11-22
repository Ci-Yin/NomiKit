package ciyin.designsystem.component.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
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
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一文本入口；未指定视觉参数时保留原生 Text 的内容环境。
 *
 * 逐字段参数是显式覆盖，未指定（null 或 Unspecified）时沿用 style 与原生文本环境，
 * 与原生 Text 的优先级一致。
 *
 * @param text 要显示的文本内容。
 * @param modifier 应用于文本布局节点的修饰符。
 * @param style 文本样式增量；null 保留继承的显式 slot 样式及原生文本环境。
 * @param color 文本颜色；Unspecified 依次回退到本次 style、显式 slot 颜色、继承样式颜色和原生环境，Transparent 是有效覆盖。
 * @param fontSize 显式字号；Unspecified 沿用 style 与原生环境。
 * @param fontStyle 显式字形（如斜体）；null 沿用 style 与原生环境。
 * @param fontWeight 显式字重；null 沿用 style 与原生环境。
 * @param fontFamily 显式字体族；null 沿用 style 与原生环境。
 * @param letterSpacing 显式字距；Unspecified 沿用 style 与原生环境。
 * @param textDecoration 显式文本装饰（如下划线）；null 沿用 style 与原生环境。
 * @param textAlign 显式对齐方式；null 沿用 style 与原生环境。
 * @param lineHeight 显式行高；Unspecified 沿用 style 与原生环境。
 * @param overflow 内容超出行数或布局范围时的显示方式。
 * @param softWrap 是否允许在软换行位置折行。
 * @param maxLines 最大显示行数，须不小于 minLines。
 * @param minLines 文本布局的最少行数，须大于零。
 * @param onTextLayout 新文本布局计算完成时的回调；null 表示调用方不需要布局结果。
 * @param autoSize 自动字号策略；null 保留固定字号，当前设计系统不支持时明确拒绝。
 * @param nativeSpec 文本专属参数；当前没有实现，保留给未来的原生文本能力，传 null 即原生默认。
 */
@Composable
fun NomiText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    autoSize: TextAutoSize? = null,
    nativeSpec: NomiTextSpec? = null,
) {
    val resolved = resolveNomiText(style, color)
    requireNomiDesignSystem().components.text.Text(
        text = text,
        modifier = modifier,
        style = resolved.style,
        color = resolved.color,
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
        nativeSpec = nativeSpec,
    )
}

/**
 * 统一富文本入口；内联内容映射原样交给当前设计系统的原生文本布局。
 *
 * @param text 带注解的富文本内容。
 * @param modifier 应用于文本布局节点的修饰符。
 * @param style 文本样式增量；null 保留继承的显式 slot 样式及原生文本环境。
 * @param color 文本颜色；Unspecified 依次回退到本次 style、显式 slot 颜色、继承样式颜色和原生环境，Transparent 是有效覆盖。
 * @param fontSize 显式字号；Unspecified 沿用 style 与原生环境。
 * @param fontStyle 显式字形（如斜体）；null 沿用 style 与原生环境。
 * @param fontWeight 显式字重；null 沿用 style 与原生环境。
 * @param fontFamily 显式字体族；null 沿用 style 与原生环境。
 * @param letterSpacing 显式字距；Unspecified 沿用 style 与原生环境。
 * @param textDecoration 显式文本装饰（如下划线）；null 沿用 style 与原生环境。
 * @param textAlign 显式对齐方式；null 沿用 style 与原生环境。
 * @param lineHeight 显式行高；Unspecified 沿用 style 与原生环境。
 * @param overflow 内容超出行数或布局范围时的显示方式。
 * @param softWrap 是否允许在软换行位置折行。
 * @param maxLines 最大显示行数，须不小于 minLines。
 * @param minLines 文本布局的最少行数，须大于零。
 * @param inlineContent 替换文本指定区段的内联组合映射；空映射表示纯富文本。
 * @param onTextLayout 新文本布局计算完成时的回调；null 表示调用方不需要布局结果。
 * @param autoSize 自动字号策略；null 保留固定字号，当前设计系统不支持时明确拒绝。
 * @param nativeSpec 文本专属参数；当前没有实现，保留给未来的原生文本能力，传 null 即原生默认。
 */
@Composable
fun NomiText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    autoSize: TextAutoSize? = null,
    nativeSpec: NomiTextSpec? = null,
) {
    val resolved = resolveNomiText(style, color)
    requireNomiDesignSystem().components.text.Text(
        text = text,
        modifier = modifier,
        style = resolved.style,
        color = resolved.color,
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
        onTextLayout = onTextLayout,
        autoSize = autoSize,
        nativeSpec = nativeSpec,
    )
}

/**
 * 文本公共入口的样式与颜色解析结果。
 *
 * @property style 合并继承显式 slot 后的样式增量；null 表示没有显式样式。
 * @property color 按显式 color、本次 style、显式 slot、继承样式顺序解析的颜色。
 */
private data class ResolvedNomiText(
    val style: TextStyle?,
    val color: Color,
)

/**
 * 合并继承的显式 slot 样式并按既有优先级解析文本颜色。
 *
 * @param style 调用方本次提供的样式增量。
 * @param color 调用方本次提供的颜色。
 * @return 交给 renderer 的解析样式与颜色；未指定时保持 Unspecified 交原生环境。
 */
@Composable
@ReadOnlyComposable
private fun resolveNomiText(style: TextStyle?, color: Color): ResolvedNomiText {
    val inheritedStyle = LocalNomiTextStyle.current
    val resolvedStyle =
        if (inheritedStyle == TextStyle.Default) style else inheritedStyle.merge(style)
    val explicitStyleColor = style?.color ?: Color.Unspecified
    val resolvedColor = when {
        color != Color.Unspecified -> color
        explicitStyleColor != Color.Unspecified -> explicitStyleColor
        LocalNomiContentColor.current != Color.Unspecified -> LocalNomiContentColor.current
        inheritedStyle.color != Color.Unspecified -> inheritedStyle.color
        else -> Color.Unspecified
    }
    return ResolvedNomiText(resolvedStyle, resolvedColor)
}

/**
 * 文本专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；当前没有任何实现，属于预留插孔，
 * 用于将来透传中立排版参数未覆盖的原生文本能力。
 */
sealed interface NomiTextSpecFamily : NomiSpecFamily

/**
 * 文本专属参数。
 *
 * 中立 NomiText 已逐字段暴露原生 Text 的全部参数，因此本轮没有实现；预留该契约是为了让未来的
 * 原生专有能力无需修改公共签名即可接入。
 */
typealias NomiTextSpec = NomiNativeSpec<NomiTextSpecFamily>
