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

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    `java-gradle-plugin`
}

publishOption.shouldBePublishedAsPlugin = false

gradlePlugin {
    plugins {
        create("rootProjectPlugin") {
            id = "me.proton.core.root"
            displayName = "Proton build plugin for Core root project"
            implementationClass = "me.proton.core.gradle.plugin.RootProjectPlugin"
        }
        create("androidAppConvention") {
            id = "me.proton.core.app.android"
            displayName = "Proton build convention plugin for Android app modules"
            implementationClass = "me.proton.core.gradle.plugin.android.AndroidAppPlugin"
        }
        create("androidLibraryConvention") {
            id = "me.proton.core.library.android"
            displayName = "Proton build convention plugin for Android library modules"
            implementationClass = "me.proton.core.gradle.plugin.android.AndroidLibraryPlugin"
        }
        create("androidTestConvention") {
            id = "me.proton.core.test.android"
            displayName = "Proton build convention plugin for Android test modules"
            implementationClass = "me.proton.core.gradle.plugin.android.AndroidTestPlugin"
        }
        create("androidUiLibraryConvention") {
            id = "me.proton.core.library.android.ui"
            displayName = "Proton build convention plugin for Android UI library modules"
            implementationClass = "me.proton.core.gradle.plugin.android.AndroidUiLibraryPlugin"
        }
        create("composeUiLibraryConvention") {
            id = "me.proton.core.library.android.ui.compose"
            displayName = "Proton build convention plugin for Compose UI library modules"
            implementationClass = "me.proton.core.gradle.plugin.android.ComposeUiLibraryPlugin"
        }
        create("daggerConvention") {
            id = "me.proton.core.dagger"
            displayName = "Proton build convention plugin for dagger modules"
            implementationClass = "me.proton.core.gradle.plugin.dagger.DaggerPlugin"
        }
        create("kotlinLibraryConvention") {
            id = "me.proton.core.library.kotlin"
            displayName = "Proton build convention plugin for Kotlin library modules"
            implementationClass = "me.proton.core.gradle.plugin.kotlin.KotlinLibraryPlugin"
        }
    }
}

kotlin {
    explicitApiWarning()
}

repositories {
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(project(":coverage"))
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    compileOnly(libs.android.gradle)
    api(libs.easyGradle.androidDsl)
    api(libs.paparazzi.gradle.plugin)
}
