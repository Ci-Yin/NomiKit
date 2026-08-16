package ciyin.permissions

/** 权限当前状态。 */
enum class PermissionStatus {
    /** 尚未向用户请求权限。 */
    NotDetermined,

    /** 权限已经授予。 */
    Granted,

    /** 权限被拒绝，但平台仍可能允许再次申请。 */
    Denied,

    /** 权限被永久拒绝，需要用户前往设置修改。 */
    PermanentlyDenied,

    /** 权限受系统策略、家长控制或设备能力限制。 */
    Restricted,

    /** 当前平台没有对应的运行时权限能力。 */
    Unsupported,
}
