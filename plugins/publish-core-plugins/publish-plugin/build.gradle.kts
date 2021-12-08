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
    kotlin("jvm")
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("plugin") {
            id = "publish-core-plugins"
            implementationClass = "ProtonPublishPluginsPlugin"
        }
    }
}

repositories {
    google()
    jcenter()
    gradlePluginPortal()
}

java.sourceSets["main"].java {
    srcDir("../../shared/src/main/kotlin")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.dokka.pluginGradle)
    implementation("com.gradle.publish:plugin-publish-plugin:0.18.0")
    implementation(libs.easyGradle.dsl)
    implementation(libs.publish.pluginGradle)
}
