@file:OptIn(io.github.composefluent.ExperimentalFluentApi::class)

package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiTypography
import io.github.composefluent.FluentTheme as NativeFluentTheme
import io.github.composefluent.Typography as FluentTypography

/**
 * 展示页读取的原生排版角色。
 */
internal enum class ShowcaseTextRole {
    /**
     * 超大标题一。
     */
    DisplayLarge,

    /**
     * 超大标题二。
     */
    DisplayMedium,

    /**
     * 超大标题三。
     */
    DisplaySmall,

    /**
     * 大标题一。
     */
    HeadlineLarge,

    /**
     * 大标题二。
     */
    HeadlineMedium,

    /**
     * 大标题三。
     */
    HeadlineSmall,

    /**
     * 标题一。
     */
    TitleLarge,

    /**
     * 标题二。
     */
    TitleMedium,

    /**
     * 标题三。
     */
    TitleSmall,

    /**
     * 正文一。
     */
    BodyLarge,

    /**
     * 正文二。
     */
    BodyMedium,

    /**
     * 正文三。
     */
    BodySmall,

    /**
     * 标签一。
     */
    LabelLarge,

    /**
     * 标签二。
     */
    LabelMedium,

    /**
     * 标签三。
     */
    LabelSmall,
}

/**
 * 展示页读取的原生表面层级。
 */
internal enum class ShowcaseSurfaceLevel {
    /**
     * 最低表面。
     */
    Lower,

    /**
     * 较低表面。
     */
    Low,

    /**
     * 默认表面。
     */
    Default,

    /**
     * 较高表面。
     */
    High,

    /**
     * 最高表面。
     */
    Higher,
}

/**
 * Material 3 的稳定标识，用于匹配当前设计系统。
 */
private val Material3Id = NomiDesignSystem.Material3.id

/**
 * Fluent 的稳定标识，用于匹配当前设计系统。
 */
private val FluentId = NomiDesignSystem.Fluent.id

/**
 * 当前设计系统标识；用于选择原生 token 来源。
 *
 * @return 当前提供者标识；未提供时为 null。
 */
@Composable
@ReadOnlyComposable
private fun currentSystemId(): NomiDesignSystemId? = LocalNomiDesignSystem.current?.id

/**
 * 读取当前设计系统的原生排版角色。
 *
 * @param role 展示所需的语义角色。
 * @return 当前系统原生排版中的对应 TextStyle。
 */
@Composable
@ReadOnlyComposable
internal fun nativeTextStyle(role: ShowcaseTextRole): TextStyle = when (currentSystemId()) {
    Material3Id -> MaterialTheme.typography.textStyle(role)
    FluentId -> NativeFluentTheme.typography.textStyle(role)
    else -> NomiTheme.typography.textStyle(role)
}

/**
 * 读取当前设计系统的原生表面颜色。
 *
 * @param level 五级表面层级。
 * @return 当前原生主题中对应的表面颜色。
 */
@Composable
@ReadOnlyComposable
internal fun nativeSurfaceColor(level: ShowcaseSurfaceLevel): Color = when (currentSystemId()) {
    Material3Id -> with(MaterialTheme.colorScheme) {
        when (level) {
            ShowcaseSurfaceLevel.Lower -> surfaceContainerLowest
            ShowcaseSurfaceLevel.Low -> surfaceContainerLow
            ShowcaseSurfaceLevel.Default -> surface
            ShowcaseSurfaceLevel.High -> surfaceContainerHigh
            ShowcaseSurfaceLevel.Higher -> surfaceContainerHighest
        }
    }

    FluentId -> with(NativeFluentTheme.colors) {
        when (level) {
            ShowcaseSurfaceLevel.Lower -> background.solid.base
            ShowcaseSurfaceLevel.Low -> background.card.secondary
            ShowcaseSurfaceLevel.Default -> background.layer.default
            ShowcaseSurfaceLevel.High -> background.layer.alt
            ShowcaseSurfaceLevel.Higher -> background.card.default
        }
    }

    else -> with(NomiTheme.colorScheme) {
        when (level) {
            ShowcaseSurfaceLevel.Lower -> surfaceLower
            ShowcaseSurfaceLevel.Low -> surfaceLow
            ShowcaseSurfaceLevel.Default -> surface
            ShowcaseSurfaceLevel.High -> surfaceHigh
            ShowcaseSurfaceLevel.Higher -> surfaceHigher
        }
    }
}

/**
 * 当前原生主题的主色。
 *
 * @return Material3 / Fluent / Nomi 各自的主色。
 */
@Composable
@ReadOnlyComposable
internal fun nativePrimaryColor(): Color = when (currentSystemId()) {
    Material3Id -> MaterialTheme.colorScheme.primary
    FluentId -> NativeFluentTheme.colors.shades.base
    else -> NomiTheme.colorScheme.primary
}

/**
 * 当前原生主题的次要文本色。
 *
 * @return Material3 / Fluent / Nomi 各自的次要文本色。
 */
@Composable
@ReadOnlyComposable
internal fun nativeSecondaryTextColor(): Color = when (currentSystemId()) {
    Material3Id -> MaterialTheme.colorScheme.onSurfaceVariant
    FluentId -> NativeFluentTheme.colors.text.text.secondary
    else -> NomiTheme.colorScheme.textSecondary
}

/**
 * 中立十五角色到原生 TextStyle 的读取。
 *
 * @receiver 当前系统或输入的中立排版。
 * @param role 语义角色。
 * @return 对应角色样式。
 */
private fun NomiTypography.textStyle(role: ShowcaseTextRole): TextStyle = when (role) {
    ShowcaseTextRole.DisplayLarge -> displayLarge
    ShowcaseTextRole.DisplayMedium -> displayMedium
    ShowcaseTextRole.DisplaySmall -> displaySmall
    ShowcaseTextRole.HeadlineLarge -> headlineLarge
    ShowcaseTextRole.HeadlineMedium -> headlineMedium
    ShowcaseTextRole.HeadlineSmall -> headlineSmall
    ShowcaseTextRole.TitleLarge -> titleLarge
    ShowcaseTextRole.TitleMedium -> titleMedium
    ShowcaseTextRole.TitleSmall -> titleSmall
    ShowcaseTextRole.BodyLarge -> bodyLarge
    ShowcaseTextRole.BodyMedium -> bodyMedium
    ShowcaseTextRole.BodySmall -> bodySmall
    ShowcaseTextRole.LabelLarge -> labelLarge
    ShowcaseTextRole.LabelMedium -> labelMedium
    ShowcaseTextRole.LabelSmall -> labelSmall
}

/**
 * Material 3 十五角色读取。
 *
 * @receiver 当前 Material 原生排版。
 * @param role 语义角色。
 * @return 对应角色样式。
 */
private fun Typography.textStyle(role: ShowcaseTextRole): TextStyle = when (role) {
    ShowcaseTextRole.DisplayLarge -> displayLarge
    ShowcaseTextRole.DisplayMedium -> displayMedium
    ShowcaseTextRole.DisplaySmall -> displaySmall
    ShowcaseTextRole.HeadlineLarge -> headlineLarge
    ShowcaseTextRole.HeadlineMedium -> headlineMedium
    ShowcaseTextRole.HeadlineSmall -> headlineSmall
    ShowcaseTextRole.TitleLarge -> titleLarge
    ShowcaseTextRole.TitleMedium -> titleMedium
    ShowcaseTextRole.TitleSmall -> titleSmall
    ShowcaseTextRole.BodyLarge -> bodyLarge
    ShowcaseTextRole.BodyMedium -> bodyMedium
    ShowcaseTextRole.BodySmall -> bodySmall
    ShowcaseTextRole.LabelLarge -> labelLarge
    ShowcaseTextRole.LabelMedium -> labelMedium
    ShowcaseTextRole.LabelSmall -> labelSmall
}

/**
 * Fluent 八角色到十五语义角色的近似映射。
 *
 * @receiver 当前 Fluent 原生排版。
 * @param role 语义角色。
 * @return 最接近的原生角色样式。
 */
private fun FluentTypography.textStyle(role: ShowcaseTextRole): TextStyle = when (role) {
    ShowcaseTextRole.DisplayLarge,
    ShowcaseTextRole.DisplayMedium,
    ShowcaseTextRole.DisplaySmall -> display

    ShowcaseTextRole.HeadlineLarge -> titleLarge
    ShowcaseTextRole.HeadlineMedium -> title
    ShowcaseTextRole.HeadlineSmall,
    ShowcaseTextRole.TitleLarge -> subtitle

    ShowcaseTextRole.TitleMedium,
    ShowcaseTextRole.TitleSmall,
    ShowcaseTextRole.LabelLarge -> bodyStrong

    ShowcaseTextRole.BodyLarge -> bodyLarge
    ShowcaseTextRole.BodyMedium,
    ShowcaseTextRole.LabelMedium -> body

    ShowcaseTextRole.BodySmall,
    ShowcaseTextRole.LabelSmall -> caption
}