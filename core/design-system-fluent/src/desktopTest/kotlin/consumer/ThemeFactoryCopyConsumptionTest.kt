package consumer

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.theme.ComponentHeights
import ciyin.designsystem.theme.LayoutConstraints
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiShapes
import ciyin.designsystem.theme.NomiSizes
import ciyin.designsystem.theme.NomiTypography
import ciyin.designsystem.theme.SizeScale
import ciyin.designsystem.theme.Spacings
import ciyin.designsystem.theme.Strokes
import ciyin.designsystem.theme.defaultAvatarSizeScale
import ciyin.designsystem.theme.defaultComponentHeights
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultIconSizeScale
import ciyin.designsystem.theme.defaultLayoutConstraints
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import ciyin.designsystem.theme.defaultStrokes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * 在仅通过常规依赖消费核心的 Fluent 模块验证公开构造与生成成员 copy。
 *
 * 本测试不具备核心 friend 权限，也不导入 copy 扩展，避免同模块测试掩盖可见性问题。
 */
class ThemeFactoryCopyConsumptionTest {
    /**
     * 公开构造保留原有缺省字段，默认工厂的共享子组与新外层对象身份不变。
     */
    @Test
    fun publicDefaultsPreserveFactoryValuesAndIdentity() {
        val light = defaultLightNomiColorScheme()
        assertEquals(light, NomiColorScheme())
        assertEquals(light.copy(primary = Color.Red), NomiColorScheme(primary = Color.Red))
        assertEquals(defaultNomiShapes(), NomiShapes())
        assertEquals(defaultSpacings(), Spacings())
        assertEquals(defaultNomiSizes(), NomiSizes())
        assertNotSame(light, defaultLightNomiColorScheme())
        assertNotSame(defaultDarkNomiColorScheme(), defaultDarkNomiColorScheme())
        assertNotSame(defaultNomiShapes(), defaultNomiShapes())
        assertNotSame(defaultSpacings(), defaultSpacings())
        assertNotSame(defaultNomiSizes(), defaultNomiSizes())
        assertSame(defaultNomiTypography(), defaultNomiTypography())
        assertSame(defaultIconSizeScale(), defaultIconSizeScale())
        assertSame(defaultAvatarSizeScale(), defaultAvatarSizeScale())
        assertSame(defaultStrokes(), defaultStrokes())
        assertSame(defaultComponentHeights(), defaultComponentHeights())
        assertSame(defaultLayoutConstraints(), defaultLayoutConstraints())
        val sizes = NomiSizes()
        assertSame(defaultIconSizeScale(), sizes.icon)
        assertSame(defaultAvatarSizeScale(), sizes.avatar)
        assertSame(defaultStrokes(), sizes.strokes)
        assertSame(defaultComponentHeights(), sizes.componentHeights)
        assertSame(defaultLayoutConstraints(), sizes.layoutConstraints)
    }

    /**
     * 明暗工厂返回的色板均能公开复制，原对象和未修改字段保持不变。
     */
    @Test
    fun colorFactoriesExposeGeneratedCopy() {
        for (original in listOf(defaultLightNomiColorScheme(), defaultDarkNomiColorScheme())) {
            val snapshot = original.copy()
            val changed = original.copy(primary = Color.Red)
            assertNotSame(original, changed)
            assertEquals(Color.Red, changed.primary)
            assertNotEquals(original, changed)
            assertEquals(original, changed.copy(primary = original.primary))
            val (primary, onPrimary) = changed
            assertEquals(Color.Red, primary)
            assertEquals(original.onPrimary, onPrimary)
            assertEquals(original.hashCode(), changed.copy(primary = original.primary).hashCode())
            val sentinels =
                original.copy(primary = Color.Unspecified, onPrimary = Color.Transparent)
            assertEquals(Color.Unspecified, sentinels.primary)
            assertEquals(Color.Transparent, sentinels.onPrimary)
            assertEquals(snapshot, original)
        }
    }

    /**
     * 排版、圆角、间距的默认工厂仍可配合生成成员 copy 定制。
     */
    @Test
    fun typographyShapesAndSpacingsExposeCopy() {
        val typography = defaultNomiTypography()
        val changedTypography = typography.copy(bodyMedium = TextStyle(fontSize = 29.sp))
        assertEquals(29.sp, changedTypography.bodyMedium.fontSize)
        assertEquals(typography, changedTypography.copy(bodyMedium = typography.bodyMedium))
        val shapes = defaultNomiShapes()
        val changedShape = shapes.copy(small = RoundedCornerShape(31.dp))
        assertEquals(RoundedCornerShape(31.dp), changedShape.small)
        assertEquals(shapes, changedShape.copy(small = shapes.small))
        val spacings = defaultSpacings()
        val changedSpacing = spacings.copy(large = 37.dp)
        assertEquals(37.dp, changedSpacing.large)
        assertEquals(spacings, changedSpacing.copy(large = spacings.large))
    }

    /**
     * 尺寸和各子组保留默认入口，生成成员 copy 修改嵌套字段时不改变默认实例。
     */
    @Test
    fun nestedSizeGroupsExposeCopy() {
        val icon = defaultIconSizeScale()
        val avatar = defaultAvatarSizeScale()
        val strokes = defaultStrokes()
        val heights = defaultComponentHeights()
        val constraints = defaultLayoutConstraints()
        val base = defaultNomiSizes()
        val changed = base.copy(
            icon = icon.copy(tiny = 3.dp),
            avatar = avatar.copy(colossal = 111.dp),
            strokes = strokes.copy(thick = 9.dp),
            componentHeights = heights.copy(tab = 51.dp),
            layoutConstraints = constraints.copy(dialogMaxWidth = 601.dp),
        )
        assertEquals(3.dp, changed.icon.tiny)
        assertEquals(111.dp, changed.avatar.colossal)
        assertEquals(9.dp, changed.strokes.thick)
        assertEquals(51.dp, changed.componentHeights.tab)
        assertEquals(601.dp, changed.layoutConstraints.dialogMaxWidth)
        assertEquals(
            base, changed.copy(
                icon = changed.icon.copy(tiny = icon.tiny),
                avatar = changed.avatar.copy(colossal = avatar.colossal),
                strokes = changed.strokes.copy(thick = strokes.thick),
                componentHeights = changed.componentHeights.copy(tab = heights.tab),
                layoutConstraints = changed.layoutConstraints.copy(dialogMaxWidth = constraints.dialogMaxWidth),
            )
        )
    }

    /**
     * 颜色方案可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun nomiColorSchemeConstructionAndGeneratedCopyRemainPublic() {
        val original = NomiColorScheme(
            primary = Color.Unspecified,
            onPrimary = Color.Transparent,
            secondary = Color(0xFF030303),
            onSecondary = Color(0xFF040404),
            background = Color(0xFF050505),
            onBackground = Color(0xFF060606),
            surface = Color(0xFF070707),
            surfaceHigh = Color(0xFF080808),
            surfaceHigher = Color(0xFF090909),
            surfaceLow = Color(0xFF0A0A0A),
            surfaceLower = Color(0xFF0B0B0B),
            onSurface = Color(0xFF0C0C0C),
            error = Color(0xFF0D0D0D),
            onError = Color(0xFF0E0E0E),
            warning = Color(0xFF0F0F0F),
            onWarning = Color(0xFF101010),
            success = Color(0xFF111111),
            onSuccess = Color(0xFF121212),
            info = Color(0xFF131313),
            onInfo = Color(0xFF141414),
            textPrimary = Color(0xFF151515),
            textSecondary = Color(0xFF161616),
            textHint = Color(0xFF171717),
            textCaption = Color(0xFF181818),
            textDisabled = Color(0xFF191919),
            outline = Color(0xFF1A1A1A),
            divider = Color(0xFF1B1B1B),
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val changed = original.copy(primary = Color.Red)
        assertEquals(Color.Red, changed.primary)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(primary = original.primary))
        assertEquals(Color.Unspecified, original.primary)
        assertEquals(Color.Transparent, original.onPrimary)
        assertEquals(Color.Transparent, changed.onPrimary)
        val sentinels = original.copy(primary = Color.Transparent, onPrimary = Color.Unspecified)
        assertEquals(Color.Transparent, sentinels.primary)
        assertEquals(Color.Unspecified, sentinels.onPrimary)
        val (primary, onPrimary) = changed
        assertEquals(changed.primary, primary)
        assertEquals(original.onPrimary, onPrimary)

        val defaults = defaultLightNomiColorScheme()
        val allChanged = original.copy(
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
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 十五角色排版可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun nomiTypographyConstructionAndGeneratedCopyRemainPublic() {
        val original = NomiTypography(
            displayLarge = TextStyle(fontSize = 21.sp),
            displayMedium = TextStyle(fontSize = 22.sp),
            displaySmall = TextStyle(fontSize = 23.sp),
            headlineLarge = TextStyle(fontSize = 24.sp),
            headlineMedium = TextStyle(fontSize = 25.sp),
            headlineSmall = TextStyle(fontSize = 26.sp),
            titleLarge = TextStyle(fontSize = 27.sp),
            titleMedium = TextStyle(fontSize = 28.sp),
            titleSmall = TextStyle(fontSize = 29.sp),
            bodyLarge = TextStyle(fontSize = 30.sp),
            bodyMedium = TextStyle(fontSize = 31.sp),
            bodySmall = TextStyle(fontSize = 32.sp),
            labelLarge = TextStyle(fontSize = 33.sp),
            labelMedium = TextStyle(fontSize = 34.sp),
            labelSmall = TextStyle(fontSize = 35.sp),
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val changed = original.copy(displayLarge = TextStyle(fontSize = 71.sp))
        assertEquals(TextStyle(fontSize = 71.sp), changed.displayLarge)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(displayLarge = original.displayLarge))
        assertEquals(TextStyle(fontSize = 21.sp), original.displayLarge)
        val (displayLarge, displayMedium) = changed
        assertEquals(changed.displayLarge, displayLarge)
        assertEquals(original.displayMedium, displayMedium)

        val defaults = defaultNomiTypography()
        val allChanged = original.copy(
            displayLarge = defaults.displayLarge,
            displayMedium = defaults.displayMedium,
            displaySmall = defaults.displaySmall,
            headlineLarge = defaults.headlineLarge,
            headlineMedium = defaults.headlineMedium,
            headlineSmall = defaults.headlineSmall,
            titleLarge = defaults.titleLarge,
            titleMedium = defaults.titleMedium,
            titleSmall = defaults.titleSmall,
            bodyLarge = defaults.bodyLarge,
            bodyMedium = defaults.bodyMedium,
            bodySmall = defaults.bodySmall,
            labelLarge = defaults.labelLarge,
            labelMedium = defaults.labelMedium,
            labelSmall = defaults.labelSmall,
        )
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 九级圆角可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun nomiShapesConstructionAndGeneratedCopyRemainPublic() {
        val original = NomiShapes(
            tiny = RoundedCornerShape(31.dp),
            extraSmall = RoundedCornerShape(32.dp),
            small = RoundedCornerShape(33.dp),
            medium = RoundedCornerShape(34.dp),
            large = RoundedCornerShape(35.dp),
            extraLarge = RoundedCornerShape(36.dp),
            huge = RoundedCornerShape(37.dp),
            massive = RoundedCornerShape(38.dp),
            colossal = RoundedCornerShape(39.dp),
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val changed = original.copy(tiny = RoundedCornerShape(71.dp))
        assertEquals(RoundedCornerShape(71.dp), changed.tiny)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(tiny = original.tiny))
        assertEquals(RoundedCornerShape(31.dp), original.tiny)
        val (tiny, extraSmall) = changed
        assertEquals(changed.tiny, tiny)
        assertEquals(original.extraSmall, extraSmall)

        val defaults = defaultNomiShapes()
        val allChanged = original.copy(
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
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 九级间距可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun spacingsConstructionAndGeneratedCopyRemainPublic() {
        val original = Spacings(
            tiny = 31.dp,
            extraSmall = 32.dp,
            small = 33.dp,
            medium = 34.dp,
            large = 35.dp,
            extraLarge = 36.dp,
            huge = 37.dp,
            massive = 38.dp,
            colossal = 39.dp,
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val changed = original.copy(tiny = 71.dp)
        assertEquals(71.dp, changed.tiny)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(tiny = original.tiny))
        assertEquals(31.dp, original.tiny)
        val (tiny, extraSmall) = changed
        assertEquals(changed.tiny, tiny)
        assertEquals(original.extraSmall, extraSmall)

        val defaults = defaultSpacings()
        val allChanged = original.copy(
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
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 九级尺寸可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun sizeScaleConstructionAndGeneratedCopyRemainPublic() {
        val original = SizeScale(
            tiny = 31.dp,
            extraSmall = 32.dp,
            small = 33.dp,
            medium = 34.dp,
            large = 35.dp,
            extraLarge = 36.dp,
            huge = 37.dp,
            massive = 38.dp,
            colossal = 39.dp,
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val changed = original.copy(tiny = 71.dp)
        assertEquals(71.dp, changed.tiny)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(tiny = original.tiny))
        assertEquals(31.dp, original.tiny)
        val (tiny, extraSmall) = changed
        assertEquals(changed.tiny, tiny)
        assertEquals(original.extraSmall, extraSmall)

        val defaults = defaultIconSizeScale()
        val allChanged = original.copy(
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
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 描边尺度可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun strokesConstructionAndGeneratedCopyRemainPublic() {
        val original = Strokes(
            thin = 11.dp,
            medium = 12.dp,
            thick = 13.dp,
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val changed = original.copy(thin = 21.dp)
        assertEquals(21.dp, changed.thin)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(thin = original.thin))
        assertEquals(11.dp, original.thin)
        val (thin, medium) = changed
        assertEquals(changed.thin, thin)
        assertEquals(original.medium, medium)

        val defaults = defaultStrokes()
        val allChanged = original.copy(
            thin = defaults.thin,
            medium = defaults.medium,
            thick = defaults.thick,
        )
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 组件高度可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun componentHeightsConstructionAndGeneratedCopyRemainPublic() {
        val original = ComponentHeights(
            appBar = 61.dp,
            bottomBar = 62.dp,
            tab = 63.dp,
            chip = 64.dp,
            badge = 65.dp,
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val changed = original.copy(appBar = 71.dp)
        assertEquals(71.dp, changed.appBar)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(appBar = original.appBar))
        assertEquals(61.dp, original.appBar)
        val (appBar, bottomBar) = changed
        assertEquals(changed.appBar, appBar)
        assertEquals(original.bottomBar, bottomBar)

        val defaults = defaultComponentHeights()
        val allChanged = original.copy(
            appBar = defaults.appBar,
            bottomBar = defaults.bottomBar,
            tab = defaults.tab,
            chip = defaults.chip,
            badge = defaults.badge,
        )
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 布局约束可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun layoutConstraintsConstructionAndGeneratedCopyRemainPublic() {
        val original = LayoutConstraints(
            cardMinWidth = 301.dp,
            dialogMinWidth = 302.dp,
            dialogMaxWidth = 603.dp,
            sheetMinWidth = 304.dp,
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val changed = original.copy(cardMinWidth = 701.dp)
        assertEquals(701.dp, changed.cardMinWidth)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(cardMinWidth = original.cardMinWidth))
        assertEquals(301.dp, original.cardMinWidth)
        val (cardMinWidth, dialogMinWidth) = changed
        assertEquals(changed.cardMinWidth, cardMinWidth)
        assertEquals(original.dialogMinWidth, dialogMinWidth)

        val defaults = defaultLayoutConstraints()
        val allChanged = original.copy(
            cardMinWidth = defaults.cardMinWidth,
            dialogMinWidth = defaults.dialogMinWidth,
            dialogMaxWidth = defaults.dialogMaxWidth,
            sheetMinWidth = defaults.sheetMinWidth,
        )
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 聚合尺寸可在无核心 friend 权限时完整构造，并保留生成复制的值语义与原对象。
     */
    @Test
    fun nomiSizesConstructionAndGeneratedCopyRemainPublic() {
        val original = NomiSizes(
            icon = defaultIconSizeScale().copy(tiny = 31.dp),
            avatar = defaultAvatarSizeScale().copy(tiny = 32.dp),
            strokes = defaultStrokes().copy(thin = 11.dp),
            componentHeights = defaultComponentHeights().copy(appBar = 61.dp),
            layoutConstraints = defaultLayoutConstraints().copy(cardMinWidth = 301.dp),
        )
        val unchanged = original.copy()
        assertEquals(original, unchanged)
        assertEquals(original.hashCode(), unchanged.hashCode())
        assertNotSame(original, unchanged)

        val replacement = original.icon.copy(tiny = 71.dp)
        val changed = original.copy(icon = replacement)
        assertEquals(replacement, changed.icon)
        assertNotEquals(original, changed)
        assertNotSame(original, changed)
        assertEquals(original, changed.copy(icon = original.icon))
        assertEquals(defaultIconSizeScale().copy(tiny = 31.dp), original.icon)
        val (icon, avatar) = changed
        assertEquals(changed.icon, icon)
        assertEquals(original.avatar, avatar)

        val defaults = defaultNomiSizes()
        val allChanged = original.copy(
            icon = defaults.icon,
            avatar = defaults.avatar,
            strokes = defaults.strokes,
            componentHeights = defaults.componentHeights,
            layoutConstraints = defaults.layoutConstraints,
        )
        assertEquals(defaults, allChanged)
        assertEquals(defaults.hashCode(), allChanged.hashCode())
        assertNotSame(original, allChanged)
        assertEquals(unchanged, original)
    }

    /**
     * 明暗工厂原样接收未指定色与透明色，不回填默认；调用不会修改各自默认色板。
     */
    @Test
    fun colorFactoriesPreserveExplicitSentinelsAndDefaults() {
        val light = defaultLightNomiColorScheme()
        val dark = defaultDarkNomiColorScheme()
        assertNotEquals(light, dark)
        val lightSentinels = defaultLightNomiColorScheme(
            primary = Color.Unspecified,
            onPrimary = Color.Transparent,
        )
        val darkSentinels = defaultDarkNomiColorScheme(
            primary = Color.Transparent,
            onPrimary = Color.Unspecified,
        )
        assertEquals(Color.Unspecified, lightSentinels.primary)
        assertEquals(Color.Transparent, lightSentinels.onPrimary)
        assertEquals(Color.Transparent, darkSentinels.primary)
        assertEquals(Color.Unspecified, darkSentinels.onPrimary)
        assertEquals(
            light,
            lightSentinels.copy(primary = light.primary, onPrimary = light.onPrimary)
        )
        assertEquals(dark, darkSentinels.copy(primary = dark.primary, onPrimary = dark.onPrimary))
        assertEquals(light, defaultLightNomiColorScheme())
        assertEquals(dark, defaultDarkNomiColorScheme())
    }
}
