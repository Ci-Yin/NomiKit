package ciyin.system.utils.image

//internal actual fun resizeImageFile(
//    utils: ImageUtils,
//    input: File,
//    output: File,
//    width: Int,
//    height: Int,
//    format: String
//) {
//    val original = BitmapFactory.decodeFile(input.absolutePath)
//    val scaled = Bitmap.createScaledBitmap(original, width, height, true)
//    FileOutputStream(java.io.File(output.absolutePath)).use { out ->
//        scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
//    }
//    original.recycle()
//    scaled.recycle()
//}