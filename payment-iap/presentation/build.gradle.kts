/*
 * Copyright (c) 2022 Proton Technologies AG
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

publishOption.shouldBePublishedAsLib = true

dependencies {
    api(
        project(Module.countryDomain),
        project(Module.domain),
        project(Module.humanVerificationDomain),
        project(Module.networkDomain),
        project(Module.presentation),
        project(Module.paymentPresentation),
        `activity-noktx`,
        `constraint-layout`,
        coordinatorlayout,
        `coroutines-core`,
        `hilt-android`,
        material,
        recyclerview
    )

    implementation(
        // Core
        project(Module.kotlinUtil),

        // Android
        activity,
        `android-ktx`,
        appcompat,
        fragment,
        `lifecycle-common`,
        `lifecycle-runtime`,
        `lifecycle-viewModel`,

        // Other
        `googlePlayBilling`
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
