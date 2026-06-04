# 课程要求映射

## 1. 总体目标

本项目的设计目标是用一个完整 App 覆盖 Android 课程中的核心知识点，并在答辩时能清楚说明每个知识点的应用场景。

## 2. 知识点映射表

| 课程要求 | 项目功能 | 实现方式 |
| --- | --- | --- |
| Activity 之间的转换与数据传递 | 登录、注册、答题、报告详情 | Intent 跳转和参数传递 |
| 能适应不同展示界面 | 首页、测评、报告、我的 | XML 布局、ConstraintLayout、RecyclerView |
| 登录功能 | 登录注册模块 | Room 查询用户，SharedPreferences 保存状态 |
| 强制下线功能 | 我的页面模拟管理员强制下线 | BroadcastReceiver |
| 多样化持久化 | 登录态、用户、测评、报告、导出文件 | SharedPreferences、Room、File Storage |
| 跨程序共享数据 | 分享职业规划报告 | Android Share Intent，FileProvider |
| 多媒体展示 | 首页宣传图轮播 | ViewPager2 + 本地图片 |
| Fragment | 主页面四个 Tab | HomeFragment、AssessmentFragment、ReportFragment、ProfileFragment |
| RecyclerView | 推荐职业、题目、历史报告 | Adapter + item layout |
| 网络请求 | AI 报告生成 | Retrofit / OkHttp 调用大模型 API |
| 后台服务 | 报告导出 | ReportExportService |
| AI 功能模块 | AI 职业规划报告 | CareerAiService |
| 位置服务加分 | 当前城市热门方向 | 第一版模拟城市，第二版可接真实定位 |

## 3. 亮点说明

### 3.1 AI 职业规划报告

项目不是简单展示测试结果，而是将用户资料、MBTI、霍兰德结果和城市方向组合成用户画像，再生成职业规划报告。

体现：

```text
AI 模块
网络请求
本地数据融合
Markdown 报告展示
```

### 3.2 多种持久化自然融合

本项目同时体现：

```text
SharedPreferences 保存登录状态
Room 保存结构化历史数据
File Storage 导出报告
```

每种持久化方式都有明确用途，便于在报告中比较优劣。

### 3.3 强制下线场景自然

强制下线不是孤立的广播 Demo，而是模拟账号安全场景：

```text
管理员强制下线
账号在其他设备登录
清空登录状态
返回登录页
```

### 3.4 跨程序共享报告

报告生成后可以通过系统分享面板发到其他 App。

体现：

```text
Intent
跨程序数据共享
FileProvider
```

### 3.5 位置服务变通

第一版使用模拟城市，降低权限和真机环境复杂度。

答辩说明：

```text
真实定位不是核心功能。
课程 Demo 中使用模拟城市可以体现位置服务思想。
后续可接 FusedLocationProvider 或 Android LocationManager。
```

## 4. 可舍弃或简化的功能

根据课设要求，不必完全复刻参考 App。

可以简化：

```text
复杂社区功能
完整聊天机器人
真实地图定位
云端账号同步
复杂推荐算法
```

保留原因：

```text
这些功能不能显著增加课程知识点覆盖，反而会增加不稳定性。
```

