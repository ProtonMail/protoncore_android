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
    branchCoveragePercentage.set(88)
    lineCoveragePercentage.set(100)
}

publishOption.shouldBePublishedAsLib = true

protonDagger {
    workManagerHiltIntegration = true
}

android {
    namespace = "me.proton.core.observability.data"
}

dependencies {
    api(
        `android-work-runtime`,
        `javax-inject`,
        `hilt-android`,
        project(Module.dataRoom),
        project(Module.networkData),
        project(Module.observabilityDomain),
        project(Module.userSettingsDomain),
    )

    implementation(
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        `coroutines-core`,
        retrofit,
        `room-ktx`,
        `serialization-core`,
        `serialization-json`
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `android-test-core-ktx`,
        `android-work-testing`,
        `coroutines-test`,
        `hilt-android-testing`,
        junit,
        `kotlin-reflect`,
        `kotlin-test`,
        mockk,
        robolectric
    )

    kaptTest(`hilt-android-compiler`)
}
