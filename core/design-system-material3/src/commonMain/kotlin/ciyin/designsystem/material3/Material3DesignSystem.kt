package ciyin.designsystem.material3

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.NomiTheme


/**
 * 使用原生默认映射的共享 Material 3 设计系统。
 *
 * @return 稳定单例，避免无参调用在每次组合中更换 [LocalNomiDesignSystem]。
 */
val NomiDesignSystem.Companion.Material3: NomiDesignSystem get() = nativeMaterial3System

/**
 * 返回按 [map] 把中立主题映射为 Material 3 原生主题的设计系统。
 *
 * 默认 [toNativeMaterial3Theme] 返回与 `NomiDesignSystem.Material3` 属性相同的共享实例；其他映射每次新建，
 * 需要稳定身份时由调用方在组合外保存或使用 remember。
 *
 * @param map 每次安装主题时执行的纯映射函数，决定原生色板、排版、形状与明暗模式。
 * @return 默认映射的共享实例，或使用该映射函数的新设计系统实例，标识为 material3。
 */
fun NomiDesignSystem.Companion.Material3(
    map: NomiTheme.() -> Material3Theme = nativeMaterial3Map
): NomiDesignSystem {
    return if (map === nativeMaterial3Map) nativeMaterial3System else Material3System(map)
}

/**
 * 原生默认映射；属性与带默认参数的函数共用同一实例。
 */
private val nativeMaterial3Map: NomiTheme.() -> Material3Theme = { toNativeMaterial3Theme() }

/**
 * [toNativeMaterial3Theme] 的共享实现；属性与带默认参数的函数共用，避免逐次更换身份。
 */
private val nativeMaterial3System: NomiDesignSystem = Material3System(nativeMaterial3Map)

/**
 * 按映射函数安装 Material 3 主题的设计系统实现。
 *
 * 实现本身无状态，映射结果只影响本次 [ProvideTheme] 建立的子树。
 *
 * @property map 每次安装主题时执行的纯映射函数。
 */
private class Material3System(
    private val map: NomiTheme.() -> Material3Theme,
) : NomiDesignSystem {
    /**
     * 稳定标识，所有映射变体共用 material3。
     */
    override val id: NomiDesignSystemId = NomiDesignSystemId("material3")

    /**
     * Material 3 十族组件实现。
     */
    override val components: NomiDesignSystemComponents = Material3Components

    /**
     * 安装映射得到的原生主题，只建立原生内容环境，不发布或投影中立主题。
     *
     * 色板为 null 时按已解析明暗取原生默认；排版与形状为 null 时继承当前 MaterialTheme。
     * 中立主题由主题入口原样发布，组件视觉直接读取原生主题。显式内容 slot 颜色优先于原生内容色，
     * 未指定时不重造原生默认值。
     *
     * @param theme 本层入口提供的中立主题，仅作为映射输入。
     * @param content 在实际 Material 3 主题环境中组合的子树。
     */
    @Composable
    override fun ProvideTheme(theme: NomiTheme, content: @Composable () -> Unit) {
        val spec = remember(theme, map) { map(theme) }
        MaterialTheme(
            colorScheme = spec.colorScheme ?: nativeMaterial3ColorScheme(spec.isDark),
            typography = spec.typography ?: MaterialTheme.typography,
            shapes = spec.shapes ?: MaterialTheme.shapes,
        ) {
            val explicitContentColor = LocalNomiContentColor.current
            val contentColor = if (explicitContentColor == Color.Unspecified) {
                LocalContentColor.current
            } else {
                explicitContentColor
            }
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalTextStyle provides LocalTextStyle.current.merge(LocalNomiTextStyle.current),
            ) {
                content()
            }
        }
    }
}
