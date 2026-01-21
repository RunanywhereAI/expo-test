# iOS Build Troubleshooting Guide

This document contains solutions to iOS build issues encountered with the RunAnywhere AI Studio app during migration from the bleeding-edge `expo-test-2` to the stable 54.0.6 base.

**Last Updated**: January 20, 2026  
**Build Status**: ✅ **iOS & ANDROID FULLY WORKING** - Both platforms build successfully, load user apps from Replit

---

## Migration Overview

This repo (`expo-test/`) was reset to the **stable Expo Go 54.0.6 commit** and is receiving incremental migrations from `expo-test-2/` (bleeding-edge fork with custom changes).

### Why This Migration?

The `expo-test-2` fork was based on Expo's bleeding-edge `main` branch (Jan 17, 2026) which had:
- **Missing Legacy APIs**: `EXEventEmitter`, `EXLogManager`, etc. were removed just 9 days earlier
- **PlatformConstants TurboModule Error**: Runtime crash when loading user apps
- **Unstable base**: Required extensive cherry-picking and patching

The stable 54.0.6 version:
- **Already includes Legacy APIs**: No cherry-picking needed
- **Published to App Store/Play Store**: Known to be stable
- **Better baseline**: Fewer unexpected issues

### Key Differences

| Aspect | expo-test-2 (bleeding edge) | expo-test (stable 54.0.6) |
|--------|---------------------------|--------------------------|
| React Native | 0.83.1 | 0.81.4 |
| React | 19.2.0 | 19.1.0 |
| Legacy APIs | Required cherry-picking | Already included |
| Home Screen | SwiftUI (kernel bypassed) | React Native kernel |
| expo-av | Required patching | Works with auto-linking |

---

## ⚠️ Manual Steps NOT in Git

The following changes must be applied manually after cloning or after `npm install`:

| Step | Command | Why Not in Git |
|------|---------|----------------|
| 1. Fix react-native-zip-archive | `sed -i '' "s/-GCC_PREPROCESSOR.*inherited)\"/'-DHAVE_INTTYPES_H ...'/" node_modules/react-native-zip-archive/RNZipArchive.podspec` | node_modules is gitignored |
| 2. Copy ONNX Runtime xcframework | `cp -R ../expo-test-2/.../onnxruntime.xcframework node_modules/@runanywhere/onnx/ios/Frameworks/` | node_modules is gitignored, binary too large for npm |

**These must be re-applied every time you run `npm install`.**

---

## Quick Start for New Developers

### Prerequisites
- macOS with Xcode 15+
- Node.js 18+
- CocoaPods 1.14+
- Apple Developer account (for device testing)

### Step-by-Step Setup

```bash
# 1. Install npm dependencies from workspace root
cd /path/to/expo-test
npm install --ignore-scripts

# 2. Apply node_modules patches (REQUIRED after every npm install)
# Fix react-native-zip-archive compiler flags
sed -i '' "s/-GCC_PREPROCESSOR_DEFINITIONS.*inherited)\"/'-DHAVE_INTTYPES_H -DHAVE_PKCRYPT -DHAVE_STDINT_H -DHAVE_WZAES -DHAVE_ZLIB -DMZ_ZIP_NO_SIGNING'/" \
  node_modules/react-native-zip-archive/RNZipArchive.podspec

# Copy ONNX Runtime xcframework (not included in npm package due to size)
cp -R ../expo-test-2/expo-test/node_modules/@runanywhere/onnx/ios/Frameworks/onnxruntime.xcframework \
  node_modules/@runanywhere/onnx/ios/Frameworks/

# 3. Navigate to the iOS project
cd apps/runanywhere-ai-studio/ios

# 4. Install CocoaPods
pod install

# 5. Open in Xcode
open Exponent.xcworkspace

# 6. In Xcode:
#    - Select your development team in Signing & Capabilities
#    - Select your device/simulator
#    - Build and Run (Cmd+R)
```

### If Build Fails

1. **Clean build**: `Cmd+Shift+K` in Xcode
2. **Clean derived data**: `rm -rf ~/Library/Developer/Xcode/DerivedData`
3. **Reinstall pods**: `rm -rf Pods Podfile.lock && pod install`
4. **Check this document** for specific error solutions

---

## Migration Progress & Changelog

### Session: January 20, 2026

#### Phase 1.1: Add RunAnywhere SDK Dependencies ✅

**File**: `apps/runanywhere-ai-studio/package.json`

**Changes Made**:
```json
"@runanywhere/core": "0.17.4",
"@runanywhere/llamacpp": "0.17.4",
"@runanywhere/onnx": "0.17.4",
"react-native-nitro-modules": "^0.31.10",
"react-native-fs": "^2.20.0",
"react-native-zip-archive": "^6.1.0",
"expo-av": "~16.0.8"
```

Also added autolinking exclude:
```json
"expo": {
  "autolinking": {
    "exclude": ["@runanywhere/ai-studio"]
  }
}
```

**Status**: ✅ Completed

---

#### Phase 1.2: Update Podfile for RunAnywhere SDK ✅

**File**: `apps/runanywhere-ai-studio/ios/Podfile`

**Changes Made**:

1. **Added RunAnywhere SDK pods**:
```ruby
pod 'NitroModules', :path => '../../../node_modules/react-native-nitro-modules'
pod 'RunAnywhereCore', :path => '../../../node_modules/@runanywhere/core'
pod 'RunAnywhereLlama', :path => '../../../node_modules/@runanywhere/llamacpp'
pod 'RunAnywhereONNX', :path => '../../../node_modules/@runanywhere/onnx'
```

2. **Updated react-native-maps** (removed Google Maps dependency):
```ruby
pod 'react-native-maps', :path => '../../../node_modules/react-native-maps'
```

3. **Added expo-av auto-linking note**:
```ruby
# expo-av (EXAV) now works with auto-linking after adding EXEventEmitter.h to ExpoModulesCore
puts "[RunAnywhere] expo-av enabled via auto-linking"
```

4. **Added hermesvm MinimumOSVersion fix** in post_install:
```ruby
hermes_framework_paths = [
  "#{installer.sandbox.root}/hermes-engine/destroot/Library/Frameworks/ios/hermesvm.framework/Info.plist",
  # ... other paths
]
hermes_framework_paths.each do |plist_path|
  if File.exist?(plist_path)
    system("/usr/libexec/PlistBuddy -c 'Add :MinimumOSVersion string 15.1' '#{plist_path}' ...")
  end
end
```

5. **Added react-native-zip-archive fix** in post_install:
```ruby
if pod_name == 'react-native-zip-archive'
  target_installation_result.native_target.build_configurations.each do |config|
    if config.build_settings['OTHER_CFLAGS']
      config.build_settings['OTHER_CFLAGS'] = config.build_settings['OTHER_CFLAGS'].gsub('-G', '')
    end
  end
end
```

**Status**: ✅ Completed

---

#### Phase 1.3: Patch Build Scripts ✅

**Task**: Make build scripts gracefully handle missing Expo tools

##### generate-dynamic-macros.sh

**File**: `apps/runanywhere-ai-studio/ios/Build-Phases/generate-dynamic-macros.sh`

**Original Issue**: Script requires Expo tools (`expotools.js`) which may not be fully functional in the forked repo.

**Fix Applied**: Script now exits gracefully:
```bash
#!/usr/bin/env bash
# For RunAnywhere AI Studio, skip Expo-specific dynamic macros generation
echo "⚠️  RunAnywhere AI Studio: Skipping dynamic macros generation"
echo "✅ Build will continue without dynamic macros"
exit 0
```

**Status**: ✅ Completed

##### get-app-config-ios.sh

**File**: `packages/expo-constants/scripts/get-app-config-ios.sh`

**Original Issue**: Script requires `@expo/config` which may not be built in the forked setup.

**Fix Applied**: Added graceful skip logic:
```bash
# Check if @expo/config build directory exists
if [ "$SHOULD_SKIP" = true ]; then
  echo "⚠️  Skipping app.config generation for RunAnywhere AI Studio"
  echo "⚠️  Creating empty app.config placeholder"
  mkdir -p "$RESOURCE_DEST"
  echo '{}' > "$RESOURCE_DEST/app.config"
  exit 0
fi
```

**Status**: ✅ Completed

---

#### Phase 1.4: Add hermesvm Build Phase ✅

**File**: `apps/runanywhere-ai-studio/ios/Exponent.xcodeproj/project.pbxproj`

**Task**: Add Xcode build phase to fix hermesvm.framework MinimumOSVersion for App Store validation.

**Changes Made**:

1. Added build phase reference to target:
```
RAFIX0001HERMESMINVERSION /* [RunAnywhere] Fix hermesvm MinimumOSVersion */,
```

2. Added build phase definition:
```
RAFIX0001HERMESMINVERSION /* [RunAnywhere] Fix hermesvm MinimumOSVersion */ = {
  isa = PBXShellScriptBuildPhase;
  name = "[RunAnywhere] Fix hermesvm MinimumOSVersion";
  shellScript = "# Fix hermesvm.framework MinimumOSVersion for App Store validation
HERMES_FRAMEWORK=\"${TARGET_BUILD_DIR}/${FRAMEWORKS_FOLDER_PATH}/hermesvm.framework\"
HERMES_PLIST=\"${HERMES_FRAMEWORK}/Info.plist\"
if [ -f \"$HERMES_PLIST\" ]; then
    /usr/libexec/PlistBuddy -c \"Add :MinimumOSVersion string 15.1\" \"$HERMES_PLIST\" 2>/dev/null || \\
    /usr/libexec/PlistBuddy -c \"Set :MinimumOSVersion 15.1\" \"$HERMES_PLIST\"
    echo \"[RunAnywhere] Fixed MinimumOSVersion in hermesvm.framework\"
    # Re-sign the framework after modification
    if [ -n \"$EXPANDED_CODE_SIGN_IDENTITY\" ]; then
        /usr/bin/codesign --force --sign \"$EXPANDED_CODE_SIGN_IDENTITY\" --preserve-metadata=identifier,entitlements,flags --timestamp=none \"$HERMES_FRAMEWORK\"
        echo \"[RunAnywhere] Re-signed hermesvm.framework\"
    fi
fi";
};
```

**Status**: ✅ Completed

---

#### Phase 1.5: Remove GoogleMaps Dependencies ✅

**File**: `apps/runanywhere-ai-studio/ios/Exponent/ExpoKit/ExpoKit.m`

**Original Issue**: ExpoKit.m imported GoogleMaps which wasn't in the Podfile.

**Changes Made**:
1. Removed `#import <GoogleMaps/GoogleMaps.h>`
2. Removed `GMSServices` API key configuration code

**Status**: ✅ Completed

---

### Current Build Issues

#### Issue: ONNX Runtime Undefined Symbols ✅ FIXED

**Error**:
```
Undefined symbol: _OrtGetApiBase
Undefined symbol: _OrtSessionOptionsAppendExecutionProvider_CoreML
```

**Cause**: The `@runanywhere/onnx` npm package ships without the `onnxruntime.xcframework` (large binary). It must be manually copied.

**Fix Applied**: Copy from expo-test-2 (or source location):
```bash
cp -R ../expo-test-2/expo-test/node_modules/@runanywhere/onnx/ios/Frameworks/onnxruntime.xcframework \
  node_modules/@runanywhere/onnx/ios/Frameworks/
```

Then run `pod install` again:
```bash
cd apps/runanywhere-ai-studio/ios
pod install
```

**Note**: This must be done after every clean `npm install` that recreates node_modules.

**Status**: ✅ Fixed

---

## Known Issues & Fixes

### Issue 1: react-native-zip-archive - Unsupported '-G' Flag

**Error**:
```
unsupported option '-G' for target 'arm64-apple-ios15.1'
```

**Cause**: The `RNZipArchive.podspec` has malformed compiler flags.

**Fix**: Patch the podspec after every `npm install`:
```bash
sed -i '' "s/-GCC_PREPROCESSOR_DEFINITIONS.*inherited)\"/'-DHAVE_INTTYPES_H -DHAVE_PKCRYPT -DHAVE_STDINT_H -DHAVE_WZAES -DHAVE_ZLIB -DMZ_ZIP_NO_SIGNING'/" \
  node_modules/react-native-zip-archive/RNZipArchive.podspec
```

Or manually change in `node_modules/react-native-zip-archive/RNZipArchive.podspec`:
```ruby
# Before
s.compiler_flags = '-GCC_PREPROCESSOR_DEFINITIONS="HAVE_INTTYPES_H ..."'

# After
s.compiler_flags = '-DHAVE_INTTYPES_H -DHAVE_PKCRYPT -DHAVE_STDINT_H -DHAVE_WZAES -DHAVE_ZLIB -DMZ_ZIP_NO_SIGNING'
```

---

### Issue 2: GoogleMaps Header Not Found

**Error**:
```
'GoogleMaps/GoogleMaps.h' file not found
```

**Cause**: ExpoKit.m imports GoogleMaps but we're using Apple Maps.

**Fix**: Remove GoogleMaps references from `Exponent/ExpoKit/ExpoKit.m`:
1. Remove `#import <GoogleMaps/GoogleMaps.h>`
2. Remove `GMSServices provideAPIKey:` calls

---

### Issue 3: hermesvm.framework MinimumOSVersion (App Store Validation)

**Error during validation**:
```
Invalid MinimumOSVersion. MinimumOSVersion in 
'RunAnywhere AI Studio.app/Frameworks/hermesvm.framework' is ''.
```

**Cause**: Hermes framework ships without MinimumOSVersion which Apple requires.

**Fix**: Added build phase `[RunAnywhere] Fix hermesvm MinimumOSVersion` that:
1. Adds MinimumOSVersion to Info.plist
2. Re-signs the framework

This runs after "[CP] Embed Pods Frameworks" during every build.

---

### Issue 4: Signing & Provisioning

**Error**:
```
No Account for Team 'C8D8QTF339'.
No profiles for 'host.exp.Exponent' were found.
```

**Cause**: App was using Expo's official bundle ID.

**Fix** (already applied in this repo):
- Bundle ID: `com.runanywhere.aistudio`
- Team ID: `L86FH3K93L`
- Code Sign Style: Automatic

---

### Issue 5: Firebase Configuration (Already Fixed)

**Error**:
```
Could not get GOOGLE_APP_ID in Google Services file from build environment
```

**Fix** (already applied):
- `GoogleService-Info.plist` with placeholder values exists
- `FirebaseApp.configure()` is commented out in `AppDelegate.swift`

---

### Issue 6: EXBuildConstants.plist Missing (Already Fixed)

**Error**:
```
Build input file cannot be found: '.../EXBuildConstants.plist'
```

**Fix** (already applied):
- Copied `EXBuildConstants.plist.example` to `EXBuildConstants.plist`

---

### Issue 7: ONNX Runtime Symbols Undefined

**Error**:
```
Undefined symbol: _OrtGetApiBase
Undefined symbol: _OrtSessionOptionsAppendExecutionProvider_CoreML
```

**Cause**: The `@runanywhere/onnx` npm package doesn't include the `onnxruntime.xcframework` due to its large size (~100MB). It must be manually copied.

**Fix**: Copy from expo-test-2 or another source:
```bash
cp -R EXPO/expo-test-2/expo-test/node_modules/@runanywhere/onnx/ios/Frameworks/onnxruntime.xcframework \
  EXPO/expo-test/node_modules/@runanywhere/onnx/ios/Frameworks/
```

Then reinstall pods:
```bash
cd apps/runanywhere-ai-studio/ios
pod install
```

**Important**: This must be re-applied after every clean `npm install` that recreates `node_modules`.

---

## Pending Migration Phases

### Phase 2: SwiftUI Kernel Bypass (If Needed)

**Purpose**: Implement native SwiftUI home screen to bypass React Native kernel and avoid `PlatformConstants` TurboModule error.

**Note**: Test Phase 1 first to see if the stable 54.0.6 version has the PlatformConstants issue. If not, Phase 2 may not be needed.

**Files to copy from expo-test-2**:
```
ios/Client/SwiftUI/
├── DesignSystem.swift
├── HomeRootView.swift
├── HomeViewController.swift
├── HomeViewModel.swift
├── HomeTabView.swift
├── ExpoGoHomeBridge.swift
├── RAHomeView.swift
├── RASettingsView.swift
├── RADiagnosticsTabView.swift
├── Diagnostics/
├── GraphQL/
├── Rows/
├── Services/
├── Storage/
├── Utils/
└── Views/
```

**EXRootViewController.m changes needed**:
```objc
#import "RunAnywhere_AI_Studio-Swift.h"

- (void)createRootAppAndMakeVisible
{
  _homeViewController = [[HomeViewController alloc] init];
  [self _showHomeViewController];
}
```

---

### Phase 3: Branding Assets ✅ COMPLETE

**Critical Build Settings** (`ios/Exponent.xcodeproj/project.pbxproj`):
- `PRODUCT_NAME = "RunAnywhere AI Studio"` (was "Expo Go")
- `EX_BUNDLE_NAME = "RunAnywhere AI Studio"` (was "Expo")
- Target renamed from "Expo Go" to "RunAnywhere AI Studio"
- Scheme renamed from "Expo Go.xcscheme" to "RunAnywhere AI Studio.xcscheme"
- Podfile target updated to match
- These control the actual app name shown on home screen, Settings, and App Store

**Swift Bridging Header** (required after target rename):
When the target is renamed from "Expo Go" to "RunAnywhere AI Studio", Xcode auto-generates
a new Swift bridging header named `RunAnywhere_AI_Studio-Swift.h` (spaces become underscores).

Files updated from `Expo_Go-Swift.h` → `RunAnywhere_AI_Studio-Swift.h`:
- `Client/EXRootViewController.m`
- `Client/EXHomeAppManager.m`
- `Exponent/Kernel/Services/EXUpdatesManager.h`
- `Exponent/Kernel/Services/EXSplashScreen/EXSplashScreenService.h`
- `Exponent/Kernel/Services/EXSplashScreen/EXSplashScreenService.m`
- `Exponent/Kernel/Views/EXErrorView.m`
- `Exponent/Kernel/Views/Loading/EXAppLoadingProgressWindowController.m`
- `Exponent/Kernel/Views/Loading/EXProgressHUD.m`
- `Exponent/Kernel/AppLoader/AppFetcher/EXAppFetcher.m`
- `Exponent/Kernel/AppLoader/EXAppLoaderExpoUpdates.m`
- `Exponent/Kernel/AppLoader/EXDevelopmentHomeLoader.m`
- `Exponent/Versioned/Core/Internal/EXLinkingManager.m`

**iOS Native Assets** (copied from expo-test-2):
- `ios/Exponent/Images.xcassets/AppIcon.appiconset/*` - App icons
- `ios/Exponent/Images.xcassets/ExpoGoLaunchIcon.imageset/*` - Launch screen
- `ios/Exponent/Images.xcassets/expo-go-logo.imageset/*` - Logo
- `ios/Exponent/Images.xcassets/Icon.imageset/*` - Icon variants
- `ios/Exponent/Images.xcassets/branch-icon.imageset/*`
- `ios/Exponent/Images.xcassets/cli.imageset/*`
- `ios/Exponent/Images.xcassets/snack.imageset/*`
- `ios/Exponent/Images.xcassets/shake-device.imageset/*`
- `ios/Exponent/Images.xcassets/three-finger-long-press.imageset/*`
- `ios/Exponent/Images.xcassets/update-icon.imageset/*`

**iOS Native Config**:
- `ios/Exponent/Supporting/Info.plist`:
  - `CFBundleDisplayName`: "RunAnywhere AI"
  - Permission descriptions updated
  - URL schemes: `runanywhere`, `runanywhere-ai`

**React Native Branding** (src/ directory):
- `src/constants/Colors.ts`: Changed tint from `#4e9bde` (Expo blue) → `#FF5500` (RunAnywhere orange)
- `src/assets/client-logo.png`: Replaced with RunAnywhere logo
- **Text changes** ("Expo Go" → "RunAnywhere"):
  - `src/screens/HomeScreen/HomeScreenHeader.tsx` - Header title
  - `src/components/UserReviewSection.tsx` - Review prompt
  - `src/menu/DevMenuOnboarding.tsx` - Dev menu intro
  - `src/components/UpdateListItem.tsx` - Compatibility message
  - `src/components/SnacksListItem.tsx` - SDK warning
  - `src/components/ProjectsListItem.tsx` - Code comment
  - `src/screens/HomeScreen/UpgradeWarning/index.tsx` - Upgrade title/body
  - `src/screens/HomeScreen/UpgradeWarning/IosMessage.tsx` - iOS upgrade message
  - `src/screens/HomeScreen/UpgradeWarning/AndroidMessage.tsx` - Android upgrade message
  - `src/utils/PermissionUtils.ts` - Camera permission alert

**App Config**:
- `app.json`: `primaryColor: "#FF5500"`, `android.package: "com.runanywhere.aistudio"`

---

### Phase 4: Android Migration ✅ COMPLETE

**Build Configuration** (`android/app/build.gradle`):
- `namespace`: `host.exp.exponent` → `com.runanywhere.aistudio`
- `applicationId`: `host.exp.exponent` → `com.runanywhere.aistudio`
- `appAuthRedirectScheme`: `com.runanywhere.aistudio`
- `versionCode`: 1, `versionName`: "1.0.0"

**SDK Version Fix**:
- Created `android/expoview/src/main/java/host/exp/exponent/generated/ExponentBuildConstants.java`
  - `TEMPORARY_SDK_VERSION = "54.0.0"` (was auto-generated with old SDK by expotools)
  - This fixes "Project is incompatible" errors on Android

**Branding - strings.xml**:
- `versioned_app_name`: "RunAnywhere AI Studio"
- `unversioned_app_name`: "RunAnywhere AI Studio (dev)"
- `preference_file_key`: `com.runanywhere.aistudio.SharedPreferences`
- `error_default_client`: "...go back to RunAnywhere home..."

**Branding - colors.xml** (Full palette):
- `runAnywherePrimary`: #FF5500 (orange)
- `runAnywhereSecondary`: #3B82F6 (blue)
- `runAnywhereSuccess/Error/Warning`: Semantic colors
- Dark background theme: `#0F172A` (backgroundPrimaryDark)
- `ic_launcher_background`, `splashscreen_background`: Dark theme
- `notification_icon_color`: #FF5500

**Branding - ManifestException.kt**:
- All "Expo Go" error messages → "RunAnywhere"

**App Icons** (copied from expo-test-2):
- `mipmap-*/ic_launcher.png` - Updated launcher icons
- `mipmap-*/ic_launcher_foreground.png` - Adaptive icon foregrounds
- `mipmap-*/ic_launcher_round.png` - Round icons
- `mipmap-anydpi-v26/` - Adaptive icon XMLs
- `mipmap-ldpi/` - Low density support
- `drawable/ic_launcher_background_ra.xml` - RunAnywhere background
- `drawable/ic_launcher_foreground_ra.xml` - RunAnywhere foreground

**Note**: Kernel bypass (LauncherActivity.kt changes) not needed - vanilla Expo Go works correctly.

---

## Files Modified Summary

### App-Level Files

| File | Changes |
|------|---------|
| `apps/runanywhere-ai-studio/package.json` | Added RunAnywhere SDK dependencies |
| `apps/runanywhere-ai-studio/app.json` | RunAnywhere branding, primary color #FF5500 |
| `apps/runanywhere-ai-studio/ios/Podfile` | Added SDK pods, fixes for zip-archive and hermesvm |
| `apps/runanywhere-ai-studio/ios/Exponent.xcodeproj/project.pbxproj` | Added hermesvm build phase |
| `apps/runanywhere-ai-studio/ios/Exponent/ExpoKit/ExpoKit.m` | Removed GoogleMaps references |
| `apps/runanywhere-ai-studio/ios/Build-Phases/generate-dynamic-macros.sh` | Graceful skip |
| `apps/runanywhere-ai-studio/ios/Exponent/Supporting/Info.plist` | CFBundleDisplayName: "RunAnywhere AI Studio", permissions text |
| `apps/runanywhere-ai-studio/ios/Exponent.xcodeproj/project.pbxproj` | PRODUCT_NAME, EX_BUNDLE_NAME = "RunAnywhere AI Studio" |
| `apps/runanywhere-ai-studio/ios/Exponent/Supporting/EXBuildConstants.plist` | SDK version 54.0.0 |
| `apps/runanywhere-ai-studio/ios/Exponent/Supporting/EXDynamicMacros.h` | SDK version 54.0.0 |
| `apps/runanywhere-ai-studio/ios/Exponent/Images.xcassets/*` | RunAnywhere branded icons and images |
| `apps/runanywhere-ai-studio/src/constants/Colors.ts` | Tint color: Expo blue → RunAnywhere orange |
| `apps/runanywhere-ai-studio/src/**/*.tsx` | "Expo Go" text → "RunAnywhere" (12 files) |
| `apps/runanywhere-ai-studio/android/app/build.gradle` | namespace, applicationId: com.runanywhere.aistudio |
| `apps/runanywhere-ai-studio/android/app/src/main/res/values/strings.xml` | Full RunAnywhere AI Studio branding |
| `apps/runanywhere-ai-studio/android/app/src/main/res/values/colors.xml` | Complete RunAnywhere dark theme palette |
| `apps/runanywhere-ai-studio/android/app/src/main/res/mipmap-*/*` | RunAnywhere launcher icons (all densities) |
| `apps/runanywhere-ai-studio/android/app/src/main/res/drawable/*_ra.xml` | RunAnywhere adaptive icon config |
| `apps/runanywhere-ai-studio/android/expoview/.../generated/ExponentBuildConstants.java` | SDK version 54.0.0 |
| `apps/runanywhere-ai-studio/android/expoview/.../ManifestException.kt` | Error messages: RunAnywhere |

### Package-Level Files

| File | Changes |
|------|---------|
| `packages/expo-constants/scripts/get-app-config-ios.sh` | Graceful handling when @expo/config not built |

### node_modules (Re-apply After npm install)

| File | Changes |
|------|---------|
| `node_modules/react-native-zip-archive/RNZipArchive.podspec` | Removed invalid `-G` compiler flag |
| `node_modules/@runanywhere/onnx/ios/Frameworks/onnxruntime.xcframework` | Copied from expo-test-2 (not included in npm package) |

---

## Comparison with expo-test-2

### What We DON'T Need to Do (Thanks to Stable Base)

The stable 54.0.6 base already includes:
- ✅ `EXEventEmitter.h` and `EXEventEmitterService.h`
- ✅ `EXLogManager.h/m` with EXLogInfo/Warn/Error/Fatal
- ✅ `EXUnimodulesCompat.h` with UM* to EX* mappings
- ✅ `EXUtilities.m` with EXErrorWithMessage and EXSharedApplication

**In expo-test-2, we had to cherry-pick these from commit `a3490d958a`** because the bleeding-edge Expo removed them on Jan 8, 2026.

### What We Still Need to Do

1. ✅ Add RunAnywhere SDK dependencies
2. ✅ Update Podfile with SDK pods and fixes
3. ✅ Patch build scripts for graceful handling
4. ✅ Add hermesvm MinimumOSVersion build phase
5. ✅ Remove GoogleMaps dependencies
6. ✅ Fix ONNX Runtime linker errors (copy onnxruntime.xcframework)
7. ✅ iOS Build succeeds
8. ✅ Fix SDK version in EXBuildConstants.plist (21.0.0 → 54.0.0)
9. ✅ Test loading user apps from Replit - **WORKS!**
10. ✅ Copy RunAnywhere branding assets (icons, Info.plist)
11. ⏳ Android migration

---

## Issue 8: "Project is Incompatible" - Wrong SDK Version in Build Constants ✅ FIXED

**Date**: January 20, 2026

### Error

When loading a remote Expo app (e.g., from Replit), the app shows:
```
Project is incompatible with this version of Expo Go
```

With Xcode console logs:
```
🟠 {"level":"error","code":"UpdateFailedToLoad","message":"Failed to launch embedded or launchable update: No launchable updates found in database: Unknown error"}
```

### Root Cause (FOUND!)

The `EXBuildConstants.plist` and `EXDynamicMacros.h` had **outdated SDK versions**:

| File | Before | After |
|------|--------|-------|
| `EXBuildConstants.plist` → `TEMPORARY_SDK_VERSION` | `21.0.0` ❌ | `54.0.0` ✅ |
| `EXDynamicMacros.h` → `TEMPORARY_SDK_VERSION` | `7.0.0` ❌ | `54.0.0` ✅ |

The app was reporting itself as **SDK 21**, but the Replit app requires **SDK 54**. The version check logic saw `54 > 21` and returned `EXPERIENCE_SDK_VERSION_TOO_NEW` → "Project is incompatible".

### Why This Happened

These files are normally generated by `generate-dynamic-macros.sh` at build time. We disabled that script (Step 1.3) because it requires Expo tools that aren't built in our fork. The existing template files had very old placeholder values.

### Fix Applied

Updated both files to use SDK 54.0.0:

**EXBuildConstants.plist**:
```xml
<key>TEMPORARY_SDK_VERSION</key>
<string>54.0.0</string>
```

**EXDynamicMacros.h**:
```c
#define TEMPORARY_SDK_VERSION @"54.0.0"
```

### How the SDK Version Check Works

```objc
// EXManifestResource.m
NSInteger manifestSdkVersion = [maybeManifest.expoGoSDKVersion integerValue]; // From remote app (54)
NSInteger supportedSdkVersion = [[self supportedSdkVersionInt] integerValue]; // From EXBuildConstants (was 21, now 54)

if (manifestSdkVersion > supportedSdkVersion) {
  errorCode = @"EXPERIENCE_SDK_VERSION_TOO_NEW"; // This was triggered!
}
```

### Status

✅ **FIXED** - Rebuild the app and test loading from Replit again

---

## General Build Tips

### Clean Build Steps

```bash
# 1. Clean npm/node_modules (if dependencies changed)
rm -rf node_modules
npm install --ignore-scripts

# 2. Apply required patches
# (see Issue 1 above for react-native-zip-archive)

# 3. Clean iOS build artifacts
cd apps/runanywhere-ai-studio/ios
rm -rf Pods Podfile.lock build
rm -rf ~/Library/Developer/Xcode/DerivedData

# 4. Reinstall pods
pod install

# 5. In Xcode
# - Product > Clean Build Folder (Cmd+Shift+K)
# - Select device/simulator
# - Build (Cmd+B)
```

### RunAnywhere SDK Verification

After `pod install`, verify in output:
```
[NitroModules] 🔥 Your app is boosted by nitro modules!
[RunAnywhereCore] Using bundled RACommons.xcframework from npm package
[NitroModules] 🔥 RunAnywhereCore is boosted by nitro!
[RunAnywhereLlama] Using bundled RABackendLLAMACPP.xcframework from npm package
[NitroModules] 🔥 RunAnywhereLlama is boosted by nitro!
[RunAnywhereONNX] Using bundled xcframeworks from npm package
[NitroModules] 🔥 RunAnywhereONNX is boosted by nitro!
```

---

## Version Information

- **Base Version**: Expo Go 54.0.6 (stable)
- **Bundle ID**: com.runanywhere.aistudio
- **Minimum iOS**: 15.1
- **React Native**: 0.81.4
- **Expo SDK**: ~54.0.12
- **RunAnywhere SDK**: 0.17.4

---

## Contact

For additional help with RunAnywhere SDK integration, refer to the SDK documentation or contact the RunAnywhere team.
