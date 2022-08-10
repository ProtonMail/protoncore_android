/*
 * Copyright (c) 2021 Proton Technologies AG
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
    id("kotlin-parcelize")
}

proton {
    apiModeDisabled()
}

publishOption.shouldBePublishedAsLib = true

dependencies {
    api(
        project(Module.accountDomain),
        project(Module.accountManagerDomain),
        project(Module.authDomain),
        project(Module.challengeDomain),
        project(Module.challengePresentation),
        project(Module.countryDomain),
        project(Module.cryptoCommon),
        project(Module.domain),
        project(Module.humanVerificationDomain),
        project(Module.humanVerificationPresentation),
        project(Module.networkDomain),
//        project(Module.paymentDomain),
        project(Module.paymentCommonDomain),
        project(Module.paymentPresentation),
        project(Module.planPresentation),
        project(Module.presentation),
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
        material
    )

    implementation(
        project(Module.countryPresentation),
        project(Module.kotlinUtil),
        activity,
        `android-ktx`,
        fragment,
        `lifecycle-runtime`,
        `lifecycle-viewModel`,
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
