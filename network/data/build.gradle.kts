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
    branchCoveragePercentage.set(54)
    lineCoveragePercentage.set(80)
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.network.data"
}

dependencies {
    api(
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        project(Module.challengeData),
        project(Module.challengeDomain),
        project(Module.sharedPreferencesUtil),
        `coroutines-core`,
        `javax-inject`,
        miniDns,
        okhttp,
        retrofit,
    )

    implementation(
        project(Module.domain),
        datastore,
        datastoreCoreAndroid,
        datastoreCoreOkio,
        `matthewnelson-encoding-base32`,
        `okHttp-logging`,
        `retrofit-kotlin-serialization`,
    )

    testImplementation(
        project(Module.cryptoCommon),
        project(Module.kotlinTest),
        project(Module.networkDagger),
        `android-test-core`,
        `coroutines-test`,
        `hilt-android-testing`,
        junit,
        `kotlin-test`,
        mockk,
        mockWebServer,
        `retrofit-scalars-converter`,
        robolectric,
        `kotlin-test-junit`
    )

    kaptTest(`hilt-android-compiler`)
}
