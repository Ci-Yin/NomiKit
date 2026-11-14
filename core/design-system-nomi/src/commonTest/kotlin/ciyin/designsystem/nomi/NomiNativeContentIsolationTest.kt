package ciyin.designsystem.nomi

import androidx.compose.foundation.text.InlineTextContent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.component.text.NomiTextRenderer
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.runtime.NomiThemeRequest
import ciyin.designsystem.runtime.ResolvedNomiTheme
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiThemeOverride
import ciyin.designsystem.theme.NomiThemeOverrides
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
                    NomiThemeOverride(designSystem = recorder) {
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
            assertEquals(NomiThemeOverrides(), recorder.requests.single().overrides)
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
     * 三族组装；只有文本族可用，其余族访问即失败。
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
            ) {
                SideEffect { texts[text.text] = style to color }
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
    }

    /**
     * 按文本内容记录的样式与颜色对。
     */
    val texts: MutableMap<String, Pair<TextStyle?, Color>> = mutableMapOf()

    /**
     * 按提交顺序记录的请求列表。
     */
    val requests: MutableList<NomiThemeRequest> = mutableListOf()

    /**
     * 记录请求并发布按请求明暗解析的默认五组结果。
     *
     * @param request 外部主题边界解析后的请求。
     * @param content 接收五组结果的内容。
     */
    @Composable
    override fun ProvideTheme(
        request: NomiThemeRequest,
        content: @Composable (ResolvedNomiTheme) -> Unit
    ) {
        SideEffect { requests += request }
        content(
            ResolvedNomiTheme(
                requestedMode = request.requestedMode,
                isDark = request.isDark,
                overrides = request.overrides,
            ),
        )
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
