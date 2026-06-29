# AI Career Planner 设计文档索引

本文档目录用于沉淀 AI Career Planner Android 课程设计项目的需求、架构、UI、数据、测评、AI、持久化和开发计划。

## 文档清单

```text
project_architecture.md
    项目总体架构、模块划分、技术栈和开发优先级。

feature_design.md
    功能模块设计，说明登录、首页、测评、报告、我的等模块的页面和职责。

ui_design_spec.md
    UI 视觉规范，包含整体风格、颜色系统、组件规范和高保真 UI 提示词。

ui_framework_xml_fragment.md
    UI 技术框架方案，说明如何从 Compose 模板改成 XML/View + Fragment。

database_design.md
    Room 数据库设计，包含实体、DAO、关系和查询场景。

assessment_design.md
    MBTI 和霍兰德测评设计，包含题库格式、计分规则和结果保存。

ai_report_design.md
    AI 职业规划报告模块设计，包含 CareerAiService、Prompt 和 Mock/真实 API 策略。

persistence_sharing_service_design.md
    SharedPreferences、Room、文件存储、Share Intent、FileProvider、后台服务和广播设计。

course_requirement_mapping.md
    课程知识点与 App 功能的映射，用于课设报告和答辩。

development_plan.md
    分阶段开发计划、验收标准和答辩演示路径。

task_checklist.md
    按实施顺序排列的可勾选任务清单。
```

## 当前实现原则

- 第一版先保证可运行、可演示、无明显崩溃。
- AI 功能必须保留 Mock 实现，真实 API 作为增强。
- 数据优先本地保存，不引入复杂后端。
- UI 框架优先使用 XML/View + Activity + Fragment。
- 每个功能尽量能明确对应一个 Android 课程知识点。
