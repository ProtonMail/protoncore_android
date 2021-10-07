/*
 * Copyright (c) 2020 Proton Technologies AG
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

import com.android.build.gradle.TestedExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

/** Default value for `sharedTest` modules */
private val testVersion = Version(0, 0, 0)

/**
 * Dsl for apply the android configuration to a library or application module
 * @author Davide Farella
 */
@Suppress("UnstableApiUsage")
fun org.gradle.api.Project.android(

    version: Version = testVersion,
    appId: String? = null,
    minSdk: Int = ProtonCore.minSdk,
    targetSdk: Int = ProtonCore.targetSdk,
    useDataBinding: Boolean = false,
    useViewBinding: Boolean = false,
    config: ExtraConfig = {}

) = (this as ExtensionAware).extensions.configure<TestedExtension> {

    compileSdkVersion(targetSdk)
    defaultConfig {

        // Params
        appId?.let { applicationId = it }
        this.version = version

        // SDK
        this.minSdk = minSdk
        this.targetSdk = targetSdk
        ndkVersion = "21.3.6528147"

        // Other
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }

    // Data/View Binding turned off by default to prevent unneeded generation.
    // You must turn it on if you need it in your module:  android(useDataBinding = true).
    buildFeatures.viewBinding = useDataBinding || useViewBinding
    dataBinding.isEnabled = useDataBinding

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lintOptions {
        isAbortOnError = false
        textReport = true
        textOutput("stdout")
    }

    packagingOptions {
        resources.excludes.addAll(listOf(
            "go/*.java",
            "licenses/*.txt",
            "licenses/*.txt",
            "licenses/*.TXT",
            "licenses/*.xml",
            "META-INF/*.txt",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1",
            "META-INF/licenses/ASM",
            "META-INF/plexus/*.xml",
            "org/apache/maven/project/*.xml",
            "org/codehaus/plexus/*.xml",
            "org/cyberneko/html/res/*.txt",
            "org/cyberneko/html/res/*.properties"
        ))
        resources.pickFirsts.addAll(listOf(
            "lib/armeabi-v7a/libgojni.so",
            "lib/arm64-v8a/libgojni.so",
            "lib/x86/libgojni.so",
            "lib/x86_64/libgojni.so",
            "win32-x86-64/attach_hotspot_windows.dll",
            "win32-x86/attach_hotspot_windows.dll"
        ))
    }

    apply(config)
}

typealias ExtraConfig = TestedExtension.() -> Unit

fun String?.toBuildConfigValue(): String {
    return if (this != null) "\"$this\"" else "null"
}
