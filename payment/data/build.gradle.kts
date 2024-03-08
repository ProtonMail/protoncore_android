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

protonCoverage {
    branchCoveragePercentage.set(72)
    lineCoveragePercentage.set(76)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.payment.data"
}

dependencies {
    api(
        project(Module.domain),
        project(Module.networkData),
        project(Module.paymentDomain),
        project(Module.planData),
        `javax-inject`,
        `serialization-core`
    )

    implementation(
        project(Module.dataRoom),
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        project(Module.observabilityDomain),
        project(Module.userDomain),
        project(Module.planDomain),
        retrofit,
        `room-ktx`,
        serialization("core"),
        `serialization-json`,
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        `serialization-json`
    )

    androidTestImplementation(
        `kotlin-test-junit`
    )
}
