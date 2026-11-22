package consumer

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
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
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.control.NomiControlRenderer
import ciyin.designsystem.component.text.NomiTextFieldSpec
import ciyin.designsystem.component.text.NomiTextFieldVariant
import ciyin.designsystem.component.divider.NomiDividerRenderer
import ciyin.designsystem.component.feedback.NomiFeedbackRenderer
import ciyin.designsystem.component.icon.NomiIconRenderer
import ciyin.designsystem.component.list.NomiListItemRenderer
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.component.text.NomiTextRenderer
import ciyin.designsystem.component.text.NomiTextSpec
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.fluent.FluentTheme
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.LocalNomiTheme
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.Colors
import io.github.composefluent.lightColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import io.github.composefluent.FluentTheme as NativeFluentTheme

/**
 * 从 adapter 外部验证公开 SPI、映射函数与显式中立主题消费。
 */
@OptIn(ExperimentalTestApi::class)
class ExternalApiConsumptionTest {
    /**
     * 外部模块可包装 Fluent 边界并转发主题，中立值仍由主题入口发布。
     */
    @Test
    fun externalConsumerWrapsFluentBoundaryAndForwardsTheme() = runComposeUiTest {
        val theme = NomiTheme.dark()
        var nativeColors: Colors? = null
        var observed: NomiTheme? = null
        val customSystem = object : NomiDesignSystem by NomiDesignSystem.Fluent {
            /**
             * 转发主题，不发布也不改写中立值。
             *
             * @param theme 本层显式中立主题，只作为原生映射来源。
             * @param content 在 Fluent 原生主题环境中组合的内容。
             */
            @Composable
            override fun ProvideTheme(theme: NomiTheme, content: @Composable () -> Unit) {
                NomiDesignSystem.Fluent.ProvideTheme(theme, content)
            }
        }
        setContent {
            NomiTheme(theme = theme, designSystem = customSystem) {
                nativeColors = NativeFluentTheme.colors
                observed = LocalNomiTheme.current
            }
        }
        runOnIdle {
            assertTrue(requireNotNull(nativeColors).darkMode)
            assertEquals(true, requireNotNull(observed).isDark)
            assertSame(theme, observed)
        }
    }

    /**
     * 外部映射函数只用公开类型构造原生规格，工厂不追加任何未声明的原生组。
     */
    @Test
    fun externalConsumerBuildsFluentMapFromPublicTypes() = runComposeUiTest {
        val colors = lightColors()
        val map: NomiTheme.() -> FluentTheme = {
            FluentTheme(isDark = isDark, colors = colors)
        }
        val theme = NomiTheme.light()
        var installed: Colors? = null
        var published: NomiTheme? = null
        setContent {
            NomiTheme(theme = theme, designSystem = NomiDesignSystem.Fluent(map)) {
                installed = NativeFluentTheme.colors
                published = LocalNomiTheme.current
            }
        }
        runOnIdle {
            assertSame(colors, installed)
            assertSame(theme, requireNotNull(published))
        }
    }

    /**
     * 外部调用方可替换文本族，操作族仍使用 Fluent 原生 Button。
     */
    @Test
    fun externalConsumerReplacesPublicTextSpi() = runComposeUiTest {
        val seenText = mutableSetOf<String>()
        val customText = RecordingTextRenderer(seenText)
        val customSystem = object : NomiDesignSystem by NomiDesignSystem.Fluent {
            /**
             * 外部实现只替换文本组件族。
             */
            override val components: NomiDesignSystemComponents =
                object : NomiDesignSystemComponents {
                    /**
                     * 记录调用并透传原生 Text。
                     */
                    override val text: NomiTextRenderer = customText

                    /**
                     * 沿用 Fluent 操作族。
                     */
                    override val actions: NomiActionRenderer =
                        NomiDesignSystem.Fluent.components.actions

                    /**
                     * 沿用 Fluent 表面族。
                     */
                    override val surfaces: NomiSurfaceRenderer =
                        NomiDesignSystem.Fluent.components.surfaces

                    /**
                     * 沿用 Fluent 导航套件，不改变本次替换文本族的测试语义。
                     */
                    override val navigation: NomiNavigationRenderer =
                        NomiDesignSystem.Fluent.components.navigation

                    /**
                     * 沿用 Fluent 页面脚手架，不改变本次替换文本族的测试语义。
                     */
                    override val scaffold: NomiScaffoldRenderer =
                        NomiDesignSystem.Fluent.components.scaffold

                    /**
                     * 沿用 Fluent 列表项，不改变本次替换文本族的测试语义。
                     */
                    override val listItems: NomiListItemRenderer =
                        NomiDesignSystem.Fluent.components.listItems

                    /**
                     * 沿用 Fluent 图标族，不改变本次替换文本族的测试语义。
                     */
                    override val icons: NomiIconRenderer =
                        NomiDesignSystem.Fluent.components.icons

                    /**
                     * 沿用 Fluent 装饰线族，不改变本次替换文本族的测试语义。
                     */
                    override val decorations: NomiDividerRenderer =
                        NomiDesignSystem.Fluent.components.decorations

                    /**
                     * 沿用 Fluent 控件族，不改变本次替换文本族的测试语义。
                     */
                    override val controls: NomiControlRenderer =
                        NomiDesignSystem.Fluent.components.controls

                    /**
                     * 沿用 Fluent 反馈族，不改变本次替换文本族的测试语义。
                     */
                    override val feedback: NomiFeedbackRenderer =
                        NomiDesignSystem.Fluent.components.feedback
                }
        }
        setContent {
            NomiTheme(designSystem = customSystem, darkMode = DarkMode.Light) {
                NomiButton(onClick = {}) { NomiText("External SPI") }
            }
        }
        onNodeWithText("External SPI").assertExists()
        runOnIdle {
            assertTrue("External SPI" in seenText)
            assertEquals("fluent", customSystem.id.value)
        }
    }
}

/**
 * 外部模块可实现公开文本 SPI，不依赖核心 internal 符号。
 *
 * @property seen 调用方持有的文本集合，每次渲染将 text 加入其中，相同字符串自动去重。
 */
private class RecordingTextRenderer(
    private val seen: MutableSet<String>,
) : NomiTextRenderer {
    /**
     * 记录文本并原样转发公开 Fluent 文本 SPI，不自行补全默认样式。
     *
     * @param text 待记录并渲染的文本。
     * @param modifier 转发给原生文本的布局与语义修饰符。
     * @param style 可空的显式样式；null 由 Fluent 原生环境决定。
     * @param color 已解析颜色，Unspecified 保留原生解析，透明色仍为覆盖。
     * @param fontSize 显式字号，原样交给 Fluent。
     * @param fontStyle 显式字形，原样交给 Fluent。
     * @param fontWeight 显式字重，原样交给 Fluent。
     * @param fontFamily 显式字体族，原样交给 Fluent。
     * @param letterSpacing 显式字距，原样交给 Fluent。
     * @param textDecoration 显式文本装饰，原样交给 Fluent。
     * @param textAlign 显式对齐，原样交给 Fluent。
     * @param lineHeight 显式行高，原样交给 Fluent。
     * @param overflow 文本超出可用空间或行数时的处理方式。
     * @param softWrap 是否允许按可用宽度自动换行。
     * @param maxLines 最大显示行数，原样交给 Fluent。
     * @param minLines 最小占用行数，原样交给 Fluent。
     * @param onTextLayout 布局回调，原样交给 Fluent。
     * @param autoSize 自动字号策略，原样交给 Fluent；非 null 时由 Fluent 实现拒绝。
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
        seen += text
        NomiDesignSystem.Fluent.components.text.Text(
            text = text,
            modifier = modifier,
            style = style,
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
            nativeSpec = nativeSpec,
        )
    }

    /**
     * 记录富文本并原样转发公开 Fluent 文本 SPI，内联内容不自行改写。
     *
     * @param text 待记录并渲染的富文本。
     * @param modifier 转发给原生文本的布局与语义修饰符。
     * @param style 可空的显式样式；null 由 Fluent 原生环境决定。
     * @param color 已解析颜色，Unspecified 保留原生解析，透明色仍为覆盖。
     * @param fontSize 显式字号，原样交给 Fluent。
     * @param fontStyle 显式字形，原样交给 Fluent。
     * @param fontWeight 显式字重，原样交给 Fluent。
     * @param fontFamily 显式字体族，原样交给 Fluent。
     * @param letterSpacing 显式字距，原样交给 Fluent。
     * @param textDecoration 显式文本装饰，原样交给 Fluent。
     * @param textAlign 显式对齐，原样交给 Fluent。
     * @param lineHeight 显式行高，原样交给 Fluent。
     * @param overflow 文本超出可用空间或行数时的处理方式。
     * @param softWrap 是否允许按可用宽度自动换行。
     * @param maxLines 最大显示行数，原样交给 Fluent。
     * @param minLines 最小占用行数，原样交给 Fluent。
     * @param inlineContent 内联内容映射，原样交给 Fluent。
     * @param onTextLayout 布局回调，原样交给 Fluent。
     * @param autoSize 自动字号策略，原样交给 Fluent；非 null 时由 Fluent 实现拒绝。
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
        seen += text.text
        NomiDesignSystem.Fluent.components.text.Text(
            text = text,
            modifier = modifier,
            style = style,
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
            onTextLayout = onTextLayout,
            autoSize = autoSize,
            nativeSpec = nativeSpec,
        )
    }

    /**
     * 原样转发公开 Fluent 文本输入框 SPI，不记录输入内容。
     *
     * @param value 当前文本值。
     * @param onValueChange 文本变化回调。
     * @param modifier 转发给原生输入框的修饰符。
     * @param enabled 是否接受用户操作。
     * @param readOnly 是否只读。
     * @param singleLine 是否限制为单行。
     * @param variant 显式视觉变体；null 使用原生默认。
     * @param label 标签槽；null 表示没有标签。
     * @param placeholder 占位符槽；null 表示没有占位符。
     * @param leadingIcon 前导图标槽；null 表示不占位。
     * @param trailingIcon 尾部图标槽；null 表示不占位。
     * @param isError 是否处于错误状态；Fluent 忽略并记录警告。
     * @param keyboardOptions 键盘类型与输入法动作配置。
     * @param keyboardActions 输入法动作回调。
     * @param nativeSpec 输入框专属参数；当前无实现，原样接受并忽略。
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
        NomiDesignSystem.Fluent.components.text.TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            variant = variant,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            nativeSpec = nativeSpec,
        )
    }
}
