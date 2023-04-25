/*
 * Copyright (c) 2023 Proton AG
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
    protonAndroidUiLibrary
    protonDagger
    id("kotlin-parcelize")
}

proton {
    apiModeDisabled()
}

protonCoverage {
    minBranchCoveragePercentage.set(15)
    minLineCoveragePercentage.set(24)
}

publishOption.shouldBePublishedAsLib = true

dependencies {
    api(
        project(Module.authPresentation),
        project(Module.cryptoCommon),
        project(Module.domain),
        project(Module.presentation),
        project(Module.userSettingsDomain),
        activity,
        `constraint-layout`,
        `coroutines-core`,
        `hilt-android`,
        material
    )

    implementation(
        // Core
        project(Module.challengePresentation),
        project(Module.kotlinUtil),
        project(Module.humanVerificationPresentation),
        project(Module.paymentPresentation),
        project(Module.planPresentation),

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
        project(Module.networkDomain),
        project(Module.userDomain),
        `android-arch-testing`,
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        turbine
    )
}
