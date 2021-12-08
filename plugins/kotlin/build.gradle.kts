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
import org.gradle.kotlin.dsl.kotlin

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    `java-gradle-plugin`
}

publishOption.shouldBePublishedAsPlugin = true

gradlePlugin {
    plugins {
        create("plugin") {
            id = "me.proton.core.gradle-plugins.kotlin"
            displayName = "Proton kotlin plugin"
            description = "Plugin to apply kotlin setup"
            implementationClass = "ProtonKotlinPlugin"
        }
    }
}

repositories {
    google()
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
}
