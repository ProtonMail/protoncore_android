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
    config: ExtraConfig = {}

) = (this as ExtensionAware).extensions.configure<TestedExtension> {

    compileSdkVersion(targetSdk)
    defaultConfig {

        // Params
        appId?.let { applicationId = it }
        this.version = version

        // SDK
        minSdkVersion(minSdk)
        targetSdkVersion(targetSdk)
        ndkVersion = "20.0.5594570"

        // Other
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        // Annotation processors must be explicitly declared now.  The following dependencies on
        // the compile classpath are found to contain annotation processor.  Please add them to the
        // annotationProcessor configuration.
        // - auto-service-1.0-rc4.jar (com.google.auto.service:auto-service:1.0-rc4)
        //
        // Note that this option ( 👇 ) is deprecated and will be removed in the future.
        // See https://developer.android.com/r/tools/annotation-processor-error-message.html for
        // more details.
        javaCompileOptions.annotationProcessorOptions.includeCompileClasspath = true
    }

    buildFeatures.viewBinding = true
    // buildFeatures.dataBinding = true
    dataBinding.isEnabled = true

    // Add support for `src/x/kotlin` instead of `src/x/java` only
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    compileOptions {
        sourceCompatibility = ProtonCore.jdkVersion
        targetCompatibility = sourceCompatibility
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lintOptions {
        isAbortOnError = false
        textReport = true
        textOutput("stdout")
    }

    packagingOptions {
        exclude("go/*.java")
        exclude("licenses/*.txt")
        exclude("licenses/*.TXT")
        exclude("licenses/*.xml")
        exclude("META-INF/*.txt")
        exclude("META-INF/plexus/*.xml")
        exclude("org/apache/maven/project/*.xml")
        exclude("org/codehaus/plexus/*.xml")
        exclude("org/cyberneko/html/res/*.txt")
        exclude("org/cyberneko/html/res/*.properties")
        pickFirst("lib/armeabi-v7a/libgojni.so")
        pickFirst("lib/arm64-v8a/libgojni.so")
        pickFirst("lib/x86/libgojni.so")
        pickFirst("lib/x86_64/libgojni.so")
    }

    apply(config)
}

typealias ExtraConfig = TestedExtension.() -> Unit
