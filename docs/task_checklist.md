# 任务清单

## Phase 0: 项目准备

- [ ] 确认当前项目可以在 Android Studio 中正常打开。
- [ ] 确认 JDK、Gradle、Android SDK 配置正常。
- [ ] 确认当前默认 Compose 模板能编译。
- [ ] 确认 `docs/` 设计文档完整。
- [ ] 确认 `.gitignore` 不会误忽略需要提交的项目文件。

## Phase 1: UI 框架改造

- [ ] 修改 Gradle 配置，移除 Compose 相关插件和依赖。
- [ ] 增加 AppCompat、Material、Fragment、RecyclerView、ViewPager2 依赖。
- [ ] 启用 ViewBinding。
- [ ] 将 `MainActivity` 从 Compose 页面改为 XML/View 页面。
- [ ] 创建 `activity_main.xml`。
- [ ] 创建 `menu_bottom_nav.xml`。
- [ ] 创建 `HomeFragment` 空页面。
- [ ] 创建 `AssessmentFragment` 空页面。
- [ ] 创建 `ReportFragment` 空页面。
- [ ] 创建 `ProfileFragment` 空页面。
- [ ] 实现 BottomNavigationView 四个 Fragment 切换。
- [ ] 编译运行，确认主框架无崩溃。

## Phase 2: 基础包结构

- [ ] 创建 `ui/login/`。
- [ ] 创建 `ui/register/`。
- [ ] 创建 `ui/main/`。
- [ ] 创建 `ui/home/`。
- [ ] 创建 `ui/assessment/`。
- [ ] 创建 `ui/report/`。
- [ ] 创建 `ui/profile/`。
- [ ] 创建 `data/database/dao/`。
- [ ] 创建 `data/model/`。
- [ ] 创建 `data/repository/`。
- [ ] 创建 `data/local/`。
- [ ] 创建 `network/api/`。
- [ ] 创建 `service/`。
- [ ] 创建 `receiver/`。
- [ ] 创建 `utils/`。

## Phase 3: 数据层与 Room

- [ ] 创建 `User` Entity。
- [ ] 创建 `AssessmentResult` Entity。
- [ ] 创建 `CareerReport` Entity。
- [ ] 创建 `UserDao`。
- [ ] 创建 `AssessmentDao`。
- [ ] 创建 `CareerReportDao`。
- [ ] 创建 `AppDatabase`。
- [ ] 创建 `UserRepository`。
- [ ] 创建 `AssessmentRepository`。
- [ ] 创建 `CareerReportRepository`。
- [ ] 编译确认 Room 配置正常。

## Phase 4: 登录注册

- [ ] 创建 `SessionManager`。
- [ ] 创建 `LoginActivity`。
- [ ] 创建 `activity_login.xml`。
- [ ] 创建 `RegisterActivity`。
- [ ] 创建 `activity_register.xml`。
- [ ] 实现注册表单校验。
- [ ] 实现用户注册写入 Room。
- [ ] 实现登录校验。
- [ ] 登录成功后保存 `isLogin`、`username`、`token`。
- [ ] 实现启动时登录状态判断。
- [ ] 编译运行，验证注册、登录、重新打开 App 保持登录。

## Phase 5: 首页

- [ ] 创建首页布局 `fragment_home.xml`。
- [ ] 创建职业推荐模型。
- [ ] 创建 `CareerAdapter`。
- [ ] 创建职业推荐 item 布局。
- [ ] 创建 banner 数据模型。
- [ ] 创建 `BannerAdapter`。
- [ ] 创建 banner item 布局。
- [ ] 准备本地 banner 图片资源。
- [ ] 使用 ViewPager2 展示 banner 轮播。
- [ ] 使用 RecyclerView 展示推荐职业。
- [ ] 展示当前用户名。
- [ ] 展示最近测评结果。
- [ ] 展示模拟城市热门职业方向。
- [ ] 编译运行，验证首页展示正常。

## Phase 6: 测评题库

- [ ] 创建 `app/src/main/assets/`。
- [ ] 将 `C:/Users/Lenovo/Downloads/holland_questions.json` 复制为 `app/src/main/assets/holland_questions.json`。
- [ ] 创建 `mbti_questions.json`。
- [ ] 创建 `AssessmentQuestion` 模型。
- [ ] 创建 `AssetQuestionDataSource`。
- [ ] 实现 assets JSON 读取。
- [ ] 验证霍兰德题库 60 题可正常解析。
- [ ] 验证 MBTI 题库可正常解析。

## Phase 7: 答题与计分

- [ ] 创建 `QuestionActivity`。
- [ ] 创建 `activity_question.xml`。
- [ ] 创建 `QuestionAdapter`。
- [ ] 创建题目 item 布局。
- [ ] 实现 Intent 传递测评类型。
- [ ] 实现单选题交互。
- [ ] 实现提交前未答题校验。
- [ ] 实现霍兰德 RIASEC 计分。
- [ ] 实现 MBTI 四维度计分。
- [ ] 保存测评结果到 Room。
- [ ] 创建结果展示页面或结果弹窗。
- [ ] 编译运行，验证两类测评主流程。

## Phase 8: AI 报告

- [ ] 创建 `UserProfile` 模型。
- [ ] 创建 `CareerAiService` 接口。
- [ ] 创建 `MockCareerAiService`。
- [ ] 创建 `RealCareerAiService` 占位实现。
- [ ] 创建报告 Prompt 模板。
- [ ] 在 `ReportFragment` 中生成用户画像。
- [ ] 实现 Mock Markdown 报告生成。
- [ ] 展示报告内容。
- [ ] 保存报告到 Room。
- [ ] 编译运行，验证无网络也能生成报告。

## Phase 9: 历史报告

- [ ] 创建 `ReportHistoryActivity`。
- [ ] 创建 `activity_report_history.xml`。
- [ ] 创建 `ReportHistoryAdapter`。
- [ ] 创建历史报告 item 布局。
- [ ] 实现历史报告列表。
- [ ] 创建 `ReportDetailActivity`。
- [ ] 创建 `activity_report_detail.xml`。
- [ ] 实现报告详情展示。
- [ ] 实现历史报告搜索。
- [ ] 实现历史报告删除。
- [ ] 编译运行，验证历史报告增删查。

## Phase 10: 导出与分享

- [ ] 创建 `FileReportDataSource`。
- [ ] 实现报告导出为 `career_report.txt`。
- [ ] 可选实现报告导出为 `career_report.json`。
- [ ] 创建 `ReportExportService`。
- [ ] 在 Manifest 注册 Service。
- [ ] 实现 Report 页面导出按钮。
- [ ] 实现文本 Share Intent。
- [ ] 配置 FileProvider。
- [ ] 实现文件分享。
- [ ] 编译运行，验证报告可导出和分享。

## Phase 11: 我的页面与强制下线

- [ ] 创建 `fragment_profile.xml`。
- [ ] 展示用户头像。
- [ ] 展示用户名。
- [ ] 创建 `EditProfileActivity`。
- [ ] 创建 `AboutActivity`。
- [ ] 创建历史报告入口。
- [ ] 创建强制下线按钮。
- [ ] 创建 `ForceOfflineReceiver`。
- [ ] 在 Manifest 注册 Receiver。
- [ ] 点击按钮发送强制下线广播。
- [ ] Receiver 清空登录状态。
- [ ] 返回 LoginActivity 并清空 Activity 栈。
- [ ] 弹出强制下线提示。
- [ ] 编译运行，验证强制下线流程。

## Phase 12: 网络 AI 增强

- [ ] 创建 `NetworkClient`。
- [ ] 创建 `AiApi`。
- [ ] 创建 `AiRequest`。
- [ ] 创建 `AiResponse`。
- [ ] 接入 Retrofit / OkHttp。
- [ ] 配置 API Key 读取方式。
- [ ] 实现 `RealCareerAiService`。
- [ ] 网络失败时回退 Mock。
- [ ] 增加 loading 和错误提示。
- [ ] 编译运行，验证 Mock 和真实 API 模式切换。

## Phase 13: UI 美化

- [ ] 按 `ui_design_spec.md` 统一颜色。
- [ ] 统一按钮样式。
- [ ] 统一卡片圆角和阴影。
- [ ] 优化登录页视觉。
- [ ] 优化首页信息层级。
- [ ] 优化测评答题页面可读性。
- [ ] 优化报告阅读体验。
- [ ] 优化我的页面入口布局。
- [ ] 适配不同屏幕尺寸。
- [ ] 检查空状态、加载状态和错误状态。

## Phase 14: 测试与答辩准备

- [ ] 完整跑通注册登录流程。
- [ ] 完整跑通霍兰德测评流程。
- [ ] 完整跑通 MBTI 测评流程。
- [ ] 完整跑通 AI 报告生成流程。
- [ ] 完整跑通报告保存、历史、搜索、删除。
- [ ] 完整跑通报告导出和分享。
- [ ] 完整跑通强制下线。
- [ ] 准备默认演示账号。
- [ ] 准备默认演示测评结果。
- [ ] 准备 3-5 分钟答辩演示路线。
- [ ] 对照 `course_requirement_mapping.md` 检查课程知识点覆盖。

