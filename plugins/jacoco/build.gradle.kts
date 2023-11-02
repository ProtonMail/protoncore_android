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

import org.gradle.kotlin.dsl.`java-gradle-plugin`
import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.gradlePlugin
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.repositories

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    `java-gradle-plugin`
}

publishOption.shouldBePublishedAsPlugin = true

gradlePlugin {
    plugins {
        create("plugin") {
            id = "me.proton.core.gradle-plugins.jacoco"
            displayName = "Proton jacoco plugin"
            description = "Plugin to generate jacoco coverage report compatible with GitLab"
            implementationClass = "ProtonJacocoPlugin"
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    val jacocoVersion = "0.8.10"

    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
    implementation("org.jacoco:org.jacoco.core:$jacocoVersion")
}
