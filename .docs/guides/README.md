# 专题指南

本目录记录可复用技术的使用方式。指南中的完整 DSL/API 以对应模块源码和测试为事实来源；示例仅在明确标注已验证时视为可直接编译。

| 指南                                                                      | 适用范围                                 | 主要源码入口                                                                                                                            |
|---------------------------------------------------------------------------|------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| [FlowRedux2 状态机](flow-redux.md)                                        | 状态机 DSL、状态转换和测试               | [StateMachineMviViewModel.kt](../../core/ui-foundation/src/commonMain/kotlin/ciyin/ui/foundation/viewmodel/StateMachineMviViewModel.kt) |
| [KoinBoot 介绍](koinboot-introduce.md)                                    | KoinBoot 的配置、生命周期和模块装配      | [KoinBoot.kt](../../component/koin/src/commonMain/kotlin/ciyin/koin/KoinBoot.kt)                                                        |
| [KoinBoot 第三方库适配](koinboot-third-party-library-adaptation-guide.md) | 自动配置、生命周期扩展和 BootInitializer | [KoinAutoConfiguration.kt](../../component/koin/src/commonMain/kotlin/ciyin/koin/configuration/KoinAutoConfiguration.kt)                |

## 阅读边界

- [MVI 与 ViewModel](../contributing/mvi.md) 只说明如何选择和接入 StateMachineMviViewModel；FlowRedux
  DSL 以本目录指南为准。
- KoinBoot
  介绍适合先了解概念；需要新增适配时继续阅读[第三方库适配指南](koinboot-third-party-library-adaptation-guide.md)。
- 指南不是模块 API 清单；修改公共 API 后应同步对应模块 README、skill 和测试。