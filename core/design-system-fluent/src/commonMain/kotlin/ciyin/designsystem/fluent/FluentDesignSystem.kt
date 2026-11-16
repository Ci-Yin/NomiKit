package ciyin.designsystem.fluent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.runtime.NomiThemeRequest
import ciyin.designsystem.runtime.ResolvedNomiTheme
import io.github.composefluent.Colors
import io.github.composefluent.CornerRadius
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.Typography
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors

/**
 * 跨平台原生 Fluent 设计系统，绑定在 [NomiDesignSystem.Companion] 上。
 *
 * 通过 `NomiDesignSystem.Fluent` 访问；实现对象不对外暴露，调用方只依赖核心契约。
 */
val NomiDesignSystem.Companion.Fluent: NomiDesignSystem get() = FluentSystem

/**
 * 跨平台原生 Fluent 实现的唯一实例，未指定视觉值不套用 Nomi 默认。
 */
private object FluentSystem : NomiDesignSystem by ConfiguredFluentDesignSystem()

/**
 * 当前组合分支的 Fluent 原生基线；默认 null 表示尚未进入 Fluent 边界。
 *
 * 后代复用该基线，退出 provider 后恢复父值，不在进程级缓存平台排版。
 */
private val LocalFluentNativeBaseline = staticCompositionLocalOf<FluentNativeBaseline?> { null }

/**
 * 记忆显式 Fluent 配置；未传排版与圆角从主题分支的原生基线解析，不复制上游常量。
 *
 * @param colors 完整原生色板；null 按请求明暗取默认。请求中的完整中立色板只取 primary 重新构造色板，但沿用该对象的 darkMode。
 * @param typography 完整八角色原生排版；null 使用当前分支捕获的上游基线，请求中的中立排版优先。
 * @param cornerRadius 完整原生圆角；null 使用当前分支捕获的上游基线。
 * @return 这些配置值不变时复用的设计系统；调用本工厂本身不安装主题或捕获基线。
 */
@Composable
fun rememberFluentDesignSystem(
    colors: Colors? = null,
    typography: Typography? = null,
    cornerRadius: CornerRadius? = null,
): NomiDesignSystem = remember(colors, typography, cornerRadius) {
    ConfiguredFluentDesignSystem(
        colors = colors,
        typography = typography,
        cornerRadius = cornerRadius
    )
}

/**
 * 保存施加显式覆盖前的原生环境，局部 Fluent-A / 其他系统-B / Fluent-A 切换复用同一基线。
 *
 * @property typography 首个 Fluent 边界读取的上游完整八角色排版。
 * @property cornerRadius 首个 Fluent 边界读取的上游完整圆角角色。
 */
@Immutable
private data class FluentNativeBaseline(
    val typography: Typography,
    val cornerRadius: CornerRadius,
)

/**
 * 不持有解析后的中立主题，仅保存调用方显式原生配置。
 *
 * @property colors 完整原生色板；null 使用请求模式的原生默认。请求提供完整中立色板时仅保留它的 darkMode 并重建色板。
 * @property typography 显式原生排版；null 使用捕获的上游基线，显式中立排版优先于此配置。
 * @property cornerRadius 显式原生圆角；null 使用捕获的上游基线。
 */
private class ConfiguredFluentDesignSystem(
    private val colors: Colors? = null,
    private val typography: Typography? = null,
    private val cornerRadius: CornerRadius? = null,
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
     * 安装 Fluent 原生主题，再投影只读令牌；不将父系统的解析默认值当成显式输入。
     *
     * 基线优先继承当前分支；无基线时按上游排版和圆角记忆，避免 A-B-A 嵌套重新采样已覆盖值。
     * 请求未提供完整中立色板时保留 typed 色板或原生默认；提供时只取 primary 重建强调色色板，
     * 明暗优先用 typed darkMode（无配置才用请求模式），其余字段忽略并记录一次警告。
     * 九级形状忽略并保留原生圆角，另记一次警告。中立排版优先于 typed 排版和基线；间距与尺寸只影响投影。
     *
     * @param request 已解析明暗模式及显式覆盖。
     * @param content 在 compactMode 为 true、useAcrylicPopup 为 false 的原生环境中接收五组解析结果与请求来源的内容。
     */
    @OptIn(ExperimentalFluentApi::class)
    @Composable
    override fun ProvideTheme(
        request: NomiThemeRequest,
        content: @Composable (ResolvedNomiTheme) -> Unit
    ) {
        val neutralColorScheme = request.overrides.colorScheme
        val neutralShapes = request.overrides.shapes
        rememberFluentWarning(neutralColorScheme != null, neutralColorScheme) {
            "NomiTheme 的完整 colorScheme 无 Fluent 原生入口，只取 primary 作为强调色，其余字段忽略"
        }
        rememberFluentWarning(neutralShapes != null, neutralShapes) {
            "NomiTheme 的九级 shapes 无 Fluent 原生入口，已忽略并保留原生圆角"
        }
        val inheritedBaseline = LocalFluentNativeBaseline.current
        val upstreamTypography = FluentTheme.typography
        val upstreamCorners = FluentTheme.cornerRadius
        val baseline = inheritedBaseline ?: remember(upstreamTypography, upstreamCorners) {
            FluentNativeBaseline(typography = upstreamTypography, cornerRadius = upstreamCorners)
        }
        val nativeColors = remember(request.isDark, neutralColorScheme, colors) {
            if (neutralColorScheme != null) {
                // 完整原生色板优先决定其明暗；只改强调色不能连带切换背景、文本或描边。
                neutralColorScheme.toFluentColors(isDark = colors?.darkMode ?: request.isDark)
            } else {
                colors ?: if (request.isDark) darkColors() else lightColors()
            }
        }
        val nativeTypography =
            request.overrides.typography?.toFluentTypography() ?: typography ?: baseline.typography
        val nativeCorners = cornerRadius ?: baseline.cornerRadius
        CompositionLocalProvider(LocalFluentNativeBaseline provides baseline) {
            FluentTheme(
                colors = nativeColors,
                typography = nativeTypography,
                cornerRadius = nativeCorners,
                useAcrylicPopup = false,
                compactMode = true,
            ) {
                content(
                    resolveFluentTheme(
                        request = request,
                        colors = nativeColors,
                        typography = nativeTypography,
                        cornerRadius = nativeCorners,
                    )
                )
            }
        }
    }
}
