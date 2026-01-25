# RunAnywhere AI Studio - App Store & Play Store Release Plan

## Executive Summary

This document tracks the release of RunAnywhere AI Studio to Apple App Store and Google Play Store. The app is a customized Expo Go fork with integrated RunAnywhere SDK (llama.cpp, ONNX) for on-device AI capabilities.

---

## Current State Assessment

### ✅ What's Working

| Component | iOS | Android | Notes |
|-----------|-----|---------|-------|
| Native Shell | ✅ | ✅ | App name, bundle ID, signing |
| RunAnywhere SDK | ✅ | ✅ | llama.cpp, ONNX integrated |
| Native Modules | ✅ | ✅ | Nitro modules registered |
| App Branding (Native) | ✅ | ✅ | Name: "RunAnywhere AI Studio" |

### ⚠️ What Needs Work

| Component | iOS | Android | Issue |
|-----------|-----|---------|-------|
| Home Screen UI | ❌ Custom JS fails | ✅ Native UI exists | iOS: Polyfill initialization order |
| Custom JS Bundle | ❌ FormData error | N/A (uses native) | iOS: expo export bundle incompatible |

---

## Platform-Specific Approach

### Android Strategy: Native Home UI ✅ (Already Done)

Android already has `HomeActivity.kt` with native UI that:
- Shows "RunAnywhere AI Studio" branding
- Allows connecting to Metro bundler
- Bypasses kernel JS entirely

**Status:** Ready for release with minor UI polish

### iOS Strategy: Fix Bundle Initialization

iOS needs the kernel JS bundle to work. Two sub-options:

#### Option A: Native Home UI (Like Android) - RECOMMENDED
Create a native Swift/ObjC home screen that:
- Shows RunAnywhere branding
- Connects to Metro bundler for development
- Bypasses the kernel JS entirely

#### Option B: Fix Bundle Loading
Modify `EXEmbeddedHomeLoader.m` to properly initialize polyfills before running the custom bundle.

---

## TODO List

### Phase 1: iOS Native Home UI (Priority: HIGH)
- [ ] **iOS-1**: Create native HomeViewController.swift with RunAnywhere branding
- [ ] **iOS-2**: Replace EXRootViewController home app loading with native UI
- [ ] **iOS-3**: Add "Connect to Metro" functionality (like Android)
- [ ] **iOS-4**: Test Metro connection works for development
- [ ] **iOS-5**: Clean build and verify app launches

### Phase 2: Android UI Polish (Priority: MEDIUM)
- [ ] **ANDROID-1**: Review HomeActivity.kt UI design
- [ ] **ANDROID-2**: Add RunAnywhere logo/icon to home screen
- [ ] **ANDROID-3**: Improve color scheme to match iOS
- [ ] **ANDROID-4**: Test Metro connection functionality
- [ ] **ANDROID-5**: Clean build and verify

### Phase 3: App Store Submission (Priority: HIGH)
- [ ] **APPSTORE-1**: Update app icon (iOS)
- [ ] **APPSTORE-2**: Create App Store screenshots
- [ ] **APPSTORE-3**: Write App Store description
- [ ] **APPSTORE-4**: Archive iOS build
- [ ] **APPSTORE-5**: Submit to App Store Connect
- [ ] **APPSTORE-6**: Complete App Store review questionnaire

### Phase 4: Play Store Submission (Priority: HIGH)
- [ ] **PLAYSTORE-1**: Update app icon (Android)
- [ ] **PLAYSTORE-2**: Create Play Store screenshots
- [ ] **PLAYSTORE-3**: Write Play Store description
- [ ] **PLAYSTORE-4**: Generate signed APK/AAB
- [ ] **PLAYSTORE-5**: Submit to Google Play Console
- [ ] **PLAYSTORE-6**: Complete Play Store content rating

### Phase 5: Future - Custom JS Bundle (Priority: LOW)
- [ ] **BUNDLE-1**: Investigate Expo kernel build process
- [ ] **BUNDLE-2**: Create proper bundle with polyfills
- [ ] **BUNDLE-3**: Test custom UI from src/
- [ ] **BUNDLE-4**: Document bundle build process

---

## Technical Details

### iOS Native Home Implementation Plan

**File to create:** `ios/Client/RunAnywhereHomeViewController.swift`

```swift
// Conceptual structure
class RunAnywhereHomeViewController: UIViewController {
    // 1. Display RunAnywhere branding
    // 2. Metro URL input field
    // 3. "Connect" button
    // 4. Instructions text
    
    func connectToMetro(url: String) {
        // Use EXKernel to open the experience
        EXKernel.sharedInstance().openExperience(...)
    }
}
```

**File to modify:** `ios/Client/EXRootViewController.m`
- Change from loading kernel JS to showing native home UI
- Keep EXKernel available for opening Metro experiences

### Android Current Implementation

**File:** `android/expoview/.../experience/HomeActivity.kt`
- Already implements native UI
- Already has Metro URL input
- Already has connect functionality
- Just needs visual polish

---

## Release Timeline (Estimated)

| Task | Duration | Blocker |
|------|----------|---------|
| iOS Native Home UI | 2-4 hours | None |
| Android UI Polish | 1-2 hours | None |
| App Store Assets | 1-2 hours | None |
| Play Store Assets | 1 hour | None |
| iOS Archive & Submit | 1 hour | iOS Native Home |
| Android Build & Submit | 1 hour | Android Polish |

**Total Estimated Time:** 6-10 hours to both stores

---

## App Store Guidelines Compliance

### Potential Concerns

1. **Dynamic Code Loading** - The app can load JS from Metro bundler
   - Mitigation: This is for development only, similar to other dev tools
   - The embedded functionality (AI models) is legitimate

2. **Hidden Features** - Dev mode activation
   - Mitigation: No hidden payment or restricted features

3. **Guideline 2.5.2** - Apps that download code
   - Note: Expo Go is approved on App Store with this functionality
   - We're providing a legitimate AI development tool

### Recommended App Store Description

> RunAnywhere AI Studio is an on-device AI development environment. 
> Run large language models, speech recognition, and machine learning 
> models locally on your device without cloud dependencies.
>
> Features:
> • On-device LLM inference with llama.cpp
> • ONNX Runtime for ML models
> • No internet required for AI processing
> • Connect to your development server for testing

---

## Notes

- Created: January 24, 2026
- Last Updated: January 24, 2026
- Status: In Progress
