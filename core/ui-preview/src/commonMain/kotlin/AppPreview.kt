package org.jetbrains.compose.ui.tooling.preview


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/10/26 17:33
 */


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(name = "1正常模式", showBackground = true)
@Preview(
    name = "2深色模式",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(name = "大字体模式", showBackground = true, fontScale = 2.0f)
@Preview(name = "大屏幕模式", showBackground = true, device = "id:pixel_tablet")
annotation class AppPreview
