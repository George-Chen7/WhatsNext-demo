# 功能模块设计

## 1. 功能总览

AI Career Planner 由五个核心部分组成：

```text
登录注册
首页
测评
报告
我的
```

登录注册负责用户身份和登录状态；首页负责展示用户概览和职业推荐；测评负责 MBTI 与霍兰德测试；报告负责 AI 职业规划报告生成、保存、分享和导出；我的页面负责用户资料、历史入口和强制下线测试。

## 2. 登录注册模块

### 登录页面

页面：

```text
LoginActivity
```

输入：

```text
用户名
密码
```

操作：

```text
登录
跳转注册
```

逻辑：

```text
1. 用户输入用户名和密码。
2. UserRepository 查询 Room 中的用户。
3. 校验成功后写入 SharedPreferences。
4. 跳转 MainActivity。
5. 校验失败时展示错误提示。
```

保存字段：

```text
isLogin
username
token
```

### 注册页面

页面：

```text
RegisterActivity
```

输入：

```text
用户名
密码
确认密码
```

逻辑：

```text
1. 校验用户名是否为空。
2. 校验两次密码是否一致。
3. 查询用户名是否已存在。
4. 保存 User 到 Room。
5. 注册成功后返回登录页。
```

## 3. 首页模块

页面：

```text
HomeFragment
```

展示：

```text
欢迎语
用户头像
职业规划宣传图轮播
最近一次测评结果
AI 推荐职业卡片
当前城市和热门职业方向
```

核心组件：

```text
ViewPager2
RecyclerView
MaterialCardView
```

职业推荐第一版使用本地静态数据：

```text
软件工程师
产品经理
数据分析师
AI 工程师
```

后续可以根据 MBTI、霍兰德结果和城市方向动态排序。

## 4. 测评模块

页面：

```text
AssessmentFragment
QuestionActivity
AssessmentResultActivity
```

测试类型：

```text
MBTI 简化测试
霍兰德职业兴趣测试
```

答题选项：

```text
非常不同意 = 1
不同意 = 2
一般 = 3
同意 = 4
非常同意 = 5
```

流程：

```text
1. 用户在测评页选择测试类型。
2. 通过 Intent 把测试类型传入 QuestionActivity。
3. QuestionActivity 从 assets 加载题库。
4. RecyclerView 展示题目。
5. 用户提交后本地计分。
6. 保存 AssessmentResult 到 Room。
7. 跳转结果页或回到测评页展示最新结果。
```

## 5. 报告模块

页面：

```text
ReportFragment
ReportDetailActivity
ReportHistoryActivity
```

功能：

```text
生成 AI 报告
展示 Markdown 报告
保存报告
分享报告
导出报告
搜索历史报告
删除历史报告
```

第一版报告生成策略：

```text
优先使用 MockCareerAiService，保证离线可演示。
真实 API 接入作为增强，避免答辩时受网络或密钥影响。
```

## 6. 我的模块

页面：

```text
ProfileFragment
EditProfileActivity
AboutActivity
```

功能：

```text
展示头像
展示用户名
修改资料
查看历史报告
关于我们
模拟管理员强制下线
```

强制下线流程：

```text
点击按钮
发送广播
ForceOfflineReceiver 接收广播
清空登录状态
跳转 LoginActivity
展示提示
```

提示：

```text
您的账号已在其他设备登录
请重新登录
```

