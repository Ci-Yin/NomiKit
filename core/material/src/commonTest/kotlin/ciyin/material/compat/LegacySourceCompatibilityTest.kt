package ciyin.material.compat

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ciyin.designsystem.theme.defaultComponentHeights
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultIconSizeScale
import ciyin.designsystem.theme.defaultLayoutConstraints
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultSpacings
import ciyin.designsystem.theme.defaultStrokes
import ciyin.material.theme.AppColorScheme
import ciyin.material.theme.AppShapes
import ciyin.material.theme.AppSizes
import ciyin.material.theme.ComponentHeights
import ciyin.material.theme.DarkMode.Dark
import ciyin.material.theme.DarkMode.Light
import ciyin.material.theme.DarkMode.System
import ciyin.material.theme.LayoutConstraints
import ciyin.material.theme.SizeScale
import ciyin.material.theme.Spacings
import ciyin.material.theme.Strokes
import ciyin.material.theme.bottom
import ciyin.material.theme.darkColorScheme
import ciyin.material.theme.disable
import ciyin.material.theme.enable
import ciyin.material.theme.end
import ciyin.material.theme.iconpack.ArrowRight
import ciyin.material.theme.iconpack.Home
import ciyin.material.theme.iconpack.IconPack
import ciyin.material.theme.iconpack.LightMode
import ciyin.material.theme.iconpack.Null
import ciyin.material.theme.iconpack.Settings
import ciyin.material.theme.lightColorScheme
import ciyin.material.theme.start
import ciyin.material.theme.top
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import ciyin.designsystem.icon.ArrowRight as newArrowRight
import ciyin.designsystem.icon.Home as newHome
import ciyin.designsystem.icon.IconPack as NewIconPack
import ciyin.designsystem.icon.LightMode as newLightMode
import ciyin.designsystem.icon.Null as newNull
import ciyin.designsystem.icon.Settings as newSettings

/**
 * 在消费包验证旧导入、颜色工厂命名参数、中立默认工厂、类型别名的公开构造与生成成员复制。
 *
 * 不导入 copy 扩展，既有圆角和图标扩展继续验证旧门面的语义。
 */
class LegacySourceCompatibilityTest {
    /**
     * 验证旧枚举项可直接导入，并保留 Light、Dark、System 名称。
     */
    @Test
    fun enumEntryImportsRemainValid() {
        assertEquals("Light", Light.name)
        assertEquals("Dark", Dark.name)
        assertEquals("System", System.name)
    }

    /**
     * 验证旧浅色工厂全部命名参数、深色主色覆盖及两种默认色板与中立工厂一致。
     */
    @Test
    fun factoriesKeepNamedParametersAndDefaults() {
        val defaults = lightColorScheme()
        assertEquals(defaultLightNomiColorScheme(), defaults)
        assertEquals(defaultDarkNomiColorScheme(), darkColorScheme())
        val named = lightColorScheme(
            primary = Color.Red,
            onPrimary = defaults.onPrimary,
            secondary = defaults.secondary,
            onSecondary = defaults.onSecondary,
            background = defaults.background,
            onBackground = defaults.onBackground,
            surface = defaults.surface,
            surfaceHigh = defaults.surfaceHigh,
            surfaceHigher = defaults.surfaceHigher,
            surfaceLow = defaults.surfaceLow,
            surfaceLower = defaults.surfaceLower,
            onSurface = defaults.onSurface,
            error = defaults.error,
            onError = defaults.onError,
            warning = defaults.warning,
            onWarning = defaults.onWarning,
            success = defaults.success,
            onSuccess = defaults.onSuccess,
            info = defaults.info,
            onInfo = defaults.onInfo,
            textPrimary = defaults.textPrimary,
            textSecondary = defaults.textSecondary,
            textHint = defaults.textHint,
            textCaption = defaults.textCaption,
            textDisabled = defaults.textDisabled,
            outline = defaults.outline,
            divider = defaults.divider,
        )
        assertEquals(Color.Red, named.primary)
        assertEquals(defaults, named.copy(primary = defaults.primary))
        assertEquals(Color.Blue, darkColorScheme(primary = Color.Blue).primary)
    }

    /**
     * 验证中立默认工厂与旧颜色工厂取得的颜色、圆角、间距及嵌套尺寸对象可经公开 copy 连续定制。
     */
    @Test
    fun constructorsAndCopiesRemainUsable() {
        val colors =
            defaultLightNomiColorScheme().copy(primary = Color.Red).copy(primary = Color.Blue)
        val shapes = defaultNomiShapes().copy(small = RoundedCornerShape(3.dp))
            .copy(large = RoundedCornerShape(7.dp))
        val spacings = defaultSpacings().copy(small = 3.dp).copy(large = 17.dp)
        val scale = defaultIconSizeScale().copy(
            tiny = 1.dp,
            extraSmall = 2.dp,
            small = 3.dp,
            medium = 4.dp,
            large = 5.dp,
            extraLarge = 6.dp,
            huge = 7.dp,
            massive = 8.dp,
            colossal = 9.dp,
        )
        val strokes = defaultStrokes().copy(thin = 1.dp, medium = 2.dp, thick = 3.dp)
        val heights = defaultComponentHeights().copy(
            appBar = 1.dp,
            bottomBar = 2.dp,
            tab = 3.dp,
            chip = 4.dp,
            badge = 5.dp
        )
        val constraints = defaultLayoutConstraints().copy(
            cardMinWidth = 1.dp,
            dialogMinWidth = 2.dp,
            dialogMaxWidth = 3.dp,
            sheetMinWidth = 4.dp
        )
        val sizes = defaultNomiSizes().copy(
            icon = scale,
            avatar = scale.copy(large = 6.dp),
            strokes = strokes.copy(thin = 2.dp),
            componentHeights = heights.copy(tab = 4.dp),
            layoutConstraints = constraints.copy(dialogMaxWidth = 5.dp),
        ).copy(icon = scale.copy(tiny = 2.dp))
        assertEquals(Color.Blue, colors.primary)
        assertEquals(RoundedCornerShape(7.dp), shapes.large)
        assertEquals(17.dp, spacings.large)
        assertEquals(2.dp, sizes.icon.tiny)
    }

    /**
     * 验证按命名参数启用或禁用圆角的结果与按边选择一致，未保留的角为直角。
     */
    @Test
    fun shapeExtensionsForwardWithoutChangingSemantics() {
        val shape = RoundedCornerShape(9.dp)
        val top =
            shape.enable(topStart = true, topEnd = true, bottomStart = false, bottomEnd = false)
        assertEquals(shape.top(), top)
        assertEquals(CornerSize(0.dp), top.bottomStart)
        assertEquals(shape.bottom(), shape.disable(topStart = true, topEnd = true))
        assertEquals(shape.start(), shape.enable(topStart = true, bottomStart = true))
        assertEquals(shape.end(), shape.enable(topEnd = true, bottomEnd = true))
    }

    /**
     * 验证旧图标命名空间和五个扩展仍可导入，且名称与核心缓存实例身份保持一致。
     */
    @Test
    fun iconImportsRemainUsable() {
        assertEquals("Home", IconPack.Home.name)
        assertEquals("Settings", IconPack.Settings.name)
        assertEquals("LightMode", IconPack.LightMode.name)
        assertEquals("ArrowRight", IconPack.ArrowRight.name)
        assertEquals("Null", IconPack.Null.name)
        assertSame(with(NewIconPack) { newHome }, IconPack.Home)
        assertSame(with(NewIconPack) { newSettings }, IconPack.Settings)
        assertSame(with(NewIconPack) { newLightMode }, IconPack.LightMode)
        assertSame(with(NewIconPack) { newArrowRight }, IconPack.ArrowRight)
        assertSame(with(NewIconPack) { newNull }, IconPack.Null)
        assertNotEquals(IconPack.Null, IconPack.Home)
    }

    /**
     * 旧颜色方案别名可完整构造并直接调用生成成员 copy，无须旧包转发扩展。
     */
    @Test
    fun appColorSchemeAliasExposesPublicConstructionAndCopy() {
        val defaults = lightColorScheme()
        val original = AppColorScheme(
            primary = defaults.primary,
            onPrimary = defaults.onPrimary,
            secondary = defaults.secondary,
            onSecondary = defaults.onSecondary,
            background = defaults.background,
            onBackground = defaults.onBackground,
            surface = defaults.surface,
            surfaceHigh = defaults.surfaceHigh,
            surfaceHigher = defaults.surfaceHigher,
            surfaceLow = defaults.surfaceLow,
            surfaceLower = defaults.surfaceLower,
            onSurface = defaults.onSurface,
            error = defaults.error,
            onError = defaults.onError,
            warning = defaults.warning,
            onWarning = defaults.onWarning,
            success = defaults.success,
            onSuccess = defaults.onSuccess,
            info = defaults.info,
            onInfo = defaults.onInfo,
            textPrimary = defaults.textPrimary,
            textSecondary = defaults.textSecondary,
            textHint = defaults.textHint,
            textCaption = defaults.textCaption,
            textDisabled = defaults.textDisabled,
            outline = defaults.outline,
            divider = defaults.divider,
        )
        assertEquals(defaults, original)
        assertEquals(defaults.hashCode(), original.hashCode())
        assertNotSame(defaults, original)
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = Color.Unspecified
        val changed = original.copy(primary = replacement)
        assertEquals(replacement, changed.primary)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(primary = original.primary))
        val (primary, onPrimary) = changed
        assertEquals(replacement, primary)
        assertEquals(original.onPrimary, onPrimary)
        assertEquals(defaults, original)
    }

    /**
     * 旧圆角别名可完整构造并直接调用生成成员 copy，无须旧包转发扩展。
     */
    @Test
    fun appShapesAliasExposesPublicConstructionAndCopy() {
        val defaults = defaultNomiShapes()
        val original = AppShapes(
            tiny = defaults.tiny,
            extraSmall = defaults.extraSmall,
            small = defaults.small,
            medium = defaults.medium,
            large = defaults.large,
            extraLarge = defaults.extraLarge,
            huge = defaults.huge,
            massive = defaults.massive,
            colossal = defaults.colossal,
        )
        assertEquals(defaults, original)
        assertEquals(defaults.hashCode(), original.hashCode())
        assertNotSame(defaults, original)
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = RoundedCornerShape(31.dp)
        val changed = original.copy(tiny = replacement)
        assertEquals(replacement, changed.tiny)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(tiny = original.tiny))
        val (tiny, extraSmall) = changed
        assertEquals(replacement, tiny)
        assertEquals(original.extraSmall, extraSmall)
        assertEquals(defaults, original)
    }

    /**
     * 旧间距别名可完整构造并直接调用生成成员 copy，无须旧包转发扩展。
     */
    @Test
    fun spacingsAliasExposesPublicConstructionAndCopy() {
        val defaults = defaultSpacings()
        val original = Spacings(
            tiny = defaults.tiny,
            extraSmall = defaults.extraSmall,
            small = defaults.small,
            medium = defaults.medium,
            large = defaults.large,
            extraLarge = defaults.extraLarge,
            huge = defaults.huge,
            massive = defaults.massive,
            colossal = defaults.colossal,
        )
        assertEquals(defaults, original)
        assertEquals(defaults.hashCode(), original.hashCode())
        assertNotSame(defaults, original)
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = 31.dp
        val changed = original.copy(tiny = replacement)
        assertEquals(replacement, changed.tiny)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(tiny = original.tiny))
        val (tiny, extraSmall) = changed
        assertEquals(replacement, tiny)
        assertEquals(original.extraSmall, extraSmall)
        assertEquals(defaults, original)
    }

    /**
     * 旧九级尺寸别名可完整构造并直接调用生成成员 copy，无须旧包转发扩展。
     */
    @Test
    fun sizeScaleAliasExposesPublicConstructionAndCopy() {
        val defaults = defaultIconSizeScale()
        val original = SizeScale(
            tiny = defaults.tiny,
            extraSmall = defaults.extraSmall,
            small = defaults.small,
            medium = defaults.medium,
            large = defaults.large,
            extraLarge = defaults.extraLarge,
            huge = defaults.huge,
            massive = defaults.massive,
            colossal = defaults.colossal,
        )
        assertEquals(defaults, original)
        assertEquals(defaults.hashCode(), original.hashCode())
        assertNotSame(defaults, original)
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = 31.dp
        val changed = original.copy(tiny = replacement)
        assertEquals(replacement, changed.tiny)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(tiny = original.tiny))
        val (tiny, extraSmall) = changed
        assertEquals(replacement, tiny)
        assertEquals(original.extraSmall, extraSmall)
        assertEquals(defaults, original)
    }

    /**
     * 旧描边别名可完整构造并直接调用生成成员 copy，无须旧包转发扩展。
     */
    @Test
    fun strokesAliasExposesPublicConstructionAndCopy() {
        val defaults = defaultStrokes()
        val original = Strokes(
            thin = defaults.thin,
            medium = defaults.medium,
            thick = defaults.thick,
        )
        assertEquals(defaults, original)
        assertEquals(defaults.hashCode(), original.hashCode())
        assertNotSame(defaults, original)
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = 11.dp
        val changed = original.copy(thin = replacement)
        assertEquals(replacement, changed.thin)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(thin = original.thin))
        val (thin, medium) = changed
        assertEquals(replacement, thin)
        assertEquals(original.medium, medium)
        assertEquals(defaults, original)
    }

    /**
     * 旧组件高度别名可完整构造并直接调用生成成员 copy，无须旧包转发扩展。
     */
    @Test
    fun componentHeightsAliasExposesPublicConstructionAndCopy() {
        val defaults = defaultComponentHeights()
        val original = ComponentHeights(
            appBar = defaults.appBar,
            bottomBar = defaults.bottomBar,
            tab = defaults.tab,
            chip = defaults.chip,
            badge = defaults.badge,
        )
        assertEquals(defaults, original)
        assertEquals(defaults.hashCode(), original.hashCode())
        assertNotSame(defaults, original)
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = 61.dp
        val changed = original.copy(appBar = replacement)
        assertEquals(replacement, changed.appBar)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(appBar = original.appBar))
        val (appBar, bottomBar) = changed
        assertEquals(replacement, appBar)
        assertEquals(original.bottomBar, bottomBar)
        assertEquals(defaults, original)
    }

    /**
     * 旧布局约束别名可完整构造并直接调用生成成员 copy，无须旧包转发扩展。
     */
    @Test
    fun layoutConstraintsAliasExposesPublicConstructionAndCopy() {
        val defaults = defaultLayoutConstraints()
        val original = LayoutConstraints(
            cardMinWidth = defaults.cardMinWidth,
            dialogMinWidth = defaults.dialogMinWidth,
            dialogMaxWidth = defaults.dialogMaxWidth,
            sheetMinWidth = defaults.sheetMinWidth,
        )
        assertEquals(defaults, original)
        assertEquals(defaults.hashCode(), original.hashCode())
        assertNotSame(defaults, original)
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = 301.dp
        val changed = original.copy(cardMinWidth = replacement)
        assertEquals(replacement, changed.cardMinWidth)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(cardMinWidth = original.cardMinWidth))
        val (cardMinWidth, dialogMinWidth) = changed
        assertEquals(replacement, cardMinWidth)
        assertEquals(original.dialogMinWidth, dialogMinWidth)
        assertEquals(defaults, original)
    }

    /**
     * 旧聚合尺寸别名可完整构造并直接调用生成成员 copy，无须旧包转发扩展。
     */
    @Test
    fun appSizesAliasExposesPublicConstructionAndCopy() {
        val defaults = defaultNomiSizes()
        val original = AppSizes(
            icon = defaults.icon,
            avatar = defaults.avatar,
            strokes = defaults.strokes,
            componentHeights = defaults.componentHeights,
            layoutConstraints = defaults.layoutConstraints,
        )
        assertEquals(defaults, original)
        assertEquals(defaults.hashCode(), original.hashCode())
        assertNotSame(defaults, original)
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = defaults.icon.copy(tiny = 31.dp)
        val changed = original.copy(icon = replacement)
        assertEquals(replacement, changed.icon)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(icon = original.icon))
        val (icon, avatar) = changed
        assertEquals(replacement, icon)
        assertEquals(original.avatar, avatar)
        assertEquals(defaults, original)
    }
}
