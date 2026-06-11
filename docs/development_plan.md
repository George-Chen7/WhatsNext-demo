# 开发计划

## 1. 开发原则

```text
先可运行，再补功能。
先 Mock，再接真实 API。
先本地数据，再考虑网络。
先覆盖课程知识点，再做视觉细节。
```

## 2. Phase 1: 工程框架

目标：

```text
把默认 Compose 模板改成 XML/View + Activity + Fragment 框架。
```

任务：

```text
调整 Gradle 依赖
启用 ViewBinding
创建 MainActivity
创建 BottomNavigationView
创建四个 Fragment 空页面
创建基础包结构
```

验收：

```text
App 能编译运行
底部四个 Tab 能切换
页面不崩溃
```

## 3. Phase 2: 登录注册与本地用户

任务：

```text
创建 LoginActivity
创建 RegisterActivity
创建 User Entity 和 UserDao
创建 SessionManager
实现登录状态保存
```

验收：

```text
注册用户能写入 Room
登录成功能进入 MainActivity
重新打开 App 能保持登录
退出或强制下线能清空登录态
```

## 4. Phase 3: 首页

任务：

```text
创建首页布局
实现 banner 轮播
显示当前用户和最近测评结果
```

验收：

```text
首页内容完整
ViewPager2 正常切换图片
最近测评结果能正常展示
```

## 5. Phase 4: 测评

任务：

```text
复制 holland_questions.json 到 assets
创建 mbti_questions.json
实现 AssetQuestionDataSource
实现 QuestionActivity
实现 QuestionAdapter
实现霍兰德计分
实现 MBTI 计分
保存 AssessmentResult
```

验收：

```text
能完成霍兰德测试并生成 RIASEC 前三码
能完成 MBTI 测试并生成四字母结果
结果能保存到 Room
首页能显示最近结果
```

## 6. Phase 5: AI 报告

任务：

```text
创建 UserProfile
创建 CareerAiService
实现 MockCareerAiService
实现 CareerReportRepository
生成 Markdown 报告
保存 CareerReport
```

验收：

```text
没有网络也能生成报告
报告结构完整
报告能保存到 Room
```

## 7. Phase 6: 历史、导出、分享

任务：

```text
实现历史报告列表
实现搜索和删除
实现报告详情页
实现导出 txt/json
实现 Share Intent
配置 FileProvider
```

验收：

```text
历史报告能查看
报告能删除和搜索
报告能导出到文件
报告能通过系统分享面板分享
```

## 8. Phase 7: 强制下线和后台服务

任务：

```text
实现 ForceOfflineReceiver
在我的页面添加强制下线按钮
实现 ReportExportService
补充 Manifest 配置
```

验收：

```text
点击强制下线后返回登录页
登录状态被清空
导出服务能完成文件写入
```

## 9. Phase 8: UI 美化和答辩准备

任务：

```text
按 UI 设计规范统一颜色和卡片
准备默认演示用户
准备默认测评结果或演示流程
准备答辩演示路线
整理课程知识点映射
```

验收：

```text
主流程 3-5 分钟内可演示
无明显崩溃
每个课程知识点都能指出对应页面和代码
```

## 10. 推荐答辩演示路径

```text
1. 注册用户
2. 登录进入首页
3. 展示首页轮播和最近测评区域
4. 进入测评页完成霍兰德或 MBTI
5. 生成 AI 职业规划报告
6. 保存报告
7. 查看历史报告
8. 导出或分享报告
9. 到我的页面模拟强制下线
10. 返回登录页并说明广播机制
```
