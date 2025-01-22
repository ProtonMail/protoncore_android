#!/usr/bin/env bash

# Copyright (c) 2025 Proton AG
# This file is part of Proton AG and ProtonCore.
#
# ProtonCore is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# ProtonCore is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.

set -e

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

echo "Updating Gradle Wrapper JAR checksum..."
echo "https://docs.gradle.org/current/userguide/gradle_wrapper.html#manually_verifying_the_gradle_wrapper_jar"
WRAPPER_DIR="${SCRIPT_DIR}/../wrapper"
WRAPPER_SHA_FILE="${WRAPPER_DIR}/gradle-wrapper.jar.sha256"
GRADLE_VERSION=$(grep distributionUrl "${WRAPPER_DIR}/gradle-wrapper.properties" | sed -E 's/.*gradle-([0-9]+\.[0-9]+(\.[0-9]+)?)-.*$/\1/')
truncate -s 0 "${WRAPPER_SHA_FILE}"
{
  echo "# Gradle ${GRADLE_VERSION}"
  echo "# Updated on $(date)"
  curl --silent --show-error --location "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-wrapper.jar.sha256"
  echo "  gradle-wrapper.jar"
} >> "${WRAPPER_SHA_FILE}"
echo "Checking Gradle Wrapper JAR checksum..."
(cd "${WRAPPER_DIR}" && shasum -a 256 --check "${WRAPPER_SHA_FILE}")


echo ""
echo "Updating Gradle distribution checksum (distributionSha256Sum)..."
DISTRIBUTION_SHA_256_SUM=$(curl --silent --show-error --location "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip.sha256")
WRAPPER_PROPERTIES_FILE="${WRAPPER_DIR}/gradle-wrapper.properties"
sed -i '' -e "s/^distributionSha256Sum=.*$/distributionSha256Sum=${DISTRIBUTION_SHA_256_SUM}/" "${WRAPPER_PROPERTIES_FILE}"
echo "Gradle distribution checksum updated (${DISTRIBUTION_SHA_256_SUM})."


echo ""
echo "Updating Gradle dependency verification metadata..."
echo "https://docs.gradle.org/current/userguide/dependency_verification.html#sec:bootstrapping-verification"
./gradlew --write-verification-metadata sha256 assembleDebug computeResolvedDependencies


echo ""
echo "VERIFICATION DATA UPDATED."
echo ""
echo "NOTE: Manually fetch and update the checksum for group=\"com.android.tools.build\" name=\"aapt2\" for linux/osx artefact"
echo "      by executing the following command (replacing LIB_ARCH and LIB_VERSION appropriately):"
echo ""
echo "      > LIB_ARCH=linux && LIB_VERSION=8.7.3-12006047 && curl \"https://dl.google.com/android/maven2/com/android/tools/build/aapt2/\${LIB_VERSION}/aapt2-\${LIB_VERSION}-\${LIB_ARCH}.jar.sha256\""
echo ""
echo ""
echo "NOTE: Manually fetch and update the checksum for group=\"app.cash.paparazzi\" name=\"layoutlib-native-*\" for linux/macarm artefact"
echo "      by executing the following command (replacing LIB_ARCH and LIB_VERSION appropriately):"
echo ""
echo "      > LIB_ARCH=linux && LIB_VERSION=2022.2.1-5128371-2 && curl \"https://repo1.maven.org/maven2/app/cash/paparazzi/layoutlib-native-\${LIB_ARCH}/\${LIB_VERSION}/layoutlib-native-\${LIB_ARCH}-\${LIB_VERSION}.pom.sha256\""
echo ""
echo "You can verify the Gradle checksums independently by visiting https://gradle.org/release-checksums/"
