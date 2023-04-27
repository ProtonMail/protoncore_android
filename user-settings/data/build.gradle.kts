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
    kotlin("plugin.serialization")
}

protonBuild {
    apiModeDisabled()
}

protonCoverage {
    minBranchCoveragePercentage.set(33)
    minLineCoveragePercentage.set(74)
}

publishOption.shouldBePublishedAsLib = true

dependencies {
    api(
        project(Module.cryptoCommon),
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.eventManagerDomain),
        project(Module.keyData),
        project(Module.networkData),
        project(Module.userSettingsDomain),
        `coroutines-core`,
        `javax-inject`,
        retrofit,
        `serialization-core`
    )

    implementation(
        project(Module.authDomain),
        project(Module.data),
        project(Module.networkDomain),
        project(Module.kotlinUtil),
        project(Module.userData),

        // Other
        `datastorePreferences`,
        `room-ktx`,
        store4
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk
    )
}
