package ciyin.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiTypography
import ciyin.designsystem.theme.SizeScale
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 独立黄金值：除主色与主色内容色外，逐项摘自 HEAD 120ccec01a9ccaf04a3c1855318c1e02dfc77c08 的旧 core/material/theme。
 *
 * primary/onPrimary 于品牌色调整时替换为海蓝主色及对应前景色（浅色偏白、深色为深色前景），其余字段仍锁定历史值；
 * 预期值不调用新生产默认工厂，避免两个错误实现相互比较仍通过。
 */
class HistoricalThemeGoldenTest {
    /**
     * 旧浅色色板全部 27 项，包括语义色和全部表面层级。
     */
    @Test
    fun lightColorsMatchEveryHistoricalField() {
        val expected = listOf(
            0xFF0284C7, 0xFFF0FBFF, 0xFF03DAC6, 0xFF000000,
            0xFFFFFBFE, 0xFF1C1B1F, 0xFFF3EDF7, 0xFFECE6F0, 0xFFE6E0E9,
            0xFFF7F2FA, 0xFFFFFFFF, 0xFF1C1B1F,
            0xFFBA1A1A, 0xFFFFFFFF, 0xFFFF9800, 0xFFFFFFFF, 0xFF4CAF50, 0xFFFFFFFF,
            0xFF2196F3, 0xFFFFFFFF, 0xFF1C1B1F, 0xFF49454F, 0xFF79747E,
            0xFF938F99, 0xFFCAC4D0, 0xFF79747E, 0xFFE7E0EC,
        ).map { Color(it) }
        listOf(defaultLightNomiColorScheme())
            .forEach { assertEquals(expected, it.historicalFields().values.toList()) }
        assertEquals(27, defaultLightNomiColorScheme().historicalFields().size)
    }

    /**
     * 旧深色色板全部 27 项，不允许只对比 primary 而漏掉其它字段。
     */
    @Test
    fun darkColorsMatchEveryHistoricalField() {
        val expected = listOf(
            0xFF38BDF8, 0xFF1C1B1F, 0xFFCCC2DC, 0xFF332D41,
            0xFF1C1B1F, 0xFFE6E1E5, 0xFF211F26, 0xFF2B2930, 0xFF36343B,
            0xFF1D1B20, 0xFF0F0D13, 0xFFE6E1E5,
            0xFFFFB4AB, 0xFF690005, 0xFFFFB74D, 0xFF663C00, 0xFF81C784, 0xFF1B5E20,
            0xFF64B5F6, 0xFF003258, 0xFFE6E1E5, 0xFFCAC4D0, 0xFF938F99,
            0xFF79747E, 0xFF938F99, 0xFF938F99, 0xFF49454F,
        ).map { Color(it) }
        listOf(defaultDarkNomiColorScheme())
            .forEach { assertEquals(expected, it.historicalFields().values.toList()) }
        assertEquals(27, defaultDarkNomiColorScheme().historicalFields().size)
    }

    /**
     * 默认颜色工厂仍支持具名覆盖，未指定字段和透明色的含义保持不变。
     */
    @Test
    fun defaultColorFactoriesKeepNamedOverrides() {
        val light = defaultLightNomiColorScheme(primary = Color.Red, divider = Color.Transparent)
        val expectedLight = defaultLightNomiColorScheme().historicalFields().toMutableMap().apply {
            this["primary"] = Color.Red
            this["divider"] = Color.Transparent
        }
        assertEquals(expectedLight, light.historicalFields())
        val dark = defaultDarkNomiColorScheme(primary = Color.Blue, onPrimary = Color.Transparent)
        val expectedDark = defaultDarkNomiColorScheme().historicalFields().toMutableMap().apply {
            this["primary"] = Color.Blue
            this["onPrimary"] = Color.Transparent
        }
        assertEquals(expectedDark, dark.historicalFields())
    }

    /**
     * 15 个文字角色逐一锁定字体族、字重、字号、行高和字距的旧值。
     */
    @Test
    fun allFifteenTypographyRolesMatchHistoricalMetrics() {
        val expected = listOf(
            Triple(57, 64, -0.2f), Triple(45, 52, 0f), Triple(36, 44, 0f),
            Triple(32, 40, 0f), Triple(28, 36, 0f), Triple(24, 32, 0f),
            Triple(22, 28, 0f), Triple(16, 24, 0.2f), Triple(14, 20, 0.1f),
            Triple(16, 24, 0.5f), Triple(14, 20, 0.2f), Triple(12, 16, 0.4f),
            Triple(14, 20, 0.1f), Triple(12, 16, 0.5f), Triple(10, 16, 0.5f),
        )
        val mediumWeightRoles =
            setOf("titleMedium", "titleSmall", "labelLarge", "labelMedium", "labelSmall")
        listOf(defaultNomiTypography())
            .forEach { typography ->
                val roles = typography.historicalStyles()
                assertEquals(15, roles.size)
                roles.entries.forEachIndexed { index, (name, style) ->
                    val (size, height, spacing) = expected[index]
                    assertEquals(FontFamily.Default, style.fontFamily, name + " fontFamily")
                    assertEquals(
                        if (name in mediumWeightRoles) FontWeight.Medium else FontWeight.Normal,
                        style.fontWeight,
                        name + " fontWeight"
                    )
                    assertEquals(size.sp, style.fontSize, name + " fontSize")
                    assertEquals(height.sp, style.lineHeight, name + " lineHeight")
                    assertEquals(spacing.sp, style.letterSpacing, name + " letterSpacing")
                }
            }
    }

    /**
     * 九级圆角全部保持历史半径，不仅验证 M3 标准五级。
     */
    @Test
    fun allNineShapesMatchHistoricalRadii() {
        val expected = listOf(2, 4, 8, 12, 16, 20, 24, 32, 48).map { RoundedCornerShape(it.dp) }
        listOf(defaultNomiShapes())
            .forEach { shapes ->
                assertEquals(
                    expected,
                    listOf(
                        shapes.tiny,
                        shapes.extraSmall,
                        shapes.small,
                        shapes.medium,
                        shapes.large,
                        shapes.extraLarge,
                        shapes.huge,
                        shapes.massive,
                        shapes.colossal
                    ),
                )
            }
    }

    /**
     * 九级间距均使用 HEAD 中固定的历史值。
     */
    @Test
    fun allNineSpacingsMatchHistoricalValues() {
        val expected = listOf(2, 4, 8, 12, 16, 20, 24, 32, 48).map { it.dp }
        listOf(defaultSpacings())
            .forEach { spacing ->
                assertEquals(
                    expected,
                    listOf(
                        spacing.tiny,
                        spacing.extraSmall,
                        spacing.small,
                        spacing.medium,
                        spacing.large,
                        spacing.extraLarge,
                        spacing.huge,
                        spacing.massive,
                        spacing.colossal
                    ),
                )
            }
    }

    /**
     * 图标、头像、描边、组件与布局约束的每一个尺寸字段均锁定历史值。
     */
    @Test
    fun everySizeFieldMatchesHistoricalValues() {
        listOf(defaultNomiSizes())
            .forEach { sizes ->
                assertEquals(
                    listOf(14, 16, 18, 20, 24, 32, 40, 48, 56).map { it.dp },
                    sizes.icon.orderedValues()
                )
                assertEquals(
                    listOf(18, 24, 32, 40, 48, 56, 64, 72, 96).map { it.dp },
                    sizes.avatar.orderedValues()
                )
                assertEquals(
                    listOf(1, 2, 3).map { it.dp },
                    listOf(sizes.strokes.thin, sizes.strokes.medium, sizes.strokes.thick)
                )
                assertEquals(
                    listOf(56, 56, 48, 32, 16).map { it.dp },
                    with(sizes.componentHeights) { listOf(appBar, bottomBar, tab, chip, badge) },
                )
                assertEquals(
                    listOf(280, 280, 560, 360).map { it.dp },
                    with(sizes.layoutConstraints) {
                        listOf(
                            cardMinWidth,
                            dialogMinWidth,
                            dialogMaxWidth,
                            sheetMinWidth
                        )
                    },
                )
            }
    }
}

/**
 * 按历史声明顺序列出全部颜色字段，漏字段会直接改变数量与序列。
 *
 * @receiver 待与独立历史颜色值比较的色板，不修改其字段。
 * @return 新的有序映射，以 27 个历史字段名为键、对应颜色为值。
 */
private fun NomiColorScheme.historicalFields(): Map<String, Color> = linkedMapOf(
    "primary" to primary,
    "onPrimary" to onPrimary,
    "secondary" to secondary,
    "onSecondary" to onSecondary,
    "background" to background,
    "onBackground" to onBackground,
    "surface" to surface,
    "surfaceHigh" to surfaceHigh,
    "surfaceHigher" to surfaceHigher,
    "surfaceLow" to surfaceLow,
    "surfaceLower" to surfaceLower,
    "onSurface" to onSurface,
    "error" to error,
    "onError" to onError,
    "warning" to warning,
    "onWarning" to onWarning,
    "success" to success,
    "onSuccess" to onSuccess,
    "info" to info,
    "onInfo" to onInfo,
    "textPrimary" to textPrimary,
    "textSecondary" to textSecondary,
    "textHint" to textHint,
    "textCaption" to textCaption,
    "textDisabled" to textDisabled,
    "outline" to outline,
    "divider" to divider,
)

/**
 * 按旧 Typography 构造顺序提取 15 个角色。
 *
 * @receiver 待校验字体族、字重、字号、行高和字距的中立排版。
 * @return 新的有序映射，依次保存 display、headline、title、body、label 的三个尺寸角色。
 */
private fun NomiTypography.historicalStyles(): Map<String, TextStyle> = linkedMapOf(
    "displayLarge" to displayLarge,
    "displayMedium" to displayMedium,
    "displaySmall" to displaySmall,
    "headlineLarge" to headlineLarge,
    "headlineMedium" to headlineMedium,
    "headlineSmall" to headlineSmall,
    "titleLarge" to titleLarge,
    "titleMedium" to titleMedium,
    "titleSmall" to titleSmall,
    "bodyLarge" to bodyLarge,
    "bodyMedium" to bodyMedium,
    "bodySmall" to bodySmall,
    "labelLarge" to labelLarge,
    "labelMedium" to labelMedium,
    "labelSmall" to labelSmall,
)

/**
 * 按历史尺寸等级顺序提取全部字段。
 *
 * @receiver 待校验的图标或头像尺寸刻度，不修改其值。
 * @return 从 tiny 到 colossal 排列的九项 Dp 值列表。
 */
private fun SizeScale.orderedValues(): List<Dp> = listOf(
    tiny, extraSmall, small, medium, large, extraLarge, huge, massive, colossal,
)
