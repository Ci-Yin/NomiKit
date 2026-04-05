package ciyin.ui.foundation.uitl

import androidx.compose.ui.graphics.Color


/**
 * Parse color from hexadecimal string format
 * Supported formats:
 * - #RGB
 * - #RGBA
 * - #RRGGBB
 * - #RRGGBBAA
 */
fun parseHexColor(colorString: String?): Color {
    if (colorString == null || colorString.lowercase() == "unspecified") return Color.Unspecified
    val formattedColor = colorString.trim().removePrefix("#")

    return when (formattedColor.length) {
        3 -> parseRGB(formattedColor)
        4 -> parseARGB(formattedColor)
        6 -> parseRRGGBB(formattedColor)
        8 -> parseAARRGGBB(formattedColor)
        else -> Color.Unspecified
    }
}

fun parseStrColor(colorString: String?): Color {
    if (colorString == null) return Color.Unspecified
    return when (colorString.lowercase()) {
        "red" -> Color.Red
        "green" -> Color.Green
        "blue" -> Color.Blue
        "white" -> Color.White
        "black" -> Color.Black
        "gray" -> Color.Gray
        "yellow" -> Color.Yellow
        "cyan" -> Color.Cyan
        "magenta" -> Color.Magenta
        "transparent" -> Color.Transparent
        else -> Color.Unspecified
    }
}

/**
 * 将 Color 对象转换为十六进制颜色字符串。
 *
 * 完全不透明时返回 #RRGGBB 格式,有透明度时返回 #AARRGGBB 格式。
 *
 * @return 十六进制颜色字符串,例如 "#FF5733" 或 "#80FF5733"。如果颜色为 Unspecified,返回 null
 */
fun Color.toHexString(): String {
    // 处理 Unspecified 情况
    if (this == Color.Unspecified) return "unspecified"

    // 将 ULong 转换为 8 位十六进制字符串 (AARRGGBB)
    val hexValue = this.value.toString(16)
        .uppercase()
        .take(8)

    return if (hexValue.startsWith("FF")) {
        // 完全不透明,使用 #RRGGBB 格式 (去掉 alpha 通道)
        "#${hexValue.substring(2)}"
    } else {
        // 有透明度,使用 #AARRGGBB 格式
        "#$hexValue"
    }
}

private fun parseRGB(colorString: String): Color {
    val r = colorString[0].toString().repeat(2).toInt(16)
    val g = colorString[1].toString().repeat(2).toInt(16)
    val b = colorString[2].toString().repeat(2).toInt(16)
    return Color(r, g, b)
}

private fun parseARGB(colorString: String): Color {
    val a = colorString[0].toString().repeat(2).toInt(16)
    val r = colorString[1].toString().repeat(2).toInt(16)
    val g = colorString[2].toString().repeat(2).toInt(16)
    val b = colorString[3].toString().repeat(2).toInt(16)
    return Color(r, g, b, a)
}

private fun parseRRGGBB(colorString: String): Color {
    val r = colorString.take(2).toInt(16)
    val g = colorString.substring(2, 4).toInt(16)
    val b = colorString.substring(4, 6).toInt(16)
    return Color(r, g, b)
}

private fun parseAARRGGBB(colorString: String): Color {
    val a = colorString.take(2).toInt(16)
    val r = colorString.substring(2, 4).toInt(16)
    val g = colorString.substring(4, 6).toInt(16)
    val b = colorString.substring(6, 8).toInt(16)
    return Color(r, g, b, a)
}
