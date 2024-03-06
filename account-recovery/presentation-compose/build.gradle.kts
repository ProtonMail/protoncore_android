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

protonCoverage {
    branchCoveragePercentage.set(52)
    lineCoveragePercentage.set(77)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.accountrecovery.presentation.compose"
}

protonBuild {
    apiModeDisabled()
}

dependencies {
    api(
        project(Module.accountRecoveryDomain),
        project(Module.accountRecoveryPresentation),
        project(Module.accountManagerDomain),
        project(Module.userSettingsPresentation),
        project(Module.presentation),
        project(Module.presentationCompose),
        project(Module.notification),
        project(Module.kotlinUtil),
        `compose-foundation`,
        `compose-foundation-layout`,
        `compose-material`,
        `compose-runtime`,
        `compose-ui`,
        `compose-ui-graphics`,
        `compose-ui-text`,
        `coroutines-core`,
        `hilt-android`,
        `hilt-navigation-compose`,
        `lifecycle-common`,
        `lifecycle-viewModel`,
        `lifecycle-viewModel-compose`,
    )

    implementation(
        project(Module.observabilityDomain),
        project(Module.androidUtilDagger),
        `coroutines-core`,
        `android-ktx`,
        `appcompat`,
        `compose-material3`,
        `compose-ui-tooling-preview`,
        `lifecycle-runtime`,
    )

    debugImplementation(
        `compose-ui-tooling`,
    )

    androidTestImplementation(
        `android-test-runner`,
        `compose-ui-test`,
        `compose-ui-test-junit`,
        `compose-ui-test-manifest`,
        junit,
        `mockk-android`,
        `hilt-android-testing`,
        `junit-ktx`,
        `kotlin-test`
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        project(Module.notificationTest),
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        turbine
    )
}