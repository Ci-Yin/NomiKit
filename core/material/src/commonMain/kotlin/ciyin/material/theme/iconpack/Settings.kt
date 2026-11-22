package ciyin.material.theme.iconpack

import androidx.compose.ui.graphics.vector.ImageVector
import ciyin.designsystem.icon.IconPack as DesignSystemIconPack
import ciyin.designsystem.icon.Settings as designSystemSettings

/**
 * 兼容旧包的设置图标入口，固有尺寸为 24dp × 24dp。
 *
 * 转发中立核心的矢量图标，首次读取由核心构建，后续读取复用核心缓存。
 *
 * @receiver 旧图标命名空间，与中立核心共用同一对象。
 */
val IconPack.Settings: ImageVector
    get() = DesignSystemIconPack.designSystemSettings