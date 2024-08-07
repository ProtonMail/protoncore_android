/*
 * Copyright (c) 2024 Proton AG
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
    branchCoveragePercentage.set(83)
    lineCoveragePercentage.set(94)
}

publishOption.shouldBePublishedAsLib = true

protonDagger {
    workManagerHiltIntegration = true
}

android {
    namespace = "me.proton.core.userrecovery.data"
}

dependencies {
    api(
        project(Module.domain),
        project(Module.userRecoveryDomain),
        project(Module.eventManagerDomain),
        `android-work-runtime`,
        `coroutines-core`,
        `javax-inject`,
        retrofit,
        `serialization-core`
    )

    implementation(
        project(Module.accountData),
        project(Module.androidUtilDatetime),
        project(Module.data),
        project(Module.kotlinUtil),
        project(Module.userData),
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.kotlinTest),
        `android-work-testing`,
        `coroutines-test`,
        `hilt-android-testing`,
        junit,
        `kotlin-test`,
        mockk,
        robolectric,
        turbine,
        `android-work-testing`
    )

    kaptTest(`hilt-android-compiler`)
}
