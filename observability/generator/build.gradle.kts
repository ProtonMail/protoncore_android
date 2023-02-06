import org.gradle.kotlin.dsl.implementation
import studio.forface.easygradle.dsl.*

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
    id("org.jetbrains.kotlin.jvm")
}

application {
    mainClass.set("me.proton.core.observability.generator.ObservabilityGeneratorKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.4.0")
    implementation("com.github.victools:jsonschema-generator:4.28.0")
    implementation("com.github.victools:jsonschema-module-swagger-2:4.28.0")
    implementation(`swagger-annotations`)
    implementation(`kotlin-reflect`)
    implementation(project(Module.observabilityDomain))
}
