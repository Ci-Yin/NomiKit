package com.ciyin.app.presentation.screen.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import org.jetbrains.skia.Image
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 响应式布局、字体缩放和主题作用域的离线像素验证。
 */
@OptIn(ExperimentalTestApi::class)
class DesignSystemDemoLayoutTest {
    /**
     * Expanded 视口的控制面板与展示面板必须并排。
     */
    @Test
    fun expandedViewportUsesTwoColumns() = runComposeUiTest {
        setGallery(width = WideWidth)
        val control = onNodeWithTag(DesignSystemDemoTags.Controls).getUnclippedBoundsInRoot()
        val showcase = onNodeWithTag(DesignSystemDemoTags.Showcase).getUnclippedBoundsInRoot()
        assertTrue(abs(control.top.value - showcase.top.value) < PixelTolerance)
        assertTrue(control.right <= showcase.left)
        assertTrue(control.right - control.left < showcase.right - showcase.left)
        saveScreenshot("wide-nomi-light")
    }

    /**
     * Fluent 的五种变体在足够宽度时横向排列，而非占满五行空白。
     */
    @Test
    fun fluentVariantsShareOneRowOnWideViewport() = runComposeUiTest {
        setGallery(width = WideWidth, system = NomiDesignSystem.Fluent)
        val primary =
            onNodeWithTag("design-system-demo-explicit-button-Primary-default").getUnclippedBoundsInRoot()
        val destructive =
            onNodeWithTag("design-system-demo-explicit-button-Destructive-default").getUnclippedBoundsInRoot()
        assertTrue(abs(primary.top.value - destructive.top.value) < PixelTolerance)
        assertTrue(primary.right < destructive.left)
    }

    /**
     * 接近桌面窗口的视口可同时查看设置、原生组件和交互分区。
     */
    @Test
    fun desktopFluentOverviewKeepsExamplesCompact() = runComposeUiTest {
        setGallery(
            width = DesktopWidth,
            system = NomiDesignSystem.Fluent,
            height = DesktopHeight,
            density = DesktopCaptureDensity,
        )
        onNodeWithTag("design-system-demo-native-defaults").assertIsDisplayed()
        onNodeWithTag(DesignSystemDemoTags.CounterButton).assertIsDisplayed()
        saveScreenshot("layout-fluent-desktop")
    }

    /**
     * 紧凑视口纵向排列；字体两倍仍可滚动到末尾主题作用域区域。
     */
    @Test
    fun compactLargeFontCanReachLastSection() = runComposeUiTest {
        setGallery(width = NarrowWidth, fontScale = 2f)
        val control = onNodeWithTag(DesignSystemDemoTags.Controls).getUnclippedBoundsInRoot()
        val showcase = onNodeWithTag(DesignSystemDemoTags.Showcase).getUnclippedBoundsInRoot()
        assertTrue(control.bottom <= showcase.top)
        saveScreenshot("compact-large-font-top")
        onNodeWithTag(DesignSystemDemoTags.OuterAfter).performScrollTo().assertIsDisplayed()
        saveScreenshot("compact-large-font-bottom")
    }

    /**
     * 按钮高度随多行文本增长，字体放大后底部不能溢出自身容器。
     */
    @Test
    fun longChineseButtonGrowsWithTypography() = runComposeUiTest {
        setGallery(width = NarrowWidth, fontScale = 2f)
        val button = onNodeWithTag(DesignSystemDemoTags.LongButton).performScrollTo()
        button.assertIsDisplayed()
        val bounds = button.fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.height > MinimumMultilineButtonHeight)
        assertTrue(bounds.width <= NarrowWidth.value + PixelTolerance)
        saveScreenshot("compact-large-font-button")
    }

    /**
     * 默认 Nomi/M3/Fluent 在浅色与深色均能完整渲染；先保存原生默认首屏，再保存显式覆盖区。
     */
    @Test
    fun defaultSystemsRenderLightAndDarkScreenshots() {
        listOf(
            NomiDesignSystem.Default,
            NomiDesignSystem.Material3,
            NomiDesignSystem.Fluent
        ).forEach { system ->
            listOf(DarkMode.Light, DarkMode.Dark).forEach { mode ->
                runComposeUiTest {
                    setGallery(width = WideWidth, system = system, mode = mode)
                    onNodeWithTag("design-system-demo-native-defaults").assertIsDisplayed()
                    onNodeWithTag("design-system-demo-native-text").assertIsDisplayed()
                    onNodeWithTag("design-system-demo-native-button").assertIsDisplayed()
                    onNodeWithTag("design-system-demo-native-surface").assertIsDisplayed()
                    onNodeWithTag("design-system-demo-native-card").assertIsDisplayed()
                    saveScreenshot("native-${system.id.value}-$mode")
                    onNodeWithTag(DesignSystemDemoTags.CounterButton).performScrollTo()
                        .assertIsDisplayed()
                    saveScreenshot("explicit-${system.id.value}-$mode")
                }
            }
        }
    }

    /**
     * 局部主题覆盖改变内层像素，前后外层仍保持相同主色。
     */
    @Test
    fun localOverrideRestoresOuterTheme() = runComposeUiTest {
        setGallery(width = WideWidth, customTokens = true)
        val before = surfaceCorner(DesignSystemDemoTags.OuterBefore)
        val inner = surfaceCorner(DesignSystemDemoTags.InnerOverride)
        val after = surfaceCorner(DesignSystemDemoTags.OuterAfter)
        assertEquals(before, after)
        assertTrue(before != inner)
        saveScreenshot("override-scope")
    }

    /**
     * 固定逻辑密度而不是主机 DPI，测试视口固定尺寸只用于离线测量；组合完成后等待 UI 空闲。
     *
     * @receiver 承载当前测试组合并提供空闲同步的 Compose 测试环境。
     * @param width 测试视口的逻辑宽度，单位为 dp。
     * @param fontScale 展示页使用的字体比例，默认 1 倍。
     * @param system 指定待选设计系统，须存在于共享默认候选目录中；这里只取其标识而不注入实现。
     * @param mode 展示页的外观请求，默认固定浅色而非跟随主机。
     * @param customTokens 是否启用强调色覆盖；局部主题示例始终开启。
     * @param height 测试视口的逻辑高度，默认使用滚动测试高度。
     * @param density 每 dp 对应的像素密度，默认 1；独立于字体比例。
     */
    private fun ComposeUiTest.setGallery(
        width: Dp,
        fontScale: Float = 1f,
        system: NomiDesignSystem = NomiDesignSystem.Default,
        mode: DarkMode = DarkMode.Light,
        customTokens: Boolean = false,
        height: Dp = ViewportHeight,
        density: Float = 1f,
    ) {
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density,
                    fontScale = 1f
                )
            ) {
                Box(modifier = Modifier.requiredSize(width = width, height = height)) {
                    DesignSystemDemoContent(
                        state = DesignSystemDemoUiState(
                            designSystemId = system.id,
                            darkMode = mode,
                            customTokens = customTokens,
                            override = true,
                            fontScale = fontScale,
                        ),
                        onAction = {},
                    )
                }
            }
        }
        waitForIdle()
    }

    /**
     * 采样表面内部靠左像素，避开文字和圆角。
     *
     * @receiver 包含目标表面且可执行滚动与截图的 Compose 测试环境。
     * @param tag 待采样表面的唯一语义标签，采样前先滚动到可见位置。
     * @return 表面左侧 [SurfaceInset] 像素、垂直中点处的 ARGB 颜色整数。
     */
    private fun ComposeUiTest.surfaceCorner(tag: String): Int {
        val node = onNodeWithTag(tag).performScrollTo()
        val bitmap = node.captureToImage().asSkiaBitmap()
        return bitmap.getColor(SurfaceInset, bitmap.height / 2)
    }

    /**
     * PNG 先写系统临时目录并校验，finally 清理本次目录。
     * 配置 nomikit.designSystem.artifacts 系统属性或 NOMIKIT_DESIGN_SYSTEM_ARTIFACTS 环境变量时，
     * 才把结果复制到指定产物目录供测试报告使用；默认不污染工作树。
     *
     * @receiver 包含展示页根节点的 Compose 测试环境。
     * @param name 不带扩展名的截图基名；显式产物目录中同名 PNG 会被覆盖。
     * @throws IllegalStateException Skia 无法将截图编码为图像数据时抛出。
     * @throws AssertionError 写出的临时截图文件为空时断言失败。
     */
    private fun ComposeUiTest.saveScreenshot(name: String) {
        val tempRoot = Files.createTempDirectory("nomikit-design-system-layout-")
        try {
            val bitmap = onNodeWithTag(DesignSystemDemoTags.Root).captureToImage().asSkiaBitmap()
            val bytes = Image.makeFromBitmap(bitmap).use { image ->
                image.encodeToData()?.use { data -> data.bytes } ?: error("无法编码展示页截图")
            }
            val temporaryFile = tempRoot.resolve("$name.png")
            Files.write(temporaryFile, bytes)
            assertTrue(Files.size(temporaryFile) > 0)
            val outputDirectory = System.getProperty("nomikit.designSystem.artifacts")
                ?: System.getenv("NOMIKIT_DESIGN_SYSTEM_ARTIFACTS")
            if (!outputDirectory.isNullOrBlank()) {
                val target = Path.of(outputDirectory).toAbsolutePath().normalize()
                Files.createDirectories(target)
                Files.copy(
                    temporaryFile,
                    target.resolve("$name.png"),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        } finally {
            tempRoot.toFile().deleteRecursively()
        }
    }

    /**
     * 与产品令牌无关的测试视口和像素断言常量。
     */
    private companion object {
        /**
         * 展开布局测试视口的逻辑宽度，单位为 dp。
         */
        val WideWidth = 1000.dp

        /**
         * 桌面截图视口逻辑宽度，单位为 dp，仅用于测试。
         */
        val DesktopWidth = 1240.dp

        /**
         * 桌面截图视口逻辑高度，单位为 dp，仅用于测试。
         */
        val DesktopHeight = 1000.dp

        /**
         * 桌面截图每 dp 对应的像素密度，将逻辑视口缩入测试宿主，避免 requiredSize 超出画布。
         */
        const val DesktopCaptureDensity = 0.75f

        /**
         * 手机级紧凑测试视口的逻辑宽度，单位为 dp。
         */
        val NarrowWidth = 360.dp

        /**
         * 滚动测试视口的可见逻辑高度，单位为 dp。
         */
        val ViewportHeight = 720.dp

        /**
         * 浮点坐标比较容差为 1；用于 dp 边界以及密度为 1 时的像素边界断言。
         */
        const val PixelTolerance = 1f

        /**
         * 两倍字体中文按钮的像素高度下限，用于断言多行内容已撑开控件。
         */
        const val MinimumMultilineButtonHeight = 64f

        /**
         * 表面颜色采样点距左边缘的像素数，用于避开边缘和文字。
         */
        const val SurfaceInset = 2
    }
}
