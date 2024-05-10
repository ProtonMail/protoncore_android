/*
 * Copyright (c) 2020 Proton Technologies AG
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
    protonComposeUiLibrary
    protonDagger
    id("kotlin-parcelize")
}

publishOption.shouldBePublishedAsLib = true

protonBuild {
    apiModeDisabled()
}

protonCoverage {
    branchCoveragePercentage.set(26)
    lineCoveragePercentage.set(60)
}

android {
    namespace = "me.proton.core.accountmanager.presentation.compose"
}

dependencies {
    api(
        project(Module.accountManagerPresentation),
        `compose-runtime`,
        `coroutines-core`,
        `compose-ui`,
        `lifecycle-viewModel`,
    )

    implementation(
        project(Module.presentationCompose),

        // Compose
        `coroutines-core`,
        `hilt-navigation-compose`,
        `lifecycle-viewModel`,
        `lifecycle-viewModel-compose`,
        `compose-foundation-layout`,
        `compose-foundation`,
        `compose-material`,
        `compose-material3`,
        `compose-ui-graphics`,
        `compose-ui-text`,
        `compose-ui-tooling-preview`,
        `compose-ui`,
        `compose-ui-unit`,
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        `kotlin-test`,
        mockk,
        turbine,
    )
}
