package ciyin.designsystem

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonSpec
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.card.NomiCardSpec
import ciyin.designsystem.component.list.NomiListItemRenderer
import ciyin.designsystem.component.list.NomiListItemSpec
import ciyin.designsystem.component.navigation.NomiNavigationItemColors
import ciyin.designsystem.component.navigation.NomiNavigationItemScope
import ciyin.designsystem.component.navigation.NomiNavigationItemSpec
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.navigation.NomiNavigationSpec
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldSpec
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.surface.NomiSurfaceSpec
import ciyin.designsystem.component.text.NomiTextRenderer
import ciyin.designsystem.component.text.NomiTextSpec
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.theme.NomiTheme

/**
 * 记录中立层传给文本 renderer 的真实参数，不补任何默认样式。
 *
 * @property style 中立入口解析后的可空排版；null 表示无显式样式。
 * @property color 中立入口按优先级解析的颜色；Unspecified 仍表示未指定。
 * @property overflow 超出布局限制时的截断策略。
 * @property softWrap 是否允许软换行。
 * @property maxLines 传给 renderer 的最大行数。
 * @property minLines 传给 renderer 的最小行数。
 * @property fontSize 原样记录的显式字号，Unspecified 表示未指定。
 * @property fontStyle 原样记录的显式字形；null 表示未指定。
 * @property fontWeight 原样记录的显式字重；null 表示未指定。
 * @property fontFamily 原样记录的显式字体族；null 表示未指定。
 * @property letterSpacing 原样记录的显式字距，Unspecified 表示未指定。
 * @property textDecoration 原样记录的显式文本装饰；null 表示未指定。
 * @property textAlign 原样记录的显式对齐；null 表示未指定。
 * @property lineHeight 原样记录的显式行高，Unspecified 表示未指定。
 * @property inlineContentKeys 原样记录的内联内容键集合，空集合表示纯文本或无内联内容。
 * @property hasOnTextLayout 是否传入了非空布局回调。
 * @property autoSize 原样记录的自动字号策略；null 表示未指定。
 * @property nativeSpec 原样记录的文本专属参数；null 表示未指定。
 */
@Immutable
internal data class RecordedText(
    val style: TextStyle?,
    val color: Color,
    val overflow: TextOverflow,
    val softWrap: Boolean,
    val maxLines: Int,
    val minLines: Int,
    val fontSize: TextUnit = TextUnit.Unspecified,
    val fontStyle: FontStyle? = null,
    val fontWeight: FontWeight? = null,
    val fontFamily: FontFamily? = null,
    val letterSpacing: TextUnit = TextUnit.Unspecified,
    val textDecoration: TextDecoration? = null,
    val textAlign: TextAlign? = null,
    val lineHeight: TextUnit = TextUnit.Unspecified,
    val inlineContentKeys: Set<String> = emptySet(),
    val hasOnTextLayout: Boolean = false,
    val autoSize: TextAutoSize? = null,
    val nativeSpec: NomiTextSpec? = null,
)

/**
 * 记录按钮的稀疏参数，验证中立层不提前填入 Nomi 默认值。
 *
 * @property enabled 请求的按钮启用状态。
 * @property variant 显式按钮变体，null 表示未指定。
 * @property size 显式尺寸预设，null 表示未指定。
 * @property colors 逐字段颜色覆盖，保留 Unspecified 与 Transparent 的区别。
 * @property shape 显式形状，null 表示未指定。
 * @property contentPadding 显式内边距，null 表示未指定。
 * @property textStyle 显式文本样式增量，null 表示未指定。
 * @property nativeSpec 原样记录的按钮专属参数；null 表示未指定。
 */
@Immutable
internal data class RecordedButton(
    val enabled: Boolean,
    val variant: NomiButtonVariant?,
    val size: NomiControlSize?,
    val colors: NomiButtonColors,
    val shape: Shape?,
    val contentPadding: PaddingValues?,
    val textStyle: TextStyle?,
    val nativeSpec: NomiButtonSpec? = null,
)

/**
 * 记录 Surface 与独立 Card 派发的参数。
 *
 * @property shape 显式形状，null 表示未指定。
 * @property colors 容器色与内容色的稀疏覆盖。
 * @property border 保留默认、无描边及指定描边三种意图。
 * @property contentPadding Card 专用的显式内边距；null 表示未指定，Surface 记录固定保留 null。
 * @property nativeSpec 原样记录的专属参数；Surface 与 Card 共用该字段，null 表示未指定。
 */
@Immutable
internal data class RecordedSurface(
    val shape: Shape?,
    val colors: NomiSurfaceColors,
    val border: NomiBorder,
    val contentPadding: PaddingValues? = null,
    val nativeSpec: NomiNativeSpec<*>? = null,
)

/**
 * 记录单个导航项派发参数；槽位只记录是否提供，不执行槽内容。
 *
 * @property selected 原样记录的选中状态。
 * @property hasModifier 是否传入了非默认修饰符。
 * @property hasIcon 是否提供了图标槽。
 * @property hasBadge 是否提供了徽标槽。
 * @property hasLabel 是否提供了文案槽。
 * @property colors 原样记录的稀疏颜色覆盖。
 * @property hasInteractionSource 是否传入了交互源。
 * @property nativeSpec 原样记录的导航项专属参数；null 表示未指定。
 */
@Immutable
internal data class RecordedNavigationItem(
    val selected: Boolean,
    val hasModifier: Boolean,
    val hasIcon: Boolean,
    val hasBadge: Boolean,
    val hasLabel: Boolean,
    val colors: NomiNavigationItemColors,
    val hasInteractionSource: Boolean,
    val nativeSpec: NomiNavigationItemSpec? = null,
)

/**
 * 记录一次导航套件派发，按分组保留主导航项与底部导航项。
 *
 * @property layoutType 调用方请求的导航布局。
 * @property containerColor 原样记录的导航外壳容器色；Unspecified 仍表示未指定。
 * @property items 主导航项，按声明顺序记录。
 * @property footerItems 底部导航项，按声明顺序记录。
 * @property nativeSpec 原样记录的导航套件专属参数；null 表示未指定。
 */
@Immutable
internal data class RecordedNavigationSuite(
    val layoutType: NomiNavigationSuiteType,
    val containerColor: Color,
    val items: List<RecordedNavigationItem>,
    val footerItems: List<RecordedNavigationItem>,
    val nativeSpec: NomiNavigationSpec? = null,
)

/**
 * 记录中立层传给脚手架的真实颜色参数。
 *
 * @property colors 原样记录的容器色与内容色覆盖；Unspecified 仍表示未指定。
 * @property nativeSpec 原样记录的脚手架专属参数；null 表示未指定。
 */
@Immutable
internal data class RecordedScaffold(
    val colors: NomiScaffoldColors,
    val nativeSpec: NomiScaffoldSpec? = null,
)

/**
 * 记录列表项派发；槽位只记录是否提供，不执行槽内容。
 *
 * @property hasModifier 是否传入非默认修饰符。
 * @property hasOnClick 是否传入点击回调。
 * @property hasLeading 是否提供前导槽。
 * @property hasSubtitle 是否提供副标题槽。
 * @property hasTrailing 是否提供尾部槽。
 * @property nativeSpec 原样记录的专属参数；null 表示调用方未指定任何设计系统的参数。
 */
@Immutable
internal data class RecordedListItem(
    val hasModifier: Boolean,
    val hasOnClick: Boolean,
    val hasLeading: Boolean,
    val hasSubtitle: Boolean,
    val hasTrailing: Boolean,
    val nativeSpec: NomiListItemSpec? = null,
)

/**
 * 不依赖任何原生 adapter 的录制型设计系统。
 *
 * 派发记录在 SideEffect 提交，列表在实例生命周期内累积，不在重组时自动清空。
 *
 * @property name 构造设计系统标识的名称，用于区分嵌套实例。
 */
internal class RecordingDesignSystem(
    private val name: String = "recording",
) : NomiDesignSystem {
    /**
     * 按组合提交顺序记录入口传入的主题。
     */
    val received: MutableList<NomiTheme> = mutableListOf()

    /**
     * 按文本标识记录最后一次已提交的派发。
     */
    val texts: MutableMap<String, RecordedText> = mutableMapOf()

    /**
     * 按组合顺序记录按钮派发。
     */
    val buttons: MutableList<RecordedButton> = mutableListOf()

    /**
     * 仅记录 Surface 派发，Card 不得混入。
     */
    val surfaces: MutableList<RecordedSurface> = mutableListOf()

    /**
     * 仅记录 Card 派发。
     */
    val cards: MutableList<RecordedSurface> = mutableListOf()

    /**
     * 按组合顺序记录导航套件派发及分组后的导航项。
     */
    val navigations: MutableList<RecordedNavigationSuite> = mutableListOf()

    /**
     * 按组合顺序记录页面脚手架派发。
     */
    val scaffolds: MutableList<RecordedScaffold> = mutableListOf()

    /**
     * 按组合顺序记录列表项派发。
     */
    val listItems: MutableList<RecordedListItem> = mutableListOf()

    /**
     * 录制型设计系统标识。
     */
    override val id: NomiDesignSystemId = NomiDesignSystemId(name)

    /**
     * 四族 renderer；其中 surfaces 显式实现 Surface 与 Card 两个入口，navigation 记录导航套件派发。
     */
    override val components: NomiDesignSystemComponents = object : NomiDesignSystemComponents {
        /**
         * 原样记录文本参数。
         */
        override val text: NomiTextRenderer = object : NomiTextRenderer {
            /**
             * 在组合提交后按文本键记录参数，不绘制文本、不填默认排版或正文颜色。
             *
             * @param text 记录映射的键；相同文本的后续提交覆盖先前记录。
             * @param modifier 接口传入的布局修饰符；此录制夹具不应用也不记录。
             * @param style 原样记录的可空文本样式。
             * @param color 原样记录的颜色，保留 Unspecified。
             * @param fontSize 原样记录的显式字号。
             * @param fontStyle 原样记录的显式字形。
             * @param fontWeight 原样记录的显式字重。
             * @param fontFamily 原样记录的显式字体族。
             * @param letterSpacing 原样记录的显式字距。
             * @param textDecoration 原样记录的显式文本装饰。
             * @param textAlign 原样记录的显式对齐。
             * @param lineHeight 原样记录的显式行高。
             * @param overflow 原样记录的溢出策略。
             * @param softWrap 原样记录的软换行开关。
             * @param maxLines 原样记录的最大行数。
             * @param minLines 原样记录的最小行数。
             * @param onTextLayout 原样记录是否非空的布局回调。
             * @param autoSize 原样记录的自动字号策略。
             * @param nativeSpec 专属参数；录制夹具原样记录，不解析也不应用。
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
                val call = RecordedText(
                    style = style,
                    color = color,
                    overflow = overflow,
                    softWrap = softWrap,
                    maxLines = maxLines,
                    minLines = minLines,
                    fontSize = fontSize,
                    fontStyle = fontStyle,
                    fontWeight = fontWeight,
                    fontFamily = fontFamily,
                    letterSpacing = letterSpacing,
                    textDecoration = textDecoration,
                    textAlign = textAlign,
                    lineHeight = lineHeight,
                    hasOnTextLayout = onTextLayout != null,
                    autoSize = autoSize,
                    nativeSpec = nativeSpec,
                )
                SideEffect { texts[text] = call }
            }

            /**
             * 在组合提交后按富文本内容记录参数，内联内容只记录键集合。
             *
             * @param text 记录映射的键；相同富文本的后续提交覆盖先前记录。
             * @param modifier 接口传入的布局修饰符；此录制夹具不应用也不记录。
             * @param style 原样记录的可空文本样式。
             * @param color 原样记录的颜色，保留 Unspecified。
             * @param fontSize 原样记录的显式字号。
             * @param fontStyle 原样记录的显式字形。
             * @param fontWeight 原样记录的显式字重。
             * @param fontFamily 原样记录的显式字体族。
             * @param letterSpacing 原样记录的显式字距。
             * @param textDecoration 原样记录的显式文本装饰。
             * @param textAlign 原样记录的显式对齐。
             * @param lineHeight 原样记录的显式行高。
             * @param overflow 原样记录的溢出策略。
             * @param softWrap 原样记录的软换行开关。
             * @param maxLines 原样记录的最大行数。
             * @param minLines 原样记录的最小行数。
             * @param inlineContent 原样记录键集合的内联内容映射。
             * @param onTextLayout 原样记录是否非空的布局回调。
             * @param autoSize 原样记录的自动字号策略。
             * @param nativeSpec 专属参数；录制夹具原样记录，不解析也不应用。
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
                val call = RecordedText(
                    style = style,
                    color = color,
                    overflow = overflow,
                    softWrap = softWrap,
                    maxLines = maxLines,
                    minLines = minLines,
                    fontSize = fontSize,
                    fontStyle = fontStyle,
                    fontWeight = fontWeight,
                    fontFamily = fontFamily,
                    letterSpacing = letterSpacing,
                    textDecoration = textDecoration,
                    textAlign = textAlign,
                    lineHeight = lineHeight,
                    inlineContentKeys = inlineContent.keys,
                    hasOnTextLayout = onTextLayout != null,
                    autoSize = autoSize,
                    nativeSpec = nativeSpec,
                )
                SideEffect { texts[text.text] = call }
            }
        }

        /**
         * 原样记录按钮参数，并运行真实 RowScope slot。
         */
        override val actions: NomiActionRenderer = object : NomiActionRenderer {
            /**
             * 记录稀疏按钮参数并组合 Row 内容，不绘制按钮或建立点击交互。
             *
             * @param onClick 接口要求的点击回调；录制夹具不会调用。
             * @param modifier 接口要求的按钮修饰符；录制夹具不应用也不记录。
             * @param enabled 原样记录的启用状态，不影响夹具内容组合。
             * @param variant 原样记录的可空变体，不补默认值。
             * @param size 原样记录的可空尺寸，不补默认值。
             * @param colors 原样记录的四态颜色，不解析 Unspecified。
             * @param shape 原样记录的可空形状。
             * @param contentPadding 原样记录的可空内边距，不施加到 Row。
             * @param textStyle 原样记录的可空样式，不发布到内容环境。
             * @param content 在真实 RowScope 中组合的内容，供嵌套派发测试使用。
             */
            @Composable
            override fun Button(
                onClick: () -> Unit,
                modifier: Modifier,
                enabled: Boolean,
                variant: NomiButtonVariant?,
                size: NomiControlSize?,
                colors: NomiButtonColors,
                shape: Shape?,
                contentPadding: PaddingValues?,
                textStyle: TextStyle?,
                nativeSpec: NomiButtonSpec?,
                content: @Composable RowScope.() -> Unit,
            ) {
                val call = RecordedButton(
                    enabled = enabled,
                    variant = variant,
                    size = size,
                    colors = colors,
                    shape = shape,
                    contentPadding = contentPadding,
                    textStyle = textStyle,
                    nativeSpec = nativeSpec,
                )
                SideEffect { buttons += call }
                Row(content = content)
            }
        }

        /**
         * 独立记录两个容器入口，避免 Card 被降格为 Surface。
         */
        override val surfaces: NomiSurfaceRenderer = object : NomiSurfaceRenderer {
            /**
             * 在组合提交后追加 Surface 参数，并直接组合内容而不绘制表面。
             *
             * @param modifier 接口要求的表面修饰符；录制夹具不应用也不记录。
             * @param shape 原样记录的可空形状。
             * @param colors 原样记录的稀疏表面颜色，不建立颜色环境。
             * @param border 原样记录的三态描边。
             * @param nativeSpec 专属参数；录制夹具原样记录，不解析也不应用。
             * @param content 直接组合的内容，不添加额外布局 Scope 或样式。
             */
            @Composable
            override fun Surface(
                modifier: Modifier,
                shape: Shape?,
                colors: NomiSurfaceColors,
                border: NomiBorder,
                nativeSpec: NomiSurfaceSpec?,
                content: @Composable () -> Unit,
            ) {
                val call = RecordedSurface(
                    shape = shape,
                    colors = colors,
                    border = border,
                    nativeSpec = nativeSpec
                )
                SideEffect { this@RecordingDesignSystem.surfaces += call }
                content()
            }

            /**
             * 在组合提交后独立追加 Card 参数，并为内容提供 ColumnScope。
             *
             * @param modifier 接口要求的卡片修饰符；录制夹具不应用也不记录。
             * @param shape 原样记录的可空形状。
             * @param colors 原样记录的稀疏颜色，不建立颜色环境。
             * @param border 原样记录的三态描边。
             * @param contentPadding 原样记录的可空内边距，不施加到 Column。
             * @param nativeSpec 专属参数；录制夹具原样记录，不解析也不应用。
             * @param content 在真实 ColumnScope 中组合的内容，供嵌套派发测试使用。
             */
            @Composable
            override fun Card(
                modifier: Modifier,
                shape: Shape?,
                colors: NomiSurfaceColors,
                border: NomiBorder,
                contentPadding: PaddingValues?,
                nativeSpec: NomiCardSpec?,
                content: @Composable ColumnScope.() -> Unit,
            ) {
                val call = RecordedSurface(
                    shape = shape,
                    colors = colors,
                    border = border,
                    contentPadding = contentPadding,
                    nativeSpec = nativeSpec,
                )
                SideEffect { cards += call }
                Column(content = content)
            }
        }

        /**
         * 记录导航布局与两个导航项分组，并组合页面内容，不绘制原生导航。
         */
        override val navigation: NomiNavigationRenderer = object : NomiNavigationRenderer {
            /**
             * 按声明顺序收集两个分组，提交后追加一条导航派发记录。
             *
             * @param layoutType 原样记录的导航布局。
             * @param modifier 接口要求的修饰符；录制夹具不应用也不记录。
             * @param containerColor 原样记录的导航外壳容器色；Unspecified 仍表示未指定。
             * @param items 主导航项声明；在录制作用域中执行以收集参数。
             * @param footerItems 底部导航项声明；在录制作用域中执行以收集参数。
             * @param nativeSpec 专属参数；录制夹具原样记录，不解析也不应用。
             * @param content 直接组合的页面内容，不添加原生布局。
             */
            @Composable
            override fun NavigationSuite(
                layoutType: NomiNavigationSuiteType,
                modifier: Modifier,
                containerColor: Color,
                items: @Composable NomiNavigationItemScope.() -> Unit,
                footerItems: @Composable NomiNavigationItemScope.() -> Unit,
                nativeSpec: NomiNavigationSpec?,
                content: @Composable () -> Unit,
            ) {
                val itemCalls = mutableListOf<RecordedNavigationItem>()
                val footerCalls = mutableListOf<RecordedNavigationItem>()
                items(RecordingNavigationItemScope(itemCalls))
                footerItems(RecordingNavigationItemScope(footerCalls))
                val call = RecordedNavigationSuite(
                    layoutType = layoutType,
                    containerColor = containerColor,
                    items = itemCalls.toList(),
                    footerItems = footerCalls.toList(),
                    nativeSpec = nativeSpec,
                )
                SideEffect { navigations += call }
                content()
            }
        }

        /**
         * 记录脚手架颜色并按声明顺序组合三个槽位，不绘制原生外壳。
         */
        override val scaffold: NomiScaffoldRenderer = object : NomiScaffoldRenderer {
            /**
             * 提交后追加一条脚手架派发记录，并按顶栏、主体、底栏顺序组合内容。
             *
             * @param modifier 接口要求的修饰符；录制夹具不应用也不记录。
             * @param colors 原样记录的容器色与内容色覆盖。
             * @param topBar 顶部栏槽，直接组合。
             * @param bottomBar 底部栏槽，直接组合。
             * @param nativeSpec 专属参数；录制夹具原样记录，不解析也不应用。
             * @param content 页面主体槽，直接组合。
             */
            @Composable
            override fun Scaffold(
                modifier: Modifier,
                colors: NomiScaffoldColors,
                topBar: @Composable () -> Unit,
                bottomBar: @Composable () -> Unit,
                nativeSpec: NomiScaffoldSpec?,
                content: @Composable () -> Unit,
            ) {
                SideEffect {
                    scaffolds += RecordedScaffold(
                        colors = colors,
                        nativeSpec = nativeSpec
                    )
                }
                topBar()
                content()
                bottomBar()
            }
        }

        /**
         * 记录列表项槽位与点击参数，并按顺序组合提供的槽以验证文本派发。
         */
        override val listItems: NomiListItemRenderer = object : NomiListItemRenderer {
            /**
             * 组合提交后追加列表项记录，并直接组合提供的槽位，不绘制原生列表行。
             *
             * @param modifier 接口要求的修饰符；录制夹具只记录是否非默认。
             * @param onClick 接口要求的点击回调；录制夹具只记录是否提供，不会调用。
             * @param leading 前导槽；null 表示不占位。
             * @param title 标题槽，直接组合。
             * @param subtitle 副标题槽；null 表示不占位。
             * @param trailing 尾部槽；null 表示不占位。
             * @param nativeSpec 专属参数；录制夹具原样记录，不解析也不应用。
             */
            @Composable
            override fun ListItem(
                modifier: Modifier,
                onClick: (() -> Unit)?,
                leading: (@Composable () -> Unit)?,
                title: @Composable () -> Unit,
                subtitle: (@Composable () -> Unit)?,
                trailing: (@Composable () -> Unit)?,
                nativeSpec: NomiListItemSpec?,
            ) {
                val call = RecordedListItem(
                    hasModifier = modifier != Modifier,
                    hasOnClick = onClick != null,
                    hasLeading = leading != null,
                    hasSubtitle = subtitle != null,
                    hasTrailing = trailing != null,
                    nativeSpec = nativeSpec,
                )
                SideEffect { this@RecordingDesignSystem.listItems += call }
                Column {
                    leading?.invoke()
                    title()
                    subtitle?.invoke()
                    trailing?.invoke()
                }
            }
        }
    }

    /**
     * 记录入口传入的主题并组合内容；夹具不建立额外原生环境或发布主题。
     *
     * @param theme 本层显式主题值。
     * @param content 在录制环境中组合的内容。
     */
    @Composable
    override fun ProvideTheme(theme: NomiTheme, content: @Composable () -> Unit) {
        SideEffect { received += theme }
        content()
    }
}

/**
 * 收集导航项声明参数的录制作用域，不绘制任何内容。
 *
 * @property calls 收集目标列表，按声明顺序追加。
 */
private class RecordingNavigationItemScope(
    private val calls: MutableList<RecordedNavigationItem>,
) : NomiNavigationItemScope {
    /**
     * 记录单个导航项参数；录制阶段不执行槽内容，也不触发点击。
     *
     * @param selected 原样记录的选中状态。
     * @param onClick 点击回调；此录制夹具不会调用。
     * @param modifier 原样判断是否传入非默认修饰符，不应用也不记录实例。
     * @param icon 图标槽；只记录是否提供。
     * @param badge 徽标槽；只记录是否提供。
     * @param label 文案槽；只记录是否提供。
     * @param colors 原样记录的稀疏颜色覆盖。
     * @param interactionSource 交互源；只记录是否提供。
     * @param nativeSpec 专属参数；录制夹具原样记录，不解析也不应用。
     */
    @Composable
    override fun NavigationItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        icon: @Composable (() -> Unit)?,
        badge: @Composable (() -> Unit)?,
        label: @Composable (() -> Unit)?,
        colors: NomiNavigationItemColors,
        interactionSource: MutableInteractionSource?,
        nativeSpec: NomiNavigationItemSpec?,
    ) {
        calls += RecordedNavigationItem(
            selected = selected,
            hasModifier = modifier != Modifier,
            hasIcon = icon != null,
            hasBadge = badge != null,
            hasLabel = label != null,
            colors = colors,
            hasInteractionSource = interactionSource != null,
            nativeSpec = nativeSpec,
        )
    }
}