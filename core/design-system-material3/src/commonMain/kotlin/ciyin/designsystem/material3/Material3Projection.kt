package ciyin.designsystem.material3

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import ciyin.designsystem.runtime.NomiThemeRequest
import ciyin.designsystem.runtime.ResolvedNomiTheme
import ciyin.designsystem.theme.NomiSizes
import ciyin.designsystem.theme.Spacings
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes

/**
 * 把实际 Material 3 环境解析为运行时五组主题结果。
 *
 * 原生存在对应角色时直接映射原生值；warning、success、info、未映射颜色角色以及
 * 九级形状中的非标准等级使用各自的中立单组默认值，不创建聚合默认令牌。请求元数据
 * 原样写入结果，使核心主题能够继续掌握输入来源和覆盖范围。
 *
 * @param request 已解析的 Nomi 主题请求，提供模式、实际明暗状态和显式覆盖来源。
 * @param colorScheme 当前 MaterialTheme 使用的完整原生色板。
 * @param typography 当前 MaterialTheme 使用的完整原生排版。
 * @param shapes 当前 MaterialTheme 使用的原生形状；标准角色必须是 RoundedCornerShape。
 * @param spacings 当前主题使用的中立间距组。
 * @param sizes 当前主题使用的中立尺寸组。
 * @return 包含原生映射、单组默认值和请求元数据的运行时主题结果。
 */
internal fun material3ResolvedTheme(
    request: NomiThemeRequest,
    colorScheme: ColorScheme,
    typography: Typography,
    shapes: Shapes,
    spacings: Spacings,
    sizes: NomiSizes,
): ResolvedNomiTheme {
    val defaultColors =
        if (request.isDark) defaultDarkNomiColorScheme() else defaultLightNomiColorScheme()
    val defaultShapes = defaultNomiShapes()
    return ResolvedNomiTheme(
        requestedMode = request.requestedMode,
        isDark = request.isDark,
        colorScheme = defaultColors.copy(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            secondary = colorScheme.secondary,
            onSecondary = colorScheme.onSecondary,
            background = colorScheme.background,
            onBackground = colorScheme.onBackground,
            surface = colorScheme.surface,
            surfaceHigh = colorScheme.surfaceContainerHigh,
            surfaceHigher = colorScheme.surfaceContainerHighest,
            surfaceLow = colorScheme.surfaceContainerLow,
            surfaceLower = colorScheme.surfaceContainerLowest,
            onSurface = colorScheme.onSurface,
            error = colorScheme.error,
            onError = colorScheme.onError,
            textPrimary = colorScheme.onSurface,
            textSecondary = colorScheme.onSurfaceVariant,
            textHint = colorScheme.onSurfaceVariant,
            textCaption = colorScheme.onSurfaceVariant,
            textDisabled = colorScheme.onSurface.copy(alpha = 0.38f),
            outline = colorScheme.outline,
            divider = colorScheme.outlineVariant,
        ),
        typography = typography.toNomiTypography(),
        shapes = defaultShapes.copy(
            extraSmall = shapes.extraSmall as RoundedCornerShape,
            small = shapes.small as RoundedCornerShape,
            medium = shapes.medium as RoundedCornerShape,
            large = shapes.large as RoundedCornerShape,
            extraLarge = shapes.extraLarge as RoundedCornerShape,
        ),
        spacings = spacings,
        sizes = sizes,
        overrides = request.overrides,
    )
}
