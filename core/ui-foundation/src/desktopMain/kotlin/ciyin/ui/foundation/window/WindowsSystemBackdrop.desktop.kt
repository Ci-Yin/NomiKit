package ciyin.ui.foundation.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.FrameWindowScope
import ciyin.platform.logger
import ciyin.platform.thisLogger
import com.sun.jna.CallbackReference
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import org.jetbrains.skiko.SkiaLayer
import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.RootPaneContainer
import java.awt.Window as AwtWindow

/** Windows 系统窗口背景材质。 */
enum class WindowsSystemBackdrop {
    /** Windows 11 长期存在窗口使用的 Mica。 */
    Mica,

    /** Windows 11 实时模糊的 Desktop Acrylic。 */
    DesktopAcrylic,

    /** Windows 11 标签式窗口使用的 Mica Alt。 */
    MicaAlt,
}

/**
 * 为当前 Compose Desktop 窗口同步 Windows 系统背景材质。
 *
 * @param type 系统背景材质类型
 * @param darkTitleBar 是否使用深色系统标题栏
 * @param onApplied 系统材质与透明客户区是否均已生效
 */
@Composable
fun FrameWindowScope.WindowsSystemBackdropEffect(
    type: WindowsSystemBackdrop,
    darkTitleBar: Boolean,
    onApplied: (Boolean) -> Unit,
) {
    val currentOnApplied = rememberUpdatedState(onApplied)
    val controller = remember(window) { WindowsSystemBackdropController(window) }

    DisposableEffect(controller, type) {
        currentOnApplied.value(controller.apply(type, darkTitleBar))
        onDispose {
            controller.clear()
            currentOnApplied.value(false)
        }
    }
    SideEffect {
        controller.updateDarkTitleBar(darkTitleBar)
    }
}

/**
 * 在窗口可显示前安装 Windows 系统背景材质所需的透明客户区宿主。
 *
 * 该函数必须从 `SwingWindow` 的 `init` 回调调用，以免重新挂载已经显示的 Compose 渲染层。
 *
 * @return 是否找到并成功预配置 Compose Desktop 的 Skia 渲染层
 */
fun ComposeWindow.prepareWindowsSystemBackdrop(): Boolean {
    if (!isWindows()) {
        return false
    }
    val currentContentPane = rootPane.contentPane
    if (currentContentPane is AcrylicContentPane) {
        return currentContentPane.enable(this)
    }
    check(!isDisplayable) { "系统背景材质宿主必须在窗口变为可显示状态前安装" }
    val backdropContentPane = AcrylicContentPane(currentContentPane)
    rootPane.contentPane = backdropContentPane
    return backdropContentPane.enable(this)
}

/** Windows 系统材质与 Compose 客户区透明状态控制器。 */
private class WindowsSystemBackdropController(
    /** 当前原生窗口。 */
    private val window: AwtWindow,
) {
    /** 系统材质日志。 */
    private val logger = thisLogger()

    /** Compose 客户区透明状态控制器。 */
    private val transparencyController = ComposeClientTransparencyController(window)

    /** 已安装到主窗口与 Skia 硬件层的背景擦除控制器。 */
    private var backgroundEraseControllers: List<WindowsBackgroundEraseController>? = null

    /** 延迟创建的 DWM 属性控制器，避免非 Windows 平台加载 dwmapi。 */
    private val dwmController by lazy {
        WindowsDwmController(
            api = DwmApiHolder.api,
            hwndProvider = { HWND(Native.getComponentPointer(window)) },
        )
    }

    /** 当前是否已经成功应用系统材质。 */
    private var applied: Boolean = false

    /**
     * 应用系统材质并在成功后打开 Compose 客户区透明绘制。
     *
     * @param type 系统材质类型
     * @param darkTitleBar 是否使用深色系统标题栏
     * @return 系统材质与客户区透明状态是否均已生效
     */
    fun apply(
        type: WindowsSystemBackdrop,
        darkTitleBar: Boolean,
    ): Boolean {
        if (!isWindows()) {
            return false
        }
        return runCatching {
            val windowsBuild = windowsBuildNumber()
            if (windowsBuild == null || windowsBuild < MINIMUM_BACKDROP_BUILD) {
                transparencyController.restore()
                logger.d { "当前 Windows 版本不支持系统背景材质，主窗口将继续使用实色背景" }
                return@runCatching false
            }
            val backdropResult = dwmController.applyBackdrop(
                type = type,
                enableRedirectionAlpha = windowsBuild >= REDIRECTION_ALPHA_BUILD,
            )
            if (!backdropResult.applied) {
                transparencyController.restore()
                logger.w { "设置 Windows 系统背景材质失败，主窗口将继续使用实色背景" }
                return@runCatching false
            }
            if (!backdropResult.redirectionAlphaApplied) {
                logger.d { "当前 Windows 版本未启用重定向位图 Alpha，将使用系统兼容合成路径" }
            }
            if (!enableBackgroundEraseSuppression()) {
                transparencyController.restore()
                dwmController.clearBackdrop()
                logger.w { "无法阻止窗口缩放时擦除透明客户区，主窗口将继续使用实色背景" }
                return@runCatching false
            }
            if (!transparencyController.enable()) {
                transparencyController.restore()
                restoreBackgroundEraseSuppression()
                dwmController.clearBackdrop()
                logger.w { "未找到可透明化的 Compose 渲染层，主窗口将继续使用实色背景" }
                return@runCatching false
            }
            dwmController.updateDarkTitleBar(darkTitleBar)
            applied = true
            true
        }.getOrElse { error ->
            transparencyController.restore()
            runCatching { restoreBackgroundEraseSuppression() }
            runCatching { dwmController.clearBackdrop() }
            applied = false
            logger.w(error) { "设置 Windows 系统背景材质时发生异常，主窗口将继续使用实色背景" }
            false
        }
    }

    /**
     * 在不重建系统材质的前提下更新标题栏深浅模式。
     *
     * @param dark 是否使用深色系统标题栏
     */
    fun updateDarkTitleBar(dark: Boolean) {
        if (!applied) {
            return
        }
        runCatching {
            if (!dwmController.updateDarkTitleBar(dark)) {
                logger.w { "更新 Windows 系统标题栏主题失败，将保留系统当前外观" }
            }
        }.onFailure { error ->
            logger.w(error) { "更新 Windows 系统标题栏主题时发生异常" }
        }
    }

    /** 清除系统材质并恢复客户区原始绘制状态。 */
    fun clear() {
        transparencyController.restore()
        if (isWindows()) {
            runCatching { restoreBackgroundEraseSuppression() }
                .onSuccess { restored ->
                    if (!restored) {
                        logger.w { "恢复窗口原始消息处理过程失败" }
                    }
                }
                .onFailure { error -> logger.w(error) { "恢复窗口原始消息处理过程时发生异常" } }
        }
        if (isWindows()) {
            runCatching { dwmController.clearBackdrop() }
                .onFailure { error -> logger.w(error) { "恢复 Windows 系统背景材质时发生异常" } }
        }
        applied = false
    }

    /**
     * 在顶层窗口与 Skia 硬件子窗口安装背景擦除拦截。
     *
     * @return 是否全部成功安装
     */
    private fun enableBackgroundEraseSuppression(): Boolean {
        val controllers = backgroundEraseControllers ?: window.backgroundEraseComponents()
            .map { component ->
                WindowsBackgroundEraseController(
                    api = User32WindowProcedureApi,
                    hwndProvider = { HWND(Native.getComponentPointer(component)) },
                )
            }
            .also { backgroundEraseControllers = it }
        val enabledControllers = mutableListOf<WindowsBackgroundEraseController>()
        controllers.forEach { controller ->
            if (!controller.enable()) {
                enabledControllers.asReversed().forEach { enabledController ->
                    enabledController.restore()
                }
                return false
            }
            enabledControllers += controller
        }
        return true
    }

    /**
     * 恢复顶层窗口与 Skia 硬件子窗口的原始消息处理过程。
     *
     * @return 是否全部成功恢复
     */
    private fun restoreBackgroundEraseSuppression(): Boolean {
        var restored = true
        backgroundEraseControllers.orEmpty().asReversed().forEach { controller ->
            if (!controller.restore()) {
                restored = false
            }
        }
        return restored
    }
}

/** 可替换原生窗口消息处理过程的 Win32 API 边界。 */
internal interface WindowProcedureApi {
    /**
     * 读取当前窗口消息处理过程。
     *
     * @param hwnd 原生窗口句柄
     * @return 当前窗口消息处理过程地址
     */
    fun getWindowProcedure(hwnd: HWND): Pointer?

    /**
     * 替换当前窗口消息处理过程。
     *
     * @param hwnd 原生窗口句柄
     * @param procedure 新的窗口消息处理过程地址
     * @return 替换前的窗口消息处理过程地址
     */
    fun setWindowProcedure(
        hwnd: HWND,
        procedure: Pointer,
    ): Pointer?

    /**
     * 返回 JNA 回调对应的原生函数地址。
     *
     * @param callback 窗口消息回调
     * @return 可传给 Win32 的回调地址
     */
    fun callbackPointer(callback: WinUser.WindowProc): Pointer

    /**
     * 将消息转发给原始窗口消息处理过程。
     *
     * @param procedure 原始窗口消息处理过程地址
     * @param hwnd 原生窗口句柄
     * @param message 窗口消息编号
     * @param wParam 消息参数
     * @param lParam 消息参数
     * @return 原始过程的处理结果
     */
    fun callWindowProcedure(
        procedure: Pointer,
        hwnd: HWND,
        message: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT
}

/** 使用 JNA User32 实现的窗口消息处理过程 API。 */
private object User32WindowProcedureApi : WindowProcedureApi {
    /** 读取当前窗口消息处理过程。 */
    override fun getWindowProcedure(hwnd: HWND): Pointer? =
        User32.INSTANCE.GetWindowLongPtr(hwnd, WinUser.GWL_WNDPROC).toPointer()

    /** 替换当前窗口消息处理过程。 */
    override fun setWindowProcedure(
        hwnd: HWND,
        procedure: Pointer,
    ): Pointer? = User32.INSTANCE.SetWindowLongPtr(hwnd, WinUser.GWL_WNDPROC, procedure)

    /** 返回 JNA 回调对应的原生函数地址。 */
    override fun callbackPointer(callback: WinUser.WindowProc): Pointer =
        CallbackReference.getFunctionPointer(callback)

    /** 将消息转发给原始窗口消息处理过程。 */
    override fun callWindowProcedure(
        procedure: Pointer,
        hwnd: HWND,
        message: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT = User32.INSTANCE.CallWindowProc(procedure, hwnd, message, wParam, lParam)
}

/** 在系统材质生效期间阻止 Win32 用默认实色擦除客户区。 */
internal class WindowsBackgroundEraseController(
    /** 可替换的 Win32 窗口过程 API。 */
    private val api: WindowProcedureApi,
    /** 当前窗口句柄提供器。 */
    private val hwndProvider: () -> HWND,
) {
    /** 原始窗口消息处理过程地址。 */
    private var originalWindowProcedure: Pointer? = null

    /** 强引用当前 JNA 回调，避免原生窗口仍使用时被垃圾回收。 */
    private var windowProcedureCallback: WinUser.WindowProc? = null

    /**
     * 安装窗口级背景擦除拦截。
     *
     * @return 是否成功安装或此前已经安装
     */
    fun enable(): Boolean {
        if (originalWindowProcedure != null) {
            return true
        }
        val hwnd = hwndProvider()
        val originalProcedure = api.getWindowProcedure(hwnd)
            ?.takeUnless { Pointer.nativeValue(it) == 0L }
            ?: return false
        val callback = WinUser.WindowProc { callbackHwnd, message, wParam, lParam ->
            if (message == ERASE_BACKGROUND_MESSAGE) {
                LRESULT(BACKGROUND_ERASED_RESULT)
            } else {
                api.callWindowProcedure(
                    procedure = originalProcedure,
                    hwnd = callbackHwnd,
                    message = message,
                    wParam = wParam,
                    lParam = lParam,
                )
            }
        }
        val callbackPointer = api.callbackPointer(callback)
        windowProcedureCallback = callback
        originalWindowProcedure = originalProcedure
        val replacedProcedure = api.setWindowProcedure(hwnd, callbackPointer)
        if (replacedProcedure == null || Pointer.nativeValue(replacedProcedure) == 0L) {
            originalWindowProcedure = null
            windowProcedureCallback = null
            return false
        }
        return true
    }

    /**
     * 恢复原始窗口消息处理过程。
     *
     * @return 是否无需恢复或已经成功恢复
     */
    fun restore(): Boolean {
        val originalProcedure = originalWindowProcedure ?: return true
        val replacedProcedure = api.setWindowProcedure(
            hwnd = hwndProvider(),
            procedure = originalProcedure,
        )
        if (replacedProcedure == null || Pointer.nativeValue(replacedProcedure) == 0L) {
            return false
        }
        originalWindowProcedure = null
        windowProcedureCallback = null
        return true
    }
}

/** Compose Desktop 客户区透明状态控制器。 */
private class ComposeClientTransparencyController(
    /** 当前原生窗口。 */
    private val window: AwtWindow,
) {
    /**
     * 打开 Compose 客户区透明绘制。
     *
     * @return 是否找到并成功配置了 Skia 渲染层
     */
    fun enable(): Boolean =
        ((window as? RootPaneContainer)?.rootPane?.contentPane as? AcrylicContentPane)
            ?.enable(window)
            ?: false

    /** 恢复修改前的 Swing 与 Skia 绘制状态。 */
    fun restore() {
        ((window as? RootPaneContainer)?.rootPane?.contentPane as? AcrylicContentPane)
            ?.restore()
    }
}

/** 使用预乘 Alpha 清除客户区背景的 Swing 绘制层。 */
internal class AcrylicContentPane(
    /** Compose Desktop 创建的原始客户区。 */
    content: Container? = null,
) : JPanel(BorderLayout()) {
    /** 禁用系统材质时恢复的原始背景色。 */
    private val fallbackBackground = content?.background ?: background

    /** 修改前的 Swing 不透明状态。 */
    private val originalOpacity = linkedMapOf<JComponent, Boolean>()

    /** 修改前的 Skia 透明状态。 */
    private val originalTransparency = linkedMapOf<SkiaLayer, Boolean>()

    /** 是否在绘制 Compose 内容前清除客户区 Alpha。 */
    var backdropEnabled: Boolean = false
        set(value) {
            field = value
            isOpaque = !value
            background = if (value) TRANSPARENT_AWT_COLOR else fallbackBackground
            repaint()
        }

    init {
        isOpaque = true
        background = fallbackBackground
        content?.let { add(it, BorderLayout.CENTER) }
    }

    /**
     * 在窗口显示前打开 Swing 与 Skia 客户区透明绘制。
     *
     * @param window 当前原生窗口
     * @return 是否找到并成功配置 Skia 渲染层
     */
    fun enable(window: AwtWindow): Boolean = runCatching {
        if (originalTransparency.isNotEmpty()) {
            backdropEnabled = true
            return@runCatching true
        }
        val components = window.flattenComponents()
        val skiaLayers = components.filterIsInstance<SkiaLayer>()
        if (skiaLayers.isEmpty()) {
            return@runCatching false
        }
        components.filterIsInstance<JComponent>().forEach { component ->
            originalOpacity.putIfAbsent(component, component.isOpaque)
            component.isOpaque = false
        }
        skiaLayers.forEach { layer ->
            originalTransparency.putIfAbsent(layer, layer.transparency)
            layer.transparency = true
        }
        backdropEnabled = true
        true
    }.getOrElse {
        restore()
        false
    }

    /** 恢复预安装前的 Swing 与 Skia 绘制状态。 */
    fun restore() {
        backdropEnabled = false
        originalTransparency.forEach { (layer, transparency) ->
            layer.transparency = transparency
        }
        originalOpacity.forEach { (component, opaque) ->
            component.isOpaque = opaque
        }
        originalTransparency.clear()
        originalOpacity.clear()
    }

    /** 以透明源像素清除背景后再绘制 Compose 子组件。 */
    override fun paint(graphics: Graphics) {
        if (backdropEnabled) {
            val transparentGraphics = graphics.create()
            try {
                if (transparentGraphics is Graphics2D) {
                    transparentGraphics.composite = AlphaComposite.Src
                    transparentGraphics.color = TRANSPARENT_AWT_COLOR
                    transparentGraphics.fillRect(0, 0, width, height)
                }
            } finally {
                transparentGraphics.dispose()
            }
        }
        super.paint(graphics)
    }
}

/** 完全透明的 AWT 客户区背景色。 */
private val TRANSPARENT_AWT_COLOR = java.awt.Color(0, 0, 0, 0)

/**
 * 返回组件及其全部后代，用于定位 Compose Desktop 的 Swing 与 Skia 渲染层。
 *
 * @return 当前组件树的稳定快照
 */
private fun Component.flattenComponents(): List<Component> = buildList {
    /** 递归收集一个组件节点。 */
    fun collect(component: Component) {
        add(component)
        if (component is Container) {
            component.components.forEach(::collect)
        }
    }
    collect(this@flattenComponents)
}

/**
 * 返回需要阻止原生背景擦除的顶层窗口与 Skia 硬件子窗口。
 *
 * @return 去重后的原生绘制组件
 */
private fun AwtWindow.backgroundEraseComponents(): List<Component> = buildList {
    add(this@backgroundEraseComponents)
    this@backgroundEraseComponents.flattenComponents()
        .filterIsInstance<SkiaLayer>()
        .mapTo(this) { layer -> layer.canvas }
}.distinct()

/** DWM 背景材质调用结果。 */
internal data class DwmBackdropResult(
    /** 背景材质是否已生效。 */
    val applied: Boolean,
    /** 重定向位图 Alpha 是否已生效。 */
    val redirectionAlphaApplied: Boolean,
)

/** 可注入原生接口与窗口句柄的 DWM 属性控制器。 */
internal class WindowsDwmController(
    /** DWM 原生接口。 */
    private val api: DwmApi,
    /** 当前窗口句柄提供器。 */
    private val hwndProvider: () -> HWND,
) {
    /**
     * 应用 Windows 系统背景材质。
     *
     * @param type 系统背景材质类型
     * @param enableRedirectionAlpha 是否尝试启用 24H2 重定向位图 Alpha
     * @return 背景材质及可选 Alpha 属性调用结果
     */
    fun applyBackdrop(
        type: WindowsSystemBackdrop,
        enableRedirectionAlpha: Boolean = true,
    ): DwmBackdropResult {
        val hwnd = hwndProvider()
        val backdropResult = api.DwmSetWindowAttribute(
            hwnd = hwnd,
            attribute = SYSTEM_BACKDROP_ATTRIBUTE,
            value = IntByReference(type.nativeValue),
            valueSize = Int.SIZE_BYTES,
        )
        if (backdropResult != DWM_SUCCESS) {
            return DwmBackdropResult(
                applied = false,
                redirectionAlphaApplied = false,
            )
        }
        if (!enableRedirectionAlpha) {
            return DwmBackdropResult(
                applied = true,
                redirectionAlphaApplied = false,
            )
        }
        val alphaResult = api.DwmSetWindowAttribute(
            hwnd = hwnd,
            attribute = REDIRECTION_BITMAP_ALPHA_ATTRIBUTE,
            value = IntByReference(1),
            valueSize = Int.SIZE_BYTES,
        )
        return DwmBackdropResult(
            applied = true,
            redirectionAlphaApplied = alphaResult == DWM_SUCCESS,
        )
    }

    /**
     * 更新 Windows 系统标题栏深浅模式。
     *
     * @param dark 是否使用深色系统标题栏
     * @return 现代或兼容属性是否成功
     */
    fun updateDarkTitleBar(dark: Boolean): Boolean {
        val hwnd = hwndProvider()
        val enabled = IntByReference(if (dark) 1 else 0)
        val modernResult = api.DwmSetWindowAttribute(
            hwnd = hwnd,
            attribute = DARK_MODE_ATTRIBUTE,
            value = enabled,
            valueSize = Int.SIZE_BYTES,
        )
        if (modernResult == DWM_SUCCESS) {
            return true
        }
        return api.DwmSetWindowAttribute(
            hwnd = hwnd,
            attribute = LEGACY_DARK_MODE_ATTRIBUTE,
            value = enabled,
            valueSize = Int.SIZE_BYTES,
        ) == DWM_SUCCESS
    }

    /** 清除系统材质并恢复 DWM 自动背景和默认 Alpha 处理。 */
    fun clearBackdrop() {
        val hwnd = hwndProvider()
        api.DwmSetWindowAttribute(
            hwnd = hwnd,
            attribute = REDIRECTION_BITMAP_ALPHA_ATTRIBUTE,
            value = IntByReference(0),
            valueSize = Int.SIZE_BYTES,
        )
        api.DwmSetWindowAttribute(
            hwnd = hwnd,
            attribute = SYSTEM_BACKDROP_ATTRIBUTE,
            value = IntByReference(SYSTEM_BACKDROP_AUTO),
            valueSize = Int.SIZE_BYTES,
        )
    }
}

/**
 * 为 Windows 原生窗口设置深浅标题栏；失败时保留系统默认标题栏。
 *
 * @param window 原生 AWT 窗口
 * @param dark 是否使用深色系统标题栏
 */
internal fun applyWindowsDarkTitleBar(
    window: AwtWindow,
    dark: Boolean,
) {
    if (!isWindows()) {
        return
    }
    val logger = logger("WindowsSystemTitleBar")
    runCatching {
        val controller = WindowsDwmController(
            api = DwmApiHolder.api,
            hwndProvider = { HWND(Native.getComponentPointer(window)) },
        )
        if (!controller.updateDarkTitleBar(dark)) {
            logger.w { "设置 Windows 系统标题栏失败，将继续使用系统默认标题栏" }
        }
    }.onFailure { error ->
        logger.w(error) { "设置 Windows 系统标题栏时发生异常，将继续使用系统默认标题栏" }
    }
}

/** 当前运行环境是否为 Windows Desktop。 */
private fun isWindows(): Boolean =
    System.getProperty("os.name").contains("windows", ignoreCase = true)

/** 读取 Windows 注册表中的当前系统 build 编号。 */
private fun windowsBuildNumber(): Int? = runCatching {
    Advapi32Util.registryGetStringValue(
        HKEY_LOCAL_MACHINE,
        WINDOWS_VERSION_REGISTRY_PATH,
        WINDOWS_BUILD_VALUE,
    ).toInt()
}.getOrNull()

/** Windows 系统背景材质对应的 DWM 原生值。 */
private val WindowsSystemBackdrop.nativeValue: Int
    get() = when (this) {
        WindowsSystemBackdrop.Mica -> 2
        WindowsSystemBackdrop.DesktopAcrylic -> 3
        WindowsSystemBackdrop.MicaAlt -> 4
    }

/** 延迟加载只在 Windows 上使用的 DWM API。 */
private object DwmApiHolder {
    /** DWM 原生 API 实例。 */
    val api: DwmApi = Native.load(
        "dwmapi",
        DwmApi::class.java,
        W32APIOptions.DEFAULT_OPTIONS,
    )
}

/** Windows Desktop Window Manager 原生接口。 */
internal interface DwmApi : StdCallLibrary {
    /**
     * 设置一个原生窗口的 DWM 属性。
     *
     * @param hwnd 窗口句柄
     * @param attribute 属性编号
     * @param value 属性值
     * @param valueSize 属性值字节数
     * @return HRESULT，0 表示成功
     */
    @Suppress("FunctionName")
    fun DwmSetWindowAttribute(
        hwnd: HWND,
        attribute: Int,
        value: IntByReference,
        valueSize: Int,
    ): Int
}

/** Windows 11 系统背景材质属性。 */
internal const val SYSTEM_BACKDROP_ATTRIBUTE: Int = 38

/** Windows 11 24H2 重定向位图 Alpha 属性。 */
internal const val REDIRECTION_BITMAP_ALPHA_ATTRIBUTE: Int = 39

/** Windows 10 20H1 及之后的深色标题栏属性。 */
internal const val DARK_MODE_ATTRIBUTE: Int = 20

/** 旧版 Windows 10 的深色标题栏属性。 */
internal const val LEGACY_DARK_MODE_ATTRIBUTE: Int = 19

/** DWM 自动选择系统背景材质。 */
internal const val SYSTEM_BACKDROP_AUTO: Int = 0

/** DWM API 成功返回值。 */
internal const val DWM_SUCCESS: Int = 0

/** Win32 擦除客户区背景消息。 */
internal const val ERASE_BACKGROUND_MESSAGE: Int = 0x0014

/** 表示应用已经处理客户区背景擦除。 */
internal const val BACKGROUND_ERASED_RESULT: Long = 1L

/** Windows 11 系统背景材质支持的最低 build。 */
private const val MINIMUM_BACKDROP_BUILD: Int = 22621

/** 支持重定向位图 Alpha 属性的 Windows 11 最低 build。 */
private const val REDIRECTION_ALPHA_BUILD: Int = 26100

/** Windows 当前版本注册表路径。 */
private const val WINDOWS_VERSION_REGISTRY_PATH: String =
    "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion"

/** Windows 当前 build 注册表值名称。 */
private const val WINDOWS_BUILD_VALUE: String = "CurrentBuildNumber"
