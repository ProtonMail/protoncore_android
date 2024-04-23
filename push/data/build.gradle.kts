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
    branchCoveragePercentage.set(56)
    lineCoveragePercentage.set(67)
}

protonDagger {
    workManagerHiltIntegration = true
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.push.data"
}

dependencies {
    api(
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.eventManagerDomain),
        project(Module.networkData),
        project(Module.pushDomain),
        project(Module.kotlinUtil),
        `coroutines-core`,
        `javax-inject`,
    )

    implementation(
        project(Module.userData),
        project(Module.data),
        project(Module.networkDomain),
        retrofit,
        `room-ktx`,
        `serialization-core`,
        store4,
        `android-work-runtime`,
    )

    testImplementation(
        project(Module.accountData),
        project(Module.accountDomain),
        project(Module.cryptoAndroid),
        project(Module.cryptoCommon),
        project(Module.keyDomain),
        project(Module.kotlinTest),
        project(Module.userDomain),
        `android-test-core`,
        `android-work-testing`,
        `coroutines-test`,
        `hilt-android-testing`,
        junit,
        `kotlin-test`,
        mockk,
        robolectric,
        turbine
    )

    kaptTest(`hilt-android-compiler`)
    kaptTest(`room-compiler`)
}
