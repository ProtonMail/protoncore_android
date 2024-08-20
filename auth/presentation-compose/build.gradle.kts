/*
 * Copyright (c) 2024 Proton Technologies AG
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
import studio.forface.easygradle.dsl.android.`lifecycle-viewModel`

plugins {
    protonComposeUiLibrary
    protonDagger
}

publishOption.shouldBePublishedAsLib = true

protonCoverage {
    branchCoveragePercentage.set(68)
    lineCoveragePercentage.set(95)
}

android {
    namespace = "me.proton.core.auth.presentation.compose"
}

dependencies {
    api(
        project(Module.presentation),
        `compose-runtime`,
        `coroutines-core`,
        `lifecycle-viewModel`,
    )

    implementation(
        project(Module.authDomain),
        project(Module.accountManagerDomain),
        project(Module.userDomain),
        project(Module.telemetryDomain),
        project(Module.presentationCompose),
        `androidx-navigation-common`,
        `compose-foundation-layout`,
        `compose-foundation`,
        `compose-material`,
        `compose-material3`,
        `compose-ui-graphics`,
        `compose-ui-text`,
        `compose-ui-tooling-preview`,
        `compose-ui`,
        `compose-ui-unit`,
        `hilt-navigation-compose`,
        `lifecycle-viewModel-compose`,
    )

    debugImplementation(
        `compose-ui-tooling`,
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        `kotlin-test`,
        mockk,
        turbine,
    )
}
