# 🧩 Kotlin Multiplatform 层级结构总览

> 本文档用于说明 **Kotlin Multiplatform (KMP)** 项目的多平台层级结构、继承关系与 Compose 支持情况。  
> 可直接参考此文档确定源集（source sets）的组织方式与代码共享边界。

---

## 🧱 一、通用层（Common）

| 层级           | 说明        | 可访问 API              | Compose 支持                      |
|--------------|-----------|----------------------|---------------------------------|
| `commonMain` | 所有平台共享逻辑层 | Kotlin 标准库 (无平台 API) | ✅ Compose Multiplatform 通用 UI 层 |
| `commonTest` | 单元测试层     | 同上                   | ❌                               |

---

## ⚙️ 二、JVM 系列（Android / Desktop）

| 层级            | 说明                | 可访问 API                          | Compose 支持              |
|---------------|-------------------|----------------------------------|-------------------------|
| `jvmMain`     | 基础 JVM 平台，控制台、后端等 | `java.*`, `javax.*`              | ✅ Compose Multiplatform |
| `androidMain` | Android 专用层       | Android SDK, Jetpack, Compose UI | ✅ Compose Android       |
| `desktopMain` | Compose Desktop 层 | JVM API, AWT/Swing               | ✅ Compose Desktop       |

> 💡 `desktopMain` 和 `jvmMain` 的区别主要在依赖配置与 UI 框架不同。  
> 如果是纯 Kotlin 逻辑层，可放在 `jvmMain`。

---

## 🧠 三、JavaScript 系列

| 层级              | 说明          | 可访问 API                       | Compose 支持                   |
|-----------------|-------------|-------------------------------|------------------------------|
| `jsMain`        | 通用 JS 平台逻辑  | JS 标准库                        | ✅ Compose Web (experimental) |
| `jsBrowserMain` | 浏览器环境层      | `window`, `document`, DOM API | ✅ Compose Web                |
| `jsNodeMain`    | Node.js 环境层 | `fs`, `path`, Node modules    | ❌                            |

> ✅ `jsBrowserMain` 适合做前端交互页面。  
> ❌ `jsNodeMain` 适合后端服务逻辑，不支持 UI。

---

## 🧮 四、WASM 系列（WebAssembly）

| 层级                  | 说明                      | 可访问 API      | Compose 支持     |
|---------------------|-------------------------|--------------|----------------|
| `wasmMain`          | 基础 WebAssembly 层        | 无平台 API      | ✅ Compose WASM |
| `wasmJsMain`        | 通过 JS 运行的 WebAssembly 层 | JS Interop   | ✅              |
| `wasmJsBrowserMain` | 浏览器运行环境                 | DOM, Canvas  | ✅              |
| `wasmJsNodeMain`    | Node 环境运行               | Node modules | ❌              |
| `wasmWasiMain`      | WASI 环境运行（非浏览器）         | 文件系统 / I/O   | ❌              |

> 🧠 WebAssembly 目标是未来 Compose Web 的核心方向。  
> 若你要打包网页前端推荐使用 `wasmJsBrowserMain`。

---

## 🍎 五、Apple 系列（iOS / macOS）

| 层级                         | 说明                | 可访问 API                  | Compose 支持                         |
|----------------------------|-------------------|--------------------------|------------------------------------|
| `nativeMain`               | Kotlin/Native 通用层 | C Interop, expect/actual | ✅ Compose Multiplatform (iOS Beta) |
| `darwinMain`               | Apple 平台通用层       | CoreFoundation 等         | ✅                                  |
| `iosMain`                  | iPhone / iPad     | UIKit / Swift Interop    | ✅                                  |
| `macosMain`                | macOS 桌面          | AppKit                   | ✅                                  |
| `tvosMain` / `watchosMain` | Apple 其它端         | 各自 SDK                   | ❌                                  |

---

## 🖥 六、Windows / Linux 原生层级

| 层级               | 说明             | 可访问 API               | Compose 支持 |
|------------------|----------------|-----------------------|------------|
| `mingwMain`      | Windows 原生层    | Win32 API（需 CInterop） | ❌          |
| `linuxMain`      | Linux 原生层      | POSIX API（需 CInterop） | ❌          |
| `linuxX64Main`   | Linux x64 特化   | 同上                    | ❌          |
| `linuxArm64Main` | Linux ARM64 特化 | 同上                    | ❌          |

> ✅ 可用于构建无 UI 的 CLI 工具或服务端程序。  
> ❌ 不支持 Compose UI。

---

## 🧭 七、完整层级继承树

```markdown
commonMain
├── jvmMain
│ ├── androidMain
│ └── desktopMain
├── jsMain
│ ├── jsBrowserMain
│ └── jsNodeMain
├── wasmMain
│ ├── wasmJsMain
│ │ ├── wasmJsBrowserMain
│ │ └── wasmJsNodeMain
│ └── wasmWasiMain
└── nativeMain
├── darwinMain
│ ├── iosMain
│ │ ├── iosArm64Main
│ │ ├── iosX64Main
│ │ └── iosSimulatorArm64Main
│ ├── macosMain
│ │ ├── macosArm64Main
│ │ └── macosX64Main
│ └── tvosMain / watchosMain
├── linuxMain
│ ├── linuxX64Main
│ └── linuxArm64Main
└── mingwMain

```

---

## 🧩 八、推荐实践

| 场景              | 放置层级                                                    | 说明                                 |
|-----------------|---------------------------------------------------------|------------------------------------|
| 网络请求 / JSON 解析  | `commonMain`                                            | 使用 `Ktor`, `kotlinx.serialization` |
| 文件读写（平台相关）      | `expect/actual` in `commonMain`                         | 平台实现分布于各自层级                        |
| UI 界面（Compose）  | 按平台拆分：`androidMain`, `desktopMain`, `wasmJsBrowserMain` | 各平台 UI 独立实现                        |
| 平台桥接（C/ObjC/JS） | `nativeMain` / `wasmMain`                               | 通过 `cinterop` 或 `js()` 实现          |

---

## 🚀 九、构建命令速查

| 命令                                         | 说明                    |
|--------------------------------------------|-----------------------|
| `./gradlew assemble`                       | 构建所有平台                |
| `./gradlew jsBrowserProductionWebpack`     | 构建 JS 浏览器发布版          |
| `./gradlew wasmJsBrowserProductionWebpack` | 构建 WASM 浏览器发布版        |
| `./gradlew desktop:run`                    | 运行 Compose Desktop 应用 |
| `./gradlew android:assembleRelease`        | 打包 Android APK        |

---

_最后更新：2025 年 10 月_  
_作者：CiYin_  
_说明：本文档可自由修改与扩展，用于团队或项目内的多平台结构参考。_


---
