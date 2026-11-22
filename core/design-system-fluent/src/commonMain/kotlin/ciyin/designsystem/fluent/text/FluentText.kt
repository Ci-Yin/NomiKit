package ciyin.designsystem.fluent.text

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import ciyin.designsystem.component.text.NomiTextFieldSpec
import ciyin.designsystem.component.text.NomiTextFieldVariant
import ciyin.designsystem.component.text.NomiTextRenderer
import ciyin.designsystem.component.text.NomiTextSpec
import ciyin.designsystem.fluent.rememberFluentWarning
import io.github.composefluent.LocalTextStyle
import io.github.composefluent.component.Text as NativeText
import io.github.composefluent.component.TextField as NativeTextField

/**
 * Fluent 原生文本适配；未指定 style 保留原生 LocalTextStyle，文本输入框也在此实现。
 */
internal object FluentText : NomiTextRenderer {
    /**
     * 透传文本参数，不补中立正文样式；Fluent 原生不支持自动字号，显式请求时忽略并记录警告。
     *
     * @param text 待显示的纯文本。
     * @param modifier 原生文本的布局与语义修饰符。
     * @param style null 保留原生 LocalTextStyle，非 null 仅合并指定字段。
     * @param color 核心已解析的颜色；Unspecified 留给原生环境，Transparent 为显式透明覆盖。
     * @param fontSize 显式字号；Unspecified 沿用 style 与原生环境。
     * @param fontStyle 显式字形；null 沿用 style 与原生环境。
     * @param fontWeight 显式字重；null 沿用 style 与原生环境。
     * @param fontFamily 显式字体族；null 沿用 style 与原生环境。
     * @param letterSpacing 显式字距；Unspecified 沿用 style 与原生环境。
     * @param textDecoration 显式文本装饰；null 沿用 style 与原生环境。
     * @param textAlign 显式对齐；null 映射为原生 TextAlign.Unspecified。
     * @param lineHeight 显式行高；Unspecified 沿用 style 与原生环境。
     * @param overflow 超出宽度或行数限制时的显示方式。
     * @param softWrap 是否按可用宽度自动换行。
     * @param maxLines 最大显示行数，交由原生文本校验。
     * @param minLines 最小占用行数，交由原生文本校验。
     * @param onTextLayout 布局完成回调；null 映射为原生空回调。
     * @param autoSize 自动字号策略；Fluent 原生不支持，忽略覆盖并记录警告。
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
        rememberFluentWarning(autoSize != null, autoSize) {
            "NomiText 的 autoSize 覆盖无原生入口，已保留固定字号"
        }
        NativeText(
            text = text,
            modifier = modifier,
            style = LocalTextStyle.current.merge(style),
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout ?: {},
        )
    }

    /**
     * 富文本透传原生 Fluent Text，内联内容映射原样交给原生布局；自动字号忽略并记录警告。
     *
     * @param text 待显示的富文本。
     * @param modifier 原生文本的布局与语义修饰符。
     * @param style null 保留原生 LocalTextStyle，非 null 仅合并指定字段。
     * @param color 核心已解析的颜色；Unspecified 留给原生环境，Transparent 为显式透明覆盖。
     * @param fontSize 显式字号；Unspecified 沿用 style 与原生环境。
     * @param fontStyle 显式字形；null 沿用 style 与原生环境。
     * @param fontWeight 显式字重；null 沿用 style 与原生环境。
     * @param fontFamily 显式字体族；null 沿用 style 与原生环境。
     * @param letterSpacing 显式字距；Unspecified 沿用 style 与原生环境。
     * @param textDecoration 显式文本装饰；null 沿用 style 与原生环境。
     * @param textAlign 显式对齐；null 映射为原生 TextAlign.Unspecified。
     * @param lineHeight 显式行高；Unspecified 沿用 style 与原生环境。
     * @param overflow 超出宽度或行数限制时的显示方式。
     * @param softWrap 是否按可用宽度自动换行。
     * @param maxLines 最大显示行数，交由原生文本校验。
     * @param minLines 最小占用行数，交由原生文本校验。
     * @param inlineContent 替换文本区段的内联组合映射，原样交给原生文本。
     * @param onTextLayout 布局完成回调；null 映射为原生空回调。
     * @param autoSize 自动字号策略；Fluent 原生不支持，忽略覆盖并记录警告。
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
        rememberFluentWarning(autoSize != null, autoSize) {
            "NomiText 的 autoSize 覆盖无原生入口，已保留固定字号"
        }
        NativeText(
            text = text,
            modifier = modifier,
            style = LocalTextStyle.current.merge(style),
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            inlineContent = inlineContent,
            onTextLayout = onTextLayout ?: {},
        )
    }

    /**
     * 渲染原生输入框；标签映射到字段上方的 header，尾部图标映射到原生尾部槽。
     *
     * @param value 当前文本值。
     * @param onValueChange 文本变化回调。
     * @param modifier 应用于原生输入框的修饰符。
     * @param enabled 是否接受用户操作。
     * @param readOnly 是否只读。
     * @param singleLine 是否限制为单行。
     * @param variant 显式视觉变体；上游只有一种外观，非 null 时忽略并记录警告。
     * @param label 标签槽，映射为原生 header；null 表示没有标签。
     * @param placeholder 占位符槽；null 表示没有占位符。
     * @param leadingIcon 前导图标槽；null 表示不占位。
     * @param trailingIcon 尾部图标槽；null 表示不占位。
     * @param isError 错误状态；上游没有错误态，忽略并记录警告。
     * @param keyboardOptions 键盘类型与输入法动作配置。
     * @param keyboardActions 输入法动作回调。
     * @param nativeSpec 输入框专属参数；当前没有实现，原样接受并忽略。
     */
    @Composable
    override fun TextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        readOnly: Boolean,
        singleLine: Boolean,
        variant: NomiTextFieldVariant?,
        label: (@Composable () -> Unit)?,
        placeholder: (@Composable () -> Unit)?,
        leadingIcon: (@Composable () -> Unit)?,
        trailingIcon: (@Composable () -> Unit)?,
        isError: Boolean,
        keyboardOptions: KeyboardOptions,
        keyboardActions: KeyboardActions,
        nativeSpec: NomiTextFieldSpec?,
    ) {
        rememberFluentWarning(variant != null, variant) {
            "Fluent 输入框没有填充/描边变体，已忽略 variant=$variant"
        }
        rememberFluentWarning(isError, isError) {
            "Fluent 输入框没有错误态，已忽略 isError"
        }
        val trailing: (@Composable RowScope.() -> Unit)? = trailingIcon?.let { icon ->
            { icon() }
        }
        NativeTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            header = label,
            leadingIcon = leadingIcon,
            trailing = trailing,
            placeholder = placeholder,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
        )
    }
}
