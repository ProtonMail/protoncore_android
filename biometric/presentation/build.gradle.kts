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
import studio.forface.easygradle.dsl.android.`hilt-android`
import studio.forface.easygradle.dsl.android.`lifecycle-viewModel`

plugins {
    protonComposeUiLibrary
    protonDagger
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.biometric.presentation"
}

dependencies {
    api(
        project(Module.biometricDomain),
        project(Module.presentationCompose),
        `compose-runtime`,
        `androidx-biometric`,
        `hilt-android`,
    )

    debugImplementation(
        `compose-ui-tooling`,
    )

    implementation(
        project(Module.biometricData),
        project(Module.kotlinUtil),
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        `kotlin-test`,
        mockk,
        turbine,
    )
}
