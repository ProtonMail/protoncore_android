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
    apiMode = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Disabled
}

publishOption.shouldBePublishedAsLib = true

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.domain),
        project(Module.networkDomain),
        project(Module.presentation),
        project(Module.crypto),

        // Features
        project(Module.authDomain),
        project(Module.accountDomain),
        project(Module.accountManagerDomain),
        project(Module.humanVerificationDomain),
        project(Module.userSettingsDomain),
        project(Module.humanVerificationPresentation),
        project(Module.userDomain),
        project(Module.keyDomain),
        project(Module.countryPresentation),
        project(Module.countryDomain),
        project(Module.planPresentation),
        project(Module.paymentDomain),
        project(Module.paymentPresentation),

        // Kotlin
        `coroutines-android`,

        // Android
        `android-ktx`,
        `appcompat`,
        `constraint-layout`,
        `fragment`,
        `lifecycle-viewModel`,
        `material`,

        // Other
        `lottie`
    )

    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))
}
