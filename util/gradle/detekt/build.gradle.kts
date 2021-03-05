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

import org.gradle.kotlin.dsl.implementation
import studio.forface.easygradle.dsl.Version

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    `java-gradle-plugin`
}

val plugin = PluginConfig(
    name = "Detekt",
    version = Version(0, 2, 1)
)
pluginConfig = plugin

group = plugin.group
version = plugin.version

gradlePlugin {
    plugins {
        create(plugin.id) {
            id = plugin.id
            implementationClass = "ProtonDetektPlugin"
            version = plugin.version
        }
    }
}

repositories {
    google()
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.14.2")
    implementation("studio.forface.easygradle:dsl:2.7")
}
