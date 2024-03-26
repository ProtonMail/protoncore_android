/*
 * Copyright (c) 2024 Proton AG
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

import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

plugins {
    protonComposeUiLibrary
    protonDagger
    id("kotlin-parcelize")
}

protonBuild {
    apiModeDisabled()
}

protonCoverage {
    //branchCoveragePercentage.set(26)
    //lineCoveragePercentage.set(55)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.userrecovery.presentation"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api(
        project(Module.domain),
        project(Module.presentation),
        project(Module.userRecoveryData),
        project(Module.userRecoveryDomain),
        activity,
        `compose-ui`,
        `constraint-layout`,
        `coroutines-core`,
        `hilt-android`,
        material
    )

    implementation(
        // Core
        project(Module.kotlinUtil),

        // Android
        `android-ktx`,
        appcompat,
        fragment,
        `lifecycle-common`,
        `lifecycle-runtime`,
        `lifecycle-viewModel`,
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `android-arch-testing`,
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        turbine
    )
}
