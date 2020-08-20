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

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    jcenter()
    maven("https://dl.bintray.com/proton/Core-publishing")
}

dependencies {
    val android =       "4.0.0"         // Released: May 28, 2020
    val dokka =         "0.10.0"        // Released: Oct 07, 2019
    val easyGradle =    "1.5-beta-10"    // Released: Jun 14, 2020
    val protonGradle =  "0.1.10"         // Released: Aug 20, 2020

    // Needed for setup Android config
    implementation("com.android.tools.build:gradle:$android")
    // Needed for setup KDoc generation for publishing
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokka")
    // Set of utils for Gradle
    implementation("studio.forface.easygradle:dsl-android:$easyGradle")
    implementation("me.proton.core:util-gradle:$protonGradle")
}

kotlinDslPluginOptions.jvmTarget.set("1.8")
