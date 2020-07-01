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

plugins {
    `android-application`
    `kotlin-android`
}

val version = Version(0, 1, 0)
archivesBaseName = archiveNameFor(ProtonCore.core_example, version)

android(version)

dependencies {
    implementation(
        // Auth
        project(Module.auth),

        // Contacts
        project(Module.contacts),

        // Settings
        project(Module.settings),

        // Presentation
        project(Module.presentation),

        `kotlin-jdk7`,
        `kotlin-reflect`,
        `coroutines-android`,

        // Android
        activity,
        appcompat,
        fragment,
        `lifecycle-viewModel`,
        `constraint-layout`,
        material,
        viewStateStore,
        `android-work-runtime`,

        // Other
        timber
    )

    // Android
    compileOnly(`android-annotation`)

    // Other

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))

    // Lint
    lintChecks(project(Module.lint))
}

dokka()
// publish(Module.auth, version)
