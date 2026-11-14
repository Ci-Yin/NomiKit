package ciyin.designsystem.fluent

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import ciyin.designsystem.runtime.NomiThemeRequest
import ciyin.designsystem.runtime.ResolvedNomiTheme
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.NomiTypography
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import io.github.composefluent.Colors
import io.github.composefluent.CornerRadius
import io.github.composefluent.Shades
import io.github.composefluent.Typography
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors

/**
 * 只有明确指定的强调色才生成自定义色阶，省略时保留原生蓝色色阶。
 *
 * @receiver 显式主题覆盖；此转换仅消费 accentColor，不映射其余主题组。
 * @param isDark 新色板的明暗模式，调用方已处理 typed 色板模式优先级。
 * @return accent 为 Unspecified 时返回原生默认色板，否则返回以该颜色为基础的新色板；透明色仍为显式输入。
 */
internal fun NomiThemeOverrides.toFluentColors(isDark: Boolean): Colors =
    if (accentColor == Color.Unspecified) {
        if (isDark) darkColors() else lightColors()
    } else {
        Colors(shades = accentColor.toFluentShades(), darkMode = isDark)
    }

/**
 * 从中立色板的 primary 构造 Fluent 强调色色板，不表示完整中立颜色组受到支持。
 *
 * @receiver 只读取 primary 的中立色板，其余颜色字段不会映射。
 * @param isDark 新 Fluent 色板使用的明暗模式。
 * @return 基于 primary 完整色阶和指定明暗模式的新色板。
 */
internal fun NomiColorScheme.toFluentColors(isDark: Boolean): Colors =
    Colors(shades = primary.toFluentShades(), darkMode = isDark)

/**
 * 显式主色映射，绝不用于替代未指定时的原生默认调色板。
 *
 * @receiver 仅提供 primary 的中立色板。
 * @return 以 primary 为 base、向白色与黑色插值的三阶浅色和三阶深色色阶。
 */
internal fun NomiColorScheme.toFluentShades(): Shades = primary.toFluentShades()

/**
 * 为明确主色生成基础色及三阶浅色、三阶深色，避免依赖上游预设色识别。
 *
 * @receiver 调用方明确指定的主色；应由调用方排除 Unspecified。
 * @return 保留接收者为 base、按固定比例向白色和黑色插值的新 Shades。
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
 * 显式主色的一级浅色混合比例。
 */
private const val Light1Mix = 0.18f

/**
 * 显式主色的二级浅色混合比例。
 */
private const val Light2Mix = 0.42f

/**
 * 显式主色的三级浅色混合比例。
 */
private const val Light3Mix = 0.62f

/**
 * 显式主色的一级深色混合比例。
 */
private const val Dark1Mix = 0.16f

/**
 * 显式主色的二级深色混合比例。
 */
private const val Dark2Mix = 0.42f

/**
 * 显式主色的三级深色混合比例。
 */
private const val Dark3Mix = 0.68f

/**
 * 投影当前原生主题。Fluent 仅八角色排版，其余中立角色明确映射为同源别名。
 * 五层表面映射到原生 solid/card/layer，页面间距与尺寸是独立布局扩展，不改变原生控件。
 *
 * @param request 已解析主题请求；显式中立排版原样保留，间距/尺寸优先用请求覆盖。
 * @param colors 当前实际原生色板，决定全部中立语义颜色。
 * @param typography 当前实际八角色原生排版，仅在未指定中立排版时投影。
 * @param cornerRadius 当前实际原生圆角，按 intersectionEdge/control/overlay 映射到九级。
 * @return 直接包含五组结果及请求来源的运行时主题；页面间距和尺寸缺省时调用各组默认工厂，不反向覆盖控件。
 */
internal fun resolveFluentTheme(
    request: NomiThemeRequest,
    colors: Colors,
    typography: Typography,
    cornerRadius: CornerRadius,
): ResolvedNomiTheme {
    return ResolvedNomiTheme(
        requestedMode = request.requestedMode,
        isDark = request.isDark,
        overrides = request.overrides,
        colorScheme = defaultLightNomiColorScheme().copy(
            primary = colors.shades.base,
            onPrimary = colors.text.onAccent.primary,
            secondary = colors.text.accent.primary,
            onSecondary = colors.text.onAccent.primary,
            background = colors.background.solid.base,
            onBackground = colors.text.text.primary,
            surface = colors.background.layer.default,
            surfaceHigh = colors.background.layer.alt,
            surfaceHigher = colors.background.card.default,
            surfaceLow = colors.background.card.secondary,
            surfaceLower = colors.background.solid.base,
            onSurface = colors.text.text.primary,
            error = colors.system.criticalBackground,
            onError = colors.system.critical,
            warning = colors.system.cautionBackground,
            onWarning = colors.system.caution,
            success = colors.system.successBackground,
            onSuccess = colors.system.success,
            info = colors.system.attentionBackground,
            onInfo = colors.system.attention,
            textPrimary = colors.text.text.primary,
            textSecondary = colors.text.text.secondary,
            textHint = colors.text.text.tertiary,
            textCaption = colors.text.text.secondary,
            textDisabled = colors.text.text.disabled,
            outline = colors.stroke.control.default,
            divider = colors.stroke.divider.default,
        ),
        typography = request.overrides.typography ?: defaultNomiTypography().copy(
            displayLarge = typography.display,
            displayMedium = typography.titleLarge,
            displaySmall = typography.title,
            headlineLarge = typography.titleLarge,
            headlineMedium = typography.title,
            headlineSmall = typography.subtitle,
            titleLarge = typography.subtitle,
            titleMedium = typography.bodyStrong,
            titleSmall = typography.body,
            bodyLarge = typography.bodyLarge,
            bodyMedium = typography.body,
            bodySmall = typography.caption,
            labelLarge = typography.bodyStrong,
            labelMedium = typography.body,
            labelSmall = typography.caption,
        ),
        shapes = defaultNomiShapes().copy(
            tiny = RoundedCornerShape(cornerRadius.intersectionEdge),
            extraSmall = RoundedCornerShape(cornerRadius.control),
            small = RoundedCornerShape(cornerRadius.control),
            medium = RoundedCornerShape(cornerRadius.control),
            large = RoundedCornerShape(cornerRadius.overlay),
            extraLarge = RoundedCornerShape(cornerRadius.overlay),
            huge = RoundedCornerShape(cornerRadius.overlay),
            massive = RoundedCornerShape(cornerRadius.overlay),
            colossal = RoundedCornerShape(cornerRadius.overlay),
        ),
        spacings = request.overrides.spacings ?: defaultSpacings(),
        sizes = request.overrides.sizes ?: defaultNomiSizes(),
    )
}

/**
 * 明确传入的十五角色排版映射到 Fluent 的八个公开角色。
 * 未映射的中立角色保留在 NomiTheme 读模型，供调用方显式 NomiText 使用。
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
