package ciyin.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.NomiControlSize

/**
 * 按钮原生渲染契约；可空参数表示调用方未指定，不能提前套用统一外观。
 */
@Stable
interface NomiActionRenderer {
    /**
     * 使用原生按钮处理交互及默认样式，只应用显式覆盖。
     *
     * 实现须与设计系统的能力声明一致，不支持的覆盖不能静默忽略。
     *
     * @param onClick 启用状态下用户激活按钮时调用；业务状态由调用方更新。
     * @param modifier 应用于按钮根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param variant 按钮语义类型；null 使用当前设计系统的原生默认类型。
     * @param size 显式中立尺寸预设；null 保留原生尺寸。
     * @param colors 按启用/禁用状态逐字段覆盖颜色；Unspecified 保留对应原生值，Transparent 是有效覆盖。
     * @param shape 按钮轮廓；null 保留原生形状。
     * @param contentPadding 内容内边距；null 保留原生间距。
     * @param textStyle 内容文本样式增量；null 保留原生按钮排版。
     * @param nativeSpec 按钮专属参数；null 或未携带本系统类型时全部沿用原生默认。
     * @param content 在按钮 RowScope 中组合的内容，继承按钮内容样式，不负责维护点击状态。
     */
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        variant: NomiButtonVariant?,
        size: NomiControlSize?,
        colors: NomiButtonColors,
        shape: Shape?,
        contentPadding: PaddingValues?,
        textStyle: TextStyle?,
        nativeSpec: NomiButtonSpec?,
        content: @Composable RowScope.() -> Unit,
    )

}
