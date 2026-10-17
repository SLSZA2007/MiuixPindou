# 游艺文创 · 拼豆计时计费（Compose + Miuix 版）

用 Kotlin + Jetpack Compose + [compose-miuix-ui](https://github.com/compose-miuix-ui/miuix) 重写的版本，功能对齐 WebView 版。

## 环境要求

| 项目 | 版本 |
|---|---|
| Android Studio | 最新稳定版 / Canary（需支持 AGP 9.x，AGP 9.4 需要较新的 Studio） |
| JDK | 17 或 21 |
| Android SDK | compileSdk 37（Android 16/17） |

> 因为 miuix 0.9.4 要求 compileSdk 37，工程已用 AGP 9.4.1 + Gradle 9.8.1，请务必用**支持 AGP 9 的较新 Android Studio**。

## 构建步骤

1. **解压** `MiuixPindou.zip` 到任意目录。

2. **用 Android Studio 打开**：`File → Open` → 选择解压出来的 `MiuixPindou` 文件夹（选到含 `settings.gradle.kts` 的层级）。

3. **等 Gradle 同步**：Android Studio 会提示下载 Gradle 8.14 和依赖（首次约下载 1~2 GB，需联网，耐心等）。若提示 "Gradle wrapper not found"，选择「Use local gradle distribution」或让它自动下载。

4. **构建 APK**：
   - 菜单 `Build → Build Bundle(s) / APK(s) → Build APK(s)`
   - 或命令行：在项目根目录执行
     ```bash
     ./gradlew assembleDebug
     ```
   - 产物在 `app/build/outputs/apk/debug/app-debug.apk`

5. **安装**：把 `app-debug.apk` 传到手机安装即可（首次会请求通知权限，用于计时提醒）。

## 工程结构

```
MiuixPindou/
├── settings.gradle.kts          # 项目配置
├── build.gradle.kts             # 根构建（插件版本）
├── gradle.properties
├── gradle/wrapper/              # Gradle wrapper 配置（8.14）
└── app/
    ├── build.gradle.kts         # app 模块（依赖、SDK 版本）
    └── src/main/
        ├── AndroidManifest.xml
        ├── kotlin/com/pindou/shop/
        │   ├── MainActivity.kt
        │   ├── data/            # 数据模型 + 持久化（Store）
        │   ├── service/         # 前台服务（计时提醒 + 响铃震动）
        │   └── ui/
        │       ├── App.kt       # 导航（底部/侧边菜单）
        │       ├── theme/Theme.kt
        │       └── screens/     # 各页面
        └── res/
```

## 关键依赖版本

- Kotlin 2.4.21
- Compose Multiplatform 1.12.2
- AGP 9.4.1
- Gradle 9.8.1
- miuix-ui / miuix-icons 0.9.4

## 说明

- **主题色**：走 miuix 的 `ThemeController` + `keyColor`（Monet 动态取色），主题色改动能实时套用到所有 miuix 组件。
- **取色盘**：用 miuix 内置的 `ColorPicker` 组件。
- **菜单**：竖屏用 `NavigationBar`（底部），横屏用 `NavigationRail`（左侧），自动切换。
- **计时提醒**：`ReminderService` 前台服务每秒检查，结束前 10 分钟与结束时发通知 + 闹钟铃声循环 + 震动。
- **导入导出**：当前为占位实现（`SettingsScreen` 里 `exportData`/`importData` 待接入 SAF 文件选择器），其余功能已实现。


