package ciyin.designsystem.component.control

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * 选择控件的原生渲染契约。
 *
 * 实现负责把状态与回调交给当前设计系统的原生选择控件，并保留原生尺寸、配色、交互状态与
 * 无障碍语义；不替调用方维护状态，也不提前套用统一度量。
 * 组合框是带浮层的选值入口，同样归本族，但浮层由实现自己承载；滑块是连续取值入口，
 * 拖拽、刻度与语义由实现负责。
 */
@Stable
interface NomiControlRenderer {
    /**
     * 渲染一个原生开关。
     *
     * @param checked 当前开关状态，由调用方持有。
     * @param onCheckedChange 状态切换回调；控件始终可交互，禁用外观由 [enabled] 表达。
     * @param modifier 应用于原生开关根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param nativeSpec 开关专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     */
    @Composable
    fun Switch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiSwitchSpec?,
    )

    /**
     * 渲染一个原生复选框。
     *
     * @param checked 当前勾选状态，由调用方持有。
     * @param onCheckedChange 状态切换回调；控件始终可交互，禁用外观由 [enabled] 表达。
     * @param modifier 应用于原生复选框根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param nativeSpec 复选框专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     */
    @Composable
    fun Checkbox(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiCheckboxSpec?,
    )

    /**
     * 渲染一个原生单选按钮。
     *
     * @param selected 是否为当前选中项，由调用方持有。
     * @param onClick 点击回调；控件始终可交互，禁用外观由 [enabled] 表达。
     * @param modifier 应用于原生单选按钮根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param nativeSpec 单选按钮专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     */
    @Composable
    fun RadioButton(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiRadioButtonSpec?,
    )

}
