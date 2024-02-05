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

protonCoverage {
    branchCoveragePercentage.set(52)
    lineCoveragePercentage.set(83)
}

protonDagger {
    workManagerHiltIntegration = true
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.featureflag.data"
}

dependencies {

    api(
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.featureFlagDomain),
        project(Module.accountManagerDomain),
        project(Module.observabilityDomain),
        project(Module.networkData),
    )

    implementation(
        project(Module.data),
        project(Module.kotlinUtil),
        project(Module.networkDomain),

        `android-work-runtime`,
        `coroutines-core`,
        retrofit,
        `room-ktx`,
        `serialization-core`,
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
