# iOS Build Troubleshooting Guide

This document contains solutions to common iOS build issues encountered with the RunAnywhere AI Studio app.

**Last Updated**: January 19, 2026  
**Build Status**: ✅ **WORKING**

---

## Quick Start for New Developers

If you're setting up this project for the first time, follow these steps:

### Prerequisites
- macOS with Xcode 15+
- Node.js 18+
- CocoaPods 1.14+
- Apple Developer account (for device testing)

### Step-by-Step Setup

```bash
# 1. Clone the repository
git clone https://github.com/RunanywhereAI/expo-test.git
cd expo-test

# 2. Install npm dependencies
npm install --ignore-scripts

# 3. Apply node_modules patches (REQUIRED after every npm install)
# Fix react-native-zip-archive compiler flags
sed -i '' "s/-DHAVE_UNISTD_H -G'/-DHAVE_UNISTD_H'/" node_modules/react-native-zip-archive/RNZipArchive.podspec

# 4. Navigate to the iOS project
cd apps/runanywhere-ai-studio/ios

# 5. Install CocoaPods
pod install

# 6. Open in Xcode
open Exponent.xcworkspace

# 7. In Xcode:
#    - Select your development team in Signing & Capabilities
#    - Select your device
#    - Build and Run (Cmd+R)
```

### If Build Fails

1. **Clean build**: `Cmd+Shift+K` in Xcode
2. **Clean derived data**: `rm -rf ~/Library/Developer/Xcode/DerivedData`
3. **Reinstall pods**: `rm -rf Pods Podfile.lock && pod install`
4. **Check this document** for specific error solutions

### Key Things to Know

1. **expo-av compatibility**: This project uses npm expo-av@16.0.8 which requires legacy APIs. We've restored these APIs in `packages/expo-modules-core/ios/` - **DO NOT DELETE THESE FILES**.

2. **react-native-zip-archive**: Has an invalid `-G` compiler flag that must be removed after every `npm install`.

3. **Firebase**: Firebase is configured with placeholder values. The app works without real Firebase credentials.

4. **Code Signing**: Use your own Apple Developer team and bundle ID (`dev.runanywhere.aistudio`).

---

## Table of Contents

1. [Quick Start for New Developers](#quick-start-for-new-developers)
2. [Build Progress & Changelog](#build-progress--changelog)
3. [CRITICAL: expo-modules-core Legacy API Restoration](#critical-expo-modules-core-legacy-api-restoration)
4. [node_modules Changes (Manual Patches Required)](#node_modules-changes-manual-patches-required)
5. [All Files Modified Summary](#all-files-modified-summary)
6. [Issue 1: Signing & Provisioning Issues](#issue-1-signing--provisioning-issues)
7. [Issue 2: react-native-zip-archive - Unsupported '-G' Flag](#issue-2-react-native-zip-archive---unsupported--g-flag)
8. [Issue 3: EXAV Module - ExpoModulesCore Header Not Found](#issue-3-exav-module---expomodulescore-header-not-found)
9. [General Build Tips](#general-build-tips)
10. [Version Information](#version-information)

---

## Build Progress & Changelog

This section documents the progress made in setting up and fixing the RunAnywhere AI Studio iOS app.

### Session: January 19, 2026

#### Overview
Forked Expo Go app and rebranded it as **RunAnywhere AI Studio** with integrated RunAnywhere SDK for on-device AI capabilities.

---

#### Step 1: Package Updates
**Task**: Update RunAnywhere packages to version 0.17.4

**Changes Made**:
- Updated `package.json` dependencies:
  ```json
  "@runanywhere/core": "0.17.4",
  "@runanywhere/llamacpp": "0.17.4",
  "@runanywhere/onnx": "0.17.4"
  ```
- Ran `npm install --ignore-scripts` (used `--ignore-scripts` to bypass workspace build errors)
- Ran `pod install`

**Status**: ✅ Completed

---

#### Step 2: Code Signing & Bundle ID Configuration
**Task**: Fix Xcode signing errors - app was using Expo's official bundle ID which we can't sign

**Issues Encountered**:
- `No Account for Team 'C8D8QTF339'`
- `No profiles for 'host.exp.Exponent' were found`
- `Signing for "ExpoNotificationServiceExtension" requires a development team`

**Changes Made**:
1. Updated `project.pbxproj`:
   - `PRODUCT_BUNDLE_IDENTIFIER`: `host.exp.Exponent` → `dev.runanywhere.aistudio`
   - `PRODUCT_BUNDLE_IDENTIFIER` (Tests): `host.exp.Tests` → `dev.runanywhere.aistudio.Tests`
   - `PRODUCT_BUNDLE_IDENTIFIER` (Extension): `host.exp.Exponent.ExpoNotificationServiceExtension` → `dev.runanywhere.aistudio.NotificationServiceExtension`
   - `DEVELOPMENT_TEAM`: Set to `AFAL2647U9` for all targets
   - `CODE_SIGN_STYLE`: Set to `Automatic`
   - `CODE_SIGN_IDENTITY`: Set to `Apple Development`
   - `PROVISIONING_PROFILE_SPECIFIER`: Cleared
   - `PRODUCT_NAME`: `Expo Go` → `RunAnywhere AI Studio`

2. Simplified `Exponent.entitlements` to remove capabilities requiring App Store Connect setup

**Status**: ✅ Completed

---

#### Step 3: App Branding & Assets
**Task**: Rebrand app from Expo Go to RunAnywhere AI Studio

**Changes Made**:
1. **Info.plist Updates**:
   - `CFBundleDisplayName`: `Expo Go` → `RunAnywhere AI`
   - `CFBundleIdentifier`: Changed to use `$(PRODUCT_BUNDLE_IDENTIFIER)`
   - `CFBundleShortVersionString`: `54.0.5` → `1.0.0`
   - `CFBundleVersion`: `54.0.5` → `1`
   - `CFBundleURLSchemes`: Replaced Expo schemes with `runanywhere`, `runanywhere-ai`
   - Updated all permission descriptions to reference "RunAnywhere AI"
   - Added `NSSpeechRecognitionUsageDescription`
   - Removed Facebook SDK configuration
   - Removed Google AdMob configuration
   - Removed SKAdNetwork identifiers

2. **ExpoNotificationServiceExtension/Info.plist**:
   - `CFBundleDisplayName`: `Expo Go-Notification Service` → `RunAnywhere AI Notification Service`

3. **app.json Updates**:
   - Updated description to focus on on-device AI
   - Changed privacy from `unlisted` to `public`
   - Added iOS `infoPlist` configuration

4. **App Icons** (copied from `sdks/examples/ios/RunAnywhereAI`):
   - `AppIcon.appiconset/runanywhere-icon.png` (1024x1024)
   - `AppIcon.appiconset/runanywhere-icon-dark.png` (1024x1024)
   - Updated `Contents.json` for modern iOS icon format

5. **Launch Screen Icons**:
   - Copied icons at 1x, 2x, 3x scales
   - Updated `ExpoGoLaunchIcon.imageset/Contents.json`

6. **Other Logo Assets**:
   - Updated `expo-go-logo.imageset`
   - Updated `Icon.imageset`

**Status**: ✅ Completed

---

#### Step 4: Build Error - react-native-zip-archive
**Task**: Fix compiler error with `-G` flag

**Error**:
```
unsupported option '-G' for target 'arm64-apple-ios15.1'
```

**Root Cause**: Malformed `compiler_flags` in `RNZipArchive.podspec`:
```ruby
s.compiler_flags = '-GCC_PREPROCESSOR_DEFINITIONS="..."'
```

**Fix Applied**: Patched `node_modules/react-native-zip-archive/RNZipArchive.podspec`:
```ruby
# Before
s.compiler_flags = '-GCC_PREPROCESSOR_DEFINITIONS="HAVE_INTTYPES_H HAVE_PKCRYPT HAVE_STDINT_H HAVE_WZAES HAVE_ZLIB MZ_ZIP_NO_SIGNING $(inherited)"'

# After
s.compiler_flags = '-DHAVE_INTTYPES_H -DHAVE_PKCRYPT -DHAVE_STDINT_H -DHAVE_WZAES -DHAVE_ZLIB -DMZ_ZIP_NO_SIGNING'
```

**Status**: ✅ Completed

---

#### Step 5: Build Error - EXAV Module
**Task**: Fix Clang dependency scanner failure for EXAV module

**Error**:
```
fatal error: 'ExpoModulesCore/EXEventEmitter.h' file not found
Unable to find module dependency: 'EXAV'
Clang dependency scanner failure: While building module 'EXAV'
```

**Attempts**:
1. ❌ Added header search paths in Podfile post_install hook - didn't work (module scanning happens before build)
2. ✅ Excluded `expo-av` from `use_expo_modules!` in Podfile

**Fix Applied**: Added `'expo-av'` to exclude list in Podfile:
```ruby
use_expo_modules!({
  exclude: [
    # ... other excludes ...
    'expo-av'  # Added to fix EXAV module error
  ],
  # ...
})
```

Then ran:
```bash
rm -rf Pods Podfile.lock build
pod install
```

**Status**: ✅ Completed

---

#### Step 6: Documentation
**Task**: Create troubleshooting documentation

**Created**: `ios/docs/BUILD_TROUBLESHOOTING.md` with:
- All issues encountered and their solutions
- General build tips
- Clean build steps
- Version information

**Status**: ✅ Completed

---

#### Step 7: Build Error - PhaseScriptExecution Failed
**Task**: Fix script phase execution failures

**Error**:
```
Command PhaseScriptExecution failed with a nonzero exit code
```

**Root Causes Identified**:

1. **Missing Expo Tools**: The `generate-dynamic-macros.sh` script requires Expo tools (`tools/bin/expotools.js`) which aren't built in the forked repo.

2. **Missing GoogleService-Info.plist**: Firebase Crashlytics script requires this file.

**Fixes Applied**:

1. **Modified `Build-Phases/generate-dynamic-macros.sh`** to skip gracefully when Expo tools aren't available:
   ```bash
   # Check if expo tools are available
   if [[ ! -f "$EXPOTOOLS_JS" ]]; then
     echo "⚠️  Expo tools not found"
     echo "⚠️  Skipping dynamic macros generation for RunAnywhere AI Studio"
     exit 0
   fi
   ```

2. **Created placeholder `GoogleService-Info.plist`** at `Exponent/Supporting/GoogleService-Info.plist` with minimal configuration.

3. **Disabled Firebase Crashlytics upload script** in `project.pbxproj`:
   - The "Upload Debub Symbols Crashlytics" build phase was calling `FirebaseCrashlytics/run`
   - This fails without a real Firebase project configured
   - Modified to exit 0 and skip the upload

4. **Fixed EXConstants script phase** - `get-app-config-ios.sh`:
   
   **Root Cause Analysis:**
   - EXConstants pod has a script phase that runs `get-app-config-ios.sh`
   - This script requires `@expo/config` package to generate app.config
   - In the forked expo repo, `@expo/config` only has `.d.ts` files (TypeScript declarations), no compiled JS
   - The script also had a bug where it would fail with "Unsupported bundle format" BEFORE checking for @expo/config
   
   **Issues Found:**
   1. `BUNDLE_FORMAT` environment variable was empty, causing immediate failure
   2. The @expo/config check was placed AFTER the BUNDLE_FORMAT check
   3. The path to check @expo/config was incorrect (looked in app dir instead of monorepo root)
   
   **Solution Applied in `packages/expo-constants/scripts/get-app-config-ios.sh`:**
   - Moved the @expo/config check to run BEFORE the BUNDLE_FORMAT check
   - Fixed the path to check `$EXPO_CONSTANTS_PACKAGE_DIR/../..` (monorepo root)
   - Added fallback to check node_modules for non-monorepo setups
   - Creates empty `{}` app.config when @expo/config is not available
   - Defaults BUNDLE_FORMAT to "shallow" for iOS if not set

**Status**: ✅ Completed

---

#### Step 8: Build Error - Swift Bridging Header
**Task**: Fix `'Expo_Go-Swift.h' file not found` error

**Error**:
```
'Expo_Go-Swift.h' file not found
```

**Root Cause**:
- Xcode auto-generates a Swift bridging header named `<PRODUCT_NAME>-Swift.h`
- When we renamed PRODUCT_NAME from "Expo Go" to "RunAnywhere AI Studio"
- The header changed from `Expo_Go-Swift.h` to `RunAnywhere_AI_Studio-Swift.h`
- 14 Objective-C files were still importing the old header name

**Files Updated** (replaced `Expo_Go-Swift.h` → `RunAnywhere_AI_Studio-Swift.h`):
- `Exponent/Versioned/Core/EXVersionManagerObjC.mm`
- `Exponent/Versioned/Core/Internal/DevSupport/PerfMonitor/EXExpoPerfMonitor.mm`
- `Exponent/Versioned/Core/Internal/EXLinkingManager.m`
- `Exponent/Kernel/AppLoader/EXAppLoaderExpoUpdates.m`
- `Exponent/Kernel/AppLoader/AppFetcher/EXAppFetcher.m`
- `Exponent/Kernel/ReactAppManager/EXReactAppManager.mm`
- `Exponent/Kernel/Views/EXAppViewController.mm`
- `Exponent/Kernel/Views/Loading/EXProgressHUD.m`
- `Exponent/Kernel/Views/Loading/EXAppLoadingProgressWindowController.m`
- `Exponent/Kernel/Views/EXErrorView.m`
- `Exponent/Kernel/Services/EXSplashScreen/EXSplashScreenService.m`
- `Exponent/Kernel/Services/EXSplashScreen/EXSplashScreenService.h`
- `Exponent/Kernel/Services/EXUpdatesManager.h`
- `Client/EXRootViewController.m`

**Status**: ✅ Completed

---

#### Step 9: Build Error - ONNX Runtime Undefined Symbols
**Task**: Fix linker errors for ONNX Runtime symbols

**Error**:
```
Undefined symbol: _OrtGetApiBase
Undefined symbol: _OrtSessionOptionsAppendExecutionProvider_CoreML
```

**Root Cause**:
- The `@runanywhere/onnx` npm package (v0.17.4) is missing `onnxruntime.xcframework`
- The `RABackendONNX.xcframework` expects to link against ONNX Runtime separately
- Only `RABackendONNX.xcframework` was included, but `onnxruntime.xcframework` was missing

**Fix Applied**:
Copied the missing framework from the main SDK:
```bash
cp -R sdks/sdk/runanywhere-react-native/packages/onnx/ios/Frameworks/onnxruntime.xcframework \
  node_modules/@runanywhere/onnx/ios/Frameworks/
pod install
```

**Note**: This is a packaging issue with the npm package - the framework should be included. File a bug report to the RunAnywhere SDK team.

**Status**: ✅ Completed

---

#### Step 10: Runtime Crash - Firebase Configuration
**Task**: Fix app crash due to invalid Firebase configuration

**Error**:
```
*** Terminating app due to uncaught exception 'com.firebase.installations', reason: 
'[FirebaseInstallations][I-FIS008000] Could not configure Firebase Installations due to 
invalid FirebaseApp options. `FirebaseOptions.APIKey` doesn't match the expected format'
```

**Root Cause**:
- The placeholder `GoogleService-Info.plist` had an invalid API key format
- Firebase validates API key must be 39 characters and start with 'A'
- Even with valid format, Firebase features won't work without a real Firebase project

**Fix Applied**:
1. Commented out `FirebaseApp.configure()` in `Client/AppDelegate.swift`:
   ```swift
   // MARK: - Firebase disabled for RunAnywhere AI Studio
   // Uncomment and configure GoogleService-Info.plist if you need Firebase features
   // FirebaseApp.configure()
   ```

2. Updated `GoogleService-Info.plist` API_KEY to valid format (for future use):
   ```xml
   <key>API_KEY</key>
   <string>AIzaSyA0000000000000000000000000000000A</string>
   ```

**Note**: If you need Firebase features (Analytics, Crashlytics, etc.), create a real Firebase project and download the actual `GoogleService-Info.plist`.

**Status**: ✅ Completed

---

#### Step 11: Runtime Crash - Missing EXBuildConstants.plist
**Task**: Fix app crash due to nil SDK version

**Error**:
```
*** Terminating app due to uncaught exception 'NSInvalidArgumentException', reason: 
'*** -[__NSPlaceholderDictionary initWithObjects:forKeys:count:]: attempt to insert nil object from objects[0]'
```

**Root Cause**:
- The `EXBuildConstants.plist` file was missing
- This file is normally generated by `generate-dynamic-macros.sh` (which we disabled)
- Without it, `[EXBuildConstants sharedInstance].sdkVersion` returns nil
- This causes a crash when building the request headers dictionary

**Fix Applied**:
Created `ios/Exponent/Supporting/EXBuildConstants.plist` with required values:
```xml
<key>API_SERVER_ENDPOINT</key>
<string>https://exp.host</string>
<key>IS_DEV_KERNEL</key>
<false/>
<key>TEMPORARY_SDK_VERSION</key>
<string>54.0.0</string>
<key>EXPO_RUNTIME_VERSION</key>
<string>1.0.0</string>
```

**Status**: ✅ Completed

---

#### Step 12: EXAV Build Error - Missing EXEventEmitter.h (FINAL FIX)
**Task**: Fix expo-av build error "'EXEventEmitter.h' file not found"

**Error**:
```
'EXEventEmitter.h' file not found
```

**Root Cause**:
- expo-av 16.0.8 (from npm) expects `EXEventEmitter` protocol from ExpoModulesCore
- The local ExpoModulesCore (from the expo repo) was missing this header file
- This is a version mismatch between the npm expo-av and the local expo repo

**Fix Applied**:
Created the missing header files in the local ExpoModulesCore package.

**Files Created in `packages/expo-modules-core/ios/Legacy/Protocols/`**:

1. **EXEventEmitter.h**:
```objc
// Copyright © 2018 650 Industries. All rights reserved.
// Added for RunAnywhere AI Studio - expo-av compatibility

#import <Foundation/Foundation.h>

#import <ExpoModulesCore/EXDefines.h>
#import <ExpoModulesCore/EXExportedModule.h>

// Implement this protocol in your exported module to be able
// to send events through platform event emitter.

@protocol EXEventEmitter

- (void)startObserving;
- (void)stopObserving;

- (NSArray<NSString *> *)supportedEvents;

@end
```

2. **EXEventEmitterService.h**:
```objc
// Copyright © 2018 650 Industries. All rights reserved.
// Added for RunAnywhere AI Studio - expo-av compatibility

#import <Foundation/Foundation.h>

#import <ExpoModulesCore/EXDefines.h>
#import <ExpoModulesCore/EXExportedModule.h>

@protocol EXEventEmitterService

- (void)sendEventWithName:(NSString *)name body:(id)body;

@end
```

**After creating this file**:
1. expo-av can use standard auto-linking (no need to exclude or add manually)
2. No patches needed to expo-av source files in node_modules
3. No Swift file renaming needed
4. No special Podfile post_install hooks needed

**Podfile should NOT have**:
- `'expo-av'` in the exclude list
- Manual `pod 'EXAV'` line  
- EXAV-specific post_install configurations

Then reinstall pods:
```bash
cd ios
rm -rf Pods Podfile.lock build
pod install
```

**This is the correct and final fix** - all previous attempts involving node_modules patches, Swift file renaming, and Podfile hacks are no longer needed.

**Status**: ✅ Completed

---

#### Step 13: Xcode SWBBuildService Crash
**Task**: Fix "SWBBuildService quit unexpectedly" crash during build

**Error**:
```
Problem Report for SWBBuildService
SWBBuildService quit unexpectedly.
Exception Type: EXC_BREAKPOINT (SIGTRAP)
```

**Root Cause**:
- Corrupted Xcode DerivedData/build cache
- Often happens after significant project configuration changes
- Can be caused by complex module dependency resolution failures

**Fix Applied**:
1. Force-close Xcode
2. Clear all DerivedData
3. Clean Pods and rebuild

```bash
# 1. Force-close Xcode
pkill -9 Xcode

# 2. Clear DerivedData
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# 3. Clean and reinstall pods
cd ios
rm -rf Pods Podfile.lock build
pod install

# 4. Reopen Xcode and build
open Exponent.xcworkspace
# Then Cmd+Shift+K (clean) and Cmd+B (build)
```

**Status**: ✅ Completed

---

#### Step 14: Missing Legacy APIs (EXFatal, EXLogWarn, etc.) - DEFINITIVE FIX
**Task**: Fix all missing legacy API errors from expo-av

**Errors Encountered**:
```
Call to undeclared function 'EXFatal'
Call to undeclared function 'EXErrorWithMessage'  
Call to undeclared function 'EXLogWarn'
Call to undeclared function 'EXLogError'
Call to undeclared function 'EXLogInfo'
```

**Root Cause Analysis**:

The RunAnywhere fork was created from Expo's bleeding-edge `main` branch on **Jan 17, 2026**.
However, Expo removed legacy APIs from `expo-modules-core` just **9 days earlier**:

| Commit | Date | What was removed |
|--------|------|------------------|
| `abbd2b7f75` | Jan 8, 01:10 | EXEventEmitter, EXEventEmitterService |
| `57e0b9ea98` | Jan 8, 14:18 | EXLogManager, EXLogInfo/Warn/Error, EXFatal |
| `5d9339dd48` | Nov 26, 2025 | expo-av was deprecated/removed from monorepo |

The npm `expo-av@16.0.8` package still depends on these legacy APIs.

**DEFINITIVE FIX: Cherry-pick from commit `a3490d958a`** (just before removal):

```bash
cd /path/to/expo-test
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Services/EXLogManager.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Services/EXLogManager.m
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Services/EXReactLogHandler.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Services/EXReactLogHandler.m
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Protocols/EXEventEmitter.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Protocols/EXEventEmitterService.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Protocols/EXLogHandler.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/EXUnimodulesCompat.h
git checkout a3490d958a -- packages/expo-modules-core/ios/EXDefines.h
```

**Files Restored**:
- `packages/expo-modules-core/ios/Legacy/Services/EXLogManager.h` - Log manager header
- `packages/expo-modules-core/ios/Legacy/Services/EXLogManager.m` - Contains EXLogInfo/Warn/Error/Fatal implementations
- `packages/expo-modules-core/ios/Legacy/Services/EXReactLogHandler.h` - React log handler protocol
- `packages/expo-modules-core/ios/Legacy/Services/EXReactLogHandler.m` - React log handler implementation
- `packages/expo-modules-core/ios/Legacy/Protocols/EXEventEmitter.h` - Event emitter protocol
- `packages/expo-modules-core/ios/Legacy/Protocols/EXEventEmitterService.h` - Event emitter service protocol
- `packages/expo-modules-core/ios/Legacy/Protocols/EXLogHandler.h` - Log handler protocol
- `packages/expo-modules-core/ios/Legacy/EXUnimodulesCompat.h` - **UM to EX type mappings** (UMPromiseResolveBlock, etc.)
- `packages/expo-modules-core/ios/EXDefines.h` - Contains EXLogInfo/Warn/Error, EXFatal, EXErrorWithMessage declarations

**Why This Works**:
- Commit `a3490d958a` is only 11 days old (Jan 8, 2026)
- It has ALL the legacy APIs but is otherwise modern
- No need to go back to old sdk-52 branch

**After cherry-picking**:
```bash
cd ios
rm -rf Pods Podfile.lock
pod install
```

**Status**: ✅ Completed

---

#### Step 15: Missing UMPromiseResolveBlock and UMPromiseRejectBlock Types
**Task**: Fix "Expected a type" errors for UM* types in EXAV.m

**Error**:
```
EXAV.m:1016:31 Expected a type
EXAV.m:1017:31 Expected a type
EXAV.m:1027:10 Called object type 'id' is not a function or function pointer
```

**Root Cause**:
- expo-av uses legacy "Unimodules" types like `UMPromiseResolveBlock`, `UMPromiseRejectBlock`
- These were mapped from the old `UM*` naming to `EX*` naming in `EXUnimodulesCompat.h`
- This file was removed in commit `ed259608c5 [core][iOS] Remove legacy EXUnimodulesCompat.h`

**Fix Applied**:
Cherry-pick the compatibility header from commit `a3490d958a`:
```bash
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/EXUnimodulesCompat.h
```

**What EXUnimodulesCompat.h provides**:
```objc
#define UMPromiseResolveBlock EXPromiseResolveBlock
#define UMPromiseRejectBlock EXPromiseRejectBlock
#define UMLogInfo EXLogInfo
#define UMLogWarn EXLogWarn
#define UMLogError EXLogError
#define UMFatal EXFatal
#define UMErrorWithMessage EXErrorWithMessage
// ... and many more UM* to EX* mappings
```

**Status**: ✅ Completed

---

#### Step 16: Undefined Symbol _EXErrorWithMessage and _EXSharedApplication
**Task**: Fix linker error for missing EXErrorWithMessage and EXSharedApplication implementations

**Error**:
```
Undefined symbol: _EXErrorWithMessage
```

**Root Cause**:
- `EXErrorWithMessage` and `EXSharedApplication` are declared in `EXDefines.h`
- Their implementations were in `EXUtilities.m` but were removed in recent Expo updates
- The cherry-picked `EXDefines.h` has declarations but the implementations were not in the cherry-picked files

**Fix Applied**:
Added implementations to `packages/expo-modules-core/ios/Legacy/EXUtilities.m`:

```objc
#pragma mark - Legacy compatibility functions (restored from commit a3490d958a)

#if TARGET_OS_OSX
NSApplication * EXSharedApplication(void)
{
  return [NSApplication sharedApplication];
}
#else
UIApplication * EXSharedApplication(void)
{
  if ([[[[NSBundle mainBundle] bundlePath] pathExtension] isEqualToString:@"appex"]) {
    return nil;
  }
  return [[UIApplication class] performSelector:@selector(sharedApplication)];
}
#endif

NSError *EXErrorWithMessage(NSString *message)
{
  NSDictionary<NSString *, id> *errorInfo = @{NSLocalizedDescriptionKey: message};
  return [[NSError alloc] initWithDomain:@"EXModulesErrorDomain" code:0 userInfo:errorInfo];
}
```

**Status**: ✅ Completed

---

### Current Build Status

| Component | Status |
|-----------|--------|
| Package Updates | ✅ Complete |
| Code Signing | ✅ Complete |
| Bundle IDs | ✅ Complete |
| App Branding | ✅ Complete |
| App Icons | ✅ Complete |
| Info.plist | ✅ Complete |
| Podfile Fixes | ✅ Complete |
| Pod Install | ✅ Complete (219 pods) |
| Xcode Build | ✅ **SUCCESS** |
| App Running | ✅ **SUCCESS** |

### RunAnywhere SDK Integration

Verified in pod install output:
```
[RunAnywhereCore] Using bundled RACommons.xcframework from npm package
[NitroModules] 🔥 RunAnywhereCore is boosted by nitro!
[RunAnywhereLlama] Using bundled RABackendLLAMACPP.xcframework from npm package
[NitroModules] 🔥 RunAnywhereLlama is boosted by nitro!
[RunAnywhereONNX] Using bundled xcframeworks from npm package
[NitroModules] 🔥 RunAnywhereONNX is boosted by nitro!
[RunAnywhere] expo-av enabled via auto-linking
```

---

## CRITICAL: expo-modules-core Legacy API Restoration

### Background

This project is based on Expo's **bleeding-edge main branch** (forked Jan 17, 2026). 
Expo removed legacy APIs from `expo-modules-core` just **9 days earlier** (Jan 8, 2026):

| Commit | Date | What was removed |
|--------|------|------------------|
| `abbd2b7f75` | Jan 8, 01:10 | EXEventEmitter, EXEventEmitterService, EXReactNativeEventEmitter |
| `57e0b9ea98` | Jan 8, 14:18 | EXLogManager, EXLogInfo/Warn/Error, EXFatal, EXReactLogHandler |
| `ed259608c5` | Jan 8, 14:xx | EXUnimodulesCompat.h |
| `5d9339dd48` | Nov 26, 2025 | expo-av was deprecated/removed from monorepo |

The npm `expo-av@16.0.8` package still depends on these legacy APIs, causing build failures.

### Solution: Cherry-Pick from Commit `a3490d958a`

We restored the legacy files from commit `a3490d958a` (just before the removal, Jan 8, 2026):

```bash
cd /path/to/expo-test

# Cherry-pick all required legacy files
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Services/EXLogManager.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Services/EXLogManager.m
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Services/EXReactLogHandler.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Services/EXReactLogHandler.m
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Protocols/EXEventEmitter.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Protocols/EXEventEmitterService.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/Protocols/EXLogHandler.h
git checkout a3490d958a -- packages/expo-modules-core/ios/Legacy/EXUnimodulesCompat.h
git checkout a3490d958a -- packages/expo-modules-core/ios/EXDefines.h
```

### Files Added/Modified in `packages/expo-modules-core/ios/`

| File | Type | Purpose |
|------|------|---------|
| `Legacy/Services/EXLogManager.h` | Added | Log manager class header |
| `Legacy/Services/EXLogManager.m` | Added | Implements EXLogInfo, EXLogWarn, EXLogError, EXFatal |
| `Legacy/Services/EXReactLogHandler.h` | Added | React Native log handler protocol |
| `Legacy/Services/EXReactLogHandler.m` | Added | React Native log handler implementation |
| `Legacy/Protocols/EXEventEmitter.h` | Added | Event emitter protocol for expo-av |
| `Legacy/Protocols/EXEventEmitterService.h` | Added | Event emitter service protocol |
| `Legacy/Protocols/EXLogHandler.h` | Added | Log handler protocol |
| `Legacy/EXUnimodulesCompat.h` | Added | UM* to EX* type mappings (UMPromiseResolveBlock, etc.) |
| `Legacy/EXUtilities.m` | Modified | Added EXErrorWithMessage() and EXSharedApplication() |
| `EXDefines.h` | Modified | Added EXLogInfo/Warn/Error, EXFatal, EXErrorWithMessage declarations |

### Manual Addition to EXUtilities.m

The following was manually added to `packages/expo-modules-core/ios/Legacy/EXUtilities.m`:

```objc
#pragma mark - Legacy compatibility functions (restored from commit a3490d958a)

#if TARGET_OS_OSX
NSApplication * EXSharedApplication(void)
{
  return [NSApplication sharedApplication];
}
#else
UIApplication * EXSharedApplication(void)
{
  if ([[[[NSBundle mainBundle] bundlePath] pathExtension] isEqualToString:@"appex"]) {
    return nil;
  }
  return [[UIApplication class] performSelector:@selector(sharedApplication)];
}
#endif

NSError *EXErrorWithMessage(NSString *message)
{
  NSDictionary<NSString *, id> *errorInfo = @{NSLocalizedDescriptionKey: message};
  return [[NSError alloc] initWithDomain:@"EXModulesErrorDomain" code:0 userInfo:errorInfo];
}
```

---

## node_modules Changes (Manual Patches Required)

### 1. react-native-zip-archive Podspec Fix

**File**: `node_modules/react-native-zip-archive/RNZipArchive.podspec`

**Issue**: The podspec has malformed `compiler_flags` using `-G` which is invalid on iOS.

**Fix**: Replace the `compiler_flags` line:

```diff
- s.compiler_flags = '-DZLIB_COMPAT -DWITH_GZFILEOP -DHAVE_ZLIB -DHAVE_STDINT_H -DHAVE_STDDEF_H -DHAVE_UNISTD_H -G'
+ s.compiler_flags = '-DZLIB_COMPAT -DWITH_GZFILEOP -DHAVE_ZLIB -DHAVE_STDINT_H -DHAVE_STDDEF_H -DHAVE_UNISTD_H'
```

**Note**: This change is lost when running `npm install`. You must re-apply it after every npm install.

### 2. ONNX Runtime xcframework

**File**: `node_modules/@runanywhere/onnx/ios/Frameworks/onnxruntime.xcframework`

**Issue**: The npm package may not include the full xcframework.

**Fix**: If you get `Undefined symbol: _OrtGetApiBase` errors, copy the xcframework from the local SDK:

```bash
cp -R /path/to/runanywhere-all/sdks/examples/ios/Frameworks/onnxruntime.xcframework \
      node_modules/@runanywhere/onnx/ios/Frameworks/
```

---

## All Files Modified Summary

### App-Level Files

| File | Changes |
|------|---------|
| `apps/runanywhere-ai-studio/package.json` | Updated RunAnywhere SDK versions to 0.17.4 |
| `apps/runanywhere-ai-studio/app.json` | Updated branding and description |
| `apps/runanywhere-ai-studio/ios/Exponent.xcodeproj/project.pbxproj` | Bundle IDs, signing, product name |
| `apps/runanywhere-ai-studio/ios/Exponent/Supporting/Info.plist` | Display name, permissions, URL schemes, version |
| `apps/runanywhere-ai-studio/ios/Exponent/Supporting/Exponent.entitlements` | Simplified for development |
| `apps/runanywhere-ai-studio/ios/Exponent/Supporting/GoogleService-Info.plist` | Placeholder Firebase config |
| `apps/runanywhere-ai-studio/ios/Exponent/Supporting/EXBuildConstants.plist` | SDK version placeholder |
| `apps/runanywhere-ai-studio/ios/ExpoNotificationServiceExtension/Info.plist` | Display name |
| `apps/runanywhere-ai-studio/ios/Podfile` | EXAV configuration |
| `apps/runanywhere-ai-studio/ios/Exponent/Images.xcassets/*` | RunAnywhere branded icons |
| `apps/runanywhere-ai-studio/ios/Client/AppDelegate.swift` | Disabled Firebase initialization |
| `apps/runanywhere-ai-studio/ios/Build-Phases/generate-dynamic-macros.sh` | Graceful exit if tools missing |

### Package-Level Files (expo-modules-core)

| File | Changes |
|------|---------|
| `packages/expo-modules-core/ios/EXDefines.h` | Added legacy function declarations |
| `packages/expo-modules-core/ios/Legacy/EXUtilities.m` | Added EXErrorWithMessage, EXSharedApplication |
| `packages/expo-modules-core/ios/Legacy/EXUnimodulesCompat.h` | **Added** - UM to EX mappings |
| `packages/expo-modules-core/ios/Legacy/Protocols/EXEventEmitter.h` | **Added** - Event emitter protocol |
| `packages/expo-modules-core/ios/Legacy/Protocols/EXEventEmitterService.h` | **Added** - Event emitter service |
| `packages/expo-modules-core/ios/Legacy/Protocols/EXLogHandler.h` | **Added** - Log handler protocol |
| `packages/expo-modules-core/ios/Legacy/Services/EXLogManager.h` | **Added** - Log manager header |
| `packages/expo-modules-core/ios/Legacy/Services/EXLogManager.m` | **Added** - Log manager impl |
| `packages/expo-modules-core/ios/Legacy/Services/EXReactLogHandler.h` | **Added** - React log handler |
| `packages/expo-modules-core/ios/Legacy/Services/EXReactLogHandler.m` | **Added** - React log handler impl |

### Other Package Files

| File | Changes |
|------|---------|
| `packages/expo-constants/scripts/get-app-config-ios.sh` | Graceful handling when @expo/config not built |

### node_modules (Must Re-apply After npm install)

| File | Changes |
|------|---------|
| `node_modules/react-native-zip-archive/RNZipArchive.podspec` | Removed invalid `-G` compiler flag |

---

## Issue 1: Signing & Provisioning Issues

### Error
```
Signing for "ExpoNotificationServiceExtension" requires a development team.
No Account for Team 'XXXXXXXX'.
No profiles for 'host.exp.Exponent' were found.
```

### Cause
The app was originally configured with Expo's official bundle identifier (`host.exp.Exponent`) and their team ID, which you cannot use with your own Apple Developer account.

### Solution

1. **Update Bundle Identifiers** in `project.pbxproj`:
   - Main app: `dev.runanywhere.aistudio`
   - Tests: `dev.runanywhere.aistudio.Tests`
   - Notification Extension: `dev.runanywhere.aistudio.NotificationServiceExtension`

2. **Set your Development Team** for all targets:
   ```
   DEVELOPMENT_TEAM = YOUR_TEAM_ID;
   CODE_SIGN_STYLE = Automatic;
   CODE_SIGN_IDENTITY = "Apple Development";
   ```

3. **Clear provisioning profile specifiers**:
   ```
   PROVISIONING_PROFILE_SPECIFIER = "";
   ```

4. **Simplify entitlements** - Remove capabilities that require App Store Connect setup (iCloud, associated domains, Sign in with Apple) for development builds.

---

## Issue 2: react-native-zip-archive - Unsupported '-G' Flag

### Error
```
unsupported option '-G' for target 'arm64-apple-ios15.1'
```

### Cause
The `RNZipArchive.podspec` has a malformed compiler flag:
```ruby
s.compiler_flags = '-GCC_PREPROCESSOR_DEFINITIONS="HAVE_INTTYPES_H ..."'
```

The `-GCC_PREPROCESSOR_DEFINITIONS` is interpreted by the compiler as `-G` followed by arguments, which is not supported on ARM64.

### Solution

**Option A: Patch the podspec directly** (in `node_modules/react-native-zip-archive/RNZipArchive.podspec`):

Change:
```ruby
s.compiler_flags = '-GCC_PREPROCESSOR_DEFINITIONS="HAVE_INTTYPES_H HAVE_PKCRYPT HAVE_STDINT_H HAVE_WZAES HAVE_ZLIB MZ_ZIP_NO_SIGNING $(inherited)"'
```

To:
```ruby
s.compiler_flags = '-DHAVE_INTTYPES_H -DHAVE_PKCRYPT -DHAVE_STDINT_H -DHAVE_WZAES -DHAVE_ZLIB -DMZ_ZIP_NO_SIGNING'
```

**Option B: Add a post-install hook** in the Podfile (less reliable):
```ruby
if pod_name == 'react-native-zip-archive'
  target_installation_result.native_target.build_configurations.each do |config|
    if config.build_settings['OTHER_CFLAGS']
      config.build_settings['OTHER_CFLAGS'] = config.build_settings['OTHER_CFLAGS'].gsub('-G', '')
    end
  end
end
```

Then run `pod install` again.

---

## Issue 3: EXAV Module - ExpoModulesCore Header Not Found

### Error
```
fatal error: 'ExpoModulesCore/EXEventEmitter.h' file not found
Unable to find module dependency: 'EXAV'
Clang dependency scanner failure: While building module 'EXAV'
```

### Cause
The EXAV (expo-av) module cannot find ExpoModulesCore headers during the Clang dependency scanning phase. This is a modular headers configuration issue that occurs before the build starts, so post_install header search paths don't help.

### Solution

**Option A: Exclude expo-av** (Recommended if not using audio/video features)

Add `'expo-av'` to the exclude list in `use_expo_modules!`:

```ruby
use_expo_modules!({
  exclude: [
    'expo-module-template',
    'expo-module-template-local',
    'expo-dev-launcher',
    'expo-dev-client',
    'expo-dev-menu',
    'expo-maps',
    'expo-network-addons',
    'expo-insights',
    'expo-splash-screen',
    '@expo/ui',
    '@expo/app-integrity',
    'expo-brownfield',
    'expo-av'  # Add this line
  ],
  includeTests: true,
  flags: {
    :inhibit_warnings => false
  }
})
```

Then clean and reinstall:
```bash
cd ios
rm -rf Pods Podfile.lock build
pod install
```

**Option B: Clean build and reinstall** (if you need expo-av):
```bash
# Clean derived data
rm -rf ~/Library/Developer/Xcode/DerivedData

# Clean pods completely
cd ios
rm -rf Pods Podfile.lock build
pod install

# In Xcode: Product > Clean Build Folder (Cmd+Shift+K)
```

**Option C: Add header search paths** (may not work for module scanning):
```ruby
# In Podfile post_install hook
if pod_name == 'EXAV'
  target_installation_result.native_target.build_configurations.each do |config|
    config.build_settings['HEADER_SEARCH_PATHS'] ||= ['$(inherited)']
    config.build_settings['HEADER_SEARCH_PATHS'] << '"$(PODS_ROOT)/Headers/Public/ExpoModulesCore"'
    config.build_settings['HEADER_SEARCH_PATHS'] << '"$(PODS_ROOT)/Headers/Private/ExpoModulesCore"'
  end
end
```

---

## General Build Tips

### Clean Build Steps

When encountering persistent build issues:

```bash
# 1. Clean npm/node_modules (optional, if dependencies changed)
rm -rf node_modules
npm install --ignore-scripts

# 2. Clean iOS build artifacts
cd ios
rm -rf Pods Podfile.lock build
rm -rf ~/Library/Developer/Xcode/DerivedData

# 3. Reinstall pods
pod install

# 4. In Xcode
# - Product > Clean Build Folder (Cmd+Shift+K)
# - Select your device/simulator
# - Build (Cmd+B)
```

### Common Environment Issues

1. **CocoaPods version**: Ensure you're using a compatible CocoaPods version
   ```bash
   gem install cocoapods
   pod --version  # Should be 1.14+ for best compatibility
   ```

2. **Xcode Command Line Tools**:
   ```bash
   xcode-select --install
   sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
   ```

3. **Ruby version**: Some issues arise from system Ruby conflicts
   ```bash
   # Consider using rbenv or rvm for Ruby management
   rbenv install 3.2.0
   rbenv global 3.2.0
   ```

### RunAnywhere SDK Verification

After `pod install`, verify RunAnywhere modules are loaded:
```
[RunAnywhereCore] Using bundled RACommons.xcframework from npm package
[NitroModules] 🔥 RunAnywhereCore is boosted by nitro!
[RunAnywhereLlama] Using bundled RABackendLLAMACPP.xcframework from npm package
[RunAnywhereONNX] Using bundled xcframeworks from npm package
```

---

## Version Information

- **App Version**: 1.0.0
- **Bundle ID**: dev.runanywhere.aistudio
- **Minimum iOS**: 15.1
- **React Native**: 0.83.1
- **Expo SDK**: 54.0.0
- **RunAnywhere SDK**: 0.17.4

---

## Contact

For additional help with RunAnywhere SDK integration, refer to the SDK documentation or contact the RunAnywhere team.
