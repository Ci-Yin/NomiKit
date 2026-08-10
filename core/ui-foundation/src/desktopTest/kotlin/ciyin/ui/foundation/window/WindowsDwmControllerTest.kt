package ciyin.ui.foundation.window

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Windows 系统背景材质 DWM 属性映射测试。 */
class WindowsDwmControllerTest {
    /** 验证三种 PascalCase 材质均映射到官方 DWM 原生值。 */
    @Test
    fun backdropTypesUseOfficialDwmValues() {
        val expectedValues = listOf(
            WindowsSystemBackdrop.Mica to 2,
            WindowsSystemBackdrop.DesktopAcrylic to 3,
            WindowsSystemBackdrop.MicaAlt to 4,
        )

        expectedValues.forEach { (type, expectedValue) ->
            val api = RecordingDwmApi()
            val controller = WindowsDwmController(api) { testHwnd() }

            controller.applyBackdrop(
                type = type,
                enableRedirectionAlpha = false,
            )

            assertEquals(listOf(DwmCall(SYSTEM_BACKDROP_ATTRIBUTE, expectedValue)), api.calls)
        }
    }

    /** 验证透明内容窗格用预乘 Alpha 清除客户区背景。 */
    @Test
    fun acrylicContentPaneWritesTransparentPixels() {
        val image = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, image.width, image.height)
        val contentPane = AcrylicContentPane().apply {
            setSize(image.width, image.height)
            backdropEnabled = true
        }

        contentPane.paint(graphics)
        graphics.dispose()

        assertEquals(0, image.getRGB(image.width / 2, image.height / 2).ushr(24))
    }

    /** 验证关闭材质时透明客户区宿主恢复为不透明绘制。 */
    @Test
    fun acrylicContentPaneRestoresOpaqueRendering() {
        val contentPane = AcrylicContentPane()

        contentPane.backdropEnabled = true
        contentPane.restore()

        assertFalse(contentPane.backdropEnabled)
        assertTrue(contentPane.isOpaque)
    }

    /** 验证窗口缩放擦除消息被声明为已处理，避免原生白色背景覆盖透明客户区。 */
    @Test
    fun backgroundEraseMessageIsSuppressed() {
        val api = RecordingWindowProcedureApi()
        val controller = WindowsBackgroundEraseController(api) { testHwnd() }

        assertTrue(controller.enable())
        val result = api.installedCallback!!.callback(
            testHwnd(),
            ERASE_BACKGROUND_MESSAGE,
            WPARAM(0L),
            LPARAM(0L),
        )

        assertEquals(BACKGROUND_ERASED_RESULT, result.toLong())
        assertTrue(api.forwardedMessages.isEmpty())
    }

    /** 验证非背景擦除消息继续交给 Compose Desktop 原始窗口过程。 */
    @Test
    fun otherWindowMessagesAreForwarded() {
        val api = RecordingWindowProcedureApi()
        val controller = WindowsBackgroundEraseController(api) { testHwnd() }

        assertTrue(controller.enable())
        val result = api.installedCallback!!.callback(
            testHwnd(),
            TEST_WINDOW_MESSAGE,
            WPARAM(2L),
            LPARAM(3L),
        )

        assertEquals(FORWARDED_MESSAGE_RESULT, result.toLong())
        assertEquals(listOf(TEST_WINDOW_MESSAGE), api.forwardedMessages)
    }

    /** 验证关闭系统材质时恢复 Compose Desktop 原始窗口过程。 */
    @Test
    fun backgroundEraseHookRestoresOriginalWindowProcedure() {
        val api = RecordingWindowProcedureApi()
        val controller = WindowsBackgroundEraseController(api) { testHwnd() }

        assertTrue(controller.enable())
        assertTrue(controller.restore())

        assertEquals(api.originalProcedure, api.currentProcedure)
    }

    /** 验证 Desktop Acrylic 使用瞬态窗口背景值并尝试启用重定向 Alpha。 */
    @Test
    fun desktopAcrylicUsesTransientWindowBackdropAndAlpha() {
        val api = RecordingDwmApi()
        val controller = WindowsDwmController(api) { testHwnd() }

        val result = controller.applyBackdrop(WindowsSystemBackdrop.DesktopAcrylic)

        assertTrue(result.applied)
        assertTrue(result.redirectionAlphaApplied)
        assertEquals(
            listOf(
                DwmCall(SYSTEM_BACKDROP_ATTRIBUTE, 3),
                DwmCall(REDIRECTION_BITMAP_ALPHA_ATTRIBUTE, 1),
            ),
            api.calls,
        )
    }

    /** 验证背景属性失败时不会继续启用重定向 Alpha。 */
    @Test
    fun backdropFailureStopsBeforeAlpha() {
        val api = RecordingDwmApi(results = mapOf(SYSTEM_BACKDROP_ATTRIBUTE to 1))
        val controller = WindowsDwmController(api) { testHwnd() }

        val result = controller.applyBackdrop(WindowsSystemBackdrop.DesktopAcrylic)

        assertFalse(result.applied)
        assertFalse(result.redirectionAlphaApplied)
        assertEquals(listOf(DwmCall(SYSTEM_BACKDROP_ATTRIBUTE, 3)), api.calls)
    }

    /** 验证 Alpha 属性不可用时仍保留已经生效的系统背景材质。 */
    @Test
    fun alphaFailureKeepsBackdropActive() {
        val api = RecordingDwmApi(results = mapOf(REDIRECTION_BITMAP_ALPHA_ATTRIBUTE to 1))
        val controller = WindowsDwmController(api) { testHwnd() }

        val result = controller.applyBackdrop(WindowsSystemBackdrop.DesktopAcrylic)

        assertTrue(result.applied)
        assertFalse(result.redirectionAlphaApplied)
    }

    /** 验证旧版 Windows 兼容路径不会调用重定向 Alpha 属性。 */
    @Test
    fun compatibilityPathSkipsRedirectionAlpha() {
        val api = RecordingDwmApi()
        val controller = WindowsDwmController(api) { testHwnd() }

        val result = controller.applyBackdrop(
            type = WindowsSystemBackdrop.DesktopAcrylic,
            enableRedirectionAlpha = false,
        )

        assertTrue(result.applied)
        assertFalse(result.redirectionAlphaApplied)
        assertEquals(listOf(DwmCall(SYSTEM_BACKDROP_ATTRIBUTE, 3)), api.calls)
    }

    /** 验证现代深色标题栏属性失败时使用旧属性回退。 */
    @Test
    fun darkTitleBarFallsBackToLegacyAttribute() {
        val api = RecordingDwmApi(results = mapOf(DARK_MODE_ATTRIBUTE to 1))
        val controller = WindowsDwmController(api) { testHwnd() }

        assertTrue(controller.updateDarkTitleBar(dark = true))
        assertEquals(
            listOf(
                DwmCall(DARK_MODE_ATTRIBUTE, 1),
                DwmCall(LEGACY_DARK_MODE_ATTRIBUTE, 1),
            ),
            api.calls,
        )
    }

    /** 验证释放时恢复默认 Alpha 处理与 DWM 自动背景。 */
    @Test
    fun clearRestoresAutomaticBackdropAndAlpha() {
        val api = RecordingDwmApi()
        val controller = WindowsDwmController(api) { testHwnd() }

        controller.clearBackdrop()

        assertEquals(
            listOf(
                DwmCall(REDIRECTION_BITMAP_ALPHA_ATTRIBUTE, 0),
                DwmCall(SYSTEM_BACKDROP_ATTRIBUTE, SYSTEM_BACKDROP_AUTO),
            ),
            api.calls,
        )
    }
}

/** 一次 DWM 属性调用记录。 */
private data class DwmCall(
    /** DWM 属性编号。 */
    val attribute: Int,
    /** 传入的整数值。 */
    val value: Int,
)

/** 可按属性返回预设结果的记录型 DWM 接口。 */
private class RecordingDwmApi(
    /** 属性编号到 HRESULT 的映射。 */
    private val results: Map<Int, Int> = emptyMap(),
) : DwmApi {
    /** 已收到的全部 DWM 调用。 */
    val calls = mutableListOf<DwmCall>()

    /** 记录参数并返回该属性的预设 HRESULT。 */
    override fun DwmSetWindowAttribute(
        hwnd: HWND,
        attribute: Int,
        value: IntByReference,
        valueSize: Int,
    ): Int {
        calls += DwmCall(attribute, value.value)
        return results[attribute] ?: DWM_SUCCESS
    }
}

/** 记录窗口过程替换与消息转发的测试 API。 */
private class RecordingWindowProcedureApi : WindowProcedureApi {
    /** Compose Desktop 原始窗口过程地址。 */
    val originalProcedure = Pointer(2L)

    /** 当前窗口过程地址。 */
    var currentProcedure: Pointer = originalProcedure

    /** 最近安装的窗口过程回调。 */
    var installedCallback: WinUser.WindowProc? = null

    /** 已转发给原始窗口过程的消息。 */
    val forwardedMessages = mutableListOf<Int>()

    /** 读取当前窗口过程。 */
    override fun getWindowProcedure(hwnd: HWND): Pointer = currentProcedure

    /** 替换当前窗口过程并返回旧地址。 */
    override fun setWindowProcedure(
        hwnd: HWND,
        procedure: Pointer,
    ): Pointer {
        val previous = currentProcedure
        currentProcedure = procedure
        return previous
    }

    /** 记录窗口过程回调并返回稳定测试地址。 */
    override fun callbackPointer(callback: WinUser.WindowProc): Pointer {
        installedCallback = callback
        return TEST_CALLBACK_POINTER
    }

    /** 记录转发消息并返回稳定测试结果。 */
    override fun callWindowProcedure(
        procedure: Pointer,
        hwnd: HWND,
        message: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT {
        forwardedMessages += message
        return LRESULT(FORWARDED_MESSAGE_RESULT)
    }
}

/** 非背景擦除的测试窗口消息。 */
private const val TEST_WINDOW_MESSAGE: Int = 0x0400

/** 原始窗口过程返回的测试结果。 */
private const val FORWARDED_MESSAGE_RESULT: Long = 7L

/** JNA 测试回调的稳定地址。 */
private val TEST_CALLBACK_POINTER = Pointer(3L)

/** 返回仅供记录型原生调用使用的稳定测试窗口句柄。 */
private fun testHwnd(): HWND = HWND(Pointer(1L))
