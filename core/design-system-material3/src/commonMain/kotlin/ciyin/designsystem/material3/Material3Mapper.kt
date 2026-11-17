package ciyin.designsystem.material3

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiShapes
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiTypography
import ciyin.designsystem.theme.defaultNomiTypography
import androidx.compose.material3.darkColorScheme as materialDarkColorScheme
import androidx.compose.material3.lightColorScheme as materialLightColorScheme

/**
 * 只传递已解析明暗模式；颜色取原生默认，排版与形状继承当前 MaterialTheme。
 *
 * @receiver 本层显式中立主题，仅读取 [NomiTheme.isDark]。
 * @return 只包含明暗模式的 Material 3 主题规格。
 */
fun NomiTheme.toNativeMaterial3Theme(): Material3Theme = Material3Theme(isDark = isDark)

/**
 * 把中立主色写入原生默认色板，onPrimary、tertiary 等其余角色保留原生默认。
 *
 * @receiver 本层显式中立主题。
 * @return 色板只替换 primary 的 Material 3 主题规格。
 */
fun NomiTheme.toPrimaryMaterial3Theme(): Material3Theme = Material3Theme(
    isDark = isDark,
    colorScheme = nativeMaterial3ColorScheme(isDark).copy(primary = colorScheme.primary),
)

/**
 * 整组映射中立颜色、十五个排版角色与五个标准形状等级。
 *
 * 原生存在对应角色的字段参与映射；warning、success、info、文本派生色以及
 * 非标准形状等级等中立扩展不参与映射，原生组保持各自默认值。
 *
 * @receiver 本层显式中立主题。
 * @return 颜色、排版与形状都取中立值的 Material 3 主题规格。
 */
fun NomiTheme.toFullMaterial3Theme(): Material3Theme = Material3Theme(
    isDark = isDark,
    colorScheme = colorScheme.toMaterial3ColorScheme(isDark),
    typography = typography.toMaterial3Typography(),
    shapes = shapes.toMaterial3Shapes(),
)

/**
 * 将中立颜色映射到指定明暗的原生默认色板。
 *
 * @receiver 待映射的完整中立色板；无原生对应的语义色不写入 Material 色板。
 * @param isDark 是否使用深色原生默认基线。
 * @return 覆盖可映射角色的新色板。
 */
fun NomiColorScheme.toMaterial3ColorScheme(isDark: Boolean): ColorScheme {
    return toMaterial3ColorScheme(
        base = if (isDark) materialDarkColorScheme() else materialLightColorScheme(),
    )
}

/**
 * 只覆盖可映射的中立颜色角色，保留 [base] 的 tertiary、primaryContainer 等未映射配置。
 *
 * @receiver 提供 primary、secondary、error、背景、表面层级和 outline 的中立色板。
 * @param base 原生色板基线；surfaceContainer 会随中立 surface 覆盖，其余未映射字段保留。
 * @return 新的原生色板，不修改接收者或 base。
 */
fun NomiColorScheme.toMaterial3ColorScheme(base: ColorScheme): ColorScheme = base.copy(
    primary = primary,
    onPrimary = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    error = error,
    onError = onError,
    background = background,
    onBackground = onBackground,
    surface = surface,
    surfaceContainer = surface,
    surfaceContainerHigh = surfaceHigh,
    surfaceContainerHighest = surfaceHigher,
    surfaceContainerLow = surfaceLow,
    surfaceContainerLowest = surfaceLower,
    onSurface = onSurface,
    outline = outline,
)

/**
 * 将中立圆角映射到 Material 3 五个标准等级。
 *
 * 最大等级 extraLarge 取中立 huge；tiny、massive、colossal 没有原生对应等级，不进入结果。
 *
 * @receiver 中立九级圆角，其中 extraSmall、small、medium、large 和 huge 参与映射。
 * @return 新的五角色原生形状；不修改接收者。
 */
fun NomiShapes.toMaterial3Shapes(): Shapes = Shapes(
    extraSmall = extraSmall,
    small = small,
    medium = medium,
    large = large,
    extraLarge = huge,
)

/**
 * 将全部十五个中立排版角色映射到 Material 3。
 *
 * [base] 的其他角色保持不变，包括 Material 3 内部的强调角色。
 *
 * @receiver 提供十五个标准排版角色的中立排版。
 * @param base 原生排版基线，默认新建 Typography；未映射的原生专属角色保持原值。
 * @return 覆盖十五角色后的新原生排版，不修改 base。
 */
fun NomiTypography.toMaterial3Typography(base: Typography = Typography()): Typography = base.copy(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    displaySmall = displaySmall,
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall,
)

/**
 * 把 Material 3 的十五个公开排版角色转换为中立排版。
 *
 * 只做显式类型转换，不读取原生主题环境也不自动发布结果；旧 material 门面等调用方
 * 需要把原生 Typography 转成中立排版时显式调用。原生专属强调角色不在转换范围内，
 * 未覆盖角色保留 [defaultNomiTypography] 的默认值，不修改接收者。
 *
 * @receiver 提供标准公开角色的完整原生排版。
 * @return 逐角色保留 TextStyle 的新中立排版。
 */
fun Typography.toNomiTypography(): NomiTypography = defaultNomiTypography().copy(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    displaySmall = displaySmall,
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall,
)

/**
 * 返回指定明暗的 Material 3 原生默认色板。
 *
 * 不读取平台设置，System 已由应用层提前解析。
 *
 * @param isDark 是否使用深色默认色板。
 * @return true 为新建的 darkColorScheme()，其余情况为 lightColorScheme()。
 */
internal fun nativeMaterial3ColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) darkColorScheme() else lightColorScheme()
}