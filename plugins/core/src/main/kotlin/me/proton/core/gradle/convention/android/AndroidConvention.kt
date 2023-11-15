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

@file:Suppress("UnstableApiUsage") // AGP APIs are @Incubating

package me.proton.core.gradle.convention.android

import java.io.File
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Packaging
import com.android.build.api.dsl.TestExtension
import me.proton.core.gradle.convention.BuildConvention
import me.proton.core.gradle.plugin.CommonConfigurationExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

internal class AndroidConvention : BuildConvention<AndroidConventionSettings> {
    override fun apply(target: Project, settings: AndroidConventionSettings) {
        val commonConfig = target.rootProject.extensions.getByType<CommonConfigurationExtension>()
        val targetSdk = commonConfig.targetSdk

        target.extensions.findByType<LibraryExtension>()?.applyConvention(commonConfig, settings)

        target.extensions.findByType<ApplicationExtension>()?.let {
            it.applyConvention(commonConfig, settings)
            it.defaultConfig.targetSdk = targetSdk.get()
        }

        target.extensions.findByType<TestExtension>()?.let {
            it.applyConvention(commonConfig, settings)
            it.defaultConfig.targetSdk = targetSdk.get()
        }
    }
}

private fun <T> T.applyConvention(
    commonConfig: CommonConfigurationExtension,
    settings: AndroidConventionSettings
) where T : CommonExtension<*, *, *, *, *> {
    compileSdk = commonConfig.compileSdk.get()
    ndkVersion = commonConfig.ndkVersion.get()

    buildFeatures {
        viewBinding = settings.viewBinding
    }

    defaultConfig {
        minSdk = commonConfig.minSdk.get()
        // Note: targetSdk is set separately, since this property doesn't exist in `CommonExtension`
        testInstrumentationRunner = commonConfig.testInstrumentationRunner.get()
        vectorDrawables.useSupportLibrary = settings.vectorDrawablesSupport
    }

    compileOptions {
        sourceCompatibility = commonConfig.jvmTarget.get()
        targetCompatibility = commonConfig.jvmTarget.get()
    }

    lint {
        abortOnError = false
        textReport = true
        textOutput = File("stdout")
    }

    packaging {
        applyExclusions()
    }

    // Ensure sources are set for published artifacts
    sourceSets {
        findByName("main")?.java?.srcDirs("src/main/kotlin")
        findByName("test")?.java?.srcDirs("src/test/kotlin")
        findByName("androidTest")?.java?.srcDirs("src/androidTest/kotlin")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

private fun Packaging.applyExclusions() {
    resources.excludes.addAll(
        listOf(
            "go/*.java",
            "licenses/*.txt",
            "licenses/*.txt",
            "licenses/*.TXT",
            "licenses/*.xml",
            "MANIFEST.MF",
            "META-INF/*.txt",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1",
            "META-INF/licenses/ASM",
            "META-INF/LICENSE*",
            "META-INF/plexus/*.xml",
            "org/apache/maven/project/*.xml",
            "org/codehaus/plexus/*.xml",
            "org/cyberneko/html/res/*.txt",
            "org/cyberneko/html/res/*.properties"
        )
    )
    resources.pickFirsts.addAll(
        listOf(
            "lib/armeabi-v7a/libgojni.so",
            "lib/arm64-v8a/libgojni.so",
            "lib/x86/libgojni.so",
            "lib/x86_64/libgojni.so",
            "win32-x86-64/attach_hotspot_windows.dll",
            "win32-x86/attach_hotspot_windows.dll"
        )
    )
}
