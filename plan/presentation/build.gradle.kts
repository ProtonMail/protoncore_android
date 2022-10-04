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
    protonAndroidUiLibrary
    protonDagger
    id("kotlin-parcelize")
}

proton {
    apiModeDisabled()
}

publishOption.shouldBePublishedAsLib = true

android {
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    api(
        project(Module.domain),
        project(Module.paymentDomain),
        project(Module.paymentPresentation),
        project(Module.planDomain),
        project(Module.presentation),
        project(Module.userSettingsDomain),
        project(Module.userDomain),
        `activity-noktx`,
        appcompat,
        `constraint-layout`,
        coordinatorlayout,
        `hilt-android`,
        material,
        recyclerview,
        `startup-runtime`
    )

    coreLibraryDesugaring(`desugar-jdk-libs`)

    implementation(
        project(Module.kotlinUtil),
        `android-ktx`,
        `coroutines-core`,
        fragment,
        `lifecycle-common`,
        `lifecycle-runtime`,
        `lifecycle-viewModel`
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `android-arch-testing`,
        `coroutines-test`,
        junit,
        `kotlin-test`,
        `kotlin-test-junit`,
        mockk,
        turbine
    )
}
