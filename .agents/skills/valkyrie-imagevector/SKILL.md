---
name: valkyrie-imagevector
description: 使用 Valkyrie CLI 将 SVG 或 Android VectorDrawable XML 转为本项目 IconPack 下的 Compose ImageVector；适用于新增或迁移 app/shared 的矢量图标，不用于普通 XML、位图或 painterResource。
---

# Valkyrie ImageVector 转换

将 SVG 或 Android VectorDrawable XML 转成当前项目可直接使用的 Kotlin ImageVector 源码。默认目标如下：

- 输入：用户明确指定的单个 `.svg`、Android VectorDrawable `.xml`，或包含这两类文件的目录。
- 输出目录：`app/shared/src/commonMain/kotlin/com/ciyin/app/presentation/theme/iconpack/`。
- 输出包名：`com.ciyin.app.presentation.theme.iconpack`。
- IconPack 接收者：`ciyin.material.theme.iconpack.IconPack`。
-

命名空间定义：[IconPack.kt](../../../core/material/src/commonMain/kotlin/ciyin/material/theme/iconpack/IconPack.kt)。

不要修改 `IconPack.kt`，也不要在 app 包中创建第二个 `IconPack` object。生成的属性应属于已有接收者，例如
`val IconPack.Example: ImageVector`。

## 1. 预检与输入判定

1. 从仓库根目录检查 `valkyrie`、Windows 下的 `valkyrie.bat` 或 `valkyrie.exe` 是否在 `PATH`
   中；本次测试安装路径为 `D:\APP\valkyrie-cli-1.2.0\bin\valkyrie.bat`。执行本机 launcher 的
   `--version` 和 `--help`，本机 help 是参数的最终依据，不能盲目套用网上旧版本示例。
2. 如果 CLI 不存在，停止转换并告知用户安装方式：
    - Homebrew：`brew install ComposeGears/repo/valkyrie`。
    - 其他平台从 [Valkyrie Releases](https://github.com/ComposeGears/Valkyrie/releases) 下载 CLI
      压缩包，解压后运行 `bin/valkyrie`（Windows 使用对应可执行文件）。 未经用户明确授权，不自动下载、安装或执行远程脚本。
3. SVG 必须是可解析的 SVG 文档；XML 必须读取根元素并确认是 Android VectorDrawable 的 `<vector>`。普通布局
   XML 和任意业务 XML 不属于本 skill 的输入范围；如果遇到 AnimatedVectorDrawable
   定义，先确认用户是否只需静态图标，并明确动画语义不会由静态 ImageVector 自动保留。
4. 先列出输入文件、预计生成的 `.kt` 文件和目标目录。目录批处理时必须使用目录输出；同名 Kotlin
   文件已存在，或目标文件属于用户未提交的改动时，先报告冲突并等待明确覆盖授权。
5. 临时试转换优先使用系统临时目录，不要把中间文件写到仓库；任务结束后清理本次临时产物。

## 2. 执行转换

使用 `svgxml2imagevector` 子命令。对当前项目，默认命令如下（PowerShell 可直接使用；将输入路径替换为实际路径）：

```powershell
valkyrie.bat svgxml2imagevector --input-path=<svg-or-vector-xml> --output-path=app/shared/src/commonMain/kotlin/com/ciyin/app/presentation/theme/iconpack --package-name=com.ciyin.app.presentation.theme.iconpack --iconpack-name=IconPack --output-format=lazy-property --use-compose-colors=true --flatpackage=true --indent-size=4
```

参数意图：

- `--input-path`：单个 SVG/XML 文件或输入目录。
- `--output-path`：固定为 app 的 iconpack 目录；不会写到 `core/material`。
- `--package-name`：固定为 app 目标包名。
- `--iconpack-name=IconPack`：在已测试的 Valkyrie CLI 1.2.0 中生成 `IconPack.X` 扩展属性；当前发布二进制的
  `--help` 仍接受此参数。不要传 `--iconpack`：Valkyrie CLI 1.2.0 会报 `no such option --iconpack`。
- `--output-format=lazy-property`：与当前 app 图标目录中常见写法一致；用户明确要求 backing property
  时才切换为 `backing-property`。
- `--use-compose-colors=true`：优先使用 Compose 颜色名称。
- `--flatpackage=true`：避免目录输入时生成额外嵌套包目录。
- `--indent-size=4`：保持项目 Kotlin 缩进。

参数版本存在差异：Valkyrie 主分支 changelog 记录过从 `--iconpack-name` 迁移到 `--iconpack`，但实际下载的
CLI 1.2.0 `--help` 仍显示并接受 `--iconpack-name`/`--nested-pack-name`。因此必须先读取本地
help，再使用本地真实支持的等价参数；本项目已用 CLI 1.2.0 验证 `--iconpack-name=IconPack`。不要为了转换而引入
Gradle 插件、Version Catalog 依赖或修改构建配置。

如果本机 help 显示布尔参数要求空格形式，把 `--flag=true` 改为 `--flag true`；除此之外不要随意改变目标包名、输出目录或
IconPack 名称。命令失败时读取完整错误并修正根因，不要静默生成手写替代代码。

## 3. 生成后检查与整理

逐个检查生成结果：

- 文件位于 `app/shared/src/commonMain/kotlin/com/ciyin/app/presentation/theme/iconpack/`，没有误写到
  `build/`、`generated/` 或 `core/material`。
- `package` 正确，且显式导入 `ciyin.material.theme.iconpack.IconPack`。Valkyrie CLI 1.2.0 的实际输出会生成
  `IconPack.X`，但不会自动补充这个跨包导入；必须在生成后添加该 import（可按现有图标文件的 import 顺序插入）。
- 属性接收者是 `IconPack`，属性类型是 `ImageVector`，且图标名是合法且不冲突的 Kotlin 标识符。
- 未意外生成 `@Preview`，除非用户明确要求。
- 若工具生成的公开属性没有中文 KDoc，补充一句简短、准确的中文 KDoc，并补上缺失的
  `import ciyin.material.theme.iconpack.IconPack`；不要手工改写 Valkyrie 生成的路径、填充、描边、透明度或颜色数据。
- 只处理本次生成的文件，不格式化、移动、删除或回滚其他用户改动。

如果输入含有渐变、透明度、文本、`image`、滤镜、mask、动画语义等复杂 SVG 特征，不把“CLI
命令成功”当作“视觉完全一致”。必须至少进行 Kotlin 编译；可行时在 Compose
预览或实际运行环境中做视觉核验，并向用户说明任何未验证项。AnimatedVectorDrawable 的动画行为不应被表述为已由静态
`ImageVector` 保留。

## 4. 验证

生成或整理完成后，优先运行 Windows 下的最窄相关任务：

```powershell
.\gradlew.bat :app:shared:compileCommonMainKotlinMetadata --console=plain
```

其他 shell 使用 `./gradlew :app:shared:compileCommonMainKotlinMetadata --console=plain`
。编译失败时修复包名、导入、声明冲突或工具参数等根因；不要通过删除图标路径来掩盖错误。若用户只要求输出到临时目录，无法把该文件纳入模块编译时，要明确说明未完成编译验证。

## 官方参考

- [Valkyrie README](https://github.com/ComposeGears/Valkyrie/blob/main/README.md)：CLI 安装和
  `svgxml2imagevector` 参数。
- [Valkyrie CLI changelog](https://github.com/ComposeGears/Valkyrie/blob/main/tools/cli/CHANGELOG.md)
  ：IconPack 参数迁移记录。
- [Compose ImageVector.Builder](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/vector/ImageVector.Builder)
  ：生成代码所使用的 Compose API。

此 skill 负责转换工作流，不替代设计资产授权、图标语义命名或 UI 视觉验收。