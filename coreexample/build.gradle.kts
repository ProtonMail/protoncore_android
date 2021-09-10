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
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android(
    version = Version(0, 5, 1),
    useDataBinding = true
)
{
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                // arguments["room.incremental"] = "true"
            }
        }
        buildConfigField("String", "HOST", "\"proton.black\"")
    }
}

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.presentation),
        project(Module.network),
        project(Module.domain),
        project(Module.data),
        project(Module.dataRoom),

        // Features
        project(Module.account),
        project(Module.accountManager),
        project(Module.accountManagerDagger),
        project(Module.auth),
        project(Module.crypto),
        project(Module.domain),
        project(Module.gopenpgp),
        project(Module.humanVerification),
        project(Module.key),
        project(Module.user),
        project(Module.mailMessage),
        project(Module.mailSettings),
        project(Module.payment),
        project(Module.country),
        project(Module.plan),
        project(Module.userSettings),

        `kotlin-jdk7`,
        `coroutines-android`,

        // Android
        `activity`,
        `appcompat`,
        `constraint-layout`,
        `fragment`,
        `gotev-cookieStore`,
        `hilt-android`,
        `lifecycle-viewModel`,
        `hilt-androidx-annotations`,
        `material`,
        `android-work-runtime`,

        // Other
        `room-ktx`,
        `retrofit`,
        `timber`
    )

    kapt(
        `hilt-android-compiler`,
        `hilt-androidx-compiler`,
        `room-compiler`
    )

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))

    // Lint - off temporary
    // lintChecks(project(Module.lint))
}
