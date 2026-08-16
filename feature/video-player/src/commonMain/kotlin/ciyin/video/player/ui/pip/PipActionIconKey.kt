package ciyin.video.player.ui.pip

/**
 * PiP 动作图标语义键。
 *
 * 用于在 commonMain 中标识 PiP 小窗按钮的图标身份，
 * 屏蔽平台具体资源索引，由平台侧实现映射为实际资源。
 *
 * 当前定义四个标准键：
 * - [Rewind]：快退
 * - [Play]：播放（播放中状态时切换为 [Pause]）
 * - [Pause]：暂停（暂停中状态时切换为 [Play]）
 * - [FastForward]：快进
 */
public sealed interface PipActionIconKey {

    /** 快退图标键。 */
    public data object Rewind : PipActionIconKey

    /** 播放图标键。 */
    public data object Play : PipActionIconKey

    /** 暂停图标键。 */
    public data object Pause : PipActionIconKey

    /** 快进图标键。 */
    public data object FastForward : PipActionIconKey
}
