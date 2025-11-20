package ciyin.io


///**
// * 压缩当前文件
// *
// * @param zipPath 压缩后的文件
// * @return 是否成功压缩
// * @throws IOException IO错误时抛出
// */
//fun File.zip(zipPath: String): Boolean {
//    return zip(File(zipPath))
//}
//
///**
// * 压缩当前文件
// *
// * @param zipFile 压缩后的文件
// * @return 是否成功压缩
// * @throws IOException IO错误时抛出
// */
//fun File.zip(zipFile: File): Boolean {
//    try {
//        return ZipUtils.zipFile(this, zipFile)
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//    return false
//}
//
///**
// * 解压当前文件
// *
// * @param destDir 解压到的目录
// * @return 是否成功解压
// * @throws IOException IO错误时抛出
// */
//fun File.upzip(destDir: String): Boolean {
//    return upzip(File(destDir))
//}
//
///**
// * 解压当前文件
// *
// * @param destDir 解压到的目录
// * @return 是否成功解压
// * @throws IOException IO错误时抛出
// */
//fun File.upzip(destDir: File): Boolean {
//    try {
//        return ZipUtils.unzipFile(this, destDir)
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//    return false
//}
