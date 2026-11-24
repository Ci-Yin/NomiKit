package ciyin.designsystem.nomi

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.control.NomiControlRenderer
import ciyin.designsystem.component.text.NomiTextFieldSpec
import ciyin.designsystem.component.text.NomiTextFieldVariant
import ciyin.designsystem.component.divider.NomiDividerRenderer
import ciyin.designsystem.component.feedback.NomiFeedbackRenderer
import ciyin.designsystem.component.icon.NomiIconRenderer
import ciyin.designsystem.component.list.NomiListItemRenderer
import ciyin.designsystem.component.menu.NomiMenuRenderer
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.component.text.NomiTextRenderer
import ciyin.designsystem.component.text.NomiTextSpec
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 验证 Nomi 自身解析环境与公共显式 slot 相互隔离。
 *
 * 该用例自核心迁入：核心不再引用内置实现，原生内容本地变量由本模块维护。
 */
@OptIn(ExperimentalTestApi::class)
class NomiNativeContentIsolationTest {
    /**
     * 默认 Nomi Card 的解析色留在 Nomi 私有环境，切换 renderer 不得携带伪显式 slot。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun defaultCardDoesNotLeakResolvedSlotsIntoRendererOverride() = runComposeUiTest {
        val recorder = SlotRecordingDesignSystem()
        val slots = mutableListOf<Pair<Color, TextStyle>>()
        val nativeColors = mutableListOf<Color>()
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiCard {
                    val nativeColor = LocalNomiNativeContentColor.current
                    SideEffect { nativeColors += nativeColor }
                    RecordSlots(slots)
                    NomiTheme(designSystem = recorder) {
                        RecordSlots(slots)
                        NomiText("other-renderer-default")
                    }
                    RecordSlots(slots)
                }
            }
        }
        runOnIdle {
            assertEquals(listOf(defaultLightNomiColorScheme().onSurface), nativeColors)
            assertEquals(
                listOf(
                    Color.Unspecified to TextStyle.Default,
                    Color.Unspecified to TextStyle.Default,
                    Color.Unspecified to TextStyle.Default,
                ),
                slots,
            )
            val text = recorder.texts.getValue("other-renderer-default")
            assertNull(text.first)
            assertEquals(Color.Unspecified, text.second)
            assertEquals(NomiTheme.light(), recorder.themes.single())
        }
    }
}

/**
 * 记录传入文本样式与颜色的最小设计系统，用于验证跨 renderer 的 slot 隔离。
 */
private class SlotRecordingDesignSystem : NomiDesignSystem {
    /**
     * 录制型设计系统标识。
     */
    override val id: NomiDesignSystemId = NomiDesignSystemId("slot-recording")

    /**
     * 十一族组装；文本族记录参数，未验证的族委托 Nomi 默认实现或直接拒绝访问。
     */
    override val components: NomiDesignSystemComponents = object : NomiDesignSystemComponents {
        /**
         * 记录文本参数，不绘制内容。
         */
        override val text: NomiTextRenderer = object : NomiTextRenderer {
            /**
             * 记录文本样式与颜色，不做任何默认补齐。
             *
             * @param text 记录键；相同文本的后续提交覆盖先前记录。
             * @param modifier 接口要求的修饰符，此夹具不应用。
             * @param style 原样记录的样式。
             * @param color 原样记录的颜色，保留 Unspecified。
             * @param fontSize 接口要求的显式字号，此夹具不记录。
             * @param fontStyle 接口要求的显式字形，此夹具不记录。
             * @param fontWeight 接口要求的显式字重，此夹具不记录。
             * @param fontFamily 接口要求的显式字体族，此夹具不记录。
             * @param letterSpacing 接口要求的显式字距，此夹具不记录。
             * @param textDecoration 接口要求的显式文本装饰，此夹具不记录。
             * @param textAlign 接口要求的显式对齐，此夹具不记录。
             * @param lineHeight 接口要求的显式行高，此夹具不记录。
             * @param overflow 接口要求的溢出策略，此夹具不记录。
             * @param softWrap 接口要求的软换行开关，此夹具不记录。
             * @param maxLines 接口要求的最大行数，此夹具不记录。
             * @param minLines 接口要求的最小行数，此夹具不记录。
             * @param onTextLayout 接口要求的布局回调，此夹具不调用。
             * @param autoSize 接口要求的自动字号策略，此夹具不记录。
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
                SideEffect { texts[text] = style to color }
            }

            /**
             * 富文本重载同样记录样式与颜色。
             *
             * @param text 记录键，取自富文本纯文本内容。
             * @param modifier 接口要求的修饰符，此夹具不应用。
             * @param style 原样记录的样式。
             * @param color 原样记录的颜色，保留 Unspecified。
             * @param fontSize 接口要求的显式字号，此夹具不记录。
             * @param fontStyle 接口要求的显式字形，此夹具不记录。
             * @param fontWeight 接口要求的显式字重，此夹具不记录。
             * @param fontFamily 接口要求的显式字体族，此夹具不记录。
             * @param letterSpacing 接口要求的显式字距，此夹具不记录。
             * @param textDecoration 接口要求的显式文本装饰，此夹具不记录。
             * @param textAlign 接口要求的显式对齐，此夹具不记录。
             * @param lineHeight 接口要求的显式行高，此夹具不记录。
             * @param overflow 接口要求的溢出策略，此夹具不记录。
             * @param softWrap 接口要求的软换行开关，此夹具不记录。
             * @param maxLines 接口要求的最大行数，此夹具不记录。
             * @param minLines 接口要求的最小行数，此夹具不记录。
             * @param inlineContent 接口要求的内联内容映射，此夹具不记录。
             * @param onTextLayout 接口要求的布局回调，此夹具不调用。
             * @param autoSize 接口要求的自动字号策略，此夹具不记录。
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
                SideEffect { texts[text.text] = style to color }
            }

            /**
             * 文本输入框委托 Nomi 默认实现，本夹具不记录输入框参数。
             *
             * @param value 当前文本值。
             * @param onValueChange 文本变化回调。
             * @param modifier 接口要求的修饰符，此夹具委托后不直接处理。
             * @param enabled 是否接受用户操作。
             * @param readOnly 是否只读。
             * @param singleLine 是否限制为单行。
             * @param maxLines 最大显示行数。
             * @param visualTransformation 文本可视化转换。
             * @param variant 显式视觉变体；null 使用原生默认。
             * @param label 标签槽；null 表示没有标签。
             * @param placeholder 占位符槽；null 表示没有占位符。
             * @param leadingIcon 前导图标槽；null 表示不占位。
             * @param trailingIcon 尾部图标槽；null 表示不占位。
             * @param isError 是否处于错误状态。
             * @param keyboardOptions 键盘类型与输入法动作配置。
             * @param keyboardActions 输入法动作回调。
             * @param nativeSpec 输入框专属参数；委托默认实现原样接受。
             */
            @Composable
            override fun TextField(
                value: String,
                onValueChange: (String) -> Unit,
                modifier: Modifier,
                enabled: Boolean,
                readOnly: Boolean,
                singleLine: Boolean,
                maxLines: Int,
                visualTransformation: VisualTransformation,
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
                NomiDesignSystem.Default.components.text.TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = modifier,
                    enabled = enabled,
                    readOnly = readOnly,
                    singleLine = singleLine,
                    maxLines = maxLines,
                    visualTransformation = visualTransformation,
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

        /**
         * 此夹具不验证按钮族。
         */
        override val actions: NomiActionRenderer
            get() = error("SlotRecordingDesignSystem 不提供按钮族")

        /**
         * 此夹具不验证表面族。
         */
        override val surfaces: NomiSurfaceRenderer
            get() = error("SlotRecordingDesignSystem 不提供表面族")

        /**
         * 导航族委托 Nomi 默认实现，本夹具不验证导航视觉。
         */
        override val navigation: NomiNavigationRenderer =
            NomiDesignSystem.Default.components.navigation

        /**
         * 脚手架族委托 Nomi 默认实现，本夹具不验证页面外壳视觉。
         */
        override val scaffold: NomiScaffoldRenderer =
            NomiDesignSystem.Default.components.scaffold

        /**
         * 列表项族委托 Nomi 默认实现，本夹具不验证列表行视觉。
         */
        override val listItems: NomiListItemRenderer =
            NomiDesignSystem.Default.components.listItems

        /**
         * 图标族委托 Nomi 默认实现，本夹具不验证图标视觉。
         */
        override val icons: NomiIconRenderer =
            NomiDesignSystem.Default.components.icons

        /**
         * 装饰线族委托 Nomi 默认实现，本夹具不验证分割线视觉。
         */
        override val decorations: NomiDividerRenderer =
            NomiDesignSystem.Default.components.decorations

        /**
         * 控件族委托 Nomi 默认实现，本夹具不验证控件视觉。
         */
        override val controls: NomiControlRenderer =
            NomiDesignSystem.Default.components.controls

        /**
         * 反馈族委托 Nomi 默认实现，本夹具不验证提示浮层视觉。
         */
        override val feedback: NomiFeedbackRenderer =
            NomiDesignSystem.Default.components.feedback

        /**
         * 菜单族委托 Nomi 默认实现，本夹具不验证菜单视觉。
         */
        override val menus: NomiMenuRenderer =
            NomiDesignSystem.Default.components.menus
    }

    /**
     * 按文本内容记录的样式与颜色对。
     */
    val texts: MutableMap<String, Pair<TextStyle?, Color>> = mutableMapOf()

    /**
     * 按提交顺序记录的显式主题列表。
     */
    val themes: MutableList<NomiTheme> = mutableListOf()

    /**
     * 记录本层主题；中立主题由入口发布，本夹具不再回写。
     *
     * @param theme 本层显式提供的中立主题。
     * @param content 原样组合的内容。
     */
    @Composable
    override fun ProvideTheme(theme: NomiTheme, content: @Composable () -> Unit) {
        SideEffect { themes += theme }
        content()
    }
}

/**
 * 记录实际中立 slot 的颜色与样式，不在非组合 callback 中读取 Local。
 *
 * @param snapshots 接收内容色与文本样式二元组的可变列表，每次组合提交追加一项。
 */
@Composable
private fun RecordSlots(snapshots: MutableList<Pair<Color, TextStyle>>) {
    val color = LocalNomiContentColor.current
    val style = LocalNomiTextStyle.current
    SideEffect { snapshots += color to style }
}