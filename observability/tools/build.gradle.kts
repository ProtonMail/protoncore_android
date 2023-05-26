import org.gradle.kotlin.dsl.implementation
import studio.forface.easygradle.dsl.`serialization-json`

/*
 * Copyright (c) 2022 Proton Technologies AG
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
    id("application")
    id("java")
    alias(libs.plugins.kotlin.gradle)
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("me.proton.core.observability.tools.ObservabilityToolsKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.4.0")
    implementation("com.github.victools:jsonschema-generator:4.28.0")
    implementation("com.github.victools:jsonschema-module-swagger-2:4.28.0")
    implementation(okhttp)
    implementation(`serialization-json`)
    implementation(`swagger-annotations`)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(project(Module.observabilityDomain))
}
