package ciyin.designsystem.fluent

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.designsystem.theme.NomiShapes
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiTypography
import io.github.composefluent.Colors
import io.github.composefluent.CornerRadius
import io.github.composefluent.Shades
import io.github.composefluent.Typography
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors

/**
 * 只传递已解析明暗模式；原生色板取默认，排版与圆角继承当前 Fluent 环境。
 *
 * @receiver 本层显式中立主题，仅读取 [NomiTheme.isDark]。
 * @return 只包含明暗模式的 Fluent 主题规格。
 */
fun NomiTheme.toNativeFluentTheme(): FluentTheme = FluentTheme(
    isDark = this.isDark,
)

/**
 * 在原生色板上只替换强调色阶，非强调色角色与色板的 typed 明暗保持不变。
 *
 * @receiver 本层显式中立主题。
 * @return 色阶由 [NomiTheme.colorScheme] 主色重建的 Fluent 主题规格。
 */
fun NomiTheme.toPrimaryFluentTheme(): FluentTheme = FluentTheme(
    isDark = this.isDark,
    colors = nativeFluentColors(isDark).withPrimary(colorScheme.primary),
)

/**
 * 强调色、八角色排版与三档圆角都取中立主题。
 *
 * @receiver 本层显式中立主题。
 * @return 颜色、排版与圆角都取中立值的 Fluent 主题规格。
 */
fun NomiTheme.toFullFluentTheme(): FluentTheme = FluentTheme(
    isDark = this.isDark,
    colors = nativeFluentColors(isDark).withPrimary(colorScheme.primary),
    typography = this.typography.toFluentTypography(),
    cornerRadius = this.shapes.toFluentCornerRadius(),
)

/**
 * 按已解析明暗模式取一份 Fluent 原生色板。
 *
 * @param isDark 是否使用深色原生色板；系统解析由应用层在主题边界完成。
 * @return 新的原生浅色或深色色板，不包含调用方的 typed 覆盖。
 */
internal fun nativeFluentColors(isDark: Boolean): Colors {
    return if (isDark) darkColors() else lightColors()
}

/**
 * 只替换色板的强调色阶，保留接收者自带的 typed 明暗。
 *
 * Fluent 没有完整色板入口，因此强调色以外的角色由原生色板按明暗模式决定，本函数不重写它们。
 *
 * @receiver 提供明暗模式的当前原生色板；接收者本身不修改。
 * @param primary 新的强调底色；即使为 [Color.Unspecified] 也原样用作底色，不替换为默认色。
 * @return 与原色板同明暗模式、强调色阶由 primary 重新生成的新色板。
 */
internal fun Colors.withPrimary(primary: Color): Colors = Colors(
    shades = primary.toFluentShades(),
    darkMode = darkMode,
)

/**
 * 为明确主色生成基础色及三阶浅色、三阶深色。
 *
 * 上游 `generateShades` 只识别预置强调色，其它颜色会回退到默认蓝色，因此这里按固定比例自行插值。
 *
 * @receiver 调用方明确指定的主色；应由调用方排除 Unspecified。
 * @return 保留接收者为 base、向白色和黑色插值的新色阶。
 */
private fun Color.toFluentShades(): Shades = Shades(
    base = this,
    light1 = lerp(this, Color.White, Light1Mix),
    light2 = lerp(this, Color.White, Light2Mix),
    light3 = lerp(this, Color.White, Light3Mix),
    dark1 = lerp(this, Color.Black, Dark1Mix),
    dark2 = lerp(this, Color.Black, Dark2Mix),
    dark3 = lerp(this, Color.Black, Dark3Mix),
)

/**
 * 强调主色的一级浅色混合比例。
 */
private const val Light1Mix = 0.18f

/**
 * 强调主色的二级浅色混合比例。
 */
private const val Light2Mix = 0.42f

/**
 * 强调主色的三级浅色混合比例。
 */
private const val Light3Mix = 0.62f

/**
 * 强调主色的一级深色混合比例。
 */
private const val Dark1Mix = 0.16f

/**
 * 强调主色的二级深色混合比例。
 */
private const val Dark2Mix = 0.42f

/**
 * 强调主色的三级深色混合比例。
 */
private const val Dark3Mix = 0.68f

/**
 * 明确传入的十五角色排版映射到 Fluent 的八个公开角色。
 * 未映射的中立角色保留在入口传入的 `NomiTheme`，供调用方显式使用。
 *
 * @receiver 调用方明确提供的中立排版，不应用于零参数主题的默认路径。
 * @return 新原生排版；caption/body/bodyStrong/bodyLarge/subtitle/title/titleLarge/display 分别取
 * bodySmall/bodyMedium/labelLarge/bodyLarge/titleLarge/headlineMedium/headlineLarge/displayLarge。
 */
internal fun NomiTypography.toFluentTypography(): Typography = Typography(
    caption = bodySmall,
    body = bodyMedium,
    bodyStrong = labelLarge,
    bodyLarge = bodyLarge,
    subtitle = titleLarge,
    title = headlineMedium,
    titleLarge = headlineLarge,
    display = displayLarge,
)

/**
 * 把九级中立圆角映射为三档原生圆角。
 *
 * 选取三个代表档位：tiny → intersectionEdge、small → control、large → overlay。
 * 其余档位不参与原生映射，原生也没有对应入口。
 *
 * @receiver 调用方明确提供的中立形状组；每个形状取起始侧上角半径作为代表。
 * @return 三档原生圆角的新实例。
 */
internal fun NomiShapes.toFluentCornerRadius(): CornerRadius = CornerRadius(
    overlay = large.topStart.toCornerDp(),
    control = small.topStart.toCornerDp(),
    intersectionEdge = tiny.topStart.toCornerDp(),
)

/**
 * 一倍密度，用于把 Dp 角半径直接读回数值。
 */
private val UnitDensity = Density(density = 1f, fontScale = 1f)

/**
 * 把单个角尺寸换算为 Dp。
 *
 * 以一倍密度和零尺寸求值：Dp 角尺寸因此得到原值，百分比角尺寸没有固定 Dp，按 0 处理。
 *
 * @receiver 中立形状的单个角尺寸。
 * @return 与该角尺寸等值的 Dp；百分比角尺寸返回 0.dp。
 */
private fun CornerSize.toCornerDp(): Dp = toPx(Size.Zero, UnitDensity).dp
