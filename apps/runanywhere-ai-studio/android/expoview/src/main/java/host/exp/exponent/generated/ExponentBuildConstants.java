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
    // Return empty string for RunAnywhere - we don't use built-in kernel manifest
    return "{}";
  }
}
