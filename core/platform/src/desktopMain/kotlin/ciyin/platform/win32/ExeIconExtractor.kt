package ciyin.platform.win32

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.WString
import com.sun.jna.platform.win32.GDI32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HBITMAP
import com.sun.jna.platform.win32.WinDef.HICON
import com.sun.jna.platform.win32.WinGDI
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.absoluteValue

/**
 * Windows 可执行文件图标提取器。
 *
 * 所有由 Win32 API 返回的图标、位图和设备上下文句柄都会在当前调用内释放。
 */
object ExeIconExtractor {

    /** Shell32 图标提取接口。 */
    private interface Shell32 : StdCallLibrary {

        /** 按指定像素尺寸提取最匹配的图标句柄。 */
        fun SHDefExtractIconW(
            pszIconFile: WString?,
            iIndex: Int,
            uFlags: Int,
            phiconLarge: Array<HICON?>?,
            phiconSmall: Array<HICON?>?,
            nIconSize: Int,
        ): Int

        /** 从指定文件提取大图标和小图标句柄。 */
        fun ExtractIconExW(
            lpszFile: WString?,
            nIconIndex: Int,
            phiconLarge: Array<HICON?>?,
            phiconSmall: Array<HICON?>?,
            nIcons: Int,
        ): Int

        /** Shell32 接口实例。 */
        companion object {
            /** 采用 Unicode Win32 选项加载的 Shell32。 */
            val INSTANCE: Shell32 = Native.load(
                "shell32",
                Shell32::class.java,
                W32APIOptions.UNICODE_OPTIONS,
            )
        }
    }

    /**
     * 将图标句柄转换为指定尺寸的 ARGB 图片。
     *
     * @param hIcon 图标句柄
     * @param width 目标宽度
     * @param height 目标高度
     */
    private fun hIconToBufferedImage(
        hIcon: HICON,
        width: Int,
        height: Int,
    ): BufferedImage {
        require(width > 0 && height > 0) { "图标尺寸必须大于 0" }

        val iconInfo = WinGDI.ICONINFO()
        check(User32.INSTANCE.GetIconInfo(hIcon, iconInfo)) { "GetIconInfo 调用失败" }

        try {
            val colorBitmap = iconInfo.hbmColor
                ?: error("图标不包含可读取的彩色位图")
            val rawImage = readBitmap(colorBitmap)
            val sourceImage = if (rawImage.hasVisibleAlpha()) {
                rawImage
            } else {
                rawImage.applyMaskAlpha(iconInfo.hbmMask)
            }
            if (sourceImage.width == width && sourceImage.height == height) {
                return sourceImage
            }

            return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also { target ->
                val graphics = target.createGraphics()
                try {
                    graphics.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                    )
                    graphics.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY,
                    )
                    graphics.drawImage(sourceImage, 0, 0, width, height, null)
                } finally {
                    graphics.dispose()
                }
            }
        } finally {
            deleteBitmap(iconInfo.hbmColor)
            deleteBitmap(iconInfo.hbmMask)
        }
    }

    /**
     * 读取 Win32 位图中的 ARGB 像素。
     *
     * @param bitmapHandle 位图句柄
     */
    private fun readBitmap(bitmapHandle: HBITMAP): BufferedImage {
        val bitmap = WinGDI.BITMAP()
        check(
            GDI32.INSTANCE.GetObject(
                bitmapHandle,
                bitmap.size(),
                bitmap.pointer,
            ) > 0,
        ) { "GetObject 调用失败" }
        bitmap.read()

        val width = bitmap.bmWidth.toInt().absoluteValue
        val height = bitmap.bmHeight.toInt().absoluteValue
        check(width > 0 && height > 0) { "图标位图尺寸无效" }

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val pixels = (image.raster.dataBuffer as DataBufferInt).data
        val bitmapInfo = WinGDI.BITMAPINFO().apply {
            bmiHeader.biSize = bmiHeader.size()
            bmiHeader.biWidth = width
            bmiHeader.biHeight = -height
            bmiHeader.biPlanes = 1
            bmiHeader.biBitCount = 32
            bmiHeader.biCompression = WinGDI.BI_RGB
        }
        val pixelMemory = Memory(width.toLong() * height.toLong() * Int.SIZE_BYTES)
        val deviceContext = User32.INSTANCE.GetDC(null)
            ?: error("GetDC 调用失败")

        try {
            val copiedLines = GDI32.INSTANCE.GetDIBits(
                deviceContext,
                bitmapHandle,
                0,
                height,
                pixelMemory,
                bitmapInfo,
                WinGDI.DIB_RGB_COLORS,
            )
            check(copiedLines == height) { "GetDIBits 只读取了 $copiedLines/$height 行" }
            pixelMemory.read(0, pixels, 0, pixels.size)
        } finally {
            User32.INSTANCE.ReleaseDC(null, deviceContext)
        }

        return image
    }

    /** 判断图像是否包含 Win32 可直接使用的 Alpha 通道。 */
    private fun BufferedImage.hasVisibleAlpha(): Boolean =
        (raster.dataBuffer as DataBufferInt).data.any { argb -> argb ushr ALPHA_SHIFT > 0 }

    /**
     * 使用 HICON 的掩码位图补全缺失的 Alpha 通道。
     *
     * @param maskHandle 图标掩码位图句柄
     * @return 带有有效透明度的新图像
     */
    private fun BufferedImage.applyMaskAlpha(maskHandle: HBITMAP?): BufferedImage {
        val mask = maskHandle?.let(::readBitmap) ?: return this
        return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also { target ->
            for (y in 0 until height) {
                val maskY = y * mask.height / height
                for (x in 0 until width) {
                    val maskX = x * mask.width / width
                    val alpha = if (mask.getRGB(maskX, maskY) and RGB_MASK == 0) {
                        OPAQUE_ALPHA
                    } else {
                        0
                    }
                    target.setRGB(x, y, alpha shl ALPHA_SHIFT or (getRGB(x, y) and RGB_MASK))
                }
            }
        }
    }

    /**
     * 释放由 GetIconInfo 返回的位图句柄。
     *
     * @param bitmapHandle 可空位图句柄
     */
    private fun deleteBitmap(bitmapHandle: HBITMAP?) {
        if (bitmapHandle != null) {
            GDI32.INSTANCE.DeleteObject(bitmapHandle)
        }
    }

    /**
     * 提取文件的首个可用图标句柄。
     *
     * 调用方负责通过 DestroyIcon 释放返回值。
     *
     * @param executablePath 可执行文件路径
     */
    private fun extractLegacyExeIconHandle(executablePath: String): HICON? {
        val largeIcons = arrayOfNulls<HICON>(1)
        val smallIcons = arrayOfNulls<HICON>(1)
        val count = Shell32.INSTANCE.ExtractIconExW(
            WString(executablePath),
            0,
            largeIcons,
            smallIcons,
            1,
        )
        if (count <= 0) {
            return null
        }

        val selectedIcon = largeIcons[0] ?: smallIcons[0]
        largeIcons[0]
            ?.takeUnless { it == selectedIcon }
            ?.let(User32.INSTANCE::DestroyIcon)
        smallIcons[0]
            ?.takeUnless { it == selectedIcon }
            ?.let(User32.INSTANCE::DestroyIcon)
        return selectedIcon
    }

    /**
     * 按目标尺寸提取首个可用图标句柄。
     *
     * 优先请求资源中最匹配的原始尺寸，仅在 Shell 尺寸提取失败时回退系统大图标。
     * 调用方负责通过 DestroyIcon 释放返回值。
     *
     * @param executablePath 可执行文件或图标文件路径
     * @param size 目标像素尺寸
     */
    private fun extractExeIconHandle(
        executablePath: String,
        size: Int,
    ): HICON? {
        val sizedIcons = arrayOfNulls<HICON>(1)
        val result = Shell32.INSTANCE.SHDefExtractIconW(
            WString(executablePath),
            0,
            0,
            sizedIcons,
            null,
            size and MAX_ICON_SIZE,
        )
        if (result == S_OK && sizedIcons[0] != null) {
            return sizedIcons[0]
        }
        sizedIcons[0]?.let(User32.INSTANCE::DestroyIcon)
        return extractLegacyExeIconHandle(executablePath)
    }

    /**
     * 根据可执行文件生成图标输出路径。
     *
     * @param executablePath 可执行文件路径
     * @param outputDirectory 输出目录
     * @param suffix 文件名后缀
     */
    private fun getIconOutputFile(
        executablePath: String,
        outputDirectory: File,
        suffix: String,
    ): File {
        val executableName = File(executablePath).nameWithoutExtension
        return File(outputDirectory, "$executableName$suffix.png")
    }

    /**
     * 提取可执行文件图标并按多个尺寸保存为 PNG。
     *
     * @param executablePath 可执行文件路径
     * @param outputDirectory 输出目录
     * @param sizes 需要输出的正整数尺寸
     * @return 已写入的 PNG 文件
     */
    fun extractAndSaveExeIcons(
        executablePath: String,
        outputDirectory: File,
        sizes: List<Int> = listOf(16, 32, 64, 256),
    ): List<File> {
        require(sizes.all { it in 1..MAX_ICON_SIZE }) {
            "图标尺寸必须位于 1..$MAX_ICON_SIZE"
        }
        requireDirectory(outputDirectory)

        return sizes.distinct().mapNotNull { size ->
            val iconHandle = extractExeIconHandle(executablePath, size)
                ?: return@mapNotNull null
            try {
                val image = hIconToBufferedImage(iconHandle, size, size)
                val outputFile = getIconOutputFile(
                    executablePath = executablePath,
                    outputDirectory = outputDirectory,
                    suffix = "_icon_$size",
                )
                check(ImageIO.write(image, "png", outputFile)) {
                    "当前运行环境没有可用的 PNG 写入器"
                }
                outputFile
            } finally {
                User32.INSTANCE.DestroyIcon(iconHandle)
            }
        }
    }

    /**
     * 提取可执行文件图标并保存为 PNG。
     *
     * @param executablePath 可执行文件路径
     * @param outputFile 目标 PNG 文件
     * @param size 目标正整数尺寸
     */
    fun extractExeIcon(
        executablePath: String,
        outputFile: File,
        size: Int = 32,
    ) {
        require(size in 1..MAX_ICON_SIZE) { "图标尺寸必须位于 1..$MAX_ICON_SIZE" }
        val iconHandle = checkNotNull(extractExeIconHandle(executablePath, size)) {
            "未在 $executablePath 中找到图标"
        }

        try {
            val parent = requireNotNull(outputFile.absoluteFile.parentFile) {
                "图标输出文件必须具有父目录：$outputFile"
            }
            requireDirectory(parent)
            val image = hIconToBufferedImage(iconHandle, size, size)
            check(ImageIO.write(image, "png", outputFile)) {
                "当前运行环境没有可用的 PNG 写入器"
            }
        } finally {
            User32.INSTANCE.DestroyIcon(iconHandle)
        }
    }

    /** 确保图标输出目录存在且可写入。 */
    private fun requireDirectory(directory: File) {
        check(directory.isDirectory || directory.mkdirs()) {
            "无法创建图标输出目录：${directory.absolutePath}"
        }
        check(directory.canWrite()) {
            "图标输出目录不可写：${directory.absolutePath}"
        }
    }

    /** 成功的 HRESULT。 */
    private const val S_OK: Int = 0

    /** Win32 图标尺寸参数可表达的最大值。 */
    private const val MAX_ICON_SIZE: Int = 0xFFFF

    /** ARGB Alpha 通道位移。 */
    private const val ALPHA_SHIFT: Int = 24

    /** 完全不透明的 Alpha 值。 */
    private const val OPAQUE_ALPHA: Int = 0xFF

    /** ARGB 中的 RGB 通道掩码。 */
    private const val RGB_MASK: Int = 0x00FFFFFF
}
