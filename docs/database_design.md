# 数据库设计

## 1. 数据库方案

本项目使用 Room 作为本地结构化数据存储方案。

数据库名称：

```text
ai_career_planner.db
```

数据库类：

```text
AppDatabase
```

第一版包含三张核心表：

```text
users
assessment_results
career_reports
```

## 2. User 表

用途：

```text
保存本地注册用户和用户基础资料。
```

字段：

```text
id: Long，主键，自增
username: String，唯一
password: String
nickname: String?
major: String?
avatarPath: String?
createTime: Long
```

索引：

```text
username unique
```

主要查询：

```text
根据 username 查询用户
插入用户
更新用户资料
判断用户名是否存在
```

DAO 草案：

```kotlin
interface UserDao {
    suspend fun insert(user: User): Long
    suspend fun findByUsername(username: String): User?
    suspend fun update(user: User)
}
```

## 3. AssessmentResult 表

用途：

```text
保存用户每次 MBTI 或霍兰德测评结果。
```

字段：

```text
id: Long，主键，自增
username: String
type: String
result: String
scoreDetail: String
createTime: Long
```

字段说明：

```text
type:
    MBTI
    HOLLAND

result:
    MBTI 示例：INTJ
    HOLLAND 示例：IAS

scoreDetail:
    JSON 字符串，保存各维度分数。
```

霍兰德 scoreDetail 示例：

```json
{
  "R": 38,
  "I": 42,
  "A": 31,
  "S": 26,
  "E": 30,
  "C": 22
}
```

MBTI scoreDetail 示例：

```json
{
  "E": 12,
  "I": 18,
  "S": 10,
  "N": 20,
  "T": 21,
  "F": 9,
  "J": 19,
  "P": 11
}
```

主要查询：

```text
插入测评结果
查询当前用户最新测评结果
查询当前用户所有测评历史
按 type 查询最新结果
```

DAO 草案：

```kotlin
interface AssessmentDao {
    suspend fun insert(result: AssessmentResult): Long
    suspend fun getLatestByType(username: String, type: String): AssessmentResult?
    suspend fun getAllByUser(username: String): List<AssessmentResult>
    suspend fun deleteById(id: Long)
}
```

## 4. CareerReport 表

用途：

```text
保存 AI 生成的职业规划报告。
```

字段：

```text
id: Long，主键，自增
username: String
title: String
createTime: Long
content: String
mbti: String
holland: String
```

字段说明：

```text
content:
    Markdown 格式报告正文。

mbti / holland:
    保存生成报告时使用的测评结果，避免后续用户重新测评后旧报告上下文丢失。
```

主要查询：

```text
保存报告
查询最新报告
查询当前用户所有报告
按标题或内容搜索报告
删除报告
```

DAO 草案：

```kotlin
interface CareerReportDao {
    suspend fun insert(report: CareerReport): Long
    suspend fun getLatest(username: String): CareerReport?
    suspend fun getAllByUser(username: String): List<CareerReport>
    suspend fun search(username: String, keyword: String): List<CareerReport>
    suspend fun deleteById(id: Long)
}
```

## 5. 数据关系

第一版不强制使用外键，使用 `username` 关联用户数据，降低课程 Demo 的实现复杂度。

逻辑关系：

```text
User 1 -> N AssessmentResult
User 1 -> N CareerReport
```

如果后续需要增强，可以改为使用 `userId` 外键。

## 6. 数据库版本策略

第一版：

```text
version = 1
fallbackToDestructiveMigration 可在课程 Demo 阶段使用
```

正式提交前建议：

```text
固定 Entity 字段
避免频繁改表
准备一组演示数据
```

