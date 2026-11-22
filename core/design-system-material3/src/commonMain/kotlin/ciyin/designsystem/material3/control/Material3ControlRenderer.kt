package ciyin.designsystem.material3.control

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider as NativeSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.control.NomiCheckboxSpec
import ciyin.designsystem.component.control.NomiComboBoxSpec
import ciyin.designsystem.component.control.NomiControlRenderer
import ciyin.designsystem.component.control.NomiRadioButtonSpec
import ciyin.designsystem.component.control.NomiSliderSpec
import ciyin.designsystem.component.control.NomiSwitchSpec
import ciyin.designsystem.icon.ChevronDown
import ciyin.designsystem.icon.IconPack
import androidx.compose.material3.Checkbox as NativeCheckbox
import androidx.compose.material3.RadioButton as NativeRadioButton
import androidx.compose.material3.Switch as NativeSwitch

/**
 * 原生 Material 3 选择控件适配。
 *
 * 三个入口都直接使用原生控件，尺寸、配色、交互状态与无障碍语义由原生实现决定；文本输入框由
 * [ciyin.designsystem.material3.text.Material3TextRenderer] 实现。
 */
internal object Material3ControlRenderer : NomiControlRenderer {
    /**
     * 渲染原生开关。
     *
     * @param checked 当前开关状态。
     * @param onCheckedChange 状态切换回调。
     * @param modifier 应用于原生开关的修饰符。
     * @param enabled 是否接受用户操作。
     * @param nativeSpec 开关专属参数；当前没有实现，原样接受并忽略。
     */
    @Composable
    override fun Switch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiSwitchSpec?,
    ) {
        NativeSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )
    }

    /**
     * 渲染原生复选框。
     *
     * @param checked 当前勾选状态。
     * @param onCheckedChange 状态切换回调。
     * @param modifier 应用于原生复选框的修饰符。
     * @param enabled 是否接受用户操作。
     * @param nativeSpec 复选框专属参数；当前没有实现，原样接受并忽略。
     */
    @Composable
    override fun Checkbox(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiCheckboxSpec?,
    ) {
        NativeCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )
    }

    /**
     * 渲染原生单选按钮。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调。
     * @param modifier 应用于原生单选按钮的修饰符。
     * @param enabled 是否接受用户操作。
     * @param nativeSpec 单选按钮专属参数；当前没有实现，原样接受并忽略。
     */
    @Composable
    override fun RadioButton(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiRadioButtonSpec?,
    ) {
        NativeRadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }

    /**
     * 使用原生描边按钮作收起态容器，展开浮层使用原生下拉菜单并按容器宽度对齐。
     *
     * @param expanded 当前是否展开。
     * @param onExpandedChange 展开状态变化回调。
     * @param menuItems 展开时显示的菜单项槽，放进原生下拉菜单。
     * @param modifier 应用于组合框根节点的修饰符；尺寸变化用于同步浮层宽度。
     * @param enabled 是否接受用户操作。
     * @param nativeSpec 组合框专属参数；当前没有实现，原样接受并忽略。
     * @param content 收起态内容槽。
     */
    @Composable
    override fun ComboBox(
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        menuItems: @Composable () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiComboBoxSpec?,
        content: @Composable RowScope.() -> Unit,
    ) {
        var anchorWidth by remember { mutableStateOf(0) }
        val widthDp = with(LocalDensity.current) { anchorWidth.toDp() }
        Box(modifier = modifier.onSizeChanged { anchorWidth = it.width }) {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    content()
                }
                Icon(
                    imageVector = IconPack.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(Material3ComboBoxIconSize)
                        .rotate(if (expanded) 180f else 0f),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.width(widthDp),
            ) {
                menuItems()
            }
        }
    }

    /**
     * 使用原生滑块；轨迹、拇指、刻度与无障碍语义全部由原生实现决定。
     *
     * @param value 当前值，由调用方持有。
     * @param onValueChange 值变化回调。
     * @param modifier 应用于原生滑块的修饰符。
     * @param enabled 是否接受用户操作。
     * @param valueRange 取值区间。
     * @param steps 离散刻度数量；负值按 0 处理。
     * @param onValueChangeFinished 用户结束交互后的回调；null 表示不关心。
     * @param nativeSpec 滑块专属参数；当前没有实现，原样接受并忽略。
     */
    @Composable
    override fun Slider(
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        valueRange: ClosedFloatingPointRange<Float>,
        steps: Int,
        onValueChangeFinished: (() -> Unit)?,
        nativeSpec: NomiSliderSpec?,
    ) {
        NativeSlider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps.coerceAtLeast(0),
            onValueChangeFinished = onValueChangeFinished,
        )
    }
}

/**
 * 组合框尾部下拉图标的尺寸，与原生按钮内容图标一致。
 */
private val Material3ComboBoxIconSize = 18.dp
