package ciyin.system.utils.image

import ciyin.io.File
import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

internal actual fun resizeImageFile(
    utils: ImageUtils,
    input: File,
    output: File,
    width: Int,
    height: Int,
    format: String,
) {
    val original = ImageIO.read(java.io.File(input.absolutePath))
    val scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH)
    val buffered = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g = buffered.createGraphics()
    g.drawImage(scaled, 0, 0, null)
    g.dispose()
    ImageIO.write(buffered, format.lowercase(), java.io.File(output.absolutePath))
}