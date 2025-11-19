#!/bin/bash

# Usage: ./build.sh v1.1

VERSION="$1"

if [ -z "$VERSION" ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

# Update gradle.properties
sed -i "s/^mod_version=.*/mod_version=${VERSION}/" gradle.properties
echo "Set mod_version=${VERSION} in gradle.properties"

./gradlew build

JAR_NAME="hspemoji-${VERSION}.jar"
SRC_PATH="build/libs/${JAR_NAME}"
DST_PATH="/home/warze/.local/share/multimc/instances/WynnCraft/.minecraft/mods/hspemojiv${VERSION}.jar"

echo "${VERSION}" > "/home/warze/.local/share/multimc/instances/WynnCraft/.minecraft/mods/hspemojilatest.txt"

cp "$SRC_PATH" "$DST_PATH"
echo "Copied ${JAR_NAME} to ${DST_PATH}"
