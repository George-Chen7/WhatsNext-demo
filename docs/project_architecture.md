# AI Career Planner Project Architecture

## 1. Project Goal

AI Career Planner（AI 职业规划助手）是一个 Android 课程设计 Demo。

项目目标是用一个完整 App 串联 Android 课程核心知识点，包括 Activity、Fragment、RecyclerView、BroadcastReceiver、SharedPreferences、Room、File Storage、Intent、网络请求、多媒体展示、数据共享、后台服务和 AI 功能模块。

本项目优先保证：

- 项目结构合理
- 功能完整可运行
- 代码规范
- 界面清晰美观
- 方便课程答辩演示
- 不依赖复杂后台

## 2. Technical Stack

推荐技术方案：

```text
Language: Kotlin
UI: XML Layout + Android View System
Navigation: Activity + Fragment + BottomNavigationView
List: RecyclerView
Database: Room
Preferences: SharedPreferences
File Storage: Internal / External App-specific Storage
Network: Retrofit + OkHttp
Media: ViewPager2 + Local Images
Sharing: Android Share Intent + FileProvider
Background: Service
Broadcast: BroadcastReceiver
AI: CareerAiService with Mock and optional real API implementation
```

当前课程设计更适合使用传统 XML/View 体系，而不是纯 Jetpack Compose。这样更容易在答辩中展示书本知识点。

## 3. Package Structure

目标包结构：

```text
com.example.whatsnextdemo/

ui/
    login/
    register/
    main/
    home/
    assessment/
    report/
    profile/

data/
    database/
        dao/
    model/
    repository/
    local/

network/
    api/

service/

receiver/

utils/
```

说明：

- `ui/` 目录用于放置 Activity、Fragment、Adapter、ViewModel 等界面相关类。
- 本次只创建文档，不生成 `ui/` 下的业务文件。
- 后续创建 `ui/` 文件时，可先创建空文件或最小可编译骨架，再逐步实现页面逻辑。

## 4. Module Design

### 4.1 Login And Register

页面：

```text
LoginActivity
RegisterActivity
```

功能：

- 用户登录
- 用户注册
- 登录状态保存
- 注册用户写入 Room

涉及知识点：

- Activity 跳转
- Intent 数据传递
- SharedPreferences
- Room

SharedPreferences:

```text
isLogin: Boolean
username: String
token: String
```

### 4.2 Main Page

页面：

```text
MainActivity
```

包含四个 Fragment：

```text
HomeFragment
AssessmentFragment
ReportFragment
ProfileFragment
```

功能：

- 底部导航切换
- Fragment 页面管理
- 登录后主入口

涉及知识点：

- Activity
- Fragment
- BottomNavigationView

### 4.3 Home

功能：

- 展示欢迎语
- 展示用户头像
- 展示最近一次测评结果
- 展示 AI 推荐职业卡片
- 展示职业规划宣传图轮播
- 展示模拟城市热门职业方向

涉及知识点：

- RecyclerView
- ViewPager2
- 多媒体本地资源展示
- 本地数据读取

职业推荐示例：

```text
软件工程师
产品经理
数据分析师
AI 工程师
```

### 4.4 Assessment

功能：

- 霍兰德职业兴趣测试
- MBTI 简化测试
- 本地 JSON 题库读取
- 单选答题
- 本地计分
- 保存测评结果

涉及知识点：

- RecyclerView
- assets 文件读取
- Room 数据保存
- Intent 参数传递

题库文件：

```text
assets/holland_questions.json
assets/mbti_questions.json
```

选项：

```text
非常不同意
不同意
一般
同意
非常同意
```

### 4.5 AI Career Report

核心接口：

```kotlin
interface CareerAiService {
    suspend fun generateCareerReport(profile: UserProfile): String
}
```

建议实现：

```text
MockCareerAiService
RealCareerAiService
```

说明：

- `MockCareerAiService` 用于保证课程答辩稳定演示。
- `RealCareerAiService` 用于接入真实大模型 API，作为项目亮点。
- 报告返回 Markdown 格式。

报告内容：

```text
用户画像分析
性格分析
优势分析
劣势分析
推荐职业方向
不推荐职业方向
未来 3 个月行动计划
推荐技能学习路线
```

涉及知识点：

- 网络请求
- Repository 封装
- AI 功能模块
- Markdown 文本展示

### 4.6 Report

功能：

- 展示 AI 生成报告
- 保存报告
- 分享报告
- 导出报告
- 查看历史报告
- 删除历史报告
- 搜索历史报告

涉及知识点：

- Room
- File Storage
- Android Share Intent
- FileProvider
- RecyclerView

导出文件：

```text
career_report.txt
career_report.json
```

### 4.7 Profile

功能：

- 展示用户头像
- 展示用户名
- 修改资料
- 历史报告入口
- 关于我们
- 模拟管理员强制下线

涉及知识点：

- SharedPreferences
- Activity 跳转
- BroadcastReceiver

强制下线流程：

```text
点击“模拟管理员强制下线”
    -> 发送广播
    -> ForceOfflineReceiver 接收
    -> 清空登录状态
    -> 返回 LoginActivity
    -> 弹出提示
```

提示文本：

```text
您的账号已在其他设备登录
请重新登录
```

## 5. Data Layer

### 5.1 Entity: User

```text
id: Long
username: String
password: String
nickname: String?
major: String?
avatarPath: String?
createTime: Long
```

### 5.2 Entity: AssessmentResult

```text
id: Long
username: String
type: String
result: String
scoreDetail: String
createTime: Long
```

### 5.3 Entity: CareerReport

```text
id: Long
username: String
title: String
createTime: Long
content: String
mbti: String
holland: String
```

## 6. Repository Layer

```text
UserRepository
    - register
    - login
    - updateProfile
    - getCurrentUser

AssessmentRepository
    - loadQuestions
    - calculateResult
    - saveResult
    - getLatestResult

CareerReportRepository
    - generateReport
    - saveReport
    - searchReports
    - deleteReport
    - exportReport
    - shareReport

CareerRepository
    - getRecommendedCareers
    - getCityHotDirections
```

## 7. Local Data Sources

```text
SessionManager
    - 负责 SharedPreferences 登录状态

AssetQuestionDataSource
    - 负责读取 assets 中的测评题库

FileReportDataSource
    - 负责报告导出

CityCareerDataSource
    - 负责模拟城市职业方向
```

## 8. Android Knowledge Mapping

```text
Activity:
LoginActivity, RegisterActivity, QuestionActivity, ReportDetailActivity

Fragment:
HomeFragment, AssessmentFragment, ReportFragment, ProfileFragment

RecyclerView:
职业推荐列表, 题目列表, 历史报告列表

BroadcastReceiver:
强制下线功能

SharedPreferences:
登录状态, 当前用户名, 模拟 token, 用户配置

Room:
用户, 测评结果, AI 报告

File Storage:
导出职业规划报告

Intent:
页面跳转, 参数传递, 系统分享

Network:
真实 AI API 请求

Multimedia:
首页图片轮播

Service:
报告后台导出

Location:
模拟城市与热门职业方向
```

## 9. Development Order

```text
1. 调整 Gradle 依赖
2. 创建基础包结构
3. 创建数据模型和 Room 数据库
4. 实现登录注册
5. 实现 MainActivity 和四个 Fragment
6. 实现首页轮播和职业推荐列表
7. 实现测评题库读取和答题页面
8. 实现本地计分和测评结果保存
9. 实现 Mock AI 报告生成
10. 实现报告保存、历史、搜索和删除
11. 实现报告导出和分享
12. 实现强制下线广播
13. 接入可选真实 AI API
14. UI 美化和答辩演示数据准备
```

## 10. Demo Priority

第一版必须完成：

```text
登录注册
SharedPreferences 登录状态
Room 用户表
MainActivity + BottomNavigationView
四个 Fragment
RecyclerView 推荐列表
本地 JSON 测评
测评结果保存
Mock AI 报告
报告保存
报告导出
报告分享
强制下线广播
```

第二版增强：

```text
真实 AI API
历史报告搜索
城市职业方向
后台导出 Service
UI 美化
```

可暂缓：

```text
真实定位
复杂 Markdown 渲染
复杂头像上传
完整聊天机器人
复杂推荐算法
```
