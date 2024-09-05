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
    kotlin("plugin.serialization")
}

protonBuild {
    apiModeDisabled()
}

protonCoverage {
    branchCoveragePercentage.set(43)
    lineCoveragePercentage.set(38)
}

protonDagger {
    workManagerHiltIntegration = true
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.auth.data"
}

dependencies {
    api(
        project(Module.authDomain),
        project(Module.authFidoDagger),
        project(Module.challengeData),
        project(Module.challengeDomain),
        project(Module.cryptoCommon),
        project(Module.domain),
        project(Module.networkData),
        project(Module.networkDomain),
        project(Module.featureFlagDomain),
        `android-work-runtime`,
        `serialization-core`,
        `serialization-json`,
        `hilt-android`,
        `javax-inject`,
        retrofit,
    )

    implementation(
        project(Module.accountData),
        project(Module.featureFlagData),
        project(Module.kotlinUtil),
        project(Module.data),
        `coroutines-core`,
        store4
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        turbine
    )
}
