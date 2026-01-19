#!/usr/bin/env bash

# For RunAnywhere AI Studio, skip Expo-specific dynamic macros generation
# This script originally required expo-tools which aren't available in the forked setup

set -eo pipefail

if [[ -z "$EXPO_TOOLS_DIR" ]]; then
  EXPO_TOOLS_DIR="${SRCROOT}/../tools"
fi

if [[ -f "$PODS_ROOT/../.xcode.env" ]]; then
  source "$PODS_ROOT/../.xcode.env"
fi
if [[ -f "$PODS_ROOT/../.xcode.env.local" ]]; then
  source "$PODS_ROOT/../.xcode.env.local"
fi

export PATH="${SRCROOT}/../../../bin:$PATH"

ET_BIN="$PODS_ROOT/../../../../bin/et"
EXPOTOOLS_JS="$PODS_ROOT/../../../../tools/bin/expotools.js"

# Check if expo tools are available
if [[ ! -f "$EXPOTOOLS_JS" ]]; then
  echo "⚠️  Expo tools not found at $EXPOTOOLS_JS"
  echo "⚠️  Skipping dynamic macros generation for RunAnywhere AI Studio"
  echo "✅ Build will continue without dynamic macros"
  exit 0
fi

if [ "${APP_OWNER}" == "Expo" ]; then
  $NODE_BINARY $ET_BIN ios-generate-dynamic-macros --configuration ${CONFIGURATION}
else
  $NODE_BINARY $ET_BIN ios-generate-dynamic-macros --configuration ${CONFIGURATION} --skip-template=GoogleService-Info.plist
fi
