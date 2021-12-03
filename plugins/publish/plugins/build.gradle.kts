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
    id("com.gradle.plugin-publish") version "0.12.0"
}

object Plugin {
    const val group = "me.proton"
    const val name = "Publish-Plugins"
    const val version = "0.7"
    val id = "$group.$name".toLowerCase()
}

group = Plugin.group
version = Plugin.version

gradlePlugin {
    plugins {
        create(Plugin.id) {
            id = Plugin.id
            implementationClass = "ProtonPublishPluginsPlugin"
            version = Plugin.version
        }
    }
}

pluginBundle {
    val url = "https://github.com/ProtonMail/protoncore_android"
    website = url
    vcsUrl = url
    description = "Proton Gradle plugin"
    tags = listOf(
        "Android",
        "plugin",
        "Proton",
        "ProtonTechnologies",
        "ProtonMail",
        "ProtonVpn",
        "ProtonCalendar",
        "ProtonDrive"
    )

    plugins.getByName(Plugin.id).displayName = Plugin.name
}

repositories {
    google()
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.dokka.pluginGradle)
    implementation("com.gradle.publish:plugin-publish-plugin:0.18.0")
    implementation(libs.easyGradle.dsl)
    implementation(libs.publish.pluginGradle)
}
