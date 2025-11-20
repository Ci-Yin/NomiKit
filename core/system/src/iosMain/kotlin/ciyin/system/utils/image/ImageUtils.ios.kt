package ciyin.system.utils.image

import ciyin.io.File
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
internal actual fun resizeImageFile(
    utils: ImageUtils,
    input: File,
    output: File,
    width: Int,
    height: Int,
    format: String
) {
//    val data = NSData.dataWithContentsOfFile(input.absolutePath) ?: return
//    val image = UIImage(data = data)
//    val size = CGSizeMake(width.toDouble(), height.toDouble())
//
//    UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
//    image.drawInRect(CGRectMake(0.0, 0.0, size.width, size.height))
//    val newImage = UIGraphicsGetImageFromCurrentImageContext()
//    UIGraphicsEndImageContext()
//
//    val pngData = newImage?.let { UIImagePNGRepresentation(it) }
//    pngData?.writeToFile(output.absolutePath, true)
}