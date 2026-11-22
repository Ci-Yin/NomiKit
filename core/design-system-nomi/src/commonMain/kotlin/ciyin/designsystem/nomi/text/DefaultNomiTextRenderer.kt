package ciyin.designsystem.nomi.text

import androidx.compose.foundation.text.BasicText
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
import ciyin.designsystem.nomi.LocalNomiNativeContentColor
import ciyin.designsystem.nomi.LocalNomiNativeTextStyle
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiTheme
import androidx.compose.material3.OutlinedTextField as NativeOutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField as NativeTextField
import androidx.compose.material3.TextFieldDefaults

/**
 * Nomi 默认文本渲染器；文本输入框也在此实现，配色读取 Nomi 主题令牌。
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

    /**
     * 渲染输入框；显式描边变体使用描边入口，其余情况使用填充入口。
     *
     * Nomi 不安装外层原生主题，输入框配色显式取自 [NomiColorScheme]，不读取宿主主题。
     *
     * @param value 当前文本值。
     * @param onValueChange 文本变化回调。
     * @param modifier 应用于原生输入框的修饰符。
     * @param enabled 是否接受用户操作。
     * @param readOnly 是否只读。
     * @param singleLine 是否限制为单行。
     * @param variant 显式视觉变体；null 使用 Nomi 默认的填充外观。
     * @param label 标签槽；null 表示没有标签。
     * @param placeholder 占位符槽；null 表示没有占位符。
     * @param leadingIcon 前导图标槽；null 表示不占位。
     * @param trailingIcon 尾部图标槽；null 表示不占位。
     * @param isError 是否处于错误状态。
     * @param keyboardOptions 键盘类型与输入法动作配置。
     * @param keyboardActions 输入法动作回调。
     * @param nativeSpec 输入框专属参数；Nomi 外观完全由 [NomiTheme] 令牌决定，本实现忽略它。
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
        val scheme = NomiTheme.colorScheme
        if (variant == NomiTextFieldVariant.Outlined) {
            NativeOutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                isError = isError,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = scheme.textPrimary,
                    unfocusedTextColor = scheme.textPrimary,
                    disabledTextColor = scheme.textDisabled,
                    errorTextColor = scheme.error,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    cursorColor = scheme.primary,
                    errorCursorColor = scheme.error,
                    focusedBorderColor = scheme.primary,
                    unfocusedBorderColor = scheme.outline,
                    disabledBorderColor = scheme.textDisabled,
                    errorBorderColor = scheme.error,
                    focusedLeadingIconColor = scheme.textSecondary,
                    unfocusedLeadingIconColor = scheme.textSecondary,
                    disabledLeadingIconColor = scheme.textDisabled,
                    errorLeadingIconColor = scheme.error,
                    focusedTrailingIconColor = scheme.textSecondary,
                    unfocusedTrailingIconColor = scheme.textSecondary,
                    disabledTrailingIconColor = scheme.textDisabled,
                    errorTrailingIconColor = scheme.error,
                    focusedLabelColor = scheme.primary,
                    unfocusedLabelColor = scheme.textSecondary,
                    disabledLabelColor = scheme.textDisabled,
                    errorLabelColor = scheme.error,
                    focusedPlaceholderColor = scheme.textHint,
                    unfocusedPlaceholderColor = scheme.textHint,
                    disabledPlaceholderColor = scheme.textDisabled,
                    errorPlaceholderColor = scheme.textHint,
                ),
            )
        } else {
            NativeTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                isError = isError,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = scheme.textPrimary,
                    unfocusedTextColor = scheme.textPrimary,
                    disabledTextColor = scheme.textDisabled,
                    errorTextColor = scheme.error,
                    focusedContainerColor = scheme.surfaceLow,
                    unfocusedContainerColor = scheme.surfaceLow,
                    disabledContainerColor = scheme.surfaceLow,
                    errorContainerColor = scheme.surfaceLow,
                    cursorColor = scheme.primary,
                    errorCursorColor = scheme.error,
                    focusedIndicatorColor = scheme.primary,
                    unfocusedIndicatorColor = scheme.outline,
                    disabledIndicatorColor = scheme.textDisabled,
                    errorIndicatorColor = scheme.error,
                    focusedLeadingIconColor = scheme.textSecondary,
                    unfocusedLeadingIconColor = scheme.textSecondary,
                    disabledLeadingIconColor = scheme.textDisabled,
                    errorLeadingIconColor = scheme.error,
                    focusedTrailingIconColor = scheme.textSecondary,
                    unfocusedTrailingIconColor = scheme.textSecondary,
                    disabledTrailingIconColor = scheme.textDisabled,
                    errorTrailingIconColor = scheme.error,
                    focusedLabelColor = scheme.primary,
                    unfocusedLabelColor = scheme.textSecondary,
                    disabledLabelColor = scheme.textDisabled,
                    errorLabelColor = scheme.error,
                    focusedPlaceholderColor = scheme.textHint,
                    unfocusedPlaceholderColor = scheme.textHint,
                    disabledPlaceholderColor = scheme.textDisabled,
                    errorPlaceholderColor = scheme.textHint,
                ),
            )
        }
    }
}
