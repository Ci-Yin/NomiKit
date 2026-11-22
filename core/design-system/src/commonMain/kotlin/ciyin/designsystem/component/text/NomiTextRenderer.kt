package ciyin.designsystem.component.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * 文本原生渲染契约，空样式由原生内容环境决定。
 *
 * 两个 `Text` 重载与原生库的完整 Text 对齐：纯文本与富文本各自透传逐字段覆盖、
 * 布局回调和自动字号；`TextField` 承载文本编辑，与显示共用同一族的实现替换点。
 * 不支持的能力由实现按能力声明明确拒绝。
 */
@Stable
interface NomiTextRenderer {
    /**
     * 渲染经公共入口解析的纯文本，不将未指定字段替换为其他设计系统的正文默认。
     *
     * @param text 要显示的文本内容。
     * @param modifier 应用于文本布局节点的修饰符。
     * @param style 已合并显式 slot 的样式增量；null 保留原生文本环境。
     * @param color 公共入口按优先级解析的颜色；Unspecified 交给原生文本环境，Transparent 是有效覆盖。
     * @param fontSize 显式字号；Unspecified 沿用 style 与原生环境。
     * @param fontStyle 显式字形（如斜体）；null 沿用 style 与原生环境。
     * @param fontWeight 显式字重；null 沿用 style 与原生环境。
     * @param fontFamily 显式字体族；null 沿用 style 与原生环境。
     * @param letterSpacing 显式字距；Unspecified 沿用 style 与原生环境。
     * @param textDecoration 显式文本装饰（如下划线）；null 沿用 style 与原生环境。
     * @param textAlign 显式对齐方式；null 沿用 style 与原生环境。
     * @param lineHeight 显式行高；Unspecified 沿用 style 与原生环境。
     * @param overflow 内容超出行数或布局范围时的显示方式。
     * @param softWrap 是否允许在软换行位置折行。
     * @param maxLines 最大显示行数，须不小于 minLines。
     * @param minLines 文本布局的最少行数，须大于零。
     * @param onTextLayout 新文本布局计算完成时的回调；null 表示调用方不需要布局结果。
     * @param autoSize 自动字号策略；null 保留固定字号，不支持的实现必须拒绝而非静默忽略。
     * @param nativeSpec 文本专属参数；实现应原样接受并忽略。
     */
    @Composable
    fun Text(
        text: String,
        modifier: Modifier,
        style: TextStyle?,
        color: Color,
        fontSize: TextUnit,
        fontStyle: FontStyle?,
        fontWeight: FontWeight?,
        fontFamily: FontFamily?,
        letterSpacing: TextUnit,
        textDecoration: TextDecoration?,
        textAlign: TextAlign?,
        lineHeight: TextUnit,
        overflow: TextOverflow,
        softWrap: Boolean,
        maxLines: Int,
        minLines: Int,
        onTextLayout: ((TextLayoutResult) -> Unit)?,
        autoSize: TextAutoSize?,
        nativeSpec: NomiTextSpec?,
    )

    /**
     * 渲染经公共入口解析的富文本，内联内容映射原样交给原生文本布局。
     *
     * @param text 带注解的富文本内容。
     * @param modifier 应用于文本布局节点的修饰符。
     * @param style 已合并显式 slot 的样式增量；null 保留原生文本环境。
     * @param color 公共入口按优先级解析的颜色；Unspecified 交给原生文本环境，Transparent 是有效覆盖。
     * @param fontSize 显式字号；Unspecified 沿用 style 与原生环境。
     * @param fontStyle 显式字形（如斜体）；null 沿用 style 与原生环境。
     * @param fontWeight 显式字重；null 沿用 style 与原生环境。
     * @param fontFamily 显式字体族；null 沿用 style 与原生环境。
     * @param letterSpacing 显式字距；Unspecified 沿用 style 与原生环境。
     * @param textDecoration 显式文本装饰（如下划线）；null 沿用 style 与原生环境。
     * @param textAlign 显式对齐方式；null 沿用 style 与原生环境。
     * @param lineHeight 显式行高；Unspecified 沿用 style 与原生环境。
     * @param overflow 内容超出行数或布局范围时的显示方式。
     * @param softWrap 是否允许在软换行位置折行。
     * @param maxLines 最大显示行数，须不小于 minLines。
     * @param minLines 文本布局的最少行数，须大于零。
     * @param inlineContent 替换文本指定区段的内联组合映射；空映射表示纯富文本。
     * @param onTextLayout 新文本布局计算完成时的回调；null 表示调用方不需要布局结果。
     * @param autoSize 自动字号策略；null 保留固定字号，不支持的实现必须拒绝而非静默忽略。
     */
    @Composable
    fun Text(
        text: AnnotatedString,
        modifier: Modifier,
        style: TextStyle?,
        color: Color,
        fontSize: TextUnit,
        fontStyle: FontStyle?,
        fontWeight: FontWeight?,
        fontFamily: FontFamily?,
        letterSpacing: TextUnit,
        textDecoration: TextDecoration?,
        textAlign: TextAlign?,
        lineHeight: TextUnit,
        overflow: TextOverflow,
        softWrap: Boolean,
        maxLines: Int,
        minLines: Int,
        inlineContent: Map<String, InlineTextContent>,
        onTextLayout: ((TextLayoutResult) -> Unit)?,
        autoSize: TextAutoSize?,
        nativeSpec: NomiTextSpec?,
    )
    /**
     * 渲染文本输入框；标签、占位符与前后置图标都是调用方槽位，状态由调用方持有。
     *
     * @param value 当前文本值。
     * @param onValueChange 文本变化回调。
     * @param modifier 应用于原生输入框根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param readOnly 是否只读；只读时仍可聚焦与复制，但不可修改。
     * @param singleLine 是否限制为单行并抑制换行。
     * @param variant 显式输入框变体；null 使用当前设计系统的原生默认变体。
     * @param label 标签槽；null 表示没有标签。
     * @param placeholder 占位符槽；null 表示没有占位符。
     * @param leadingIcon 前导图标槽；null 表示不占位。
     * @param trailingIcon 尾部图标槽；null 表示不占位。
     * @param isError 是否处于错误状态；无法表达错误态的实现忽略该值并记录一次警告。
     * @param keyboardOptions 键盘类型与输入法动作配置。
     * @param keyboardActions 输入法动作回调。
     * @param nativeSpec 输入框专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     */
    @Composable
    fun TextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        readOnly: Boolean,
        singleLine: Boolean,
        variant: NomiTextFieldVariant?,
        label: (@Composable () -> Unit)?,
        placeholder: (@Composable () -> Unit)?,
        leadingIcon: (@Composable () -> Unit)?,
        trailingIcon: (@Composable () -> Unit)?,
        isError: Boolean,
        keyboardOptions: KeyboardOptions,
        keyboardActions: KeyboardActions,
        nativeSpec: NomiTextFieldSpec?,
    )
}
