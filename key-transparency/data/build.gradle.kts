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
    minBranchCoveragePercentage.set(5)
    minLineCoveragePercentage.set(5)
}

protonDagger {
    workManagerHiltIntegration = true
}

protonBuild {
    // apiModeDisabled()
}

android {
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

publishOption.shouldBePublishedAsLib = true

dependencies {
    implementation(
        project(Module.kotlinUtil),
        project(Module.userData),
        project(Module.accountManagerPresentation),
        project(Module.accountDomain),
        project(Module.keyData),
        project(Module.networkDomain),

        // Other
        `android-work-runtime`,
        retrofit,
        `lifecycle-common`,
        `serialization-core`,
        `room-ktx`,

    )

    coreLibraryDesugaring(`desugar-jdk-libs`)

    api(
        project(Module.cryptoCommon),
        project(Module.networkData),
        project(Module.accountManagerDomain),
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.keyTransparencyDomain),
        project(Module.keyDomain),
        project(Module.presentation),
        project(Module.userDomain),
    )

    compileOnly(project(Module.gopenpgp))

    androidTestImplementation(project(Module.androidTest)) {
        exclude(mockk)
    }

    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        project(Module.gopenpgp),
        project(Module.kotlinTest),
        junit,
        `kotlin-test`,
        `mockk-android`,
        project(Module.cryptoAndroid),
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk
    )
}

