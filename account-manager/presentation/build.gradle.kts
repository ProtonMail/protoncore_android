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
    protonAndroidUiLibrary
    protonDagger
}

protonBuild {
    apiModeDisabled()
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.accountmanager.presentation"
}

dependencies {
    api(
        project(Module.accountDomain),
        project(Module.accountManagerDomain),
        project(Module.authPresentation),
        project(Module.domain),
        project(Module.userDomain),
        appcompat,
        `constraint-layout`,
        `coroutines-core`,
        `lifecycle-common`,
        recyclerview
    )

    implementation(
        project(Module.challengePresentation),
        project(Module.humanVerificationPresentation),
        project(Module.kotlinUtil),
        project(Module.paymentPresentation),
        project(Module.planPresentation),
        project(Module.presentation),

        `android-ktx`,
        `hilt-androidx-annotations`,
        `lifecycle-viewModel`,
        `lifecycle-runtime`,
        material
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `android-arch-testing`,
        `coroutines-test`,
        `kotlin-test`,
        `kotlin-test-junit`,
        junit,
        mockk,
        turbine
    )
}
