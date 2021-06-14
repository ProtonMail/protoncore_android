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
    kotlin("android.extensions")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

libVersion = Version(1, 15, 2)

android(useDataBinding = true)

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.domain),
        project(Module.networkDomain),
        project(Module.userDomain),
        project(Module.presentation),

        // Feature
        project(Module.humanVerificationDomain),
        project(Module.country),

        // Kotlin
        `kotlin-jdk7`,
        `coroutines-android`,

        // Android
        `android-ktx`,
        `appcompat`,
        `constraint-layout`,
        `fragment`,
        `hilt-android`,
        `lifecycle-viewModel`,
        `material`,
        `lifecycle-runtime`
    )

    kapt(
        `hilt-android-compiler`,
        `hilt-androidx-compiler`
    )

    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))
}

