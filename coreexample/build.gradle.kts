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
    `android-application`
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android(Version(0, 1))

dependencies {
    implementation(
        // Auth - off for now
//        project(Module.auth),

        // Contacts - off for now
//        project(Module.contacts),

        // Settings - off for now
//        project(Module.settings),

        // Human Verification
        project(Module.humanVerification),

        // Presentation
        project(Module.presentation),
        project(Module.network),

        `kotlin-jdk7`,
        `kotlin-reflect`,
        `coroutines-android`,

        // Android
        `activity`,
        `appcompat`,
        `fragment`,
        `lifecycle-viewModel`,
        `constraint-layout`,
        `material`,
        `viewStateStore`,
        `android-work-runtime`,
        `hilt-android`,
        `hilt-androidx-annotations`,
        `hilt-androidx-viewModel`,

        // Other
        `timber`
    )

    // Android
    compileOnly(`android-annotation`)

    kapt(
        `assistedInject-processor-dagger`,
        `hilt-android-compiler`,
        `hilt-androidx-compiler`
    )

    // Other
    compileOnly(`assistedInject-annotations-dagger`)

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))

    // Lint - off temporary
//    lintChecks(project(Module.lint))
}
