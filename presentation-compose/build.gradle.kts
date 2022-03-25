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

import studio.forface.easygradle.dsl.android.`android-annotation`
import studio.forface.easygradle.dsl.android.`lifecycle-runtime`
import studio.forface.easygradle.dsl.android.androidTestImplementation
import studio.forface.easygradle.dsl.compileOnly
import studio.forface.easygradle.dsl.implementation
import studio.forface.easygradle.dsl.testImplementation

plugins {
    protonComposeUiLibrary
}

proton {
    apiModeDisabled()
}

publishOption.shouldBePublishedAsLib = true

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.presentation),

        // Android
        `lifecycle-runtime`,

        // Compose
        `activity-compose`,
        `compose-animation`,
        `compose-foundation`,
        `compose-foundation-layout`,
        `compose-material`,
        `compose-runtime`,
        `compose-ui`,
        `compose-ui-tooling`,
        `navigation-compose`
    )

    // Android
    compileOnly(`android-annotation`)

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        `compose-ui-test`,
        `compose-ui-test-junit`
    )
    debugImplementation(
        `compose-ui-test-manifest`,
    )
}
