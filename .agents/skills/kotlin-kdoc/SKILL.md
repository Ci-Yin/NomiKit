---
name: kotlin-kdoc
description: 编写、审计和修复 Kotlin 中文 KDoc，覆盖各可见性声明、构造属性、参数、返回值、扩展和 Compose API。新增或重构 Kotlin 声明、检查文档注释规范、修复缺失或错误契约时使用；不负责普通实现注释的机械转换、全仓格式化或无关历史代码重写。
---

# Kotlin 中文文档注释

以[仓库规则](<../../rules/AGENTS.md>)
和[代码风格](<../../../.docs/contributing/code-style.md#八文档注释规范>)
为基础。先读实际声明、默认参数、调用方和实现，再写契约；不能把“有一句中文”当成文档完整。

## 范围与格式

- 开始时明确检查文件/模块和审计或修复模式；发现范围外问题先报告，不默认重写全仓。
- 顶层和成员类型、函数、属性、类型别名、枚举项及扩展，无论 public/internal/protected/private，都使用中文
  KDoc；override、测试辅助声明和 Preview 也不自动豁免。局部临时变量、匿名 lambda 的步骤不强制 API 文档。
- 声明 KDoc 使用多行块：`/**`、每行 ` *`、` */` 分别独立成行；注释紧邻声明之前，有注解时放在注解之前。不得与声明同行、在
  primary constructor 参数列表中插入 KDoc，或用普通 // 冒充声明文档。
- 构造 val/var 属性统一在类型 KDoc 中写 @property；普通构造入参写 @param。已被类级 @property
  完整覆盖的属性不重复写独立 KDoc。成员属性仍写自身说明。
- 保留有意义的实现 //、字段分组标题和分支说明；不要把每条实现注释都升级成 KDoc。

## 内容与标签

| 声明                 | 应记录的契约                                                                                                         |
|----------------------|----------------------------------------------------------------------------------------------------------------------|
| 类型 / object / enum | 职责、适用边界、关键不变量；构造属性 @property，普通入参 @param；枚举项有独立中文说明                                |
| 函数                 | 摘要和每个真实参数的 @param；回调说明触发时机、接收者环境与状态职责；泛型参数在语义需要时补充                        |
| 返回值               | 非 Unit 结果写 @return，说明值的含义、空值/失败/新实例等约定；推断返回类型也要检查，不为纯 Compose/Unit 填“无返回值” |
| 扩展                 | 必要时 @receiver 说明接收者约束，不伪造 @param ReceiverType；返回新值还是修改原值要说清                              |
| 属性                 | 值的用途、单位/范围、缺省含义；缓存、CompositionLocal、可变/计算属性说明生命周期或来源                               |
| 异常                 | @throws 写真实可达的业务边界或 require 校验，不虚构异常、不罗列底层所有潜在错误                                      |

- 简单声明允许简短的多行摘要，不为凑行数重复函数名。复杂 API 必须描述可观察语义，不能把“不是某实现”的历史说明当唯一文档。
- 参数名与声明一致，移除过时/不存在标签；标签内容不能为空或仅重复参数名。内部符号用 [Symbol]
  链接，需要时限定接收者/类型，避免添加只供文档使用的 import。
- 工厂和 copy 说明默认来源、显式覆盖与返回对象；委托/转发也要让调用方看到完整参数契约，不因“仅转发”删掉旧文档。
- Compose 参数明确 null、Color.Unspecified、Color.Transparent 的不同含义，原生默认由谁决定、slot 的
  Scope/内容样式、状态归属和不支持覆盖的失败边界。主题输入、解析结果、继承/重置规则不能混写。
- 描述当前实际行为，不夹带本轮测试数量、迁移日志、过时名称或没有证据的保证。

## 例子

反例（合法 Kotlin 语法不代表满足本项目文档格式）：

```kotlin
/** 原生排版。 */ val typography: Typography
// 在参数列表内写 /** 候选标识。 */ val id 同样不采用。
```

构造属性示例：

```kotlin
/**
 * 当前组合分支捕获的原生主题基线。
 *
 * 嵌套切换主题时复用此基线，避免把显式覆盖后的结果当作原生默认。
 *
 * @property typography 原生主题的排版角色。
 * @property cornerRadius 原生主题的圆角配置。
 */
@Immutable
private data class NativeBaseline(
    val typography: Typography,
    val cornerRadius: CornerRadius,
)
```

带返回值和失败边界的扩展示例（示意，不是仓库 API）：

```kotlin
/**
 * 截取前若干个候选项，不修改原列表。
 *
 * @receiver 按展示顺序排列的候选项。
 * @param count 最多保留的项数，必须非负；超过列表长度时保留全部。
 * @return 包含前 count 项或全部现有项的列表。
 * @throws IllegalArgumentException count 为负数时抛出。
 */
fun <T> List<T>.takeCandidates(count: Int): List<T> = take(count)
```

## 修复与验证

1. 明确文件范围，读取现状；批量修复前在系统临时目录保存基线，不把 Git HEAD 的历史差异误当本次改动。
2. 逐声明核对格式、摘要、参数/属性/返回/接收者及异常说明；允许把已有内嵌属性说明搬到 @property，不改变代码
   token、签名、默认值、可见性或测试断言。
3. 用[只读检查器](<scripts/check_kdoc.py>)扫描指定文件/目录；--manifest 可读取显式路径数组，--compare-root
   可与对应目录结构的快照比较非注释 token：

```text
python -B -X utf8 .agents/skills/kotlin-kdoc/scripts/check_kdoc.py <source-path> --json
python -B -X utf8 .agents/skills/kotlin-kdoc/scripts/check_kdoc.py --manifest <paths.json> --compare-root <snapshot-root> --json
```

检查器自身的回归用例可执行
`python -B -X utf8 -m unittest discover -s .agents/skills/kotlin-kdoc/scripts -p test_check_kdoc.py`
；这些用例验证扫描器，不代表 Kotlin 项目测试已运行。

4. 检查器只识别词法/结构候选，不是 Kotlin 编译器或完整 KDoc 语义验证；人工核对推断返回、重载/泛型、委托、注解关联、类级
   @property、符号链接和空值/失败契约。不得通过删除真实注释、改变实现或忽略报告来凑零结果。
5. 对纯注释改动核对非注释 token 和换行敏感位置；token 相同不能单独证明换行语义，避免改动表达式布局。检查本次
   diff 和空白。仅注释变更不默认启动 Gradle/Dokka/设备或全仓测试；工具结果不能取代文档质量复核。
6. 报告实际覆盖文件、修复数量、硬错误/人工候选、验证局限和范围外问题；未执行构建不能写成构建通过。清理本任务临时文件，不改
   index、不提交。
