/*
 * Copyright (c) 2025 Proton AG
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

import studio.forface.easygradle.dsl.android.*
import studio.forface.easygradle.dsl.*

plugins {
    protonComposeUiLibrary
    protonDagger
    id("kotlin-parcelize")
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.devicemigration.presentation"

    buildFeatures {
        buildConfig = true
        resValues = true
        viewBinding = true
    }
}

dependencies {
    api(
        project(Module.deviceMigrationDomain),
        project(Module.networkPresentation),
        activity,
    )

    debugImplementation(
        `compose-ui-tooling`,
    )

    implementation(
        project(Module.accountManagerDomain),
        project(Module.biometricData),
        project(Module.biometricPresentation),
        project(Module.observabilityDomain),
        project(Module.presentation),
        project(Module.presentationCompose),
        `androidx-core-ktx`,
        `compose-runtime`,
        `compose-ui-tooling-preview`,
        `compose-ui-unit`,
        `hilt-navigation-compose`,
        `lifecycle-savedState`,
        `lifecycle-viewModel`,
        `zxing-core`,
        `zxing-embedded`,
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        `kotlin-test`,
        mockk,
        turbine,
    )
}
