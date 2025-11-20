package ciyin.system.utils.image

import ciyin.io.File
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.files.Blob

internal actual fun resizeImageFile(
    utils: ImageUtils,
    input: File,
    output: File,
    width: Int,
    height: Int,
    format: String
) {
    val img = document.createElement("img") as HTMLImageElement
    img.src = input.absolutePath

    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height

    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    ctx.drawImage(img, 0.0, 0.0, width.toDouble(), height.toDouble())

    canvas.toBlob({ blob: Blob? ->
        blob?.let {
            window.fetch(output, js("{ method: 'POST', body: it }"))
        }
    }, "image/png")
}