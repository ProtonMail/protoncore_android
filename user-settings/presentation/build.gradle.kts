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
    protonComposeUiLibrary
    protonDagger
    id("kotlin-parcelize")
}

protonBuild {
    apiModeDisabled()
}

protonCoverage {
    branchCoveragePercentage.set(39)
    lineCoveragePercentage.set(56)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.usersettings.presentation"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api(
        project(Module.authPresentation),
        project(Module.authFidoDomain),
        project(Module.cryptoCommon),
        project(Module.domain),
        project(Module.presentation),
        project(Module.presentationCompose),
        project(Module.userSettingsData),
        project(Module.userSettingsDomain),
        project(Module.accountManagerPresentation),
        project(Module.accountRecoveryDomain),
        project(Module.accountRecoveryPresentationCompose),
        activity,
        `compose-ui`,
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
