# 持久化、共享、后台服务与广播设计

## 1. 设计目标

本项目需要体现多种 Android 数据处理方式：

```text
SharedPreferences
Room
File Storage
Share Intent
FileProvider
BroadcastReceiver
Service
```

这些能力要自然融入 App，而不是孤立示例。

## 2. SharedPreferences

类：

```text
SessionManager
PreferencesManager
```

保存内容：

```text
isLogin
username
token
mockAiEnabled
city
```

用途：

```text
登录状态
当前用户
模拟 token
用户配置
AI Mock 开关
模拟城市
```

退出登录或强制下线时清空：

```text
isLogin
username
token
```

## 3. Room

保存内容：

```text
User
AssessmentResult
CareerReport
```

用途：

```text
本地注册用户
测评结果
AI 报告历史
```

优点：

```text
结构化
支持查询
适合列表和历史记录
```

局限：

```text
不适合保存大文件
表结构变化需要迁移
```

## 4. File Storage

用途：

```text
导出职业规划报告
```

推荐导出目录：

```text
context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
```

导出文件：

```text
career_report.txt
career_report.json
```

TXT 适合演示阅读，JSON 适合说明结构化导出。

导出流程：

```text
用户点击导出报告
ReportExportService 执行文件写入
写入成功后 Toast 提示路径
可选：提供分享文件入口
```

## 5. Share Intent

分享内容：

```text
职业规划报告正文
```

第一版可以分享纯文本：

```text
Intent.ACTION_SEND
type = "text/plain"
Intent.EXTRA_TEXT = report.content
```

如果分享文件：

```text
使用 FileProvider 获取 content:// Uri
不要暴露 file:// 路径
添加 FLAG_GRANT_READ_URI_PERMISSION
```

## 6. FileProvider

用途：

```text
跨程序共享导出的报告文件
```

Manifest 中声明 provider。

路径配置：

```text
res/xml/file_paths.xml
```

第一版如果只分享文本，可以暂缓 FileProvider；如果导出后分享文件，则必须实现。

## 7. BroadcastReceiver

类：

```text
ForceOfflineReceiver
```

Action：

```text
com.example.whatsnextdemo.ACTION_FORCE_OFFLINE
```

触发入口：

```text
ProfileFragment -> 模拟管理员强制下线
```

流程：

```text
发送广播
接收广播
清空 SessionManager
弹出提示
跳转 LoginActivity
清空 Activity 栈
```

跳转 Flag：

```text
Intent.FLAG_ACTIVITY_NEW_TASK
Intent.FLAG_ACTIVITY_CLEAR_TASK
```

## 8. 后台服务

类：

```text
ReportExportService
```

用途：

```text
后台导出职业规划报告文件
体现 Service 的课程知识点
```

第一版实现方式：

```text
启动 Service
从 Intent 中读取 reportId 或 reportContent
写入本地文件
导出完成后发送 Toast 或本地广播
停止 Service
```

说明：

```text
报告文件通常不大，严格来说不一定需要后台服务。
但作为课程设计，可以用导出报告这个场景自然体现 Service。
```

## 9. 持久化方式对比

```text
SharedPreferences:
    优点：简单，适合少量 key-value。
    缺点：不适合复杂结构和列表。
    本项目用途：登录状态、配置项。

Room:
    优点：结构化、可查询、适合历史数据。
    缺点：需要 Entity、Dao 和迁移。
    本项目用途：用户、测评结果、AI 报告。

File Storage:
    优点：适合导出和跨程序文件共享。
    缺点：不适合复杂查询。
    本项目用途：导出职业规划报告。
```

