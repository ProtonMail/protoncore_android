/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import java.lang.Module
import java.util.Properties

plugins {
    protonAndroidLibrary
    kotlin("plugin.serialization")
}

val rootDirPath = rootDir.path
val localProperties = Properties().apply {
    val propertiesFile = file("$rootDirPath/local.properties")
    if (propertiesFile.exists()) {
        load(propertiesFile.inputStream())
    }
}

fun getProperty(key: String, value: String = ""): String {
    return System.getenv(key) ?: localProperties.getProperty(key, value)
}

fun getCommitSha(): String {
    return (System.getenv("CI_COMMIT_SHA") ?: getGitCommitSha()).toBuildConfigValue()
}

protonCoverage.disabled.set(true)
publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.test.performance"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "BUILD_COMMIT_SHA1", getCommitSha())
        buildConfigField("String", "CI_RUN_ID", getProperty("CI_JOB_ID", "unknown").toBuildConfigValue())
    }
}

fun getGitCommitSha(): String? {
    return try {
        val result = providers.of(CommandResultValueSource::class.java) {
            parameters {
                commandLine = listOf("git", "rev-parse", "HEAD")
                workingDir = projectDir
            }
        }
        val commitSha = result.orNull?.trim()
        commitSha
    } catch (e: Exception) {
        null
    }
}

dependencies {
    implementation(
        okhttp,
        junit,
        `android-ktx`,
        `serialization-json`,
        `androidx-test-monitor`,
        `android-test-runner`,
        `compose-ui-test-junit`,
        `junit-ktx`,
        retrofit
    )

    testImplementation(
        mockk
    )
}

