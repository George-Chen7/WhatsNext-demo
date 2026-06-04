# AI 职业规划报告模块设计

## 1. 模块目标

AI 职业规划报告是项目亮点模块，用于体现人工智能功能与 Android 本地数据的结合。

目标：

```text
读取用户基础信息、MBTI 结果和霍兰德结果
生成 Markdown 格式职业规划报告
支持保存、分享、导出和历史查看
```

## 2. 输入数据

输入对象：

```text
UserProfile
```

字段：

```text
name: String
username: String
major: String?
city: String?
mbti: String?
holland: String?
cityHotDirections: List<String>
```

示例：

```json
{
  "name": "George",
  "username": "george",
  "major": "Computer Science",
  "city": "深圳",
  "mbti": "INTJ",
  "holland": "IAS",
  "cityHotDirections": ["互联网", "AI", "硬件研发"]
}
```

## 3. Service 接口

统一接口：

```kotlin
interface CareerAiService {
    suspend fun generateCareerReport(profile: UserProfile): String
}
```

实现类：

```text
MockCareerAiService
RealCareerAiService
```

说明：

- Mock 实现用于离线演示和课程答辩。
- Real 实现用于真实大模型 API。
- Repository 根据配置选择使用 Mock 还是 Real。

## 4. Prompt 模板

```text
你是一名资深职业规划顾问。

请根据以下信息：
1. 用户画像
2. MBTI 结果
3. 霍兰德职业兴趣结果
4. 用户所在城市及热门职业方向

生成：
- 用户画像分析
- 性格分析
- 优势分析
- 劣势分析
- 推荐职业方向
- 不推荐职业方向
- 未来 3 个月行动计划
- 推荐技能学习路线

要求：
1. 返回 Markdown 格式。
2. 内容适合大学生职业规划。
3. 建议具体、可执行。
4. 不要输出医疗、法律或投资建议。
5. 不要编造用户未提供的经历。
```

## 5. 输出结构

Markdown 报告结构：

```markdown
# AI 职业规划报告

## 用户画像分析

## 性格分析

## 优势分析

## 劣势分析

## 推荐职业方向

## 不推荐职业方向

## 未来 3 个月行动计划

## 推荐技能学习路线
```

第一版可以直接用 TextView 展示 Markdown 原文。

第二版可接入 Markdown 渲染库，但不是必需。

## 6. Mock 策略

MockCareerAiService 需要：

```text
不联网
不需要 API Key
返回结构完整的 Markdown
根据 mbti、holland、major、city 做少量模板替换
保证每次答辩都能稳定生成报告
```

Mock 报告不需要追求复杂，只要能体现 AI 模块输入和输出链路。

## 7. 真实 API 策略

RealCareerAiService 通过 Retrofit / OkHttp 调用大模型 API。

注意：

```text
API Key 不写死在代码中
课程 Demo 可从 local.properties、BuildConfig 或设置页输入
网络失败时自动回退 Mock
请求过程展示 loading
失败时给出明确提示
```

## 8. 数据保存

生成报告后保存到：

```text
CareerReport
```

保存字段：

```text
username
title
createTime
content
mbti
holland
```

title 生成规则：

```text
AI 职业规划报告 - yyyy-MM-dd HH:mm
```

## 9. 错误处理

常见场景：

```text
未登录：
    返回登录页或提示重新登录。

没有测评结果：
    提示先完成 MBTI 和霍兰德测评。

网络失败：
    提示网络异常，并允许使用 Mock 报告。

API Key 缺失：
    使用 Mock 报告。

报告为空：
    提示生成失败，请重试。
```

