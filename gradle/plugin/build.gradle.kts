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
    kotlin("jvm") version "1.5.21" // Jul 13, 2021
    `java-gradle-plugin`
}

group = "me.proton"
version = "1.0"

gradlePlugin {
    plugins {
        create("gradlePlugin") {
            id = "core"
            implementationClass = "ProtonCorePlugin"
        }
    }
}

repositories {
    google()
    jcenter()
}

dependencies {
    val easyGradle = "2.7" // Oct 15, 2020
    val agpVersion = "7.0.1" // Aug 18, 2021

    implementation(gradleApi())
    compileOnly("com.android.tools.build:gradle:$agpVersion")
    api("studio.forface.easygradle:dsl-android:$easyGradle")
}
