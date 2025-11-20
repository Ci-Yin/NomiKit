package ciyin.platform.model

/**
 * 用于表示定时任务的时间配置。
 *
 * 此类包含三个属性：week, hour, minute，分别代表周几、小时和分钟。
 * week是一个可变集合，默认包含1到7，表示一周中的每一天。
 * hour和minute都是整数类型，分别表示一天中的小时（0-23）和分钟（0-59）。
 *
 * @property week 一周中的哪几天，使用1到7的整数表示周一到周日。
 * @property hour 定时任务执行的具体小时，范围是0到23。
 * @property minute 定时任务执行的具体分钟，范围是0到59。
 */
data class TaskSchedule(
    var week: MutableSet<Int> = mutableSetOf(1, 2, 3, 4, 5, 6, 7),
    var hour: Int = 0,
    var minute: Int = 0,
)