package ciyin.platform.win32

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.WString
import com.sun.jna.platform.win32.GDI32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HICON
import com.sun.jna.platform.win32.WinGDI
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon

object ExeIconExtractor {

    // JNA 声明 Shell32
    interface Shell32 : StdCallLibrary {
        fun ExtractIconExW(
            lpszFile: WString?,
            nIconIndex: Int,
            phiconLarge: Array<HICON?>?,
            phiconSmall: Array<HICON?>?,
            nIcons: Int
        ): Int

        companion object {
            val INSTANCE: Shell32 =
                Native.load<Shell32>("shell32", Shell32::class.java, W32APIOptions.DEFAULT_OPTIONS)
        }
    }

    private fun hIconToImage(hIcon: HICON, width: Int, height: Int): BufferedImage {
        val iconInfo = WinGDI.ICONINFO()
        if (!User32.INSTANCE.GetIconInfo(hIcon, iconInfo)) {
            throw RuntimeException("GetIconInfo failed")
        }

        val hdc = User32.INSTANCE.GetDC(null)
        val bmp = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) // 假设32x32
        val pixels = (bmp.raster.dataBuffer as DataBufferInt).data

        val memDC = GDI32.INSTANCE.CreateCompatibleDC(hdc)
        GDI32.INSTANCE.SelectObject(memDC, iconInfo.hbmColor)

        val bmi = WinGDI.BITMAPINFO()
        bmi.bmiHeader.biSize = bmi.bmiHeader.size()
        bmi.bmiHeader.biWidth = bmp.width
        bmi.bmiHeader.biHeight = -bmp.height
        bmi.bmiHeader.biPlanes = 1
        bmi.bmiHeader.biBitCount = 32
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB

        // 用 JNA Memory 来存像素数据
        val bufferSize = bmp.width * bmp.height * 4L // 4字节/像素
        val mem = Memory(bufferSize)

        GDI32.INSTANCE.GetDIBits(
            memDC,
            iconInfo.hbmColor,
            0,
            bmp.height,
            mem,  // 这里传 Memory 而不是 IntArray
            bmi,
            WinGDI.DIB_RGB_COLORS
        )

        // 把 Memory 里的数据拷到 pixels 数组
        mem.read(0, pixels, 0, pixels.size)

        GDI32.INSTANCE.DeleteDC(memDC)
        User32.INSTANCE.ReleaseDC(null, hdc)

        return bmp
    }


    /** 将 HICON 转成 BufferedImage，可指定尺寸 */
    private fun hIconToBufferedImage(
        hIcon: HICON,
        width: Int = 32,
        height: Int = 32
    ): BufferedImage {
        val icon = ImageIcon(hIconToImage(hIcon, width, height))// 转成 AWT Image
        val img = BufferedImage(icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.drawImage(icon.image, 0, 0, null)
        g.dispose()
        return img
    }

    /** 提取 exe 主图标句柄 */
    private fun extractExeIconHandle(exePath: String): HICON? {
        val large = arrayOfNulls<HICON>(1)
        val small = arrayOfNulls<HICON>(1)

        val count = Shell32.INSTANCE.ExtractIconExW(WString(exePath), 0, large, small, 1)
        if (count > 0 && large[0] != null) {
            return large[0]
        }
        return null
    }

    /** 根据 exe 文件生成图标输出路径 */
    private fun getIconOutputFile(
        exePath: String,
        outputDir: File,
        suffix: String = "_icon"
    ): File {
        val exeName = File(exePath).nameWithoutExtension
        return File(outputDir, "${exeName}${suffix}.png")
    }

    /**
     * 提取 exe 图标并保存
     * @param exePath exe 路径
     * @param outputDir 输出目录
     * @param sizes 图标尺寸列表，例如 listOf(16,32,64)
     * @return 保存的文件列表
     */
    fun extractAndSaveExeIcons(
        exePath: String,
        outputDir: File,
        sizes: List<Int> = listOf(16, 32, 64, 256)
    ): List<File> {
        val hIcon = extractExeIconHandle(exePath) ?: return emptyList()
        val files = mutableListOf<File>()

        for (size in sizes) {
            val image = hIconToBufferedImage(hIcon, size, size)
            val outFile = getIconOutputFile(exePath, outputDir, "_icon_${size}")
            ImageIO.write(image, "png", outFile)
            files += outFile
        }
        return files
    }

    fun extractExeIcon(exePath: String, outputIcoPath: File, size: Int = 32) {

        // 准备 HICON 数组
        val large = arrayOfNulls<HICON>(1)
        val small = arrayOfNulls<HICON>(1)

        // 提取图标
        val count = Shell32.INSTANCE.ExtractIconExW(
            WString(exePath),
            0, // 图标索引
            large,
            small,
            1 // 提取数量
        )
        if (count > 0 && large[0] != null) {
            val image = hIconToBufferedImage(large[0]!!, size, size)
            outputIcoPath.mkdirs()
            ImageIO.write(image, "png", outputIcoPath) // 这里保存成 PNG
            println("已提取图标到: $outputIcoPath")
        } else {
            println("未找到图标")
        }
    }

}