package ciyin.designsystem.material3

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.component.NomiComponentCapabilities
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.runtime.NomiThemeRequest
import ciyin.designsystem.runtime.ResolvedNomiTheme
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiShapes
import ciyin.designsystem.theme.NomiSizes
import ciyin.designsystem.theme.NomiTypography
import ciyin.designsystem.theme.Spacings
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultSpacings

/**
 * 无配置时使用 Material 3 原生调色板、排版和形状，不反向套用中立默认令牌的唯一实现。
 */
internal object Material3System : NomiDesignSystem {
    /**
     * 原生默认实现的固定基线，不从嵌套 MaterialTheme 反复取值。
     */
    private val baseline = ConfiguredMaterial3DesignSystem()

    /**
     * Material 3 的稳定标识。
     */
    override val id: NomiDesignSystemId = NomiDesignSystemId("material3")

    /**
     * Material 3 的组件集合。
     */
    override val components: NomiDesignSystemComponents = Material3DesignSystemComponents

    /**
     * Material 3 原生支持底部导航、侧边导航与永久抽屉三种布局，其余能力沿用默认。
     */
    override val capabilities: NomiComponentCapabilities =
        NomiComponentCapabilities(navigationDrawer = true)

    /**
     * 建立原生环境后把扁平运行时主题结果交给核心主题。
     *
     * @param request 已解析明暗模式及显式覆盖；未指定组使用固定原生基线。
     * @param content 在 Material 原生主题和显式 slot 环境中接收运行时五组主题结果的内容。
     */
    @Composable
    override fun ProvideTheme(
        request: NomiThemeRequest,
        content: @Composable (ResolvedNomiTheme) -> Unit
    ) {
        baseline.ProvideTheme(request = request, content = content)
    }
}

/**
 * 记忆显式 Material 3 主题配置；未指定项使用原生基线而非祖先的投影。
 *
 * 中立形状读模型仍为 RoundedCornerShape，因此主题级五个标准角色必须为圆角；
 * 组件级 shape 不受此限制，可精确传入任意 Shape。
 *
 * @param colorScheme 完整原生色板；null 时按每次请求的明暗模式选择 Material 默认色板。
 * @param typography 完整原生排版；null 使用新建的 Typography 基线，不采样父主题。
 * @param shapes 完整原生形状；null 使用新建的 Shapes 基线。
 * @param spacings 中立页面间距扩展；null 使用 defaultSpacings，不改写组件内边距。
 * @param sizes 中立页面尺寸扩展；null 使用 defaultNomiSizes，不强制组件尺寸。
 * @return 在这些配置值不变时复用的设计系统实例；请求中的显式主题组仍可覆盖配置。
 * @throws IllegalArgumentException shapes 的任一标准角色不是 RoundedCornerShape 时抛出。
 */
@Composable
fun rememberMaterial3DesignSystem(
    colorScheme: ColorScheme? = null,
    typography: Typography? = null,
    shapes: Shapes? = null,
    spacings: Spacings? = null,
    sizes: NomiSizes? = null,
): NomiDesignSystem = remember(colorScheme, typography, shapes, spacings, sizes) {
    ConfiguredMaterial3DesignSystem(
        colorScheme = colorScheme,
        typography = typography ?: Typography(),
        shapes = shapes ?: Shapes(),
        spacings = spacings ?: defaultSpacings(),
        sizes = sizes ?: defaultNomiSizes(),
    )
}

/**
 * 为旧门面建立 Material 3 配置并保留其五组中立读取结果，不创建聚合主题包装。
 *
 * 原生排版实例直接交给 MaterialTheme，全部 Material 专属角色保持不变；中立排版仅作为
 * 旧 getter 的对应读取组保存。颜色和形状同时保存中立组与其原生映射所需的个别基线。
 *
 * @param colorScheme 旧门面提供的完整中立色板，原生可映射角色由此建立 Material 基线。
 * @param typography 旧调用方提供的完整原生排版，不经中立角色往返替换原生实例。
 * @param shapes 旧门面提供的完整中立形状，标准 Material 角色由其对应等级建立。
 * @param spacings 旧门面提供的中立间距组。
 * @param sizes 旧门面提供的中立尺寸组。
 * @return 按五组参数记忆的旧门面设计系统；后续请求的显式覆盖仍可生效。
 */
@Composable
fun rememberLegacyMaterial3DesignSystem(
    colorScheme: NomiColorScheme,
    typography: Typography,
    shapes: NomiShapes,
    spacings: Spacings,
    sizes: NomiSizes,
): NomiDesignSystem = remember(colorScheme, typography, shapes, spacings, sizes) {
    ConfiguredMaterial3DesignSystem(
        typography = typography,
        shapes = shapes.toMaterial3Shapes(),
        spacings = spacings,
        sizes = sizes,
        legacyColorScheme = colorScheme,
        legacyTypography = typography.toNomiTypography(),
        legacyShapes = shapes,
    )
}

/**
 * 保存原生基线和旧门面所需的个别中立基线字段。
 *
 * @property colorScheme 显式原生色板；null 时按请求明暗选择原生默认。
 * @property typography 完整原生排版基线，保留中立模型未表示的专属角色。
 * @property shapes 完整原生形状基线，五个标准角色必须均为 RoundedCornerShape。
 * @property spacings 中立间距扩展，不作为原生组件默认内边距。
 * @property sizes 中立尺寸扩展，不作为原生组件默认尺寸。
 * @property legacyColorScheme 旧门面完整中立色板；null 时从实际原生主题按角色映射。
 * @property legacyTypography 旧门面中立排版；null 时从实际原生排版按角色映射。
 * @property legacyShapes 旧门面完整中立形状；null 时从实际原生标准形状按角色映射。
 * @throws IllegalArgumentException shapes 的任一标准角色不是 RoundedCornerShape 时抛出。
 */
private class ConfiguredMaterial3DesignSystem(
    private val colorScheme: ColorScheme? = null,
    private val typography: Typography = Typography(),
    private val shapes: Shapes = Shapes(),
    private val spacings: Spacings = defaultSpacings(),
    private val sizes: NomiSizes = defaultNomiSizes(),
    private val legacyColorScheme: NomiColorScheme? = null,
    private val legacyTypography: NomiTypography? = null,
    private val legacyShapes: NomiShapes? = null,
) : NomiDesignSystem {
    init {
        require(
            listOf(
                shapes.extraSmall,
                shapes.small,
                shapes.medium,
                shapes.large,
                shapes.extraLarge
            ).all { it is RoundedCornerShape }) {
            "中立 NomiShapes 读模型只支持 RoundedCornerShape；请使用组件级 shape 传入其他形状。"
        }
    }

    /**
     * 与默认 Material 设计系统共用稳定标识。
     */
    override val id: NomiDesignSystemId = NomiDesignSystemId("material3")

    /**
     * 与默认实现共用组件集合。
     */
    override val components: NomiDesignSystemComponents = Material3DesignSystemComponents

    /**
     * 与默认实现共用能力声明：三种导航布局均由原生组件支持。
     */
    override val capabilities: NomiComponentCapabilities =
        NomiComponentCapabilities(navigationDrawer = true)

    /**
     * 只将明确覆盖作用于原生配置，再生成扁平运行时主题结果。
     *
     * 完整中立组在结果中原样保留；accent 仅替换 primary，Unspecified 不覆盖，透明色仍是覆盖。
     * 原生内容环境只合入显式 slot 增量，不把原生默认发布为中立显式样式。
     *
     * @param request 已解析模式及显式主题组；请求组优先于配置和旧门面基线。
     * @param content 在实际 MaterialTheme 中执行并接收对应运行时主题结果的内容。
     */
    @Composable
    override fun ProvideTheme(
        request: NomiThemeRequest,
        content: @Composable (ResolvedNomiTheme) -> Unit,
    ) {
        val overrides = request.overrides
        val baseColors = legacyColorScheme?.toMaterial3ColorScheme(isDark = request.isDark)
            ?: colorScheme ?: if (request.isDark) darkColorScheme() else lightColorScheme()
        val overriddenColors =
            overrides.colorScheme?.toMaterial3ColorScheme(base = baseColors) ?: baseColors
        val accent = overrides.accentColor
        val actualColors =
            if (accent == Color.Unspecified) overriddenColors else overriddenColors.copy(primary = accent)
        val actualTypography =
            overrides.typography?.toMaterial3Typography(base = typography) ?: typography
        val actualShapes = overrides.shapes?.toMaterial3Shapes() ?: shapes
        val actualSpacings = overrides.spacings ?: spacings
        val actualSizes = overrides.sizes ?: sizes
        MaterialTheme(
            colorScheme = actualColors,
            typography = actualTypography,
            shapes = actualShapes,
        ) {
            val nativeProjection = material3ResolvedTheme(
                request = request,
                colorScheme = MaterialTheme.colorScheme,
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes,
                spacings = actualSpacings,
                sizes = actualSizes,
            )
            val baseProjection = nativeProjection.copy(
                colorScheme = legacyColorScheme ?: nativeProjection.colorScheme,
                typography = legacyTypography ?: nativeProjection.typography,
                shapes = legacyShapes ?: nativeProjection.shapes,
            )
            val explicitColors = overrides.colorScheme ?: baseProjection.colorScheme
            val projection = baseProjection.copy(
                colorScheme = if (accent == Color.Unspecified) explicitColors else explicitColors.copy(
                    primary = accent
                ),
                typography = overrides.typography ?: baseProjection.typography,
                shapes = overrides.shapes ?: baseProjection.shapes,
                spacings = actualSpacings,
                sizes = actualSizes,
                requestedMode = request.requestedMode,
                isDark = request.isDark,
                overrides = request.overrides,
            )
            val explicitSlotColor = LocalNomiContentColor.current
            val actualContentColor =
                if (explicitSlotColor == Color.Unspecified) LocalContentColor.current else explicitSlotColor
            CompositionLocalProvider(
                LocalContentColor provides actualContentColor,
                LocalTextStyle provides LocalTextStyle.current.merge(LocalNomiTextStyle.current),
            ) {
                content(projection)
            }
        }
    }
}

/**
 * Material 3 原生适配设计系统，绑定在 [NomiDesignSystem.Companion] 上。
 *
 * 通过 `NomiDesignSystem.Material3` 访问；实现对象不对外暴露，调用方只依赖核心契约。
 */
val NomiDesignSystem.Companion.Material3: NomiDesignSystem get() = Material3System
