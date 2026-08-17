package ciyin.platform.share

import ciyin.platform.logger
import com.sun.jna.Callback
import com.sun.jna.CallbackReference
import com.sun.jna.Function
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.StdCallLibrary.StdCallCallback
import com.sun.jna.win32.W32APIOptions
import java.awt.EventQueue
import java.awt.KeyboardFocusManager
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

/** Windows 系统分享日志。 */
private val windowsShareLogger = logger("WindowsPlatformShare")

/** Windows Share Sheet 的 WinRT/JNA 启动器。 */
internal val windowsPlatformShareLauncher: WindowsShareLauncher = WindowsShareLauncher { payload ->
    withContext(Dispatchers.Swing) {
        WindowsShareSheet.open(payload)
    }
}

/** Windows Share Sheet 会话入口。 */
private object WindowsShareSheet {

    /** 打开当前活动窗口对应的 Windows Share Sheet。 */
    suspend fun open(payload: WindowsSharePayload): PlatformShareResult {
        val presenter = resolveWindowsSharePresenter()
        var nativePayload: NativeWindowsSharePayload? = null
        var handler: ComCallableObject? = null
        var session: WindowsShareSession? = null
        try {
            val host = WindowsShareHostRegistry.getOrCreate(presenter)
            nativePayload = payload.toNativeWindowsSharePayload()
            session = WindowsShareSession(
                host = host,
                payload = nativePayload,
            )
            handler = createDataRequestedHandler(session::handleDataRequested)
            session.handler = handler
            session.eventToken = host.addDataRequested(handler)
            handler.releaseInitialReference()

            if (!WindowsShareSessionRegistry.register(session)) {
                throw PlatformShareException(
                    reason = PlatformShareFailureReason.LaunchFailed,
                    message = "当前 Windows 窗口已有正在启动的系统分享会话",
                )
            }
            session.observePresenterLifecycle()

            host.showShareUi()
            session.callbackFailure?.let { throw it }
            return PlatformShareResult.Opened
        } catch (exception: CancellationException) {
            session?.cleanupNow()
                ?: cleanupUnregisteredWindowsShareResources(
                    handler = handler,
                    payload = nativePayload,
                )
            throw exception
        } catch (exception: PlatformShareException) {
            session?.cleanupNow()
                ?: cleanupUnregisteredWindowsShareResources(
                    handler = handler,
                    payload = nativePayload,
                )
            throw exception
        } catch (exception: WinRtCallException) {
            session?.cleanupNow()
                ?: cleanupUnregisteredWindowsShareResources(
                    handler = handler,
                    payload = nativePayload,
                )
            if (isUnsupportedWindowsShareHResult(exception.hresult)) {
                return PlatformShareResult.Unsupported
            }
            throw exception.toPlatformShareException(
                fallbackReason = PlatformShareFailureReason.LaunchFailed,
                message = "Windows 系统分享面板启动失败",
            )
        } catch (exception: UnsatisfiedLinkError) {
            session?.cleanupNow()
                ?: cleanupUnregisteredWindowsShareResources(
                    handler = handler,
                    payload = nativePayload,
                )
            return PlatformShareResult.Unsupported
        } catch (exception: Throwable) {
            session?.cleanupNow()
                ?: cleanupUnregisteredWindowsShareResources(
                    handler = handler,
                    payload = nativePayload,
                )
            throw PlatformShareException(
                reason = PlatformShareFailureReason.LaunchFailed,
                message = "Windows 系统分享面板启动失败",
                cause = exception,
            )
        }
    }
}

/**
 * Windows Share Sheet 的 AWT 展示窗口。
 *
 * @property window AWT 顶层窗口
 * @property windowHandle 原生 HWND
 */
private data class WindowsSharePresenter(
    val window: Window,
    val windowHandle: Pointer,
)

/** 查找最适合作为 Share Sheet owner 的 AWT 窗口及句柄。 */
private fun resolveWindowsSharePresenter(): WindowsSharePresenter {
    check(EventQueue.isDispatchThread()) { "Windows 系统分享必须在 AWT EDT 上解析窗口" }
    val visibleWindows = Window.getWindows()
        .filter { it.isDisplayable && it.isShowing }
    val focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
    val selectedWindow = focusManager.activeWindow
        ?.takeIf(visibleWindows::contains)
        ?: focusManager.focusedWindow?.takeIf(visibleWindows::contains)
        ?: visibleWindows.singleOrNull()
        ?: throw PlatformShareException(
            reason = PlatformShareFailureReason.PresenterUnavailable,
            message = "没有找到唯一可用于展示 Windows 分享面板的活动窗口",
        )
    val pointer = try {
        Native.getComponentPointer(selectedWindow)
    } catch (exception: Throwable) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.PresenterUnavailable,
            message = "无法取得 Windows 分享面板所需的 HWND",
            cause = exception,
        )
    }
    if (pointer == null || Pointer.nativeValue(pointer) == 0L) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.PresenterUnavailable,
            message = "Windows 分享面板所需的 HWND 无效",
        )
    }
    return WindowsSharePresenter(
        window = selectedWindow,
        windowHandle = pointer,
    )
}

/** 将已校验载荷转换为持有 WinRT 对象的原生载荷。 */
private suspend fun WindowsSharePayload.toNativeWindowsSharePayload(): NativeWindowsSharePayload = when (this) {
    is WindowsSharePayload.Text -> NativeWindowsSharePayload.Text(
        title = title,
        value = value,
    )

    is WindowsSharePayload.Files -> {
        val storageFileStatics = activateWinRtInterface(
            runtimeClassName = STORAGE_FILE_RUNTIME_CLASS,
            interfaceId = WindowsShareIids.StorageFileStatics,
        )
        val storageItems = mutableListOf<WinRtComPointer>()
        try {
            values.forEach { file ->
                val storageFile = try {
                    storageFileStatics.getStorageFileFromPath(file.path.toString())
                } catch (exception: WinRtCallException) {
                    throw exception.toPlatformShareException(
                        fallbackReason = file.unavailableReason,
                        message = "Windows 无法把本地路径解析为 StorageFile: ${file.path}",
                    )
                }
                try {
                    storageItems += storageFile.queryInterface(WindowsShareIids.StorageItem)
                } finally {
                    storageFile.release()
                }
            }
            NativeWindowsSharePayload.Files(
                title = title,
                storageItems = storageItems.toList(),
            )
        } catch (exception: Throwable) {
            storageItems.forEach(WinRtComPointer::release)
            throw exception
        } finally {
            storageFileStatics.release()
        }
    }
}

/** 持有原生对象的 Windows 分享载荷。 */
private sealed interface NativeWindowsSharePayload {
    /** Windows DataPackage 标题。 */
    val title: String

    /** 原生 Windows 文本载荷。 */
    data class Text(
        override val title: String,
        val value: String,
    ) : NativeWindowsSharePayload

    /** 原生 Windows 文件载荷。 */
    data class Files(
        override val title: String,
        val storageItems: List<WinRtComPointer>,
    ) : NativeWindowsSharePayload

    /** 释放载荷持有的 COM 引用。 */
    fun release() {
        if (this is Files) storageItems.forEach(WinRtComPointer::release)
    }
}

/**
 * 与单个 HWND 同生命周期的 Windows 分享宿主。
 *
 * @property presenterWindow Share Sheet 的 AWT owner 窗口
 * @property windowHandle owner 窗口的原生 HWND
 * @property apartment owner 所在 EDT 的 WinRT apartment 引用
 * @property interop 当前窗口使用的 IDataTransferManagerInterop
 * @property manager 当前窗口唯一的 DataTransferManager
 */
private class WindowsShareHost(
    val presenterWindow: Window,
    val windowHandle: Pointer,
    private val apartment: WindowsRuntimeApartment,
    private val interop: WinRtComPointer,
    private val manager: WinRtComPointer,
) {
    /** 是否已经释放宿主持有的原生资源。 */
    private val closed = AtomicBoolean(false)

    /** owner 窗口生命周期监听器。 */
    private val presenterWindowListener = object : WindowAdapter() {
        /** owner 销毁时释放当前会话和窗口级 WinRT 引用。 */
        override fun windowClosed(event: WindowEvent) {
            WindowsShareSessionRegistry.cleanup(windowHandle)
            WindowsShareHostRegistry.remove(this@WindowsShareHost)
            close()
        }
    }

    /** 开始观察 owner 窗口的销毁事件。 */
    fun observeWindowLifecycle() {
        check(EventQueue.isDispatchThread()) { "Windows 分享宿主必须在 AWT EDT 上绑定窗口" }
        presenterWindow.addWindowListener(presenterWindowListener)
    }

    /** 为当前窗口的 DataTransferManager 注册 DataRequested 事件。 */
    fun addDataRequested(handler: ComCallableObject): Long = manager.addDataRequested(handler)

    /** 注销当前窗口的 DataRequested 事件。 */
    fun removeDataRequested(token: Long): Int = manager.removeDataRequested(token)

    /** 通过当前窗口已经注册的 interop 展示 Share Sheet。 */
    fun showShareUi() {
        interop.showShareUiForWindow(windowHandle)
    }

    /** 释放窗口级 COM 引用并成对结束 WinRT apartment。 */
    private fun close() {
        if (!closed.compareAndSet(false, true)) return
        check(EventQueue.isDispatchThread()) { "Windows 分享宿主必须在 AWT EDT 上释放" }
        presenterWindow.removeWindowListener(presenterWindowListener)
        manager.release()
        interop.release()
        apartment.close()
    }
}

/** 按 HWND 复用 DataTransferManager 的 Windows 分享宿主注册表。 */
private object WindowsShareHostRegistry {
    /** HWND 到分享宿主的映射。 */
    private val hosts = ConcurrentHashMap<Long, WindowsShareHost>()

    /** 取得现有分享宿主，或为当前 owner 创建唯一宿主。 */
    fun getOrCreate(presenter: WindowsSharePresenter): WindowsShareHost {
        check(EventQueue.isDispatchThread()) { "Windows 分享宿主必须在 AWT EDT 上创建" }
        val key = Pointer.nativeValue(presenter.windowHandle)
        hosts[key]?.let { return it }

        val apartment = WindowsRuntimeApartment.initialize()
        var interop: WinRtComPointer? = null
        var manager: WinRtComPointer? = null
        try {
            interop = activateWinRtInterface(
                runtimeClassName = DATA_TRANSFER_MANAGER_RUNTIME_CLASS,
                interfaceId = WindowsShareIids.DataTransferManagerInterop,
            )
            manager = interop.getDataTransferManagerForWindow(presenter.windowHandle)
            val host = WindowsShareHost(
                presenterWindow = presenter.window,
                windowHandle = presenter.windowHandle,
                apartment = apartment,
                interop = interop,
                manager = manager,
            )
            host.observeWindowLifecycle()
            hosts[key] = host
            return host
        } catch (exception: Throwable) {
            manager?.release()
            interop?.release()
            apartment.close()
            throw exception
        }
    }

    /** 从注册表删除已关闭的分享宿主。 */
    fun remove(host: WindowsShareHost) {
        hosts.remove(Pointer.nativeValue(host.windowHandle), host)
    }
}

/** Windows 系统分享会话。 */
private class WindowsShareSession(
    private val host: WindowsShareHost,
    private val payload: NativeWindowsSharePayload,
) {
    /** 会话所属 HWND。 */
    val windowHandle: Pointer
        get() = host.windowHandle

    /** DataRequested 事件处理器。 */
    lateinit var handler: ComCallableObject

    /** DataRequested 事件注册令牌。 */
    var eventToken: Long? = null

    /** DataRequested 同步执行时捕获的技术失败。 */
    var callbackFailure: PlatformShareException? = null
        private set

    /** 是否已经释放会话。 */
    private val cleaned = AtomicBoolean(false)

    /** Share Sheet 是否已经让 owner 窗口失去焦点。 */
    private val presenterLostFocus = AtomicBoolean(false)

    /** owner 窗口焦点生命周期监听器。 */
    private val presenterFocusListener = object : WindowFocusListener {
        /** Share Sheet 关闭并把焦点还给 owner 后释放会话。 */
        override fun windowGainedFocus(event: WindowEvent) {
            if (presenterLostFocus.get()) cleanupNow()
        }

        /** 记录 Share Sheet 已从 owner 窗口取得焦点。 */
        override fun windowLostFocus(event: WindowEvent) {
            presenterLostFocus.set(true)
        }
    }

    /** 开始观察 Share Sheet owner 的焦点生命周期。 */
    fun observePresenterLifecycle() {
        host.presenterWindow.addWindowFocusListener(presenterFocusListener)
    }

    /** 使用 DataRequestedEventArgs 填充 Windows DataPackage。 */
    fun handleDataRequested(argumentsPointer: Pointer): Int {
        val result = try {
            val arguments = WinRtComPointer(argumentsPointer)
            val request = arguments.getRequestedDataRequest()
            try {
                val dataPackage = request.getRequestedDataPackage()
                try {
                    dataPackage.populate(payload)
                } finally {
                    dataPackage.release()
                }
            } finally {
                request.release()
            }
            S_OK
        } catch (exception: WinRtCallException) {
            callbackFailure = exception.toPlatformShareException(
                fallbackReason = PlatformShareFailureReason.LaunchFailed,
                message = "Windows DataRequested 回调填充分享内容失败",
            )
            exception.hresult
        } catch (exception: Throwable) {
            callbackFailure = PlatformShareException(
                reason = PlatformShareFailureReason.LaunchFailed,
                message = "Windows DataRequested 回调填充分享内容失败",
                cause = exception,
            )
            E_FAIL
        } finally {
            scheduleCleanup()
        }
        return result
    }

    /** 在当前系统消息处理完成后释放会话。 */
    private fun scheduleCleanup() {
        EventQueue.invokeLater(::cleanupNow)
    }

    /** 注销事件并释放本会话持有的全部原生资源。 */
    fun cleanupNow() {
        if (!cleaned.compareAndSet(false, true)) return
        WindowsShareSessionRegistry.remove(this)
        host.presenterWindow.removeWindowFocusListener(presenterFocusListener)
        eventToken?.let { token ->
            val result = host.removeDataRequested(token)
            if (result < 0) {
                windowsShareLogger.w {
                    "注销 Windows DataRequested 事件失败: ${result.toHResultHex()}"
                }
            }
        }
        handler.releaseInitialReferenceIfOwned()
        payload.release()
    }
}

/** 活跃 Windows 分享会话注册表。 */
private object WindowsShareSessionRegistry {
    /** HWND 到分享会话的映射。 */
    private val sessions = ConcurrentHashMap<Long, WindowsShareSession>()

    /** 注册会话；同一 HWND 已有会话时返回 false。 */
    fun register(session: WindowsShareSession): Boolean {
        val key = Pointer.nativeValue(session.windowHandle)
        return sessions.putIfAbsent(key, session) == null
    }

    /** 删除指定会话。 */
    fun remove(session: WindowsShareSession) {
        sessions.remove(Pointer.nativeValue(session.windowHandle), session)
    }

    /** 释放指定 HWND 当前仍在活动的分享会话。 */
    fun cleanup(windowHandle: Pointer) {
        sessions[Pointer.nativeValue(windowHandle)]?.cleanupNow()
    }
}

/** 释放尚未注册为会话的 Windows 分享资源。 */
private fun cleanupUnregisteredWindowsShareResources(
    handler: ComCallableObject?,
    payload: NativeWindowsSharePayload?,
) {
    handler?.releaseInitialReferenceIfOwned()
    payload?.release()
}

/** 使用原生载荷填充 Windows DataPackage。 */
private fun WinRtComPointer.populate(payload: NativeWindowsSharePayload) {
    val properties = getDataPackageProperties()
    try {
        WinRtHString(payload.title).use { title ->
            properties.invokeHResult(DATA_PACKAGE_PROPERTY_SET_TITLE_INDEX, title.handle)
                .requireWinRtSuccess("设置 Windows DataPackage 标题")
        }
    } finally {
        properties.release()
    }

    when (payload) {
        is NativeWindowsSharePayload.Text -> WinRtHString(payload.value).use { text ->
            invokeHResult(DATA_PACKAGE_SET_TEXT_INDEX, text.handle)
                .requireWinRtSuccess("设置 Windows DataPackage 文本")
        }

        is NativeWindowsSharePayload.Files -> {
            val iterable = createStorageItemIterable(payload.storageItems)
            try {
                invokeHResult(DATA_PACKAGE_SET_STORAGE_ITEMS_INDEX, iterable.pointer)
                    .requireWinRtSuccess("设置 Windows DataPackage 文件列表")
            } finally {
                iterable.releaseInitialReference()
            }
        }
    }
}

/** 从 DataRequestedEventArgs 取得 DataRequest。 */
private fun WinRtComPointer.getRequestedDataRequest(): WinRtComPointer =
    invokeObjectResult(
        methodIndex = DATA_REQUESTED_EVENT_ARGS_REQUEST_INDEX,
        operation = "读取 Windows DataRequest",
    )

/** 从 DataRequest 取得 DataPackage。 */
private fun WinRtComPointer.getRequestedDataPackage(): WinRtComPointer =
    invokeObjectResult(
        methodIndex = DATA_REQUEST_DATA_INDEX,
        operation = "读取 Windows DataPackage",
    )

/** 从 DataPackage 取得属性集合。 */
private fun WinRtComPointer.getDataPackageProperties(): WinRtComPointer =
    invokeObjectResult(
        methodIndex = DATA_PACKAGE_PROPERTIES_INDEX,
        operation = "读取 Windows DataPackage 属性",
    )

/** 为指定 HWND 取得 DataTransferManager。 */
private fun WinRtComPointer.getDataTransferManagerForWindow(windowHandle: Pointer): WinRtComPointer {
    val result = PointerByReference()
    invokeHResult(
        DATA_TRANSFER_MANAGER_INTEROP_GET_FOR_WINDOW_INDEX,
        windowHandle,
        Guid.REFIID(WindowsShareIids.DataTransferManager),
        result,
    ).requireWinRtSuccess("为 HWND 获取 Windows DataTransferManager")
    return WinRtComPointer(result.value)
}

/** 为 DataTransferManager 注册 DataRequested 事件。 */
private fun WinRtComPointer.addDataRequested(handler: ComCallableObject): Long {
    val token = LongByReference()
    invokeHResult(DATA_TRANSFER_MANAGER_ADD_DATA_REQUESTED_INDEX, handler.pointer, token)
        .requireWinRtSuccess("注册 Windows DataRequested 事件")
    return token.value
}

/** 注销 DataTransferManager 的 DataRequested 事件。 */
private fun WinRtComPointer.removeDataRequested(token: Long): Int =
    invokeHResult(DATA_TRANSFER_MANAGER_REMOVE_DATA_REQUESTED_INDEX, token)

/** 为指定 HWND 展示 Windows Share Sheet。 */
private fun WinRtComPointer.showShareUiForWindow(windowHandle: Pointer) {
    invokeHResult(DATA_TRANSFER_MANAGER_INTEROP_SHOW_FOR_WINDOW_INDEX, windowHandle)
        .requireWinRtSuccess("展示 Windows Share Sheet")
}

/** 从绝对路径异步取得 StorageFile。 */
private suspend fun WinRtComPointer.getStorageFileFromPath(path: String): WinRtComPointer {
    val operation = WinRtHString(path).use { pathString ->
        invokeObjectResult(
            methodIndex = STORAGE_FILE_STATICS_GET_FROM_PATH_INDEX,
            operation = "从路径创建 Windows StorageFile",
            pathString.handle,
        )
    }
    return operation.awaitStorageFile()
}

/** 等待 IAsyncOperation<StorageFile> 完成并取得结果。 */
private suspend fun WinRtComPointer.awaitStorageFile(): WinRtComPointer {
    val completion = CompletableDeferred<Int>()
    val handler = createAsyncStorageFileHandler(completion::complete)
    var handlerInitialReferenceOwned = true
    try {
        invokeHResult(ASYNC_OPERATION_SET_COMPLETED_INDEX, handler.pointer)
            .requireWinRtSuccess("注册 StorageFile 异步完成回调")
        handler.releaseInitialReference()
        handlerInitialReferenceOwned = false
        completion.await()
        return invokeObjectResult(
            methodIndex = ASYNC_OPERATION_GET_RESULTS_INDEX,
            operation = "读取 StorageFile 异步结果",
        )
    } finally {
        if (handlerInitialReferenceOwned) handler.releaseInitialReferenceIfOwned()
        release()
    }
}

/** 激活指定 WinRT runtime class 的接口。 */
private fun activateWinRtInterface(
    runtimeClassName: String,
    interfaceId: Guid.IID,
): WinRtComPointer = WinRtHString(runtimeClassName).use { className ->
    val result = PointerByReference()
    WindowsRuntimeApi.instance.RoGetActivationFactory(
        className.handle,
        Guid.REFIID(interfaceId),
        result,
    ).requireWinRtSuccess("激活 WinRT 接口 $runtimeClassName")
    WinRtComPointer(result.value)
}

/** 当前线程的 Windows Runtime apartment 引用。 */
private class WindowsRuntimeApartment private constructor() : AutoCloseable {
    /** 是否已经调用 RoUninitialize。 */
    private val closed = AtomicBoolean(false)

    /** 在创建 apartment 的线程上成对释放 WinRT 初始化引用。 */
    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        check(EventQueue.isDispatchThread()) { "Windows Runtime apartment 必须在 AWT EDT 上释放" }
        WindowsRuntimeApi.instance.RoUninitialize()
    }

    /** Windows Runtime apartment 工厂。 */
    companion object {
        /** 在当前 AWT EDT 上初始化单线程 Windows Runtime apartment。 */
        fun initialize(): WindowsRuntimeApartment {
            check(EventQueue.isDispatchThread()) { "Windows Runtime apartment 必须在 AWT EDT 上初始化" }
            WindowsRuntimeApi.instance.RoInitialize(RO_INIT_SINGLETHREADED)
                .requireWinRtSuccess("初始化 Windows Runtime STA")
            return WindowsRuntimeApartment()
        }
    }
}

/** Windows Runtime HSTRING 引用。 */
private class WinRtHString(value: String) : AutoCloseable {
    /** 原生 HSTRING 句柄。 */
    val handle: Pointer

    init {
        val result = PointerByReference()
        WindowsRuntimeApi.instance.WindowsCreateString(
            WString(value),
            value.length,
            result,
        ).requireWinRtSuccess("创建 Windows Runtime HSTRING")
        handle = result.value
    }

    /** 删除原生 HSTRING。 */
    override fun close() {
        WindowsRuntimeApi.instance.WindowsDeleteString(handle)
            .requireWinRtSuccess("删除 Windows Runtime HSTRING")
    }
}

/** 原生 WinRT COM 接口指针。 */
private class WinRtComPointer(val pointer: Pointer) {

    /** 调用指定 COM vtable 方法并返回 HRESULT。 */
    fun invokeHResult(methodIndex: Int, vararg arguments: Any): Int {
        val vtable = pointer.getPointer(0)
        val functionPointer = vtable.getPointer(methodIndex.toLong() * Native.POINTER_SIZE)
        val function = Function.getFunction(functionPointer, Function.ALT_CONVENTION)
        return function.invokeInt(arrayOf(pointer, *arguments))
    }

    /** 调用返回 COM 对象的 vtable 方法。 */
    fun invokeObjectResult(
        methodIndex: Int,
        operation: String,
        vararg arguments: Any,
    ): WinRtComPointer {
        val result = PointerByReference()
        invokeHResult(methodIndex, *arguments, result)
            .requireWinRtSuccess(operation)
        return WinRtComPointer(result.value)
    }

    /** 查询指定 COM 接口。 */
    fun queryInterface(interfaceId: Guid.IID): WinRtComPointer {
        val result = PointerByReference()
        invokeHResult(IUNKNOWN_QUERY_INTERFACE_INDEX, Guid.REFIID(interfaceId), result)
            .requireWinRtSuccess("查询 WinRT COM 接口 ${interfaceId.toGuidString()}")
        return WinRtComPointer(result.value)
    }

    /** 增加 COM 引用计数。 */
    fun addRef(): Int = invokeUnsignedResult(IUNKNOWN_ADD_REF_INDEX)

    /** 释放 COM 引用。 */
    fun release(): Int = invokeUnsignedResult(IUNKNOWN_RELEASE_INDEX)

    /** 调用返回 ULONG 的 COM vtable 方法。 */
    private fun invokeUnsignedResult(methodIndex: Int): Int {
        val vtable = pointer.getPointer(0)
        val functionPointer = vtable.getPointer(methodIndex.toLong() * Native.POINTER_SIZE)
        val function = Function.getFunction(functionPointer, Function.ALT_CONVENTION)
        return function.invokeInt(arrayOf(pointer))
    }
}

/** 可由原生 WinRT 调用的 Java COM 对象。 */
private class ComCallableObject(
    private val primaryInterfaceId: Guid.IID,
    private val inspectable: Boolean,
    methodCallbacks: List<Callback>,
) {
    /** COM 引用计数。 */
    private val referenceCount = AtomicInteger(1)

    /** 是否仍持有创建方的初始引用。 */
    private val initialReferenceOwned = AtomicBoolean(true)

    /** 防止 JNA callback 被垃圾回收的强引用。 */
    private val callbacks: List<Callback>

    /** COM vtable 内存。 */
    private val vtable: Memory

    /** COM 对象地址。 */
    val pointer: Pointer

    init {
        val baseCallbacks = buildBaseCallbacks()
        callbacks = baseCallbacks + methodCallbacks
        vtable = Memory(callbacks.size.toLong() * Native.POINTER_SIZE)
        callbacks.forEachIndexed { index, callback ->
            vtable.setPointer(
                index.toLong() * Native.POINTER_SIZE,
                CallbackReference.getFunctionPointer(callback),
            )
        }
        pointer = Memory(Native.POINTER_SIZE.toLong()).apply {
            setPointer(0, vtable)
        }
        liveObjects[Pointer.nativeValue(pointer)] = this
    }

    /** 释放创建方初始持有的 COM 引用。 */
    fun releaseInitialReference() {
        check(initialReferenceOwned.compareAndSet(true, false)) { "COM 初始引用已经释放" }
        releaseReference()
    }

    /** 初始引用仍由创建方持有时释放它。 */
    fun releaseInitialReferenceIfOwned() {
        if (initialReferenceOwned.compareAndSet(true, false)) releaseReference()
    }

    /** 创建 IUnknown 或 IInspectable 基础 vtable 回调。 */
    private fun buildBaseCallbacks(): List<Callback> {
        val queryInterface = QueryInterfaceCallback { _, requestedIdPointer, result ->
            queryInterface(requestedIdPointer, result)
        }
        val addRef = ReferenceCountCallback { _ -> addReference() }
        val release = ReferenceCountCallback { _ -> releaseReference() }
        if (!inspectable) return listOf(queryInterface, addRef, release)

        val getIids = GetIidsCallback { _, count, result -> getInterfaceIds(count, result) }
        val getRuntimeClassName = ObjectOutCallback { _, result ->
            result.value = Pointer.NULL
            S_OK
        }
        val getTrustLevel = GetTrustLevelCallback { _, result ->
            result.value = BASE_TRUST
            S_OK
        }
        return listOf(
            queryInterface,
            addRef,
            release,
            getIids,
            getRuntimeClassName,
            getTrustLevel,
        )
    }

    /** 响应 COM QueryInterface。 */
    private fun queryInterface(requestedIdPointer: Pointer, result: PointerByReference): Int {
        val requestedId = Guid.GUID(requestedIdPointer)
        val supported = requestedId.hasSameWindowsShareGuidValue(primaryInterfaceId) ||
            requestedId.hasSameWindowsShareGuidValue(WindowsShareIids.Unknown) ||
            requestedId.hasSameWindowsShareGuidValue(WindowsShareIids.AgileObject) ||
            inspectable && requestedId.hasSameWindowsShareGuidValue(WindowsShareIids.Inspectable)
        if (!supported) {
            result.value = Pointer.NULL
            return E_NOINTERFACE
        }
        result.value = pointer
        addReference()
        return S_OK
    }

    /** 返回对象实现的主要 WinRT 接口 IID。 */
    private fun getInterfaceIds(count: IntByReference, result: PointerByReference): Int {
        primaryInterfaceId.write()
        val interfaceIdBytes = primaryInterfaceId.pointer.getByteArray(
            0,
            primaryInterfaceId.size(),
        )
        val memory = Ole32.INSTANCE.CoTaskMemAlloc(interfaceIdBytes.size.toLong())
            ?: return E_OUTOFMEMORY
        memory.write(0, interfaceIdBytes, 0, interfaceIdBytes.size)
        count.value = 1
        result.value = memory
        return S_OK
    }

    /** 增加本对象 COM 引用计数。 */
    private fun addReference(): Int = referenceCount.incrementAndGet()

    /** 减少本对象 COM 引用计数并在归零后解除 Java 强引用。 */
    private fun releaseReference(): Int {
        val remaining = referenceCount.decrementAndGet()
        check(remaining >= 0) { "COM 引用计数不能小于零" }
        if (remaining == 0) liveObjects.remove(Pointer.nativeValue(pointer), this)
        return remaining
    }

    /** 活跃 Java COM 对象注册表。 */
    companion object {
        /** 原生仍持有引用的 Java COM 对象。 */
        private val liveObjects = ConcurrentHashMap<Long, ComCallableObject>()
    }
}

/**
 * 比较两个 Windows GUID 的字段值，忽略 JNA `GUID` 与 `IID` 子类差异。
 *
 * JNA 的 `GUID.equals` 要求两侧运行时类型完全一致，不能直接用于 COM QueryInterface。
 */
internal fun Guid.GUID.hasSameWindowsShareGuidValue(other: Guid.GUID): Boolean =
    Data1 == other.Data1 &&
        Data2 == other.Data2 &&
        Data3 == other.Data3 &&
        Data4.contentEquals(other.Data4)

/** 创建 DataRequested 事件处理器。 */
private fun createDataRequestedHandler(onInvoke: (Pointer) -> Int): ComCallableObject {
    val invoke = EventInvokeCallback { _, _, arguments -> onInvoke(arguments) }
    return ComCallableObject(
        primaryInterfaceId = WindowsShareIids.DataRequestedHandler,
        inspectable = false,
        methodCallbacks = listOf(invoke),
    )
}

/** 创建 StorageFile 异步完成处理器。 */
private fun createAsyncStorageFileHandler(onComplete: (Int) -> Unit): ComCallableObject {
    val invoke = AsyncInvokeCallback { _, _, status ->
        onComplete(status)
        S_OK
    }
    return ComCallableObject(
        primaryInterfaceId = WindowsShareIids.StorageFileAsyncCompletedHandler,
        inspectable = false,
        methodCallbacks = listOf(invoke),
    )
}

/** 创建 IStorageItem iterable。 */
private fun createStorageItemIterable(items: List<WinRtComPointer>): ComCallableObject {
    val first = ObjectOutCallback { _, result ->
        val iterator = createStorageItemIterator(items)
        result.value = iterator.pointer
        S_OK
    }
    return ComCallableObject(
        primaryInterfaceId = WindowsShareIids.StorageItemIterable,
        inspectable = true,
        methodCallbacks = listOf(first),
    )
}

/** 创建 IStorageItem iterator。 */
private fun createStorageItemIterator(items: List<WinRtComPointer>): ComCallableObject {
    val index = AtomicInteger(0)
    val current = ObjectOutCallback { _, result ->
        val item = items.getOrNull(index.get())
        if (item == null) {
            result.value = Pointer.NULL
            E_BOUNDS
        } else {
            item.addRef()
            result.value = item.pointer
            S_OK
        }
    }
    val hasCurrent = BooleanOutCallback { _, result ->
        result.setByte(0, if (index.get() < items.size) 1 else 0)
        S_OK
    }
    val moveNext = BooleanOutCallback { _, result ->
        val nextIndex = index.incrementAndGet()
        result.setByte(0, if (nextIndex < items.size) 1 else 0)
        S_OK
    }
    val getMany = IteratorGetManyCallback { _, capacity, result, actual ->
        var copied = 0
        while (copied < capacity && index.get() < items.size) {
            val item = items[index.getAndIncrement()]
            item.addRef()
            result.setPointer(copied.toLong() * Native.POINTER_SIZE, item.pointer)
            copied++
        }
        actual.value = copied
        S_OK
    }
    return ComCallableObject(
        primaryInterfaceId = WindowsShareIids.StorageItemIterator,
        inspectable = true,
        methodCallbacks = listOf(current, hasCurrent, moveNext, getMany),
    )
}

/** COM QueryInterface 回调。 */
private fun interface QueryInterfaceCallback : StdCallCallback {
    /** 查询接口。 */
    fun invoke(self: Pointer, requestedId: Pointer, result: PointerByReference): Int
}

/** COM AddRef 或 Release 回调。 */
private fun interface ReferenceCountCallback : StdCallCallback {
    /** 更新引用计数。 */
    fun invoke(self: Pointer): Int
}

/** IInspectable.GetIids 回调。 */
private fun interface GetIidsCallback : StdCallCallback {
    /** 返回接口 IID 列表。 */
    fun invoke(self: Pointer, count: IntByReference, result: PointerByReference): Int
}

/** 返回 COM 对象指针的回调。 */
private fun interface ObjectOutCallback : StdCallCallback {
    /** 写入结果对象。 */
    fun invoke(self: Pointer, result: PointerByReference): Int
}

/** IInspectable.GetTrustLevel 回调。 */
private fun interface GetTrustLevelCallback : StdCallCallback {
    /** 返回信任级别。 */
    fun invoke(self: Pointer, result: IntByReference): Int
}

/** TypedEventHandler<DataTransferManager, DataRequestedEventArgs> 回调。 */
private fun interface EventInvokeCallback : StdCallCallback {
    /** 处理 DataRequested 事件。 */
    fun invoke(self: Pointer, sender: Pointer, arguments: Pointer): Int
}

/** AsyncOperationCompletedHandler<StorageFile> 回调。 */
private fun interface AsyncInvokeCallback : StdCallCallback {
    /** 处理 StorageFile 异步完成事件。 */
    fun invoke(self: Pointer, operation: Pointer, status: Int): Int
}

/** 返回 WinRT boolean 的回调。 */
private fun interface BooleanOutCallback : StdCallCallback {
    /** 写入 boolean 结果。 */
    fun invoke(self: Pointer, result: Pointer): Int
}

/** IIterator<IStorageItem>.GetMany 回调。 */
private fun interface IteratorGetManyCallback : StdCallCallback {
    /** 批量写入存储项。 */
    fun invoke(
        self: Pointer,
        capacity: Int,
        result: Pointer,
        actual: IntByReference,
    ): Int
}

/** combase.dll 中使用的 Windows Runtime API。 */
private interface WindowsRuntimeApi : StdCallLibrary {
    /** 初始化当前线程的 Windows Runtime apartment。 */
    fun RoInitialize(initType: Int): Int

    /** 释放当前线程的 Windows Runtime apartment。 */
    fun RoUninitialize()

    /** 创建 HSTRING。 */
    fun WindowsCreateString(
        sourceString: WString,
        length: Int,
        string: PointerByReference,
    ): Int

    /** 删除 HSTRING。 */
    fun WindowsDeleteString(string: Pointer): Int

    /** 取得 WinRT activation factory。 */
    fun RoGetActivationFactory(
        activatableClassId: Pointer,
        interfaceId: Guid.REFIID,
        factory: PointerByReference,
    ): Int

    /** Windows Runtime API 单例。 */
    companion object {
        /** 延迟加载 combase.dll，避免非 Windows Desktop 初始化失败。 */
        val instance: WindowsRuntimeApi by lazy {
            Native.load(
                "combase",
                WindowsRuntimeApi::class.java,
                W32APIOptions.DEFAULT_OPTIONS,
            )
        }
    }
}

/** WinRT COM 调用失败。 */
private class WinRtCallException(
    /** 原始 HRESULT。 */
    val hresult: Int,
    operation: String,
) : RuntimeException("$operation 失败: ${hresult.toHResultHex()}")

/** 确认 HRESULT 成功，否则抛出原生调用异常。 */
private fun Int.requireWinRtSuccess(operation: String) {
    if (this < 0) throw WinRtCallException(this, operation)
}

/** 将 WinRT 调用失败转换为公共技术异常。 */
private fun WinRtCallException.toPlatformShareException(
    fallbackReason: PlatformShareFailureReason,
    message: String,
): PlatformShareException = PlatformShareException(
    reason = windowsShareFailureReasonForHResult(
        hresult = hresult,
        fallbackReason = fallbackReason,
    ),
    message = "$message（${this.message}）",
    cause = this,
)

/** 根据 HRESULT 选择稳定的系统分享技术原因。 */
internal fun windowsShareFailureReasonForHResult(
    hresult: Int,
    fallbackReason: PlatformShareFailureReason,
): PlatformShareFailureReason = when (hresult) {
    E_ACCESSDENIED -> PlatformShareFailureReason.PermissionDenied
    else -> fallbackReason
}

/** 判断 HRESULT 是否表示当前 Windows 没有 Share Contract。 */
internal fun isUnsupportedWindowsShareHResult(hresult: Int): Boolean =
    hresult == REGDB_E_CLASSNOTREG ||
        hresult == E_NOINTERFACE ||
        hresult == HRESULT_ERROR_NOT_SUPPORTED

/** 将 HRESULT 格式化为固定宽度十六进制。 */
private fun Int.toHResultHex(): String = "0x${toUInt().toString(16).padStart(8, '0')}"

/** Windows 系统分享所需 IID。 */
private object WindowsShareIids {
    /** IUnknown IID。 */
    val Unknown = Guid.IID("{00000000-0000-0000-C000-000000000046}")

    /** IInspectable IID。 */
    val Inspectable = Guid.IID("{AF86E2E0-B12D-4C6A-9C5A-D7AA65101E90}")

    /** IAgileObject IID。 */
    val AgileObject = Guid.IID("{94EA2B94-E9CC-49E0-C0FF-EE64CA8F5B90}")

    /** IDataTransferManagerInterop IID。 */
    val DataTransferManagerInterop = Guid.IID("{3A3DCD6C-3EAB-43DC-BCDE-45671CE800C8}")

    /** IDataTransferManager IID。 */
    val DataTransferManager = Guid.IID("{A5CAEE9B-8708-49D1-8D36-67D25A8DA00C}")

    /** TypedEventHandler<DataTransferManager, DataRequestedEventArgs> IID。 */
    val DataRequestedHandler = Guid.IID("{EC6F9CC8-46D0-5E0E-B4D2-7D7773AE37A0}")

    /** IStorageFileStatics IID。 */
    val StorageFileStatics = Guid.IID("{5984C710-DAF2-43C8-8BB4-A4D3EACFD03F}")

    /** IStorageItem IID。 */
    val StorageItem = Guid.IID("{4207A996-CA2F-42F7-BDE8-8B10457A7F30}")

    /** AsyncOperationCompletedHandler<StorageFile> IID。 */
    val StorageFileAsyncCompletedHandler = Guid.IID("{E521C894-2C26-5946-9E61-2B5E188D01ED}")

    /** IIterable<IStorageItem> IID。 */
    val StorageItemIterable = Guid.IID("{BB8B8418-65D1-544B-B083-6D172F568C73}")

    /** IIterator<IStorageItem> IID。 */
    val StorageItemIterator = Guid.IID("{05B487C2-3830-5D3C-98DA-25FA11542DBD}")
}

/** DataTransferManager WinRT runtime class。 */
private const val DATA_TRANSFER_MANAGER_RUNTIME_CLASS: String =
    "Windows.ApplicationModel.DataTransfer.DataTransferManager"

/** StorageFile WinRT runtime class。 */
private const val STORAGE_FILE_RUNTIME_CLASS: String = "Windows.Storage.StorageFile"

/** RoInitialize 单线程 apartment 参数。 */
private const val RO_INIT_SINGLETHREADED: Int = 0

/** COM 成功 HRESULT。 */
private const val S_OK: Int = 0

/** COM 通用失败 HRESULT。 */
private const val E_FAIL: Int = -2147467259

/** COM 不支持接口 HRESULT。 */
private const val E_NOINTERFACE: Int = -2147467262

/** COM 内存不足 HRESULT。 */
private const val E_OUTOFMEMORY: Int = -2147024882

/** WinRT iterator 越界 HRESULT。 */
private const val E_BOUNDS: Int = -2147483637

/** Windows 访问拒绝 HRESULT。 */
private const val E_ACCESSDENIED: Int = -2147024891

/** WinRT class 未注册 HRESULT。 */
private const val REGDB_E_CLASSNOTREG: Int = -2147221164

/** Win32 ERROR_NOT_SUPPORTED 转换后的 HRESULT。 */
private const val HRESULT_ERROR_NOT_SUPPORTED: Int = -2147024846

/** IInspectable BaseTrust。 */
private const val BASE_TRUST: Int = 0

/** IUnknown.QueryInterface vtable 索引。 */
private const val IUNKNOWN_QUERY_INTERFACE_INDEX: Int = 0

/** IUnknown.AddRef vtable 索引。 */
private const val IUNKNOWN_ADD_REF_INDEX: Int = 1

/** IUnknown.Release vtable 索引。 */
private const val IUNKNOWN_RELEASE_INDEX: Int = 2

/** IDataTransferManagerInterop.GetForWindow vtable 索引。 */
private const val DATA_TRANSFER_MANAGER_INTEROP_GET_FOR_WINDOW_INDEX: Int = 3

/** IDataTransferManagerInterop.ShowShareUIForWindow vtable 索引。 */
private const val DATA_TRANSFER_MANAGER_INTEROP_SHOW_FOR_WINDOW_INDEX: Int = 4

/** IDataTransferManager.add_DataRequested vtable 索引。 */
private const val DATA_TRANSFER_MANAGER_ADD_DATA_REQUESTED_INDEX: Int = 6

/** IDataTransferManager.remove_DataRequested vtable 索引。 */
private const val DATA_TRANSFER_MANAGER_REMOVE_DATA_REQUESTED_INDEX: Int = 7

/** IDataRequestedEventArgs.get_Request vtable 索引。 */
private const val DATA_REQUESTED_EVENT_ARGS_REQUEST_INDEX: Int = 6

/** IDataRequest.get_Data vtable 索引。 */
private const val DATA_REQUEST_DATA_INDEX: Int = 6

/** IDataPackage.get_Properties vtable 索引。 */
private const val DATA_PACKAGE_PROPERTIES_INDEX: Int = 7

/** IDataPackage.SetText vtable 索引。 */
private const val DATA_PACKAGE_SET_TEXT_INDEX: Int = 16

/** IDataPackage.SetStorageItems 只读重载 vtable 索引。 */
private const val DATA_PACKAGE_SET_STORAGE_ITEMS_INDEX: Int = 22

/** IDataPackagePropertySet.put_Title vtable 索引。 */
private const val DATA_PACKAGE_PROPERTY_SET_TITLE_INDEX: Int = 7

/** IStorageFileStatics.GetFileFromPathAsync vtable 索引。 */
private const val STORAGE_FILE_STATICS_GET_FROM_PATH_INDEX: Int = 6

/** IAsyncOperation<StorageFile>.put_Completed vtable 索引。 */
private const val ASYNC_OPERATION_SET_COMPLETED_INDEX: Int = 6

/** IAsyncOperation<StorageFile>.GetResults vtable 索引。 */
private const val ASYNC_OPERATION_GET_RESULTS_INDEX: Int = 8
