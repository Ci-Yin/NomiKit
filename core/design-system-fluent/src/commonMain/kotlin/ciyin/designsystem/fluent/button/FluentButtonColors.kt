package ciyin.designsystem.fluent.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonVariant
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.ButtonColor
import io.github.composefluent.component.ButtonColorScheme
import io.github.composefluent.component.ButtonDefaults

/**
 * 只修改原生五态色板中明确指定的通道，不用自制状态色替换未覆盖的原生 hover/pressed。
 *
 * @receiver 中立启用/禁用四色；Unspecified 表示不覆盖，Transparent 也是显式颜色。
 * @param variant 选择原生色板；null/Default/Destructive 以普通按钮为基线，Destructive 为未指定的启用通道补 critical 色。
 * @return 无任何有效覆盖时返回原生色板；否则将启用色应用于 default/hovered/pressed/focused，禁用色仅用于 disabled，描边始终保留。
 */
@Composable
internal fun NomiButtonColors.toFluentButtonColors(variant: NomiButtonVariant?): ButtonColorScheme {
    val native = when (variant) {
        NomiButtonVariant.Primary -> ButtonDefaults.accentButtonColors()
        NomiButtonVariant.Subtle -> ButtonDefaults.subtleButtonColors()
        NomiButtonVariant.Text -> ButtonDefaults.hyperlinkButtonColors()
        null, NomiButtonVariant.Default, NomiButtonVariant.Destructive -> ButtonDefaults.buttonColors()
    }
    val destructive = variant == NomiButtonVariant.Destructive
    val fill = containerColor.takeOrElse {
        if (destructive) FluentTheme.colors.system.criticalBackground else Color.Unspecified
    }
    val foreground = contentColor.takeOrElse {
        if (destructive) FluentTheme.colors.system.critical else Color.Unspecified
    }
    if (fill == Color.Unspecified && foreground == Color.Unspecified &&
        disabledContainerColor == Color.Unspecified && disabledContentColor == Color.Unspecified
    ) return native
    return ButtonColorScheme(
        default = native.default.withOverrides(fill, foreground),
        hovered = native.hovered.withOverrides(fill, foreground),
        pressed = native.pressed.withOverrides(fill, foreground),
        focused = native.focused.withOverrides(fill, foreground),
        disabled = native.disabled.withOverrides(disabledContainerColor, disabledContentColor),
    )
}

/**
 * 指定状态中保留原生描边与未覆盖的颜色。
 *
 * @receiver 原生单个交互状态的按钮配色。
 * @param fill 填充覆盖，Unspecified 保留原值，透明色明确替换。
 * @param foreground 内容覆盖，Unspecified 保留原值，透明色明确替换。
 * @return 只替换指定通道的新 ButtonColor，描边及其余字段不变。
 */
private fun ButtonColor.withOverrides(fill: Color, foreground: Color): ButtonColor = copy(
    fillColor = fill.takeOrElse { fillColor },
    contentColor = foreground.takeOrElse { contentColor },
)
