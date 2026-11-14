package ciyin.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 默认尺寸令牌。
 */
private object DefaultSizeTokens {
    /**
     * 图标尺寸等级。
     */
    val icon: SizeScale = SizeScale(
        tiny = 14.dp,
        extraSmall = 16.dp,
        small = 18.dp,
        medium = 20.dp,
        large = 24.dp,
        extraLarge = 32.dp,
        huge = 40.dp,
        massive = 48.dp,
        colossal = 56.dp,
    )

    /**
     * 头像尺寸等级。
     */
    val avatar: SizeScale = SizeScale(
        tiny = 18.dp,
        extraSmall = 24.dp,
        small = 32.dp,
        medium = 40.dp,
        large = 48.dp,
        extraLarge = 56.dp,
        huge = 64.dp,
        massive = 72.dp,
        colossal = 96.dp,
    )

    /**
     * 线条宽度等级。
     */
    val strokes: Strokes = Strokes(
        thin = 1.dp,
        medium = 2.dp,
        thick = 3.dp,
    )

    /**
     * 常用组件高度。
     */
    val componentHeights: ComponentHeights = ComponentHeights(
        appBar = 56.dp,
        bottomBar = 56.dp,
        tab = 48.dp,
        chip = 32.dp,
        badge = 16.dp,
    )

    /**
     * 常用布局宽度约束。
     */
    val layoutConstraints: LayoutConstraints = LayoutConstraints(
        cardMinWidth = 280.dp,
        dialogMinWidth = 280.dp,
        dialogMaxWidth = 560.dp,
        sheetMinWidth = 360.dp,
    )
}

/**
 * 定义应用全局使用的尺寸（Dp）。
 *
 * 包含图标、头像、线条、组件高度、布局约束等各种 UI 元素的统一尺寸标准。
 * 通过 `NomiSizes` 可以在页面中复用尺寸参考；不会自动强制原生控件采用统一高度。
 * 支持公开构造和编译器生成的成员 copy；未传构造参数复用原有不可变默认子组，
 * 也可通过 [defaultNomiSizes] 获取默认对象后复制定制，无需导入复制扩展。
 *
 * @property icon 图标尺寸等级
 * @property avatar 头像尺寸等级
 * @property strokes 线条宽度（边框、分隔线等）
 * @property componentHeights 组件高度
 * @property layoutConstraints 布局约束（最小/最大宽度等）
 */
@Immutable
data class NomiSizes(
    val icon: SizeScale = DefaultSizeTokens.icon,
    val avatar: SizeScale = DefaultSizeTokens.avatar,
    val strokes: Strokes = DefaultSizeTokens.strokes,
    val componentHeights: ComponentHeights = DefaultSizeTokens.componentHeights,
    val layoutConstraints: LayoutConstraints = DefaultSizeTokens.layoutConstraints,
)

/**
 * 尺寸等级体系。
 *
 * 用于统一管理应用中各种 UI 元素的尺寸等级，采用语义化命名而非直接暴露 dp 数值。
 * 该体系共 9 个等级，由小到大依次为：
 * `tiny → extraSmall → small → medium → large → extraLarge → huge → massive → colossal`
 *
 * 适用于图标、头像等需要多级尺寸的 UI 元素。公开构造时须提供全部九级尺寸；
 * 也可通过 [defaultIconSizeScale] 或 [defaultAvatarSizeScale] 取得基准，再调用生成的成员 copy。
 *
 * 尺寸以 dp 表示；等级名表达设计用途，本类型不校验值是否非负或按等级递增。
 *
 * @property tiny 极小尺寸
 * @property extraSmall 超小尺寸
 * @property small 小尺寸
 * @property medium 中等尺寸
 * @property large 大尺寸
 * @property extraLarge 超大尺寸
 * @property huge 巨大尺寸
 * @property massive 庞大尺寸
 * @property colossal 宏伟尺寸
 */
@Immutable
data class SizeScale(
    val tiny: Dp,
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val huge: Dp,
    val massive: Dp,
    val colossal: Dp,
)

/**
 * 线条宽度（边框、分隔线等）。
 *
 * 用于统一管理应用中各种线条的宽度标准，包括边框和分隔线的粗细等级。
 * 公开构造时须提供全部三级宽度；也可通过 [defaultStrokes] 取得基准，再调用生成的成员 copy。
 *
 * @property thin 细线条宽度，默认工厂值：`1.dp`
 * @property medium 中等线条宽度，默认工厂值：`2.dp`
 * @property thick 粗线条宽度，默认工厂值：`3.dp`
 */
@Immutable
data class Strokes(
    val thin: Dp,
    val medium: Dp,
    val thick: Dp,
)

/**
 * 组件高度。
 *
 * 提供组件高度参考，不自动固定原生组件高度；含文字的容器仍应由内容、排版与内边距决定高度。
 * 公开构造时须提供全部五项尺寸；也可通过 [defaultComponentHeights] 取得基准，再调用生成的成员 copy。
 *
 * @property appBar 应用栏高度，默认工厂值：`56.dp`
 * @property bottomBar 底部导航栏高度，默认工厂值：`56.dp`
 * @property tab 标签栏高度，默认工厂值：`48.dp`
 * @property chip 芯片高度，默认工厂值：`32.dp`
 * @property badge 徽章尺寸，默认工厂值：`16.dp`
 */
@Immutable
data class ComponentHeights(
    val appBar: Dp,
    val bottomBar: Dp,
    val tab: Dp,
    val chip: Dp,
    val badge: Dp,
)

/**
 * 布局约束（最小/最大宽度等）。
 *
 * 用于统一管理应用中各种布局容器的最小和最大宽度约束。
 * 公开构造时须提供全部四项约束；也可通过 [defaultLayoutConstraints] 取得基准，再调用生成的成员 copy。
 *
 * 宽度以 dp 表示，本类型不校验最小值与最大值的大小关系。
 *
 * @property cardMinWidth 卡片最小宽度，默认工厂值：`280.dp`
 * @property dialogMinWidth 对话框最小宽度，默认工厂值：`280.dp`
 * @property dialogMaxWidth 对话框最大宽度，默认工厂值：`560.dp`
 * @property sheetMinWidth 底部表单最小宽度，默认工厂值：`360.dp`
 */
@Immutable
data class LayoutConstraints(
    val cardMinWidth: Dp,
    val dialogMinWidth: Dp,
    val dialogMaxWidth: Dp,
    val sheetMinWidth: Dp,
)

/**
 * 返回默认图标尺寸等级，数值由默认尺寸令牌单一维护，不读取当前主题。
 *
 * @return 共享的不可变图标尺度，九级默认尺寸从 14dp 到 56dp。
 */
fun defaultIconSizeScale(): SizeScale = DefaultSizeTokens.icon

/**
 * 返回默认头像尺寸等级，数值由默认尺寸令牌单一维护，不读取当前主题。
 *
 * @return 共享的不可变头像尺度，九级默认尺寸从 18dp 到 96dp。
 */
fun defaultAvatarSizeScale(): SizeScale = DefaultSizeTokens.avatar

/**
 * 返回默认完整尺寸组；子尺寸对象可通过生成的成员 copy 分别定制。
 *
 * @return 新的 Nomi 尺寸组，其子组复用默认不可变对象；不读取当前主题。
 */
fun defaultNomiSizes(): NomiSizes = NomiSizes()

/**
 * 返回默认描边尺度，不读取当前主题。
 *
 * @return 共享的不可变描边尺度，细、中、粗线分别为 1dp、2dp、3dp。
 */
fun defaultStrokes(): Strokes = DefaultSizeTokens.strokes

/**
 * 返回默认组件高度令牌，不强制原生控件采用这些高度。
 *
 * @return 共享的不可变高度组；含文字容器仍应由内容与内边距决定实际高度。
 */
fun defaultComponentHeights(): ComponentHeights = DefaultSizeTokens.componentHeights

/**
 * 返回默认页面布局宽度约束，不读取当前主题。
 *
 * @return 共享的不可变约束组，提供卡片、对话框和底部表单的宽度参考。
 */
fun defaultLayoutConstraints(): LayoutConstraints = DefaultSizeTokens.layoutConstraints
