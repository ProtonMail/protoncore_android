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
    protonAndroidLibrary
    protonDagger
    id("kotlin-parcelize")
}

protonCoverage {
    branchCoveragePercentage.set(100)
    lineCoveragePercentage.set(100)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.network.presentation"
}

dependencies {
    api(
        project(Module.kotlinUtil),
        appcompat,
        `hilt-android`,
        `startup-runtime`
    )

    implementation(
        project(Module.accountDomain),
        project(Module.domain),
        project(Module.networkData),
        project(Module.presentation),

        activity,
        `coroutines-core`,
        `hilt-androidx-annotations`,
        `lifecycle-common`,
        `lifecycle-runtime`,
        `lifecycle-viewModel`,
        material,
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.networkDomain),
        project(Module.kotlinTest),
        `android-arch-testing`,
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        turbine
    )
}
