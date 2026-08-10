package ciyin.platform.win32

import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Windows exe 图标提取器回归测试。 */
internal class ExeIconExtractorTest {

    /** 提取器应创建目标父目录，并写出缓存实际使用的高分辨率 PNG 文件。 */
    @Test
    fun extractIconCreatesParentDirectoryAndPngFile() {
        val root = createTempDirectory("desktool-exe-icon-")
        try {
            val executable = File(System.getProperty("java.home"), "bin/java.exe")
            val output = root.resolve("nested/cache/java.png").toFile()

            ExeIconExtractor.extractExeIcon(
                executablePath = executable.absolutePath,
                outputFile = output,
                size = 256,
            )

            assertTrue(output.isFile)
            val image = ImageIO.read(output)
            assertEquals(256, image.width)
            assertEquals(256, image.height)
            assertTrue(
                (0 until image.height).any { y ->
                    (0 until image.width).any { x ->
                        val argb = image.getRGB(x, y)
                        (argb ushr 24) > 0 && (argb and 0x00FFFFFF) != 0
                    }
                },
                "提取后的 PNG 必须包含可见且非黑的图标像素",
            )
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}
