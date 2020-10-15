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

object Plugin {
    const val group = "me.proton"
    const val id = "publish-libraries"
    const val version = "0.1"
}

group = Plugin.group
version = Plugin.version

gradlePlugin {
    plugins {
        create("${Plugin.id}Plugin") {
            id = "${Plugin.group}.${Plugin.id}"
            implementationClass = "ProtonPublishLibrariesPlugin"
            version = Plugin.version
        }
    }
}

repositories {
    google()
    jcenter()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.10")
    implementation("studio.forface.easygradle:dsl:2.7")
    implementation("gradle.plugin.EasyPublish:plugin:0.2.4")
}
