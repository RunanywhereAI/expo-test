#!/usr/bin/env bash

# For RunAnywhere AI Studio, skip Expo-specific dynamic macros generation
# This script originally required expo-tools which aren't fully built in the forked setup

set -eo pipefail

echo "⚠️  RunAnywhere AI Studio: Skipping dynamic macros generation"
echo "✅ Build will continue without dynamic macros"
exit 0
