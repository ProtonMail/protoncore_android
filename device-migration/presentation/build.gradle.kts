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
        project(Module.accountDomain),
        project(Module.accountManagerDomain),
        project(Module.authDomain),
        project(Module.biometricData),
        project(Module.cryptoCommon),
        project(Module.deviceMigrationDomain),
        project(Module.domain),
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        project(Module.observabilityDomain),
        project(Module.presentation),
        project(Module.presentationCompose),
        project(Module.userDomain),
        activity,
        `coroutines-core`,
        fragment,
        `lifecycle-savedState`,
        `zxing-embedded`,
    )

    debugImplementation(
        `compose-ui-tooling`,
    )

    implementation(
        project(Module.biometricDomain),
        project(Module.biometricPresentation),
        `accompanist-permissions`,
        `activity-compose`,
        `androidx-core`,
        `androidx-core-ktx`,
        `androidx-navigation-common`,
        `android-annotation`,
        `compose-animation`,
        `compose-foundation`,
        `compose-foundation-layout`,
        `compose-material`,
        `compose-runtime`,
        `compose-ui`,
        `compose-ui-graphics`,
        `compose-ui-text`,
        `compose-ui-tooling-preview`,
        `compose-ui-unit`,
        `hilt-navigation-compose`,
        `lifecycle-common`,
        `lifecycle-runtime-compose`,
        `lifecycle-viewModel`,
        `lifecycle-viewModel-compose`,
        `navigation-compose`,
        `zxing-core`,
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        turbine,
    )
}
