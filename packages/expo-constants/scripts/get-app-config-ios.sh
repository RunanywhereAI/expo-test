#!/usr/bin/env bash

set -eo pipefail

DEST="$CONFIGURATION_BUILD_DIR"
RESOURCE_BUNDLE_NAME="EXConstants.bundle"
EXPO_CONSTANTS_PACKAGE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"

# For classic main project build phases integration, will be no-op to prevent duplicated app.config creation.
#
# `$PROJECT_DIR` is passed by Xcode as the directory to the xcodeproj file.
# in classic main project setup it is something like /path/to/app/ios
# in new style pod project setup it is something like /path/to/app/ios/Pods
PROJECT_DIR_BASENAME=$(basename $PROJECT_DIR)
if [ "x$PROJECT_DIR_BASENAME" != "xPods" ]; then
  exit 0
fi

# If PROJECT_ROOT is not specified, fallback to use Xcode PROJECT_DIR
PROJECT_ROOT=${PROJECT_ROOT:-"$PROJECT_DIR/../.."}
PROJECT_ROOT=${PROJECT_ROOT:-"$EXPO_CONSTANTS_PACKAGE_DIR/../.."}

cd "$PROJECT_ROOT" || exit

# ============================================================================
# RunAnywhere AI Studio: Skip app.config generation if @expo/config not built
# This is needed because the forked expo repo doesn't have compiled JS files
# ============================================================================
# Look for @expo/config relative to the expo-constants package (monorepo structure)
MONOREPO_ROOT="${EXPO_CONSTANTS_PACKAGE_DIR}/../.."
EXPO_CONFIG_BUILD="${MONOREPO_ROOT}/packages/@expo/config/build"

# Check if @expo/config build directory exists and has JS files
SHOULD_SKIP=false
if [ -d "$EXPO_CONFIG_BUILD" ]; then
  JS_FILES=$(find "$EXPO_CONFIG_BUILD" -name "*.js" -type f 2>/dev/null | head -1)
  if [ -z "$JS_FILES" ]; then
    SHOULD_SKIP=true
    echo "⚠️  @expo/config build dir exists but has no JS files"
  fi
else
  # Also check node_modules (for non-monorepo setups)
  NODE_MODULES_CONFIG="${MONOREPO_ROOT}/node_modules/@expo/config/build"
  if [ -d "$NODE_MODULES_CONFIG" ]; then
    JS_FILES=$(find "$NODE_MODULES_CONFIG" -name "*.js" -type f 2>/dev/null | head -1)
    if [ -z "$JS_FILES" ]; then
      SHOULD_SKIP=true
      echo "⚠️  @expo/config in node_modules has no JS files"
    fi
  else
    SHOULD_SKIP=true
    echo "⚠️  @expo/config not found in packages or node_modules"
  fi
fi

if [ "$SHOULD_SKIP" = true ]; then
  echo "⚠️  Skipping app.config generation for RunAnywhere AI Studio"
  echo "⚠️  Creating empty app.config placeholder"
  # Create resource bundle in the expected location
  RESOURCE_DEST="$DEST/$RESOURCE_BUNDLE_NAME"
  mkdir -p "$RESOURCE_DEST"
  echo '{}' > "$RESOURCE_DEST/app.config"
  exit 0
fi

# Default BUNDLE_FORMAT to "shallow" for iOS if not set
BUNDLE_FORMAT=${BUNDLE_FORMAT:-"shallow"}

if [ "$BUNDLE_FORMAT" == "shallow" ]; then
  RESOURCE_DEST="$DEST/$RESOURCE_BUNDLE_NAME"
elif [ "$BUNDLE_FORMAT" == "deep" ]; then
  RESOURCE_DEST="$DEST/$RESOURCE_BUNDLE_NAME/Contents/Resources"
  mkdir -p "$RESOURCE_DEST"
else
  echo "Unsupported bundle format: $BUNDLE_FORMAT"
  exit 1
fi

"${EXPO_CONSTANTS_PACKAGE_DIR}/scripts/with-node.sh" "${EXPO_CONSTANTS_PACKAGE_DIR}/scripts/getAppConfig.js" "$PROJECT_ROOT" "$RESOURCE_DEST"
