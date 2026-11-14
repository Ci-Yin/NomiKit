package ciyin.designsystem.fluent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.component.NomiComponentCapabilities
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

/**
 * 记忆显式 Fluent 配置；未传排版与圆角从主题分支的原生基线解析，不复制上游常量。
 *
 * @param colors 完整原生色板；null 按请求明暗取默认。显式 accent 会重新构造色板，但沿用该对象的 darkMode。
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
 * 跨平台原生 Fluent 实现的唯一实例，未指定视觉值不套用 Nomi 默认。
 */
internal object FluentSystem : NomiDesignSystem by ConfiguredFluentDesignSystem()

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
 * 当前组合分支的 Fluent 原生基线；默认 null 表示尚未进入 Fluent 边界。
 *
 * 后代复用该基线，退出 provider 后恢复父值，不在进程级缓存平台排版。
 */
private val LocalFluentNativeBaseline = staticCompositionLocalOf<FluentNativeBaseline?> { null }

/**
 * 不持有解析后的中立主题，仅保存调用方显式原生配置。
 *
 * @property colors 完整原生色板；null 使用请求模式的原生默认。指定 accent 时仅保留它的 darkMode 并重建色板。
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
     * 原生按钮不支持 size/shape/contentPadding，静态卡片不支持 colors/border，原生文本不支持 autoSize；
     * 原生 NavigationView 提供顶部、侧边与紧凑侧边三种显示模式，因此声明支持抽屉式导航布局；
     * 原生导航项没有项级修饰符入口，因此声明不支持导航项 modifier；其余公开能力沿用默认声明。
     */
    override val capabilities = NomiComponentCapabilities(
        buttonSize = false,
        buttonShape = false,
        buttonContentPadding = false,
        cardColors = false,
        cardBorder = false,
        textAutoSize = false,
        navigationDrawer = true,
        navigationItemModifier = false,
    )

    /**
     * 安装 Fluent 原生主题，再投影只读令牌；不将父系统的解析默认值当成显式输入。
     *
     * 基线优先继承当前分支；无基线时按上游排版和圆角记忆，避免 A-B-A 嵌套重新采样已覆盖值。
     * accent 为 Unspecified 时保留 typed 色板或原生默认；否则按 typed darkMode（无配置才用请求模式）
     * 重建强调色色板。中立排版优先于 typed 排版和基线；间距与尺寸只影响投影。
     *
     * @param request 已解析明暗模式及显式覆盖；完整中立色板与九级形状不受支持。
     * @param content 在 compactMode 为 true、useAcrylicPopup 为 false 的原生环境中接收五组解析结果与请求来源的内容。
     * @throws IllegalArgumentException request 包含完整 colorScheme 或 shapes 时抛出，应改用 typed 配置。
     */
    @OptIn(ExperimentalFluentApi::class)
    @Composable
    override fun ProvideTheme(
        request: NomiThemeRequest,
        content: @Composable (ResolvedNomiTheme) -> Unit
    ) {
        require(request.overrides.colorScheme == null) {
            "fluent NomiTheme 不支持完整 colorScheme；请使用 accentColor 或 rememberFluentDesignSystem(colors = ...)"
        }
        require(request.overrides.shapes == null) {
            "fluent NomiTheme 不支持九级 shapes；请使用 rememberFluentDesignSystem(cornerRadius = ...)"
        }
        val inheritedBaseline = LocalFluentNativeBaseline.current
        val upstreamTypography = FluentTheme.typography
        val upstreamCorners = FluentTheme.cornerRadius
        val baseline = inheritedBaseline ?: remember(upstreamTypography, upstreamCorners) {
            FluentNativeBaseline(typography = upstreamTypography, cornerRadius = upstreamCorners)
        }
        val nativeColors = remember(request.isDark, request.overrides.accentColor, colors) {
            if (request.overrides.accentColor != Color.Unspecified) {
                // 完整原生色板优先决定其明暗；只改强调色不能连带切换背景、文本或描边。
                request.overrides.toFluentColors(colors?.darkMode ?: request.isDark)
            } else {
                colors ?: request.overrides.toFluentColors(request.isDark)
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

/**
 * 跨平台原生 Fluent 设计系统，绑定在 [NomiDesignSystem.Companion] 上。
 *
 * 通过 `NomiDesignSystem.Fluent` 访问；实现对象不对外暴露，调用方只依赖核心契约。
 */
val NomiDesignSystem.Companion.Fluent: NomiDesignSystem get() = FluentSystem
