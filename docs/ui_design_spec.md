# UI 设计规范

## 文档边界

本文档描述 AI Career Planner（AI 职业规划助手）的整体视觉风格、颜色系统、组件规范和高保真 UI 生成提示词。

UI 页面框架、Activity / Fragment 结构和底部导航实现方案见：[UI Framework: XML/View + MainActivity + BottomNavigationView + Fragment](./ui_framework_xml_fragment.md)。

## 整体视觉风格

整体风格：

- 简洁
- 专业
- 年轻化
- 带有轻微科技感
- 接近现代 SaaS、求职平台、学习社区类移动端产品
- 适合作为 Android 课程设计 App 的高保真界面参考

布局原则：

- 使用宽松留白
- 页面整体不要拥挤
- 保持清晰的信息层级
- 使用圆角卡片和柔和阴影
- 以真实 App 可用性为优先，不做过度装饰化的落地页视觉

字体与排版：

- 使用 Android 系统默认无衬线字体
- 中文界面接近 HarmonyOS Sans / PingFang SC / Noto Sans CJK 风格
- 标题加粗，层级明确
- 正文使用灰色，降低压迫感
- 按钮文字清晰可读
- 文本排版干净，便于快速扫描

## 颜色系统

### 品牌主色

```text
#051A49
```

用途：

- 深海军蓝 / 午夜蓝
- 品牌识别
- 顶部重点区域
- 重要背景区域
- 主按钮
- 关键链接
- 选中状态
- 底部导航激活状态
- 进度指示

### 高亮蓝色

```text
#051A49
```

用途：

- 选中状态
- 底部导航激活状态
- 主按钮
- 关键链接
- 进度指示
- 测评提交按钮

### 强调玫红 / 洋红色

```text
#E0007A
#D90072
```

用途：

- 推荐标签
- 收藏状态
- AI 亮点标识
- 高优先级提醒
- 小面积视觉强调

### 浅色模式背景

```text
#FFFFFF
#F7F8FA
```

用途：

- 页面背景
- 卡片背景
- 中性内容承载区域
- 表单背景

### 边框与辅助文字

```text
#D9DEE8
#2E3849
#5A6881
```

用途：

- 卡片边框
- 分割线
- 输入框描边
- 深色正文 / 标题文字：#2E3849
- 辅助说明
- 元信息
- 历史记录时间
- 浅色辅助文字：#5A6881

### 深色模式背景

```text
#051A49
```

用途：

- 深色 App 外壳
- 深色卡片
- 夜间模式页面背景

第一版课程 Demo 可以优先完成浅色模式，深色模式作为后续增强。

## 组件风格

### 卡片

- 圆角建议 16dp 到 24dp
- 使用柔和阴影
- 卡片视觉保持轻盈
- 避免卡片套卡片
- 首页职业推荐、测评入口、历史报告都使用统一卡片风格

### 输入框

- 高度约 56dp
- 描边约 1dp 到 1.5dp
- 圆角
- 占位文字清晰
- 登录、注册、资料编辑页面保持一致输入体验
- 保持舒适的移动端点击区域

### 顶部区域

- 顶部区域保留足够空间
- 标题与主要操作保持清晰可见
- 登录页和首页可使用深色品牌背景
- 普通内容页使用浅色背景和清晰标题

### 按钮

- 使用胶囊形状或大圆角矩形
- 主按钮使用高亮蓝色填充
- 次按钮使用浅色背景或描边
- 危险操作使用红色或洋红色小面积强调
- 按钮文字必须清晰可读

### 图标

- 使用简洁线性图标
- 全局图标风格统一
- 底部导航图标应容易识别且轻量
- 图标不要过度复杂，优先服务信息识别

### 间距

- 使用真实移动端 App 间距
- 页面左右边距建议 16dp 到 20dp
- 卡片内部边距建议 16dp
- 模块之间保持 16dp 到 24dp 间距
- 避免密集布局
- 保持页面节奏透气

## 页面视觉重点

本项目页面视觉应围绕：

- 职业发展
- AI 职业规划
- 测评结果
- 推荐职业方向
- 技能学习路线
- 个人档案管理
- 历史报告管理

不同模块的视觉重点：

```text
首页：
强调欢迎语、宣传图轮播、最近测评和推荐职业。

测评：
强调测试入口、测试进度、题目可读性和提交反馈。

报告：
强调 Markdown 报告阅读体验、保存、分享和导出操作。

我的：
强调用户身份、资料管理、历史报告和强制下线测试入口。
```

## 页面规范

### 登录页

视觉目标：

- 简洁
- 安全感
- 有职业规划产品气质

建议：

- 顶部使用品牌主色区域
- 标题突出 AI Career Planner
- 表单使用白色圆角卡片
- 登录按钮使用高亮蓝色
- 注册入口使用文本链接

### 首页

视觉目标：

- 信息丰富但不拥挤
- 有明确的职业规划产品方向

建议：

- 顶部欢迎区
- ViewPager2 图片轮播
- 最近测评结果卡片
- 推荐职业横向或纵向 RecyclerView
- 城市热门方向使用小标签展示

### 测评页

视觉目标：

- 清楚展示测试类型
- 降低答题压力

建议：

- MBTI 和霍兰德使用两张入口卡片
- 每个测试说明控制在 2 到 3 行
- 答题页每题使用单独卡片
- 单选项使用 RadioButton 或 Material 单选样式

### 报告页

视觉目标：

- 适合长文本阅读
- 操作入口明确

建议：

- 使用 ScrollView / NestedScrollView
- 报告内容保持较大行距
- 保存、分享、导出按钮集中在顶部或底部操作区
- 历史报告使用 RecyclerView 列表

### 我的页

视觉目标：

- 类似真实 App 的个人中心
- 管理入口清晰

建议：

- 顶部头像与用户名
- 功能入口使用列表或卡片
- 强制下线测试按钮单独放置，避免误触
- 关于我们使用普通列表项

## 高保真 UI 生成提示词

生成移动端 UI 参考图时可使用：

```text
Design a high-fidelity Android mobile app UI for AI Career Planner, an AI-powered career planning assistant for students.

Overall visual style: clean, professional, youthful, with a subtle technology feeling. Use rounded cards, soft shadows, clear information hierarchy, and generous whitespace. Typography should feel like a modern Android app with clean sans-serif text, bold titles, gray body text, and clear button labels.

The app has a bottom navigation bar with four tabs: 首页, 测评, 报告, 我的.

首页 shows a welcome message, user avatar, local banner carousel, latest assessment result, city-based hot career directions, and AI recommended career cards.
测评 includes MBTI and Holland Code tests, with simple single-choice questions and progress feedback.
报告 displays an AI-generated Markdown career planning report, with save, share, export, history, search, and delete actions.
我的 contains personal profile, edit profile, report history, about page, and a force-offline test button.

Primary colors:
Deep navy / midnight blue for brand identity, selected states, bottom navigation, primary buttons, links, and progress: #051A49.
Magenta accent for recommendation badges, favorite states, AI highlights, and small emphasis: #E0007A, #D90072.
Light mode backgrounds: #FFFFFF, #F7F8FA.
Borders: #D9DEE8.
Primary text: #2E3849.
Secondary text: #5A6881.
Dark mode backgrounds: #051A49.

Component style:
All cards use 16-24dp corner radius.
Input fields are around 56dp tall with 1-1.5dp outlines.
Buttons are capsule-shaped or large rounded rectangles, with blue-filled primary buttons.
Icons are simple, consistent, linear icons.
The layout should not feel crowded and should preserve realistic mobile app spacing.

Visual focus should emphasize career development, AI career planning, assessment results, recommended jobs, skill learning routes, report management, and personal profile management.

Output style: high-fidelity Android app UI screenshot, realistic phone interface, modern SaaS / job platform / learning product style, clean and professional, complete details, suitable as mobile app design reference.
```
