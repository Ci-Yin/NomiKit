package ciyin.material.theme

import ciyin.designsystem.theme.NomiSizes as DesignSystemAppSizes
import ciyin.designsystem.theme.SizeScale as DesignSystemSizeScale
import ciyin.designsystem.theme.Strokes as DesignSystemStrokes
import ciyin.designsystem.theme.ComponentHeights as DesignSystemComponentHeights
import ciyin.designsystem.theme.LayoutConstraints as DesignSystemLayoutConstraints

/**
 * 旧完整尺寸方案的源码兼容别名，聚合图标、头像、描边、组件高度与布局约束。
 *
 * 通过中立核心 [ciyin.designsystem.theme.defaultNomiSizes] 工厂和生成的成员 copy 定制；数据与默认值只由中立核心维护。
 */
typealias AppSizes = DesignSystemAppSizes

/**
 * 旧九级尺寸表的源码兼容别名，各等级以 dp 表示图标或头像尺寸。
 *
 * 默认图标和头像尺度分别由 [ciyin.designsystem.theme.defaultIconSizeScale]、[ciyin.designsystem.theme.defaultAvatarSizeScale] 提供。
 */
typealias SizeScale = DesignSystemSizeScale

/**
 * 旧描边宽度组的源码兼容别名，以 dp 表示细、中、粗三级线宽。
 *
 * 使用 [ciyin.designsystem.theme.defaultStrokes] 取得核心维护的默认值。
 */
typealias Strokes = DesignSystemStrokes

/**
 * 旧组件参考高度组的源码兼容别名，覆盖应用栏、底栏、标签、Chip 与徽章。
 *
 * 使用 [ciyin.designsystem.theme.defaultComponentHeights] 取得 dp 度量；不会自动向原生控件施加固定高度。
 */
typealias ComponentHeights = DesignSystemComponentHeights

/**
 * 旧页面布局宽度约束的源码兼容别名，包含卡片、对话框与 Sheet 的 dp 宽度边界。
 *
 * 默认值由 [ciyin.designsystem.theme.defaultLayoutConstraints] 提供，实际约束由消费这些度量的布局施加。
 */
typealias LayoutConstraints = DesignSystemLayoutConstraints
