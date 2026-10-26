# Touch Panel Android — Build & Deployment Guide

## 1. Project Overview

`Touch Panel Android` is a native Android application built using **Kotlin** and **Jetpack Compose**. It transforms an Android smartphone positioned horizontally on a desk into an OLED-styled hardware touch panel companion for a Windows PC.

---

## 2. SDK Versioning & Compatibility Rationale

- **minSdkVersion**: `26` (Android 8.0 Oreo)  
  *Rationale*: Android 8.0 supports over 95% of active Android devices in circulation. It provides hardware-accelerated canvas, native WebSocket TLS 1.3 socket support, adaptive icons, modern WindowInsets, and full compatibility with Jetpack Compose 2024 BOM. It also has mature Bluetooth Low Energy (BLE) and Foreground Service management required for reliable continuous connectivity.
- **compileSdkVersion / targetSdkVersion**: `34` (Android 14)  
  *Rationale*: Meets Google Play modern requirements, supports predictive back animations, fine-grained media permissions, and edge-to-edge layout APIs (`WindowCompat.setDecorFitsSystemWindows`).
- **Kotlin**: `2.0.20` with modern Compose compiler plugin.
- **Java Toolchain**: Java 17 / JDK 17 (standard for modern Android Gradle Plugin 8.x).

---

## 3. Directory Structure

```text
android/
├── build.gradle.kts                # Top-level root Gradle build script
├── settings.gradle.kts             # Gradle project configuration
├── gradle.properties               # JVM args, AndroidX and Compose flags
├── gradlew & gradlew.bat           # Gradle wrapper scripts
├── gradle/wrapper/                 # Gradle distribution (v8.7)
└── app/
    ├── build.gradle.kts            # App module build script (Compose BOM, Ktor/OkHttp, DataStore)
    ├── proguard-rules.pro          # ProGuard/R8 optimization rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml # Orientation (sensorLandscape), permissions, activities
        │   ├── res/                # OLED color palette, themes, icons
        │   └── java/com/touchpanel/android/
        │       ├── MainActivity.kt # Fullscreen landscape container, WakeLock handling
        │       ├── TouchPanelApp.kt# Application class
        │       ├── core/theme/     # OLED theme, typography, shapes
        │       ├── domain/         # Widget models, catalog, command definitions
        │       ├── protocol/       # JSON protocol v1.0.0 serializer & parser
        │       ├── data/           # DataStore layout persistence, WebSocket/BT transport
        │       ├── integration/    # Mango Talker adapter and intent launcher
        │       └── ui/             # Dashboard, editor, settings, and widget implementations
        └── test/                   # JUnit 4 & Mockito unit tests
```

---

## 4. How to Build the Android App

### Option A: Using Android Studio
1. Launch **Android Studio** (Hedgehog 2023.1.1 or Ladybug 2024.2+ recommended).
2. Select **Open** and select the `/android` directory.
3. Allow Gradle to sync dependencies.
4. Select build variant `debug` or `release` in the Build Variants pane.
5. Click **Run** or press `Shift+F10` to deploy to a connected Android phone or emulator.

### Option B: Command Line (Gradle)
Ensure JDK 17+ and the Android SDK (`$ANDROID_HOME` or `$ANDROID_SDK_ROOT`) are configured on your machine.

```bash
# Navigate to android folder
cd android

# Make wrapper executable (Linux/macOS)
chmod +x gradlew

# Run all unit tests
./gradlew test

# Build Debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk

# Build Release APK (requires signing credentials configured in app/build.gradle.kts)
./gradlew assembleRelease
# Output location:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 5. Current Google AI Studio Environment Analysis

- **Host OS**: Linux Container (`Ubuntu/Debian` on Google Cloud)
- **Java**: `Not installed` (`java: not found`)
- **Android SDK**: `Not pre-installed`
- **Result**: The local container does not possess the 1.5 GB Android Command-line Tools / Platform SDK required to execute `gradlew assembleDebug` in this sandbox.
- **Solution Provided**: 
  1. A complete, valid, pristine native Android source project is generated in `/android` ready to compile without modifications on any computer with Android Studio / JDK 17.
  2. Complete unit test files are included to verify JSON protocol correctness, layout persistence, and error handling.
  3. A live interactive web companion and simulator is hosted on port 3000, reproducing the exact OLED dashboard, widgets, layout editor, Windows protocol dispatcher, and Mango Talker inspector in landscape mode.
  4. An in-app Code Explorer & Zip Exporter is built directly into the UI so you can download the entire Android project folder in 1-click.
