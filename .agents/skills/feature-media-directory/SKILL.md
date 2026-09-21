---
name: feature-media-directory
description: Use NomiKit's feature/media-directory Kotlin Multiplatform module (package ciyin.media.directory) to expose a vendor-neutral user-media directory abstraction. Covers MediaDirectorySession / MediaDirectoryAccess / MediaDirectoryAccessToken / MediaDirectoryError / MediaDirectoryException / MediaDirectoryRequestValidator / DirectoryPickerLauncher / createMediaDirectorySession expect/actual, Android SAF (Tree URI), Desktop JFileChooser, and iOS placeholder. Use when adding a new platform implementation, debugging SAF token validation, splitting picker injection across the host page, or mapping errors to upstream layers.
---

# feature/media-directory 使用指南

`feature/media-directory` 是 NomiKit 的 KMP 用户媒体目录抽象层，包名 `ciyin.media.directory`
。本模块只描述平台契约，不持有任何 UI；UI 例子与 `DirectoryPickerLauncher` 注入示范应留在 `app/sample`。

## 一、模块结构

- `commonMain`
    - `MediaDirectorySession`：`requestAccess` / `validate` / `commit` / `delete` / `exists` 五接口。
    - `MediaDirectoryAccess` / `MediaDirectoryAccessToken` / `MediaDirectoryEntry`：业务模型与不透明令牌。
    - `MediaDirectoryError` sealed 错误模型（含 `Cancelled` / `Unsupported` / `PermissionDenied` /
      `AccessRevoked` / `AlreadyExists` / `NoSpace` / `NotFound` / `Io`）。
    - `MediaDirectoryException`：把 `MediaDirectoryError` 暴露给挂起 API。
    - `MediaDirectoryRequestValidator`：提交请求的统一校验（文件名 / MIME / 源文件大小）。
    - `DirectoryPickerLauncher`：`fun interface`，声明 `suspend fun pick(): String?`。
    - `expect fun createMediaDirectorySession(context, pickerLauncher)`。
- `androidMain`：`AndroidMediaDirectorySession`，基于 SAF `ACTION_OPEN_DOCUMENT_TREE` 与
  `DocumentsContract.createDocument/renameDocument/deleteDocument/openInputStream`；调用
  `createDocument` 时必须先用
  `DocumentsContract.buildDocumentUriUsingTree(treeUri, getTreeDocumentId(treeUri))` 得到父 Document
  URI，不能直接传原始 Tree URI。宿主可不提供 `DirectoryPickerLauncher`，此时 actual 仍可构造会话，但
  `requestAccess` 才抛 `MediaDirectoryError.Unsupported`。
- `desktopMain`：`DesktopMediaDirectorySession`，基于 `java.nio.file` + `JFileChooser`；宿主传入的
  launcher 优先，未传入时使用内置 `JvmJFileChooserDirectoryPicker`。
- `iosMain`：`IosMediaDirectorySession`，当前为占位 `Unsupported`；真实接入等待
  `UIDocumentPickerViewController` + security-scoped bookmark。

## 二、Android 端硬性约束

1. **宿主可选择注入或不注入 `DirectoryPickerLauncher`**：
    - **注入**：`createMediaDirectorySession(actual)` 不抛错，`requestAccess` 通过 launcher 拉起 SAF
      选择器，返回完整 `MediaDirectoryAccess`。
    - **不注入**：`createMediaDirectorySession(actual)` 仍可正常构造会话（Koin 自动装配不需要宿主手工准备
      launcher）；但**首次**调用 `requestAccess` 会抛
      `MediaDirectoryError.Unsupported("Android 当前未注入 DirectoryPickerLauncher；请提供页面级 AccessFactory。")`
      。此时调用方应改用 `rememberMediaDirectoryAccessFactory` 之类的页面级 SAF 桥喂入 Token，再调用
      `validate` / `commit` / `delete` / `exists`。
2. **SAF Tree URI 必须来自 `com.android.externalstorage.documents` provider**。其他 scheme/authority
   的 token 会在 `parseTreeUri` 阶段被当作 `Unsupported` 拒绝，避免上游误用。
3. `validate` 的探针策略分为三档：
    - `createDocument` 返回 `null` → `MediaDirectoryError.Io("无法在授权目录中创建校验文件")`
      （目录可读不可写，例如云端配额目录）。
    - 抛 `SecurityException` →
      `MediaDirectoryError.PermissionDenied("没有在授权目录中创建校验文件的权限")`。
    - 抛其他异常且 `isDirectoryAccessible(treeUri) == false` →
      `MediaDirectoryError.AccessRevoked("授权目录已不可访问")`；否则归因 IO。
4. `validate` 完成后会在 `finally` 里清理 staging probe；不允许抛错后保留残留探针文件。
5. `commit` 始终使用 staging → rename 策略：先将 Tree URI 转成父 Document URI，再以
   `.${fileName}.<uuid>.nomikit-part` 临时名写完后用 `renameDocument` 重命名为正式名。任何阶段失败都清理
   staging 文件。
6. Android Q（API 29）以下会走 `MediaScannerConnection.scanFile` 让旧相册可见；Q+ 由
   `createDocument/renameDocument` 自动通知 MediaProvider。
7. `DesktopContext` 与 SAF 没有任何关系；Android 端的 `validate` / `commit` / `delete` / `exists` 只依赖
   `accessToken` 中的 Tree URI，只有 `requestAccess` 才需要宿主 launcher 或页面级 AccessFactory。

## 三、Desktop 端约定

1. 目录授权以规范化绝对路径字符串作为 token。`canonicalPath` 是平台事实，`displayName` 取
   `directory.fileName`。
2. `validate` 检查 `Files.isDirectory` + `Files.isWritable`，失败时抛 `AccessRevoked`。
3. `commit` 同样走 staging → rename（`Files.move`），并在源文件大小变更时抛 `Io`。
4. `delete` 通过 `Files.deleteIfExists` 实现幂等；非法路径抛 `Unsupported`。
5. `exists` 直接 `Files.exists`，目录被外部删除时返回 `false`。

## 四、iOS 端占位

- 当前所有方法统一抛
  `MediaDirectoryError.Unsupported("iOS 用户目录功能在当前 NomiKit 版本下为占位实现…")`。
- 真实接入需要绑定 `UIDocumentPickerViewController` 与 security-scoped bookmark，并通过宿主
  `DirectoryPickerLauncher` 工厂注入到 `createMediaDirectorySession`。
- 在 `:app:ios` 引入之前，业务 Demo 页面应忽略 iOS 入口；可以保留路由但运行时直接展示 `Unsupported`
  文案。

## 五、错误模型

- `Cancelled` 用户取消目录选择，**不**要作为 UI 业务错误提示；只用于 toggle isBusy。
- `Unsupported` 表示当前平台 / 输入参数在公共契约层面不成立，应在 UI 提示"平台不支持"。
- `PermissionDenied` 表示宿主给出了 launcher 但 SAF provider 拒绝授权 / 校验探针被拒；提示用户去设置放权。
- `AccessRevoked` 表示目录被卸载或权限被撤销；UI 应翻到 "Revoked" 状态并允许重新 pick。
- `AlreadyExists` 表示目标文件名冲突；调用方决定覆盖或跳过。
- `NoSpace` 表示目录所在卷空间不足，由 Android `ErrnoException(ENOSPC)` 或 Desktop 含 "no space" 字样的
  IO 错误归因。
- `NotFound` 表示源文件不存在；通过 `File.strictSize()` 等校验路径抛出。
- `Io` 表示其他读写失败，携带原始 `cause` 并进入 `MediaDirectoryException` 的 `suppressed`。

## 六、与上层的协作

- `component/media-directory` 暴露：
    - `MediaDirectoryAutoConfiguration`：在 Koin 中未声明 `MediaDirectorySession` 时自动装配；会消费宿主的
      `DirectoryPickerLauncher`（Koin getOrNull）。
    - 顶层 `MediaDirectoryBootInitializer`：把 `MediaDirectoryAutoConfiguration` 注册到 `runKoinBoot`
      DSL；宿主在 `:app:shared` 的 `koinBootInitializer { includes(...) }` 中加入
      `projects.component.mediaDirectory`。
- 业务页面（如 `:app:sample` 的 `MediaDirectoryDemoScreen`）：
    - 通过 `rememberMediaDirectoryAccessFactory()`（平台 expect/actual 桥）拿到 Compose 侧的
      `DirectoryPickerLauncher` 工厂；
    - 在 ViewModel 构造时优先调用此 factory，命中即跳过 `session.requestAccess()`，未命中再回到
      `session.requestAccess()`（Desktop 默认即可）。
    - 这条模式是 **页面级注入**，不要在 Koin 单例创建时绑定 Activity，避免 Activity 销毁时持有泄漏的
      launcher。

## 七、修改注意

- 修改公共契约前同步检查 `component/media-directory` 和 `app/sample` 的依赖面；尤其
  `MediaDirectoryError` 子类新增会影响所有 `toDemoType()` 映射。
- 新增 error 子类要同时在 `app/sample/.../MediaDirectoryDemoModel.kt` 的 `toDemoModel()` 内补对应
  `MediaDirectoryDemoErrorType`。
- Android 端私有助手（`takePersistableTreePermission` / `queryDisplayName` / `buildTreeDocumentUri` /
  `findChild` / `copySource` / `notifyMediaScanner` / `parseTreeUri` / `isDirectoryAccessible`
  ）必须遵循"权限收紧归 PermissionDenied、URI 失效归 AccessRevoked、其他归 IO"的归因层级。
- iOS 端真实实现需要新增 `actual fun createMediaDirectorySession` + 绑定
  `UIDocumentPickerViewController` 的 `DirectoryPickerLauncher`，并在 `:app:ios` 内通过 Koin 装配。
- 公共类、属性、扩展函数必须有中文 KDoc。

## 八、Xiaomi Redmi K20 Pro 真机手动验证步骤

按以下顺序在真机上验证完整 pick → validate → commit → exists → delete 闭环。**所有操作均由用户在设备上完成，Agent
不自动触发。**

1. **构建并安装**
    - USB 连接 Xiaomi Redmi K20 Pro,开启「开发者选项 → USB 调试」。
    - 在 PowerShell 中执行:
      ```bash
      .\gradlew.bat :app:android:installDebug
      ```
    - 启动后导航至 Sample Hub → 「用户媒体目录」卡片。

2. **Pick 目录**
    - 点「选择目录」按钮,系统会弹出 SAF `OpenDocumentTree` 选择器。
    - 推荐选择内部存储的 `Download` 目录或外置 SDCard 根目录,确认授权。
    - 预期:页面状态从 `NotGranted` 翻到 `Granted`,顶部展示目录 displayName 与规范化路径,最近文件名为
      `null`。

3. **Validate**
    - 点「验证目录」按钮。
    - 预期:页面状态仍为 `Granted`,`accessState = Granted`,不报错。如目录为只读或权限收紧,会显示
      `media_directory_demo_error_permission_denied` 或 `io`。

4. **Commit**
    - 点「提交示例」按钮。
    - 预期:页面展示 `lastEntry`,文件名形如 `nomikit-media-directory-demo-<timestamp>.txt`,
      `lastEntry.exists == true`。
    - 在系统「文件管理」应用中进入对应目录,可看到该文本文件。

5. **Exists**
    - 点「检查存在」按钮。
    - 预期:`lastEntry.exists` 仍为 `true`。
    - 回到「文件管理」删除该文件,然后回页面再次点「检查存在」,预期:`exists == false`,不抛错。

6. **Delete**
    - 重新 commit 一次,再点「删除」按钮。
    - 预期:`lastEntry` 变为 `null`,「文件管理」中该文件被删除。

7. **权限撤销**
    - 在系统设置 → 应用 → 找到 NomiKit → 清除「存储访问」授权。
    - 回页面点「验证目录」,预期:`accessState = Revoked`,展示
      `media_directory_demo_error_access_revoked`。
    - 再次点「选择目录」重新授权后,`accessState` 应回到 `Granted`。

## 九、运行命令

公共契约、token、错误与文件名校验:

```bash
.\gradlew.bat :feature:media-directory:desktopTest --console=plain --tests "*MediaDirectoryAccessTokenTest" --tests "*MediaDirectoryRequestValidatorTest" --tests "*MediaDirectoryErrorTest"
```

Desktop 真实文件系统集成（含 staging/rename、IO 归因、目录撤销、幂等）:

```bash
.\gradlew.bat :feature:media-directory:desktopTest --console=plain --tests "*DesktopMediaDirectorySessionTest" --tests "*DesktopMediaDirectorySessionEdgeTest"
```

公共层编译:

```bash
.\gradlew.bat :feature:media-directory:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :feature:media-directory:compileAndroidMain --console=plain
```

修改本模块后请同步更新本 skill 和 `component-media-directory/SKILL.md`。
