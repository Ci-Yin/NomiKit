package ciyin.designsystem.theme

import androidx.compose.runtime.Immutable

/**
 * 调用方明确提供的主题设置，用于在主题层级之间保留覆盖来源。
 *
 * [NomiTheme] 与 [NomiThemeOverride] 只接受本对象作为本层显式输入。可空字段的 null 表示本层未指定，
 * 不表示清除父级。非空的颜色、排版和形状对象均表示完整组，不能据此推断调用方仅修改了哪些字段。
 *
 * 根主题只继承父级明确的非颜色组，局部主题继承全部明确设置；两者都不能把
 * 已解析的五组默认值回填为显式覆盖。适配器无法承载的组由实现自行降级并记录警告。
 *
 * @property spacings 明确指定的完整页面间距；不自动改变原生控件内部留白。
 * @property sizes 明确指定的完整布局尺寸；不强制所有原生控件采用统一高度。
 * @property colorScheme 明确指定的完整色板，是主题层级唯一的颜色覆盖来源。
 * @property typography 明确指定的完整十五角色排版，按适配器支持的原生角色映射。
 * @property shapes 明确指定的完整九级圆角；无法承载该组的设计系统降级并记录警告。
 */
@Immutable
data class NomiThemeOverrides(
    val spacings: Spacings? = null,
    val sizes: NomiSizes? = null,
    val colorScheme: NomiColorScheme? = null,
    val typography: NomiTypography? = null,
    val shapes: NomiShapes? = null,
) {
    /**
     * 将子层明确提供的设置合并到当前父层请求，返回新对象而不修改任一输入。
     *
     * 每一组取子层非空值，否则继承父层；未指定的子层值不是清除指令。
     * 本方法只负责合并，不校验原生能力；降级在适配器执行。
     *
     * @param child 子层本次明确提供的设置。
     * @return 合并后的显式请求，不包含补齐的原生默认值。
     */
    fun merge(child: NomiThemeOverrides): NomiThemeOverrides = NomiThemeOverrides(
        spacings = child.spacings ?: spacings,
        sizes = child.sizes ?: sizes,
        colorScheme = child.colorScheme ?: colorScheme,
        typography = child.typography ?: typography,
        shapes = child.shapes ?: shapes,
    )
}
