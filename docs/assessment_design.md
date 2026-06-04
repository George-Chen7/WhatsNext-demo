# 测评模块设计

## 1. 模块目标

测评模块用于体现：

- assets 本地 JSON 读取
- 卡片式单题展示
- Activity 跳转和 Intent 参数传递
- 本地算法计分
- Room 保存测评结果
- AI 报告输入数据来源

第一版包含两类测评：

```text
霍兰德职业兴趣测试
MBTI 简化测试
```

## 2. 霍兰德题库处理方案

用户提供的题库文件：

```text
C:/Users/Lenovo/Downloads/holland_questions.json
```

分析结果：

```text
根结构：JSON 数组
题目总数：60
维度数量：6
每个维度题数：10
维度：R, I, A, S, E, C
id 范围：1-60
id 是否唯一：是
reverse 反向题数量：0
```

字段结构：

```json
{
  "id": 1,
  "dimension": "R",
  "dimensionName": "现实型 Realistic",
  "question": "我喜欢使用工具、机器或设备完成实际操作。",
  "reverse": false
}
```

处理建议：

```text
1. 将该文件复制到 app/src/main/assets/holland_questions.json。
2. 保持当前 JSON 数组结构，不需要再包一层 questions。
3. Kotlin 中定义 AssessmentQuestion 数据类直接映射字段。
4. 计分时按 dimension 汇总分数。
5. reverse 字段保留，即使当前全为 false，后续题库支持反向题时无需改模型。
```

推荐模型：

```kotlin
data class AssessmentQuestion(
    val id: Int,
    val dimension: String,
    val dimensionName: String,
    val question: String,
    val reverse: Boolean = false
)
```

## 3. 霍兰德计分规则

用户选项分值：

```text
非常不同意 = 1
不同意 = 2
一般 = 3
同意 = 4
非常同意 = 5
```

正向题：

```text
实际得分 = 用户选择分值
```

反向题：

```text
实际得分 = 6 - 用户选择分值
```

虽然当前题库没有反向题，但代码应保留该逻辑。

维度汇总：

```text
R = 所有 R 题得分总和
I = 所有 I 题得分总和
A = 所有 A 题得分总和
S = 所有 S 题得分总和
E = 所有 E 题得分总和
C = 所有 C 题得分总和
```

结果生成：

```text
按维度分数从高到低排序，取前三个字母作为霍兰德结果。
```

示例：

```text
I: 42
A: 39
S: 35
R: 30
E: 25
C: 21

结果：IAS
```

并列处理：

```text
第一版按固定维度顺序 R, I, A, S, E, C 作为稳定排序兜底。
```

## 4. MBTI 简化测试设计

MBTI 维度：

```text
E / I
S / N
T / F
J / P
```

题库文件：

```text
app/src/main/assets/mbti_questions.json
```

推荐字段：

```json
{
  "id": 1,
  "dimensionA": "E",
  "dimensionB": "I",
  "question": "在聚会中，我通常会主动和别人交流。",
  "reverse": false
}
```

第一版可以每个维度 6-8 题，总题量控制在 24-32 题，方便演示。

计分规则：

```text
正向题：
    4-5 分加到 dimensionA
    1-2 分加到 dimensionB
    3 分不加或两边各加 0.5

简化实现：
    分值 >= 4，加到 dimensionA
    分值 <= 2，加到 dimensionB
    分值 = 3，不计分
```

结果生成：

```text
E 与 I 比较，取高者
S 与 N 比较，取高者
T 与 F 比较，取高者
J 与 P 比较，取高者
```

示例：

```text
I > E
N > S
T > F
J > P

结果：INTJ
```

## 5. 答题页面设计

页面：

```text
QuestionActivity
```

Intent 参数：

```text
assessment_type = MBTI / HOLLAND
```

UI：

```text
顶部标题
进度说明
返回按钮
卡片式题目区域
单选选项
上一题 / 下一题 / 提交按钮
```

交互：

```text
一次只展示一道题
点击下一题前必须完成当前题
点击上一题可回看和修改已答题目
点击返回按钮或系统返回键时，如果已有答题内容，弹窗提示当前版本不保存未提交答题记录
```

提交校验：

```text
所有题目必须作答
未完成时 Toast 提示
提交后保存结果
```

暂存记录：

```text
当前设计文档和数据库草案只包含最终测评结果 AssessmentResult，
没有答题草稿表或 SharedPreferences 草稿 key。
第一版不实现未提交答题暂存，避免增加额外数据结构影响答辩主流程。
```

## 6. 结果保存

保存到 Room：

```text
AssessmentResult
```

字段示例：

```text
username = george
type = HOLLAND
result = IAS
scoreDetail = {"R":30,"I":42,"A":39,"S":35,"E":25,"C":21}
createTime = 当前时间戳
```

## 7. 与 AI 报告的关系

生成 AI 报告时读取：

```text
当前用户基础信息
最新 MBTI 结果
最新霍兰德结果
模拟城市职业方向
```

如果缺少测评结果：

```text
提示用户先完成测评
或使用默认演示数据生成 Mock 报告
```
