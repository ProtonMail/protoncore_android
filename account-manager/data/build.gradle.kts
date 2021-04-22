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
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
    kotlin("kapt")
}

libVersion = Version(1, 1, 8)

android() {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                // arguments["room.incremental"] = "true"
            }
        }
    }
}

dependencies {

    implementation(
        project(Module.kotlinUtil),
        project(Module.network),
        project(Module.domain),
        project(Module.data),
        project(Module.accountManagerDomain),
        project(Module.crypto),

        // Features
        project(Module.authData),
        project(Module.authDomain),
        project(Module.accountData),
        project(Module.accountDomain),
        project(Module.userData),
        project(Module.userDomain),
        project(Module.keyData),
        project(Module.keyDomain),
        project(Module.humanVerificationData),
        project(Module.humanVerificationDomain),
        project(Module.mailSettingsData),
        project(Module.mailSettingsDomain),

        // Kotlin
        `kotlin-jdk8`,
        `coroutines-core`,

        // Other
        `room-ktx`
    )

    kapt(
        `room-compiler`
    )

    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))
}
