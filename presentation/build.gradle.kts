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
    minBranchCoveragePercentage.set(26)
    minLineCoveragePercentage.set(27)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.presentation"
}

dependencies {
    api(
        activity,
        appcompat,
        `constraint-layout`,
        `coroutines-core`,
        fragment,
        `javax-inject`,
        `lifecycle-common`,
        `lifecycle-savedState`,
        `lifecycle-viewModel`,
        material,
        recyclerview,
    )

    implementation(
        project(Module.kotlinUtil),
        project(Module.networkData),
        project(Module.networkDomain),
        `android-ktx`,
        `core-splashscreen`,
        `hilt-android`,
        `lifecycle-livedata-core`,
        `lifecycle-process`,
    )

    // Android
    compileOnly(`android-annotation`)

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
