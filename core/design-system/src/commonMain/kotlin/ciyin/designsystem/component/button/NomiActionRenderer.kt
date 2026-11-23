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

    /**
     * 渲染一个原生开关按钮。
     *
     * 实现负责把选中状态交给原生开关语义，并保留原生选中与未选中外观；不替调用方维护状态，
     * 也不因为 iconOnly 改变点击语义。
     *
     * @param checked 当前选中状态，由调用方持有。
     * @param onCheckedChange 状态切换回调，收到请求后的目标状态。
     * @param modifier 应用于按钮根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param iconOnly 是否只包含图标；true 时容器收敛为原生图标按钮的方形尺寸。
     * @param size 显式中立尺寸预设；null 保留原生尺寸，iconOnly 为 true 时由实现忽略。
     * @param colors 两种状态的逐字段颜色覆盖；Unspecified 保留对应原生值。
     * @param nativeSpec 开关按钮专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     * @param content 在按钮 RowScope 中组合的内容，不负责维护选中状态。
     */
    @Composable
    fun ToggleButton(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        iconOnly: Boolean,
        size: NomiControlSize?,
        colors: NomiToggleButtonColors,
        nativeSpec: NomiToggleButtonSpec?,
        content: @Composable RowScope.() -> Unit,
    )

    /**
     * 渲染一个带下拉指示的原生按钮。
     *
     * 实现负责追加原生下拉箭头与展开提示；浮层本身由调用方用菜单入口组合，本方法不持有展开状态。
     *
     * @param onClick 启用状态下用户激活按钮时调用；业务状态由调用方更新。
     * @param modifier 应用于按钮根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param variant 按钮语义类型；null 使用当前设计系统的原生默认类型。
     * @param size 显式中立尺寸预设；null 保留原生尺寸。
     * @param colors 按启用/禁用状态逐字段覆盖颜色；Unspecified 保留对应原生值。
     * @param nativeSpec 下拉按钮专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     * @param content 在按钮 RowScope 中组合的内容，不含下拉箭头。
     */
    @Composable
    fun DropDownButton(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        variant: NomiButtonVariant?,
        size: NomiControlSize?,
        colors: NomiButtonColors,
        nativeSpec: NomiDropDownButtonSpec?,
        content: @Composable RowScope.() -> Unit,
    )

    /**
     * 渲染一个原生图标按钮。
     *
     * 实现负责容器、触摸目标、状态层与内容色，不追加文本排版；内容按实现的原生方式居中，
     * 图标尺寸由调用方在内容中约束。
     *
     * @param onClick 启用状态下用户激活按钮时调用；业务状态由调用方更新。
     * @param modifier 应用于按钮根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param colors 按启用/禁用状态逐字段覆盖颜色；Unspecified 保留对应原生值，Transparent 是有效覆盖。
     * @param nativeSpec 图标按钮专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     * @param content 在按钮内居中的内容，通常是一个图标，不负责维护点击状态。
     */
    @Composable
    fun IconButton(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        colors: NomiButtonColors,
        nativeSpec: NomiIconButtonSpec?,
        content: @Composable () -> Unit,
    )
}
