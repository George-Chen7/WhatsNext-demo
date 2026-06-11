# UI Framework: XML/View + MainActivity + BottomNavigationView + Fragment

## 1. Purpose

当前项目是 Android Studio 默认生成的 Kotlin + Jetpack Compose 单 Activity 模板。

为了更好地满足 Android 课程设计要求，建议将 UI 框架调整为传统 Android View 体系：

```text
XML Layout
AppCompatActivity
Fragment
BottomNavigationView
RecyclerView
ViewPager2
```

这样可以更清晰地展示课程知识点：

- Activity 生命周期
- Fragment 创建与切换
- Intent 页面跳转与数据传递
- XML 布局与适配
- Material 底部导航

## 2. Target UI Structure

目标页面结构：

```text
LoginActivity
    -> RegisterActivity
    -> MainActivity

MainActivity
    -> HomeFragment
    -> AssessmentFragment
    -> ReportFragment
    -> ProfileFragment
```

主页面使用：

```text
MainActivity
BottomNavigationView
FragmentContainerView
```

底部导航包含：

```text
首页
测评
报告
我的
```

## 3. Gradle Direction

当前项目启用了 Compose：

```kotlin
alias(libs.plugins.kotlin.compose)
buildFeatures {
    compose = true
}
```

改造时建议：

```text
1. 移除 Compose 插件
2. 移除 Compose 依赖
3. 启用 ViewBinding
4. 增加 AppCompat、Material、Fragment、RecyclerView、ViewPager2 依赖
```

推荐依赖方向：

```kotlin
implementation("androidx.appcompat:appcompat")
implementation("com.google.android.material:material")
implementation("androidx.constraintlayout:constraintlayout")
implementation("androidx.fragment:fragment-ktx")
implementation("androidx.recyclerview:recyclerview")
implementation("androidx.viewpager2:viewpager2")
```

建议启用：

```kotlin
buildFeatures {
    viewBinding = true
}
```

## 4. Activity Plan

### 4.1 LoginActivity

职责：

- 输入用户名和密码
- 查询 Room 本地用户
- 登录成功后写入 SharedPreferences
- 跳转 MainActivity
- 跳转 RegisterActivity

布局文件：

```text
res/layout/activity_login.xml
```

主要组件：

```text
TextInputEditText usernameInput
TextInputEditText passwordInput
MaterialButton loginButton
TextView registerLink
```

### 4.2 RegisterActivity

职责：

- 输入用户名、密码、确认密码
- 校验输入
- 写入 Room
- 注册成功后返回 LoginActivity

布局文件：

```text
res/layout/activity_register.xml
```

主要组件：

```text
TextInputEditText usernameInput
TextInputEditText passwordInput
TextInputEditText confirmPasswordInput
MaterialButton registerButton
```

### 4.3 MainActivity

职责：

- 作为登录后的主容器
- 初始化 BottomNavigationView
- 管理四个 Fragment 切换
- 注册或配合强制下线广播

布局文件：

```text
res/layout/activity_main.xml
```

核心布局：

```xml
<androidx.fragment.app.FragmentContainerView />
<com.google.android.material.bottomnavigation.BottomNavigationView />
```

## 5. Fragment Plan

### 5.1 HomeFragment

职责：

- 展示欢迎语
- 展示头像
- 展示本地 banner 轮播
- 展示最近测评结果

布局文件：

```text
res/layout/fragment_home.xml
```

列表项：

```text
res/layout/item_banner.xml
```

涉及组件：

```text
ViewPager2
MaterialCardView
```

### 5.2 AssessmentFragment

职责：

- 展示 MBTI 测评入口
- 展示霍兰德测评入口
- 展示最近测评记录
- 点击入口跳转 QuestionActivity

布局文件：

```text
res/layout/fragment_assessment.xml
```

涉及组件：

```text
MaterialCardView
RecyclerView
```

### 5.3 ReportFragment

职责：

- 展示最新 AI 报告
- 生成报告
- 保存报告
- 分享报告
- 导出报告
- 进入历史报告页面

布局文件：

```text
res/layout/fragment_report.xml
```

涉及组件：

```text
NestedScrollView
TextView
MaterialButton
Toolbar 或顶部操作区
```

### 5.4 ProfileFragment

职责：

- 展示用户头像
- 展示用户名
- 修改资料
- 历史报告入口
- 关于我们
- 模拟强制下线

布局文件：

```text
res/layout/fragment_profile.xml
```

涉及组件：

```text
ImageView
TextView
MaterialButton
MaterialCardView
```

## 6. Navigation Design

底部导航菜单：

```text
res/menu/menu_bottom_nav.xml
```

菜单项：

```text
nav_home
nav_assessment
nav_report
nav_profile
```

MainActivity 中根据菜单项切换 Fragment：

```text
nav_home       -> HomeFragment
nav_assessment -> AssessmentFragment
nav_report     -> ReportFragment
nav_profile    -> ProfileFragment
```

Fragment 切换策略：

```text
第一版：replace Fragment，简单稳定
第二版：show/hide Fragment，保留页面状态
```

课程 Demo 建议第一版使用 `replace`，代码更清晰，问题更少。

## 7. Layout Resource Plan

建议布局文件：

```text
res/layout/activity_login.xml
res/layout/activity_register.xml
res/layout/activity_main.xml
res/layout/activity_question.xml
res/layout/activity_report_detail.xml
res/layout/activity_report_history.xml
res/layout/activity_edit_profile.xml

res/layout/fragment_home.xml
res/layout/fragment_assessment.xml
res/layout/fragment_report.xml
res/layout/fragment_profile.xml

res/layout/item_banner.xml
res/layout/item_question.xml
res/layout/item_report.xml
res/layout/item_assessment_result.xml
```

菜单文件：

```text
res/menu/menu_bottom_nav.xml
res/menu/menu_report.xml
```

Drawable：

```text
res/drawable/bg_card.xml
res/drawable/bg_login.xml
res/drawable/ic_home.xml
res/drawable/ic_assessment.xml
res/drawable/ic_report.xml
res/drawable/ic_profile.xml
```

## 8. UI Package Plan

后续代码文件建议：

```text
ui/login/
    LoginActivity.kt
    LoginViewModel.kt

ui/register/
    RegisterActivity.kt
    RegisterViewModel.kt

ui/main/
    MainActivity.kt

ui/home/
    HomeFragment.kt
    BannerAdapter.kt

ui/assessment/
    AssessmentFragment.kt
    QuestionActivity.kt
    QuestionAdapter.kt
    AssessmentResultActivity.kt

ui/report/
    ReportFragment.kt
    ReportDetailActivity.kt
    ReportHistoryActivity.kt
    ReportHistoryAdapter.kt

ui/profile/
    ProfileFragment.kt
    EditProfileActivity.kt
    AboutActivity.kt
```

说明：

- 本文档只描述 UI 框架，不直接创建上述代码文件。
- 真正开发时应先创建最小可运行骨架，再逐页补充功能。

## 9. Migration Steps

建议改造顺序：

```text
1. 修改 Gradle，移除 Compose，增加 View 体系依赖。
2. 创建 activity_main.xml。
3. 创建 menu_bottom_nav.xml。
4. 将 MainActivity 从 ComponentActivity 改为 AppCompatActivity。
5. 创建 HomeFragment、AssessmentFragment、ReportFragment、ProfileFragment。
6. 在 MainActivity 中实现 BottomNavigationView 切换。
7. 创建 LoginActivity 和 RegisterActivity。
8. 在 Manifest 中将 LoginActivity 设置为启动页，或使用 Splash/Launcher 判断登录状态。
9. 补充 ViewPager2、RecyclerView 题目列表和业务页面。
10. 编译运行，确认页面跳转和 Fragment 切换正常。
```

## 10. Verification Checklist

改造完成后需要验证：

```text
App 可以编译
启动后进入登录页或主页面
BottomNavigationView 四个菜单可点击
Fragment 切换不崩溃
返回键行为符合预期
横竖屏或不同尺寸下布局不严重错位
RecyclerView 题目或历史列表能正常显示
ViewPager2 能正常显示本地图片
```

## 11. Course Demonstration Value

该 UI 框架能在答辩中明确展示：

```text
Activity 之间的转换与数据传递
Fragment 的使用
底部菜单导航
多媒体轮播
不同页面模块的组织方式
完整 App 的工程结构
```
