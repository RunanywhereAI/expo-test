# Android Build Troubleshooting Guide

This document contains solutions to common Android build issues encountered with the RunAnywhere AI Studio app.

**Last Updated**: January 19, 2026  
**Build Status**: ✅ **WORKING**

---

## Quick Start for New Developers

If you're setting up this project for the first time, follow these steps:

### Prerequisites
- macOS, Linux, or Windows
- Android Studio (with SDK Manager)
- Android SDK (API 36 / compileSdk 36)
- Android NDK 27.1.12297006
- Node.js 18+
- Java 17+ (OpenJDK or Oracle JDK)
- Yarn 1.22+

### Environment Setup

Ensure these environment variables are set:

```bash
# Add to ~/.zshrc or ~/.bashrc
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/emulator"
export PATH="$PATH:$ANDROID_HOME/platform-tools"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"
```

### Step-by-Step Setup

```bash
# 1. Clone the repository
git clone https://github.com/RunanywhereAI/expo-test.git
cd expo-test

# 2. Install npm dependencies from root
yarn install

# 3. Navigate to the Android project
cd apps/runanywhere-ai-studio/android

# 4. Download RunAnywhere native libraries (REQUIRED)
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :runanywhere_core:downloadNativeLibs \
          :runanywhere_llamacpp:downloadNativeLibs \
          :runanywhere_onnx:downloadNativeLibs \
          --no-configuration-cache

# 5. Create symlinks for library files (REQUIRED - see Issue 2)
# This fixes naming mismatches between CMakeLists.txt and actual files
cd ../../../node_modules/@runanywhere/llamacpp/android/src/main/jniLibs/arm64-v8a
ln -sf librac_backend_llamacpp.so librunanywhere_llamacpp.so
cd ../../../../../../../@runanywhere/onnx/android/src/main/jniLibs/arm64-v8a
ln -sf librac_backend_onnx.so librunanywhere_onnx.so

# 6. Return to android directory and build
cd ../../../../../../../apps/runanywhere-ai-studio/android
./gradlew :app:assembleMobileDebug --no-configuration-cache

# 7. Install on device (ensure device is connected via USB with debugging enabled)
adb install -r app/build/outputs/apk/mobile/debug/app-mobile-debug.apk
```

### If Build Fails

1. **Clean build**: `./gradlew clean`
2. **Clear Gradle cache**: `rm -rf ~/.gradle/caches`
3. **Clear CMake cache**: `find . -name ".cxx" -type d -exec rm -rf {} +`
4. **Check this document** for specific error solutions

### Key Things to Know

1. **Product Flavors**: This app has two flavors - `mobile` (phones/tablets) and `quest` (Meta Quest VR). Use `assembleMobileDebug` for standard Android devices.

2. **Configuration Cache**: Gradle 9's configuration cache has compatibility issues with some build scripts. Always use `--no-configuration-cache` flag.

3. **RunAnywhere Native Libraries**: Must be downloaded via Gradle tasks before building. These are not included in the npm packages.

4. **Library File Naming**: The npm packages use different library file names than the CMakeLists.txt expects. Symlinks are required.

5. **Metro Bundler**: Not required for building the APK. The app can connect to any Metro server running on port 80.

---

## Table of Contents

1. [Quick Start for New Developers](#quick-start-for-new-developers)
2. [Build Progress & Changelog](#build-progress--changelog)
3. [Issue 1: ANDROID_HOME / ANDROID_SDK_ROOT Not Set](#issue-1-android_home--android_sdk_root-not-set)
4. [Issue 2: RunAnywhere Native Libraries Not Found](#issue-2-runanywhere-native-libraries-not-found)
5. [Issue 3: Library File Naming Mismatch](#issue-3-library-file-naming-mismatch)
6. [Issue 4: Gradle Configuration Cache Errors](#issue-4-gradle-configuration-cache-errors)
7. [Issue 5: Missing librac_backend_onnx_jni.so](#issue-5-missing-librac_backend_onnx_jniso)
8. [General Build Tips](#general-build-tips)
9. [APK Variants & Flavors](#apk-variants--flavors)
10. [Version Information](#version-information)

---

## Build Progress & Changelog

This section documents the progress made in setting up and fixing the RunAnywhere AI Studio Android app.

### Session: January 19, 2026

#### Overview
Built and deployed the RunAnywhere AI Studio Android app with integrated RunAnywhere SDK for on-device AI capabilities.

---

#### Step 1: Install Dependencies
**Task**: Install all npm dependencies for the Expo monorepo

**Commands**:
```bash
cd expo-test
yarn install
```

**Notes**:
- Some packages need to be built (e.g., `@expo/config`, `expo`)
- Workspace validation warnings about version mismatches are expected

**Status**: ✅ Completed

---

#### Step 2: Environment Setup
**Task**: Set Android SDK environment variables

**Issue**: Build failed with `Neither ANDROID_SDK_ROOT nor ANDROID_HOME is set`

**Fix**:
```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
```

**Status**: ✅ Completed

---

#### Step 3: Download Native Libraries
**Task**: Download RunAnywhere SDK native libraries

**Issue**: CMake errors about missing `.so` files

**Commands**:
```bash
./gradlew :runanywhere_core:downloadNativeLibs \
          :runanywhere_llamacpp:downloadNativeLibs \
          :runanywhere_onnx:downloadNativeLibs \
          --no-configuration-cache
```

**Output**:
```
[RunAnywhereCore] ✅ Using bundled native libraries from npm package (1 .so files)
[RunAnywhereLlama] ✅ Using bundled native libraries from npm package (2 .so files)
[RunAnywhereONNX] ✅ Using bundled native libraries from npm package (5 .so files)
```

**Status**: ✅ Completed

---

#### Step 4: Fix Library Naming Mismatch
**Task**: Create symlinks for library files with mismatched names

**Issue**: CMakeLists.txt expects different filenames than what's actually downloaded:
- Expected: `librunanywhere_llamacpp.so` | Actual: `librac_backend_llamacpp.so`
- Expected: `librunanywhere_onnx.so` | Actual: `librac_backend_onnx.so`

**Fix**:
```bash
# LlamaCPP
cd node_modules/@runanywhere/llamacpp/android/src/main/jniLibs/arm64-v8a
ln -sf librac_backend_llamacpp.so librunanywhere_llamacpp.so

# ONNX
cd node_modules/@runanywhere/onnx/android/src/main/jniLibs/arm64-v8a
ln -sf librac_backend_onnx.so librunanywhere_onnx.so
```

**Status**: ✅ Completed

---

#### Step 5: Fix Missing ONNX JNI Library
**Task**: Handle missing `librac_backend_onnx_jni.so` file

**Issue**: CMakeLists.txt requires `librac_backend_onnx_jni.so` which doesn't exist in the npm package

**Fix**: Modified `node_modules/@runanywhere/onnx/android/CMakeLists.txt` to make the check optional:

```cmake
# Before (REQUIRED check)
if(NOT EXISTS "${JNILIB_DIR}/librac_backend_onnx_jni.so")
    message(FATAL_ERROR "[RunAnywhereONNX] RABackendONNX JNI not found...")
endif()

# After (OPTIONAL check)
if(EXISTS "${JNILIB_DIR}/librac_backend_onnx_jni.so")
    add_library(rac_backend_onnx_jni SHARED IMPORTED)
    # ... configuration ...
    set(HAS_RAC_BACKEND_ONNX_JNI TRUE)
else()
    message(STATUS "[RunAnywhereONNX] RABackendONNX JNI not found, skipping")
    set(HAS_RAC_BACKEND_ONNX_JNI FALSE)
endif()
```

Also updated the `target_link_libraries` section to conditionally link:
```cmake
# Link RABackendONNX JNI if available
if(HAS_RAC_BACKEND_ONNX_JNI)
    target_link_libraries(${PACKAGE_NAME} rac_backend_onnx_jni)
endif()
```

**Note**: This is a packaging issue with the npm package - the JNI library should be included. File a bug report to the RunAnywhere SDK team.

**Status**: ✅ Completed (workaround applied)

---

#### Step 6: Build APK
**Task**: Build the debug APK

**Command**:
```bash
./gradlew :app:assembleMobileDebug --no-configuration-cache
```

**Result**:
```
BUILD SUCCESSFUL in 3m 2s
2358 actionable tasks: 147 executed, 2211 up-to-date
```

**APK Location**:
```
app/build/outputs/apk/mobile/debug/app-mobile-debug.apk
```

**Status**: ✅ Completed

---

#### Step 7: Install on Device
**Task**: Install APK on connected Android device

**Command**:
```bash
adb install -r app/build/outputs/apk/mobile/debug/app-mobile-debug.apk
```

**Result**:
```
Performing Streamed Install
Success
```

**Status**: ✅ Completed

---

### Current Build Status

| Component | Status |
|-----------|--------|
| Dependencies Installed | ✅ Complete |
| Environment Variables | ✅ Complete |
| Native Libraries Downloaded | ✅ Complete |
| Library Symlinks Created | ✅ Complete |
| CMakeLists.txt Patched | ✅ Complete |
| APK Build | ✅ **SUCCESS** |
| Device Installation | ✅ **SUCCESS** |

### RunAnywhere SDK Integration

Verified in build output:
```
[NitroModules] 🔥 runanywherecore is boosted by nitro!
[NitroModules] 🔥 runanywherellama is boosted by nitro!
[NitroModules] 🔥 runanywhereonnx is boosted by nitro!
```

---

## Issue 1: ANDROID_HOME / ANDROID_SDK_ROOT Not Set

### Error
```
FAILURE: Build failed with an exception.

* Where:
Build file '.../hermes-engine/build.gradle.kts' line: 34

* What went wrong:
Neither ANDROID_SDK_ROOT nor ANDROID_HOME is set.
```

### Cause
The Android SDK location is not configured in the environment. Gradle and CMake need to know where to find the SDK tools, NDK, and platform files.

### Solution

Set the environment variables before building:

```bash
# Temporary (current session only)
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"

# Permanent (add to ~/.zshrc or ~/.bashrc)
echo 'export ANDROID_HOME="$HOME/Library/Android/sdk"' >> ~/.zshrc
echo 'export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"' >> ~/.zshrc
source ~/.zshrc
```

**Common SDK Locations**:
- macOS: `~/Library/Android/sdk`
- Linux: `~/Android/Sdk`
- Windows: `%LOCALAPPDATA%\Android\Sdk`

---

## Issue 2: RunAnywhere Native Libraries Not Found

### Error
```
CMake Error at CMakeLists.txt:22 (message):
  [RunAnywhereLlama] RABackendLlamaCPP not found at
  .../jniLibs/arm64-v8a/librunanywhere_llamacpp.so
  Run: ./gradlew :runanywhere_llamacpp:downloadNativeLibs
```

### Cause
The RunAnywhere SDK native libraries (`.so` files) are not included in the npm packages. They must be downloaded separately via Gradle tasks.

### Solution

Run the download tasks before building:

```bash
cd apps/runanywhere-ai-studio/android

./gradlew :runanywhere_core:downloadNativeLibs \
          :runanywhere_llamacpp:downloadNativeLibs \
          :runanywhere_onnx:downloadNativeLibs \
          --no-configuration-cache
```

**Expected Output**:
```
[RunAnywhereCore] ✅ Using bundled native libraries from npm package (1 .so files)
[RunAnywhereLlama] ✅ Using bundled native libraries from npm package (2 .so files)
[RunAnywhereONNX] ✅ Using bundled native libraries from npm package (5 .so files)
```

**Libraries Downloaded**:

| Package | Architecture | Files |
|---------|--------------|-------|
| @runanywhere/core | arm64-v8a | `librac_commons.so` |
| @runanywhere/llamacpp | arm64-v8a | `librac_backend_llamacpp.so`, `libomp.so` |
| @runanywhere/onnx | arm64-v8a | `librac_backend_onnx.so`, `libonnxruntime.so`, `libsherpa-onnx-*.so` (3 files) |

---

## Issue 3: Library File Naming Mismatch

### Error
```
CMake Error at CMakeLists.txt:22 (message):
  [RunAnywhereLlama] RABackendLlamaCPP not found at
  .../jniLibs/arm64-v8a/librunanywhere_llamacpp.so
```

### Cause
The CMakeLists.txt files expect libraries with one naming convention, but the actual downloaded files have different names:

| Expected (CMakeLists.txt) | Actual (Downloaded) |
|---------------------------|---------------------|
| `librunanywhere_llamacpp.so` | `librac_backend_llamacpp.so` |
| `librunanywhere_onnx.so` | `librac_backend_onnx.so` |

This is a version mismatch between the CMakeLists.txt and the actual library build artifacts.

### Solution

Create symbolic links to map the expected names to the actual files:

```bash
# For LlamaCPP
cd node_modules/@runanywhere/llamacpp/android/src/main/jniLibs/arm64-v8a
ln -sf librac_backend_llamacpp.so librunanywhere_llamacpp.so

# For ONNX
cd node_modules/@runanywhere/onnx/android/src/main/jniLibs/arm64-v8a
ln -sf librac_backend_onnx.so librunanywhere_onnx.so
```

**Verify symlinks**:
```bash
ls -la node_modules/@runanywhere/llamacpp/android/src/main/jniLibs/arm64-v8a/
# Should show:
# librunanywhere_llamacpp.so -> librac_backend_llamacpp.so
```

**Note**: This must be re-done after every `npm install` or `yarn install` that modifies node_modules.

---

## Issue 4: Gradle Configuration Cache Errors

### Error
```
Execution failed for task ':runanywhere_core:downloadNativeLibs'.
> Invocation of 'file' references a Gradle script object from a Groovy closure
  at execution time, which is unsupported with the configuration cache.
```

### Cause
Gradle 9.0 introduced strict configuration cache requirements. Some build scripts in the RunAnywhere packages use patterns that aren't compatible with the configuration cache.

### Solution

Disable the configuration cache when building:

```bash
./gradlew :app:assembleMobileDebug --no-configuration-cache
```

Or add to `gradle.properties`:
```properties
org.gradle.configuration-cache=false
```

**Note**: This is a temporary workaround. The RunAnywhere SDK should be updated to be configuration-cache compatible.

---

## Issue 5: Missing librac_backend_onnx_jni.so

### Error
```
CMake Error at CMakeLists.txt:37 (message):
  [RunAnywhereONNX] RABackendONNX JNI not found at
  .../jniLibs/arm64-v8a/librac_backend_onnx_jni.so
  Run: ./gradlew :runanywhere_onnx:downloadNativeLibs
```

### Cause
The npm package's CMakeLists.txt expects a `librac_backend_onnx_jni.so` library that doesn't exist in the package. This appears to be a packaging error where the JNI wrapper library was not included.

### Solution

Modify `node_modules/@runanywhere/onnx/android/CMakeLists.txt` to make the JNI library optional:

**1. Change the check from FATAL_ERROR to optional**:
```cmake
# Before (lines 36-46):
if(NOT EXISTS "${JNILIB_DIR}/librac_backend_onnx_jni.so")
    message(FATAL_ERROR "[RunAnywhereONNX] RABackendONNX JNI not found at ${JNILIB_DIR}/librac_backend_onnx_jni.so\n"
                        "Run: ./gradlew :runanywhere_onnx:downloadNativeLibs")
endif()

add_library(rac_backend_onnx_jni SHARED IMPORTED)
set_target_properties(rac_backend_onnx_jni PROPERTIES
    IMPORTED_LOCATION "${JNILIB_DIR}/librac_backend_onnx_jni.so"
    IMPORTED_NO_SONAME TRUE
)
message(STATUS "[RunAnywhereONNX] Found RABackendONNX JNI at ${JNILIB_DIR}/librac_backend_onnx_jni.so")

# After:
if(EXISTS "${JNILIB_DIR}/librac_backend_onnx_jni.so")
    add_library(rac_backend_onnx_jni SHARED IMPORTED)
    set_target_properties(rac_backend_onnx_jni PROPERTIES
        IMPORTED_LOCATION "${JNILIB_DIR}/librac_backend_onnx_jni.so"
        IMPORTED_NO_SONAME TRUE
    )
    message(STATUS "[RunAnywhereONNX] Found RABackendONNX JNI at ${JNILIB_DIR}/librac_backend_onnx_jni.so")
    set(HAS_RAC_BACKEND_ONNX_JNI TRUE)
else()
    message(STATUS "[RunAnywhereONNX] RABackendONNX JNI not found, skipping")
    set(HAS_RAC_BACKEND_ONNX_JNI FALSE)
endif()
```

**2. Update target_link_libraries (around line 174)**:
```cmake
# Before:
target_link_libraries(
    ${PACKAGE_NAME}
    ${LOG_LIB}
    android
    rac_commons
    runanywhere_onnx
    rac_backend_onnx_jni
    onnxruntime
)

# After:
target_link_libraries(
    ${PACKAGE_NAME}
    ${LOG_LIB}
    android
    rac_commons
    runanywhere_onnx
    onnxruntime
)

# Link RABackendONNX JNI if available
if(HAS_RAC_BACKEND_ONNX_JNI)
    target_link_libraries(${PACKAGE_NAME} rac_backend_onnx_jni)
endif()
```

**3. Clear CMake cache and rebuild**:
```bash
rm -rf node_modules/@runanywhere/onnx/android/.cxx
./gradlew :app:assembleMobileDebug --no-configuration-cache
```

**Note**: This is a packaging issue. The `librac_backend_onnx_jni.so` library should be included in the npm package.

---

## General Build Tips

### Clean Build Steps

When encountering persistent build issues:

```bash
# 1. Clean Gradle build cache
cd apps/runanywhere-ai-studio/android
./gradlew clean

# 2. Clear CMake build directories
find . -name ".cxx" -type d -exec rm -rf {} +

# 3. Clear Gradle caches (nuclear option)
rm -rf ~/.gradle/caches

# 4. Rebuild
./gradlew :app:assembleMobileDebug --no-configuration-cache
```

### Common Environment Issues

1. **Java Version**: Ensure Java 17+ is installed
   ```bash
   java -version  # Should be 17+
   ```

2. **NDK Version**: This project requires NDK 27.1.12297006
   ```bash
   # Install via Android Studio SDK Manager or:
   sdkmanager "ndk;27.1.12297006"
   ```

3. **CMake Version**: CMake 3.22.1 is used
   ```bash
   # Install via Android Studio SDK Manager or:
   sdkmanager "cmake;3.22.1"
   ```

### Device Connection

```bash
# Check connected devices
adb devices

# Install APK
adb install -r app/build/outputs/apk/mobile/debug/app-mobile-debug.apk

# View app logs
adb logcat | grep -i runanywhere

# Launch app
adb shell am start -n dev.runanywhere.aistudio/.experience.HomeActivity
```

---

## APK Variants & Flavors

### Product Flavors

| Flavor | Description | Use Case |
|--------|-------------|----------|
| `mobile` | Standard Android phones/tablets | Most common |
| `quest` | Meta Quest VR headsets | VR applications |

### Build Types

| Type | Description | APK Location |
|------|-------------|--------------|
| `debug` | Debug build with debuggable flag | `app/build/outputs/apk/{flavor}/debug/` |
| `release` | Release build (requires signing) | `app/build/outputs/apk/{flavor}/release/` |

### Build Commands

```bash
# Mobile Debug (most common)
./gradlew :app:assembleMobileDebug --no-configuration-cache

# Mobile Release (requires signing config)
./gradlew :app:assembleMobileRelease --no-configuration-cache

# Quest Debug
./gradlew :app:assembleQuestDebug --no-configuration-cache

# All variants
./gradlew :app:assemble --no-configuration-cache

# Install directly to device
./gradlew :app:installMobileDebug --no-configuration-cache
```

### APK Locations

```
app/build/outputs/apk/
├── mobile/
│   ├── debug/
│   │   └── app-mobile-debug.apk
│   └── release/
│       └── app-mobile-release.apk
└── quest/
    ├── debug/
    │   └── app-quest-debug.apk
    └── release/
        └── app-quest-release.apk
```

---

## node_modules Changes (Manual Patches Required)

### 1. ONNX CMakeLists.txt Fix

**File**: `node_modules/@runanywhere/onnx/android/CMakeLists.txt`

**Issue**: The file expects `librac_backend_onnx_jni.so` which doesn't exist in the npm package.

**Fix**: Make the JNI library check optional (see [Issue 5](#issue-5-missing-librac_backend_onnx_jniso))

**Note**: This change is lost when running `npm install` or `yarn install`. You must re-apply it after every install that modifies node_modules.

### 2. Library Symlinks

**Files**:
- `node_modules/@runanywhere/llamacpp/android/src/main/jniLibs/arm64-v8a/librunanywhere_llamacpp.so`
- `node_modules/@runanywhere/onnx/android/src/main/jniLibs/arm64-v8a/librunanywhere_onnx.so`

**Issue**: CMakeLists.txt expects different library names than what's downloaded.

**Fix**: Create symlinks (see [Issue 3](#issue-3-library-file-naming-mismatch))

**Note**: These symlinks are lost when running `npm install` or `yarn install`. Re-create them after every install.

---

## All Files Modified Summary

### Build Configuration Files

| File | Changes |
|------|---------|
| `android/app/build.gradle` | Product flavors, signing config |
| `android/settings.gradle` | RunAnywhere module includes |
| `android/gradle.properties` | Build settings |

### node_modules (Must Re-apply After npm/yarn install)

| File | Changes |
|------|---------|
| `node_modules/@runanywhere/onnx/android/CMakeLists.txt` | Made JNI library optional |
| `node_modules/@runanywhere/llamacpp/android/src/main/jniLibs/arm64-v8a/` | Added symlink |
| `node_modules/@runanywhere/onnx/android/src/main/jniLibs/arm64-v8a/` | Added symlink |

---

## Version Information

- **App Version**: 1.0.0
- **Application ID**: `dev.runanywhere.aistudio`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **NDK Version**: 27.1.12297006
- **Kotlin Version**: 2.1.20
- **React Native**: 0.83.1
- **Expo SDK**: ~54.0.8
- **RunAnywhere SDK**: 0.17.4

---

## UI/UX Redesign (January 19, 2026) - App Store Compliant

### Overview

The Android app UI has been updated to match the iOS version with RunAnywhere branding and a 3-tab navigation structure. This design follows Expo Go's App Store-approved architecture.

### Tab Structure

| Tab | Purpose | Features |
|-----|---------|----------|
| Home | Load apps, dev servers, projects | URL entry, QR scan, recently opened, projects/snacks |
| Diagnostics | Test permissions | Audio, Background Location, Geofencing |
| Settings | App configuration | Theme, developer menu gestures, app info, account |

### Files Created/Modified

| File | Type | Description |
|------|------|-------------|
| `home/HomeAppTheme.kt` | Modified | RunAnywhere brand colors (#FF5500), typography |
| `home/DiagnosticsScreen.kt` | Created | Permission diagnostics (Audio, Location, Geofencing) |
| `home/AppNavHost.kt` | Modified | Added Diagnostics tab to navigation |
| `res/drawable/diagnostics.xml` | Created | Diagnostics tab icon |
| `res/drawable/chevron_up.xml` | Created | UI icon |
| `res/drawable/chevron_down.xml` | Created | UI icon |
| `res/drawable/pin.xml` | Created | Location icon |
| `res/drawable/lock_icon.xml` | Created | Privacy icon |
| `res/drawable/fab.xml` | Created | FAB icon |
| `res/values/strings.xml` | Modified | RunAnywhere branding |
| `res/values/colors.xml` | Modified | RunAnywhere color palette |

### Color Palette

```kotlin
// RunAnywhere Brand Colors
val PrimaryAccent = Color(0xFFFF5500)  // Orange
val PrimaryBlue = Color(0xFF3B82F6)
val PrimaryGreen = Color(0xFF10B981)
val PrimaryRed = Color(0xFFEF4444)

// Dark Theme
val BackgroundPrimaryDark = Color(0xFF0F172A)
val BackgroundSecondaryDark = Color(0xFF1A1F2E)
```

### Key Features

1. **URL Entry** - Prominently displayed on Home screen for loading dev builds
2. **QR Code Scanner** - Quick way to connect to Metro servers
3. **Diagnostics** - Test Audio, Location, Geofencing permissions
4. **Developer Menu** - Shake, 3-finger press, action button gestures

### Text Branding (All "Expo Go" → "RunAnywhere AI Studio")

| File | Changes |
|------|---------|
| `home/UserReviewSection.kt` | "Enjoying Expo Go?" → "Enjoying RunAnywhere AI Studio?" |
| `home/UpgradeWarning.kt` | All upgrade warning messages updated |
| `home/SettingsTopBar.kt` | App name in top bar updated |
| `home/UpdateRow.kt` | Compatibility message updated |
| `home/SnackRow.kt` | SDK version error messages updated |
| `exceptions/ManifestException.kt` | All error messages (13 occurrences) |
| `res/values/strings.xml` | App name, notification channels |

---

## Troubleshooting Checklist

When the build fails, go through this checklist:

- [ ] `ANDROID_HOME` and `ANDROID_SDK_ROOT` environment variables are set
- [ ] NDK 27.1.12297006 is installed
- [ ] CMake 3.22.1 is installed
- [ ] Native libraries downloaded via Gradle tasks
- [ ] Library symlinks created for llamacpp and onnx
- [ ] ONNX CMakeLists.txt patched for optional JNI
- [ ] Using `--no-configuration-cache` flag
- [ ] Android device connected and USB debugging enabled

---

## Contact

For additional help with RunAnywhere SDK integration, refer to the SDK documentation or contact the RunAnywhere team.
