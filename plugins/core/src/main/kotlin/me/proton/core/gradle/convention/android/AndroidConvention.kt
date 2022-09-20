/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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
import me.proton.core.gradle.AndroidDefaults
import me.proton.core.gradle.convention.BuildConvention
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

internal class AndroidConvention : BuildConvention<AndroidConventionSettings> {
    override fun apply(target: Project, settings: AndroidConventionSettings) {
        val targetSdk = AndroidDefaults.targetSdk

        target.extensions
            .findByType<LibraryExtension>()?.let {
                it.applyConvention(settings)
                it.defaultConfig.targetSdk = targetSdk
            }

        target.extensions
            .findByType<ApplicationExtension>()
            ?.let {
                it.applyConvention(settings)
                it.defaultConfig.targetSdk = targetSdk
            }
    }
}

private fun <T> T.applyConvention(settings: AndroidConventionSettings) where T : CommonExtension<*, *, *, *> {
    compileSdk = AndroidDefaults.compileSdk
    ndkVersion = AndroidDefaults.ndkVersion

    buildFeatures {
        viewBinding = settings.viewBinding
    }

    defaultConfig {
        minSdk = AndroidDefaults.minSdk
        // Note: targetSdk is set separately, since this property doesn't exist in `CommonExtension`
        testInstrumentationRunner = AndroidDefaults.testInstrumentationRunner
        vectorDrawables.useSupportLibrary = settings.vectorDrawablesSupport
    }

    lint {
        abortOnError = false
        textReport = true
        textOutput = File("stdout")
    }

    packagingOptions {
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

    // Ensure sources are set for published artifacts
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}
