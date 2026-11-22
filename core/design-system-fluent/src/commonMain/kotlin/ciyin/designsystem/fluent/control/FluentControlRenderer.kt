package ciyin.designsystem.fluent.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import ciyin.designsystem.component.control.NomiCheckboxSpec
import ciyin.designsystem.component.control.NomiControlRenderer
import ciyin.designsystem.component.control.NomiRadioButtonSpec
import ciyin.designsystem.component.control.NomiSwitchSpec
import ciyin.designsystem.fluent.rememberFluentWarning
import io.github.composefluent.component.CheckBox as NativeCheckBox
import io.github.composefluent.component.DropdownMenu
import io.github.composefluent.component.DropDownButton as NativeDropDownButton
import io.github.composefluent.component.RadioButton as NativeRadioButton
import io.github.composefluent.component.Slider as NativeSlider
import io.github.composefluent.component.Switcher as NativeSwitcher

/**
 * 原生 Fluent 选择控件适配。
 *
 * 三个入口都直接使用原生控件；上游没有独立图标按钮或只读态，也不区分填充/描边变体，这些请求
 * 忽略并记录一次警告。文本输入框由 [ciyin.designsystem.fluent.text.FluentText] 实现。
 */
internal object FluentControlRenderer : NomiControlRenderer {
    /**
     * 渲染原生开关；上游 Switcher 没有修饰符参数，修饰符作用在包装节点上。
     *
     * @param checked 当前开关状态。
     * @param onCheckedChange 状态切换回调。
     * @param modifier 应用于包装节点的修饰符。
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
        Box(modifier = modifier) {
            NativeSwitcher(
                checked = checked,
                onCheckStateChange = onCheckedChange,
                text = null,
                enabled = enabled,
            )
        }
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
        NativeCheckBox(
            checked = checked,
            label = null,
            modifier = modifier,
            enabled = enabled,
            onCheckStateChange = onCheckedChange,
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
            label = null,
            enabled = enabled,
        )
    }

}
