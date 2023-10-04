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
    minBranchCoveragePercentage.set(58)
    minLineCoveragePercentage.set(90)
}

android {
    namespace = "me.proton.core.key.data"
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

publishOption.shouldBePublishedAsLib = true

dependencies {
    api(
        project(Module.authData),
        project(Module.cryptoCommon),
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.keyDomain),
        project(Module.networkData),
        project(Module.networkDomain),
        `javax-inject`,
        retrofit,
        `serialization-core`,
    )

    coreLibraryDesugaring(`desugar-jdk-libs`)

    implementation(
        project(Module.kotlinUtil),
        project(Module.data),
        project(Module.authDomain),
        `coroutines-core`,
        `room-ktx`,
        store4
    )

    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        project(Module.cryptoAndroid),
        project(Module.gopenpgp),
        `kotlin-test`
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
