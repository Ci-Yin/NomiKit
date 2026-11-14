package com.ciyin.app.presentation.screen.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.nomi.Default

/**
 * 全平台样例默认候选，按 Nomi、Material 3、Fluent 排列；首项为默认选择和重置目标，宿主可覆盖完整目录。
 */
internal val DefaultSampleDesignSystems: List<NomiDesignSystem> = listOf(
    NomiDesignSystem.Default,
    NomiDesignSystem.Material3,
    NomiDesignSystem.Fluent,
)

/**
 * 当前组合分支允许展示的设计系统目录；未提供时使用 [DefaultSampleDesignSystems]，嵌套 provider 仅影响其子树。
 */
internal val LocalSampleDesignSystems = staticCompositionLocalOf { DefaultSampleDesignSystems }

/**
 * 提供样例展示页的完整候选目录，不改变应用根主题。
 *
 * 默认候选是 Nomi、Material 3 和 Fluent，在共享源码中统一提供。
 * 宿主如需定制，应在样例导航栈上方提供稳定、非空且标识唯一的完整列表。
 * 本函数只注入依赖，不独立渲染 UI，因此不需要独立预览。
 *
 * @param systems 稳定、非空且标识唯一的完整候选列表，默认使用共享目录；首项为展示页默认选择和重置目标。
 * @param content 在候选列表快照的 CompositionLocal 环境中组合的样例内容，无接收者作用域。
 * @throws IllegalArgumentException 候选列表为空或标识重复时抛出。
 */
@Composable
public fun ProvideSampleDesignSystems(
    systems: List<NomiDesignSystem> = DefaultSampleDesignSystems,
    content: @Composable () -> Unit,
) {
    require(systems.isNotEmpty()) { "样例设计系统目录不能为空" }
    require(systems.map { it.id }.distinct().size == systems.size) { "样例设计系统标识必须唯一" }
    CompositionLocalProvider(LocalSampleDesignSystems provides systems.toList(), content = content)
}
