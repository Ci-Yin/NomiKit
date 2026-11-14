package ciyin.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * NomiKit 默认排版的唯一数值来源，保留原主题十五个角色的字体、字重、字号、行高和字距。
 *
 * 此默认对象不依赖 Material 3，也不作为其他设计系统的隐式排版覆盖。
 */
private object DefaultTypographyTokens {
    /**
     * 随默认令牌对象初始化并复用的不可变排版；各角色的 TextStyle 均显式定义。
     */
    val typography: NomiTypography = NomiTypography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.2).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.2.sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.2.sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

/**
 * 与具体组件库无关的十五个语义排版角色。
 *
 * Display 用于强调展示，Headline 用于页面标题，Title 用于区块标题，Body 用于正文，
 * Label 用于按钮和标签。每个 [TextStyle] 包含完整排版属性，不只是字号。
 *
 * [defaultNomiTypography] 返回 NomiKit 的默认值；[NomiTheme.typography] 则读取当前设计系统的
 * 已解析排版。向主题明确传入本对象表示替换整组排版，不根据默认值推断局部修改。
 * 原生库的额外角色由适配器负责，不通过本类型承诺完整往返保留。
 * 支持公开构造和编译器生成的成员 copy；直接构造须提供全部十五个角色，
 * 需要 Nomi 默认排版时先调用 [defaultNomiTypography] 再复制定制，无需导入复制扩展。
 *
 * @property displayLarge 大号展示文本。
 * @property displayMedium 中号展示文本。
 * @property displaySmall 小号展示文本。
 * @property headlineLarge 大号页面标题。
 * @property headlineMedium 中号页面标题。
 * @property headlineSmall 小号页面标题。
 * @property titleLarge 大区块标题。
 * @property titleMedium 中区块标题。
 * @property titleSmall 小区块标题。
 * @property bodyLarge 正文主要文本。
 * @property bodyMedium 正文默认文本。
 * @property bodySmall 辅助说明文本。
 * @property labelLarge 大号标签。
 * @property labelMedium 默认标签。
 * @property labelSmall 小号标签。
 */
@Immutable
data class NomiTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
)

/**
 * 返回共享的 NomiKit 默认排版对象，不读取当前主题或系统配置。
 *
 * 十五个角色的数值与原主题保持一致。若需要当前设计系统的实际排版，应读取
 * [NomiTheme.typography]；本函数不会因切换 Material 3、Fluent 或明暗模式而改变结果。
 *
 * @return 不可变的默认排版实例。
 */
fun defaultNomiTypography(): NomiTypography = DefaultTypographyTokens.typography
