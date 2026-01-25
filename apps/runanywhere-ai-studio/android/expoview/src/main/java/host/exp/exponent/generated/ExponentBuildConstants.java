// Copyright 2016-present 650 Industries. All rights reserved.
// RunAnywhere AI Studio - SDK Version Constants
// This file provides a static SDK version for the stable build

package host.exp.exponent.generated;

public class ExponentBuildConstants {
  public static final String TEST_APP_URI = "";
  public static final String TEST_CONFIG = "";
  public static final String TEST_SERVER_URL = "";
  public static final String TEST_RUN_ID = "";
  public static final String BUILD_MACHINE_LOCAL_HOSTNAME = "";
  
  // SDK version must match the Expo SDK version this build supports
  // For stable Expo Go 54.0.6, this should be 54.0.0
  public static final String TEMPORARY_SDK_VERSION = "54.0.0";

  public static String getBuildMachineKernelManifestAndAssetRequestHeaders() {
    // Minimal kernel manifest for RunAnywhere AI Studio
    // This provides the required structure for HomeActivity initialization
    return "{\"manifest\":{" +
        "\"id\":\"runanywhere-ai-studio\"," +
        "\"createdAt\":\"2026-01-24T00:00:00.000Z\"," +
        "\"runtimeVersion\":\"54.0.0\"," +
        "\"launchAsset\":{\"key\":\"main\",\"contentType\":\"application/javascript\",\"url\":\"\"}," +
        "\"assets\":[]," +
        "\"extra\":{" +
          "\"expoClient\":{" +
            "\"name\":\"RunAnywhere AI Studio\"," +
            "\"slug\":\"runanywhere-ai-studio\"," +
            "\"version\":\"1.0.0\"," +
            "\"sdkVersion\":\"54.0.0\"," +
            "\"runtimeVersion\":\"54.0.0\"," +
            "\"platforms\":[\"ios\",\"android\"]," +
            "\"android\":{\"package\":\"com.runanywhere.aistudio\"}," +
            "\"ios\":{\"bundleIdentifier\":\"com.runanywhere.aistudio\"}" +
          "}," +
          "\"scopeKey\":\"@runanywhere/ai-studio\"" +
        "}" +
      "},\"assetRequestHeaders\":{}}";
  }
}
