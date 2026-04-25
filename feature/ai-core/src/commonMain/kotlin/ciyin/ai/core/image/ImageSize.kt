package ciyin.ai.core.image

/**
 * 输出图像的目标尺寸（像素）。
 *
 * 不在通用层做"是否能被 8 整除""是否符合厂商最大边长"等校验——这些限制因引擎而异，
 * 留给各 `ImageEngine.validate(...)` 处理。
 *
 * @property width 宽度（像素）。
 * @property height 高度（像素）。
 */
data class ImageSize(val width: Int, val height: Int)
