package ciyin.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 调用方明确提供的主题设置，用于在主题层级之间保留覆盖来源。
 *
 * [NomiTheme] 和 [NomiThemeOverride] 的直接参数也会归入本对象。可空字段的 null 表示本层未指定，
 * 不表示清除父级；[accentColor] 的 [Color.Unspecified] 含义相同，透明色则是有效覆盖。
 * 非空的颜色、排版和形状对象均表示完整组，不能据此推断调用方仅修改了哪些字段。
 *
 * 根主题只继承父级明确的非颜色组，局部主题继承全部明确设置；两者都不能把
 * 已解析的五组默认值回填为显式覆盖。同层重复来源及完整色板与 accent 冲突
 * 由主题入口校验，适配器负责拒绝原生库无法表达的主题组。
 *
 * @property accentColor 单独的强调色覆盖；可叠加在父层完整色板上，Unspecified 表示本层未指定。
 * @property spacings 明确指定的完整页面间距；不自动改变原生控件内部留白。
 * @property sizes 明确指定的完整布局尺寸；不强制所有原生控件采用统一高度。
 * @property colorScheme 明确指定的完整色板；替换父层色板时同时清除父层单独的强调色覆盖。
 * @property typography 明确指定的完整十五角色排版，按适配器支持的原生角色映射。
 * @property shapes 明确指定的完整九级圆角；不支持该组映射的设计系统应明确拒绝。
 */
@Immutable
data class NomiThemeOverrides(
    val accentColor: Color = Color.Unspecified,
    val spacings: Spacings? = null,
    val sizes: NomiSizes? = null,
    val colorScheme: NomiColorScheme? = null,
    val typography: NomiTypography? = null,
    val shapes: NomiShapes? = null,
) {
    /**
     * 将子层明确提供的设置合并到当前父层请求，返回新对象而不修改任一输入。
     *
     * 普通主题组取子层非空值，否则继承父层；子层完整色板且未给 accent 时清除父层 accent，
     * 子层仅提供 accent 时则保留父层色板并替换强调色。未指定的子层值不是清除指令。
     * 直接调用时若 child 同时含色板与 accent，两者均被保留；主题入口会拒绝这种同层输入。
     * 本方法只负责合并，不校验同层来源冲突或原生能力；这些校验在主题入口和适配器执行。
     *
     * @param child 子层本次明确提供的设置。
     * @return 合并后的显式请求，不包含补齐的原生默认值。
     */
    fun merge(child: NomiThemeOverrides): NomiThemeOverrides = NomiThemeOverrides(
        accentColor = when {
            child.accentColor != Color.Unspecified -> child.accentColor
            child.colorScheme != null -> Color.Unspecified
            else -> accentColor
        },
        spacings = child.spacings ?: spacings,
        sizes = child.sizes ?: sizes,
        colorScheme = child.colorScheme ?: colorScheme,
        typography = child.typography ?: typography,
        shapes = child.shapes ?: shapes,
    )
}
