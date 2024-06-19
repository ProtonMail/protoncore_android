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

protonBuild {
    apiModeDisabled()
}

protonCoverage {
    branchCoveragePercentage.set(43)
    lineCoveragePercentage.set(60)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.auth.presentation"
}

dependencies {
    api(
        project(Module.accountDomain),
        project(Module.accountManagerDomain),
        project(Module.authDomain),
        project(Module.authFidoDomain),
        project(Module.challengeDomain),
        project(Module.challengePresentation),
        project(Module.countryDomain),
        project(Module.cryptoCommon),
        project(Module.domain),
        project(Module.featureFlagDomain),
        project(Module.humanVerificationDomain),
        project(Module.humanVerificationPresentation),
        project(Module.networkData),
        project(Module.networkDomain),
        project(Module.observabilityDomain),
        project(Module.paymentDomain),
        project(Module.paymentPresentation),
        project(Module.planDomain),
        project(Module.planPresentation),
        project(Module.presentation),
        project(Module.telemetryDomain),
        project(Module.telemetryPresentation),
        project(Module.userDomain),
        project(Module.userSettingsDomain),
        `activity-noktx`,
        appcompat,
        `constraint-layout`,
        coordinatorlayout,
        `coroutines-core`,
        `hilt-android`,
        `lifecycle-common`,
        `lifecycle-savedState`,
        lottie,
        material,
        okhttp,
        `startup-runtime`
    )

    implementation(
        project(Module.challengeData),
        project(Module.countryPresentation),
        project(Module.deviceUtil),
        project(Module.kotlinUtil),
        project(Module.sharedPreferencesUtil),
        activity,
        `androidx-browser`,
        `android-ktx`,
        fragment,
        `lifecycle-runtime`,
        `lifecycle-viewModel`,
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `android-arch-testing`,
        `android-test-core`,
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        robolectric,
        turbine
    )
}
