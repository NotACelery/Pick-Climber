#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

GRADLE_VERSION="9.2.1"
DIST_ROOT="$PWD/.gradle-dist"
DIST_DIR="$DIST_ROOT/gradle-$GRADLE_VERSION"
DIST_ZIP="$DIST_ROOT/gradle-$GRADLE_VERSION-bin.zip"
MOD_VERSION="$(sed -n 's/^mod_version=//p' gradle.properties | head -n 1)"
MINECRAFT_VERSION="$(sed -n 's/^minecraft_version=//p' gradle.properties | head -n 1)"

if [ -z "$MOD_VERSION" ] || [ -z "$MINECRAFT_VERSION" ]; then
    echo "ERROR: gradle.properties does not define mod_version and minecraft_version." >&2
    exit 1
fi

EXPECTED_JAR="$PWD/build/libs/pickclimber-$MINECRAFT_VERSION-$MOD_VERSION.jar"

if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: Java is not installed or is not in PATH. Java 21 is required." >&2
    exit 1
fi

if [ ! -x "$DIST_DIR/bin/gradle" ]; then
    mkdir -p "$DIST_ROOT"
    echo "Downloading Gradle $GRADLE_VERSION..."
    curl -L "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$DIST_ZIP"
    unzip -q -o "$DIST_ZIP" -d "$DIST_ROOT"
fi

echo "Building Pick Climber release $MOD_VERSION for Minecraft $MINECRAFT_VERSION..."
"$DIST_DIR/bin/gradle" clean build

if [ ! -f "$EXPECTED_JAR" ]; then
    echo "ERROR: Gradle finished, but the expected JAR was not found:" >&2
    echo "  $EXPECTED_JAR" >&2
    exit 1
fi

echo "DONE: $EXPECTED_JAR"
