package ciyin.material.theme.iconpack

import androidx.compose.ui.graphics.vector.ImageVector
import ciyin.designsystem.icon.IconPack as DesignSystemIconPack
import ciyin.designsystem.icon.Null as designSystemNull

/**
 * 兼容旧包的空图标入口，固有尺寸为 1dp × 1dp。
 *
 * 转发中立核心的无绘制路径占位图标，复用其 lazy 实例，不在旧包创建矢量数据。
 *
 * @receiver 旧图标命名空间，与中立核心共用同一对象。
 */
val IconPack.Null: ImageVector
    get() = DesignSystemIconPack.designSystemNull