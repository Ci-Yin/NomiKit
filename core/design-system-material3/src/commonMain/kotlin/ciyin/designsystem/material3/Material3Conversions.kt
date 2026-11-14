package ciyin.designsystem.material3

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme as materialDarkColorScheme
import androidx.compose.material3.lightColorScheme as materialLightColorScheme
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiShapes
import ciyin.designsystem.theme.NomiTypography
import ciyin.designsystem.theme.defaultNomiTypography

/**
 * 将中立颜色映射到 Material 3，保留旧主题未覆盖角色的 Material 默认值。
 *
 * 深浅色必须由根主题提前解析，转换器不再读取系统设置。
 *
 * @receiver 待映射的完整中立色板；无原生对应的语义色不写入 Material 色板。
 * @param isDark 已解析的明暗模式，用于选择未映射角色的原生默认值。
 * @return 以相应原生默认色板为基线、覆盖可映射角色的新色板。
 */
fun NomiColorScheme.toMaterial3ColorScheme(isDark: Boolean): ColorScheme = toMaterial3ColorScheme(
    base = if (isDark) materialDarkColorScheme() else materialLightColorScheme(),
)

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
 * 将圆角映射到 Material 3；最大标准等级沿用旧主题的 huge，而非 extraLarge。
 *
 * @receiver 中立九级圆角，其中 extraSmall、small、medium、large 和 huge 参与映射。
 * @return 新的五角色原生形状；未映射的中立等级不会进入结果。
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
 * [base] 的其他角色保持不变，包括 Material 3 1.9 内部的强调角色。
 * 旧主题传入完整 Typography 时应保留原实例，而不是先降维再重新构造。
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
 * 将 Material 3 的十五个公开排版角色导入中立模型，不包含其专属强调角色。
 *
 * @receiver 提供标准公开角色的完整原生排版。
 * @return 逐角色保留 TextStyle 的新中立排版；专属原生角色不在结果中。
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
