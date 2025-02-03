/*
 * Copyright (c) 2021 Proton Technologies AG
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
        create("mockFilesPuller") {
            id = "me.proton.core.gradle-plugins.mock-proxy"
            displayName = "Proton pull recordings mock-proxy plugin."
            description = "Pulls recorded files from and to defined locations."
            implementationClass = "mockproxy.MockFilesPullerPlugin"
        }
    }
}

repositories {
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    compileOnly(libs.android.gradle)
    api(libs.easyGradle.androidDsl)
}
