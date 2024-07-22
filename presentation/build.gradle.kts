/*
 * Copyright (c) 2023 Proton AG
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
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

protonBuild {
    apiModeDisabled()
}

protonCoverage {
    branchCoveragePercentage.set(34)
    lineCoveragePercentage.set(32)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.presentation"
}

dependencies {
    api(
        project(Module.networkDomain),
        activity,
        appcompat,
        `constraint-layout`,
        coordinatorlayout,
        `coroutines-core`,
        fragment,
        `javax-inject`,
        `lifecycle-common`,
        `lifecycle-savedState`,
        `lifecycle-viewModel`,
        material,
        okhttp,
        recyclerview,
    )

    implementation(
        project(Module.kotlinUtil),
        project(Module.networkData),
        `android-ktx`,
        `core-splashscreen`,
        drawerLayout,
        `hilt-android`,
        `lifecycle-livedata-core`,
        `lifecycle-runtime`,
        `lifecycle-process`,
    )

    // Test
    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        robolectric,
        turbine
    )

    // Lint - off temporary
    // lintChecks(project(Module.lint))
}
