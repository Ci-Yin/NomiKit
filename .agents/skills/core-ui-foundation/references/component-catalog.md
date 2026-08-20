# UI 公共组件目录

源码根目录：`core/ui-foundation/src/commonMain/kotlin/ciyin/ui/foundation`

| 使用意图          | 首选 API                                                                                                                 | 说明                               |
|---------------|------------------------------------------------------------------------------------------------------------------------|----------------------------------|
| 标题布局          | `widget.TitleScaffold`                                                                                                 | 通用标题行和内容布局，不包含产品主题。              |
| 字符串菜单 Chip    | `widget.MenuChip`                                                                                                      | 基础 Material FilterChip，输入字符串菜单项。 |
| 通用菜单          | `widget.ExposedMenu`、`widget.MenuText`                                                                                 | Exposed Dropdown Menu 及菜单文本选择。   |
| 通用按钮          | `widget.Button`、`widget.SmallButton`、`widget.OutLineButton`、`widget.FlexButton`                                        | 使用 `ButtonStyle` 和主题令牌配置。        |
| 文本与表面         | `widget.Title`、`widget.ContentBody`、`widget.SingleText`、`widget.BoxSurface`、`widget.ColumnSurface`、`widget.RowSurface` | 通用文本和表面容器。                       |
| 刷新容器          | `layout.refresh.RefreshLayout`、`PullToRefresh`、`VerticalRefreshableLayout`                                             | 与产品文案和具体资源无关的拖动刷新能力。             |
| 网格            | `layout.grid.HorizontalGrid`、`VerticalGrid`、`SimpleGridCells`                                                          | 有限约束下的跨平台网格布局。                   |
| 滚动条           | `widget.scrollbar.Scrollbar`、`widget.scrollbar.usage.JKLazyColumn`、`JKLazyGrid`、`JKLazyVerticalStaggeredGrid`          | 通用滚动条、拖动和 Lazy 容器。               |
| 窗口尺寸          | `currentWindowWidth`、`currentWindowHeight`、`currentWindowSize`、`classifyWindowWidth`                                   | 跨平台窗口尺寸和断点分类。                    |
| 系统 UI 和效果     | `rememberSystemUiController`、`SystemUiControllerEffect`、`KeepScreenOnEffect`、`OnLifecycleEvent`                        | 平台抽象后的系统栏和生命周期效果。                |
| ViewModel/MVI | `AbsMvvmViewModel`、`AbsMviViewModel`、`StateMachineMviViewModel`、`HasBackgroundScope`                                   | 按页面复杂度选择状态和生命周期基类。               |

## 反重复检查

出现以下情况时先停下来重新检索：

- 新函数只把 Material3 组件包了一层，却没有跨平台、主题或通用状态价值；
- 新布局复制了 `TitleScaffold`、`RefreshLayout` 或网格的结构；
- 同一公共能力在多个模块出现不同公开名称或重复实现；
- 通过 `delay`、固定 offset 或吞异常来修复已有组件的交互问题。
