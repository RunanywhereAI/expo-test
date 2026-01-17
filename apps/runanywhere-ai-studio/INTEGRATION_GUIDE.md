# RunAnywhere AI Studio - Integration Guide

## Executive Summary

**RunAnywhere AI Studio = Expo Go + RunAnywhere Native Libraries**

This is a **direct fork of Expo Go** with RunAnywhere SDK native binaries pre-bundled. Users get the exact same Expo Go experience, but with on-device AI capabilities.

---

## The Vision: Vibe Coding with On-Device AI

### How It Works (User Flow)

```
1. Developer on Replit/Cursor creates an Expo app
   └── Adds: @runanywhere/core, @runanywhere/llamacpp, @runanywhere/onnx to package.json

2. Developer runs: npx expo start
   └── Metro bundler shows QR code

3. User scans QR with RunAnywhere AI Studio (NOT Expo Go)
   └── App loads from Metro bundler
   └── RunAnywhere APIs work immediately (native binaries pre-bundled!)

4. Developer's app can call:
   └── LlamaCpp.loadModel(), LlamaCpp.generate()
   └── ONNX.loadModel(), ONNX.run()
   └── All on-device, no cloud required
```

### Why This Works

| Scenario | Expo Go | RunAnywhere AI Studio |
|----------|---------|----------------------|
| Developer adds `@runanywhere/llamacpp` | ❌ Crashes - no native binary | ✅ Works - binary pre-bundled |
| Developer adds `expo-camera` | ✅ Works | ✅ Works (same Expo modules) |
| Scan QR code | ✅ Loads bundle | ✅ Loads bundle (same kernel) |
| Load GGUF model | ❌ Not possible | ✅ Native llama.cpp available |

---

## Changes Made to Expo Go

### 1. package.json
- Renamed from `@expo/home` to `@runanywhere/ai-studio`
- Added dependencies:
  - `@runanywhere/core: ^0.16.11`
  - `@runanywhere/llamacpp: ^0.16.11`
  - `@runanywhere/onnx: ^0.16.11`
  - `react-native-nitro-modules: ^0.31.10`

### 2. app.json
- Changed name to "RunAnywhere AI Studio"
- Changed bundle identifiers:
  - iOS: `dev.runanywhere.aistudio`
  - Android: `dev.runanywhere.aistudio`
- Changed scheme from `exp` to `runanywhere`

### 3. Android settings.gradle
Added module includes:
```groovy
include ':react-native-nitro-modules'
include ':runanywhere_core'
include ':runanywhere_llamacpp'
include ':runanywhere_onnx'
```

### 4. Android expoview/build.gradle
Added dependencies:
```groovy
implementation project(':react-native-nitro-modules')
implementation project(':runanywhere_core')
implementation project(':runanywhere_llamacpp')
implementation project(':runanywhere_onnx')
```

### 5. Android app/build.gradle
- Changed namespace to `dev.runanywhere.aistudio`
- Changed applicationId to `dev.runanywhere.aistudio`
- Changed versionCode to 1, versionName to 1.0.0

### 6. Android strings.xml
- Changed app name to "RunAnywhere AI Studio"

### 7. iOS Podfile
Added pods:
```ruby
pod 'NitroModules', :path => '../node_modules/react-native-nitro-modules'
pod 'RunAnywhereCore', :path => '../node_modules/@runanywhere/core'
pod 'RunAnywhereLlama', :path => '../node_modules/@runanywhere/llamacpp'
pod 'RunAnywhereONNX', :path => '../node_modules/@runanywhere/onnx'
```

### 8. App Icons
Copied from `sdks/examples/android/RunAnywhereAI/` sample app.

---

## Comparison: Expo Go vs RunAnywhere AI Studio

| Feature | Expo Go | RunAnywhere AI Studio |
|---------|---------|----------------------|
| Scan QR code | ✅ | ✅ |
| Load from Metro | ✅ | ✅ |
| expo-camera | ✅ | ✅ |
| expo-file-system | ✅ | ✅ |
| All 50+ Expo modules | ✅ | ✅ |
| Recent projects | ✅ | ✅ |
| Error overlay | ✅ | ✅ |
| Dev menu | ✅ | ✅ |
| @runanywhere/core | ❌ | ✅ |
| @runanywhere/llamacpp | ❌ | ✅ |
| @runanywhere/onnx | ❌ | ✅ |
| On-device LLM | ❌ | ✅ |
| On-device ML | ❌ | ✅ |

---

## What Developers Do

### In Their Replit/Cursor Project

```json
// package.json
{
  "dependencies": {
    "expo": "~54.0.0",
    "@runanywhere/core": "^0.16.11",
    "@runanywhere/llamacpp": "^0.16.11",
    "@runanywhere/onnx": "^0.16.11"
  }
}
```

### In Their App Code

```typescript
// App.tsx
import { LlamaCpp } from '@runanywhere/llamacpp';

export default function App() {
  const [response, setResponse] = useState('');
  
  const runAI = async () => {
    // This works because RunAnywhere AI Studio has the native binaries!
    await LlamaCpp.loadModel('/path/to/model.gguf');
    const result = await LlamaCpp.generate('Hello, AI!');
    setResponse(result);
  };

  return (
    <View>
      <Button title="Run AI" onPress={runAI} />
      <Text>{response}</Text>
    </View>
  );
}
```

### Run Flow

```bash
# Developer runs Metro
npx expo start

# Shows QR code - scan with RunAnywhere AI Studio
# App loads, RunAnywhere APIs work!
```

---

## Build Process

### Prerequisites
- Node.js 18+
- Yarn (workspace)
- Android Studio with NDK
- Xcode 15+

### Build Commands

```bash
# From expo-test root
cd /path/to/EXPO/expo-test

# Install all dependencies
yarn install

# Build Android
cd apps/runanywhere-ai-studio/android
./gradlew assembleRelease

# Build iOS
cd ../ios
pod install
xcodebuild -workspace Exponent.xcworkspace -scheme "Expo Go" -configuration Release
```

---

## Directory Structure

```
EXPO/expo-test/apps/runanywhere-ai-studio/
├── android/
│   ├── app/
│   │   ├── build.gradle          # ← Modified: namespace, applicationId
│   │   └── src/main/res/
│   │       ├── mipmap-*/         # ← Replaced: RunAnywhere icons
│   │       └── values/strings.xml # ← Modified: app name
│   ├── expoview/
│   │   └── build.gradle          # ← Modified: added RunAnywhere deps
│   └── settings.gradle           # ← Modified: added RunAnywhere modules
├── ios/
│   └── Podfile                   # ← Modified: added RunAnywhere pods
├── app.json                      # ← Modified: rebranded
├── package.json                  # ← Modified: renamed, added deps
└── INTEGRATION_GUIDE.md          # ← This file
```

---

## FAQ

### Q: Will apps made for Expo Go work in RunAnywhere AI Studio?

**Yes!** It's the same Expo kernel. Any app that works in Expo Go will work in RunAnywhere AI Studio.

### Q: Will RunAnywhere APIs work in regular Expo Go?

**No.** Expo Go doesn't have the native binaries. The app will crash if it tries to use `@runanywhere/llamacpp`.

### Q: Can developers use both Expo modules and RunAnywhere?

**Yes!** That's the whole point. They get the full Expo ecosystem PLUS on-device AI.

### Q: What about app size?

The native libraries add approximately:
- llama.cpp: ~5-10 MB per architecture
- ONNX Runtime: ~10-15 MB per architecture
- Total additional: ~15-25 MB

This is comparable to adding a few Expo modules.

---

## Next Steps

1. ✅ Copy expo-go app
2. ✅ Add RunAnywhere packages to package.json
3. ✅ Modify Android settings.gradle
4. ✅ Modify Android expoview/build.gradle
5. ✅ Modify iOS Podfile
6. ✅ Rebrand (name, bundle ID, icons)
7. 🔄 Install dependencies and build
8. ⏳ Test on device
9. ⏳ Publish to app stores
