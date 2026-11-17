package ciyin.designsystem.fluent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme as NativeFluentTheme

/**
 * 使用原生默认映射的共享 Fluent 设计系统。
 *
 * @return 稳定单例，避免无参调用在每次组合中更换 [LocalNomiDesignSystem]。
 */
val NomiDesignSystem.Companion.Fluent: NomiDesignSystem get() = nativeFluentSystem

/**
 * 取按 [map] 安装原生主题的 Fluent 设计系统。
 *
 * 工厂不是 Composable：同一个 [map] 可以在任意作用域复用，实例不持有组合状态。
 * [map] 应是只依据传入主题决定原生组的纯函数；实现按当前主题与映射函数记忆求值结果，
 * 避免逐次重建原生色板。映射中的 null 组继承当前原生 Fluent 主题，不由本工厂补默认值。
 *
 * 默认 [toNativeFluentTheme] 返回与 `NomiDesignSystem.Fluent` 属性相同的共享实例；其他映射每次新建。
 *
 * @receiver 设计系统伴生对象，仅用于把工厂挂到核心契约上，不修改核心类型。
 * @param map 把中立主题翻译为原生主题规格的映射函数；建议提升为稳定引用，避免逐次重建原生色板。
 * @return 默认映射的共享实例，或每次新建的实现实例，行为只由 [map] 决定。
 */
fun NomiDesignSystem.Companion.Fluent(
    map: NomiTheme.() -> FluentTheme = nativeFluentMap
): NomiDesignSystem {
    return if (map === nativeFluentMap) nativeFluentSystem else FluentSystem(map)
}

/**
 * 原生默认映射；属性与带默认参数的函数共用同一实例。
 */
private val nativeFluentMap: NomiTheme.() -> FluentTheme = { toNativeFluentTheme() }

/**
 * [toNativeFluentTheme] 的共享实现；属性与带默认参数的函数共用，避免逐次更换身份。
 */
private val nativeFluentSystem: NomiDesignSystem = FluentSystem(nativeFluentMap)

/**
 * 以映射函数决定原生主题的 Fluent 实现，不持有中立主题结果。
 *
 * @property map 把中立主题翻译为原生主题规格的映射函数；null 组继承当前原生 Fluent 主题。
 */
private class FluentSystem(
    private val map: NomiTheme.() -> FluentTheme,
) : NomiDesignSystem {
    /**
     * Fluent 标识。
     */
    override val id = NomiDesignSystemId("fluent")

    /**
     * 组件族仅组装原生调用。
     */
    override val components: NomiDesignSystemComponents = FluentComponents

    /**
     * 只安装映射得到的原生主题，不发布也不回写中立主题。
     *
     * 映射只对当前 [theme] 求值一次；未指定的颜色按已解析明暗取原生色板，未指定的排版与圆角
     * 继承当前原生 Fluent 主题。acrylic 弹层与紧凑密度直接取规格字段；中立主题由入口发布，
     * 原生组件视觉直接读取此处安装的 Fluent 原生值。
     *
     * @param theme 本层显式提供的中立主题，只作为映射来源。
     * @param content 在原生 Fluent 主题环境中组合的子树。
     */
    @OptIn(ExperimentalFluentApi::class)
    @Composable
    override fun ProvideTheme(theme: NomiTheme, content: @Composable () -> Unit) {
        val spec = remember(theme, map) { map(theme) }
        NativeFluentTheme(
            colors = remember(spec.isDark, spec.colors) {
                spec.colors ?: nativeFluentColors(spec.isDark)
            },
            typography = spec.typography ?: NativeFluentTheme.typography,
            cornerRadius = spec.cornerRadius ?: NativeFluentTheme.cornerRadius,
            useAcrylicPopup = spec.useAcrylicPopup,
            compactMode = spec.compactMode,
            content = content
        )
    }
}
