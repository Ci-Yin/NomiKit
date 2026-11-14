package ciyin.material.theme

import androidx.compose.material3.Typography
import ciyin.designsystem.material3.toMaterial3Typography
import ciyin.designsystem.theme.defaultNomiTypography

/**
 * 旧主题读取 Material 排版时使用的缺省基线。
 *
 * 初始化时将中立默认排版转换为 [Typography] 并复用；不随组合或当前主题重新计算。
 * 非 Material 环境将当前中立角色叠加到此基线，不在旧包重复维护排版数值。
 */
internal val DefaultLegacyTypography: Typography = defaultNomiTypography().toMaterial3Typography()
