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
    branchCoveragePercentage.set(6)
    lineCoveragePercentage.set(13)
}

protonDagger {
    workManagerHiltIntegration = true
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.label.data"
}

dependencies {
    api(
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.eventManagerDomain),
        project(Module.labelDomain),
        project(Module.networkData),
        retrofit,
        `serialization-core`
    )

    implementation(
        project(Module.kotlinUtil),
        project(Module.data),
        project(Module.networkDomain),
        project(Module.userData),

        // Kotlin
        `coroutines-core`,

        // Other
        `android-work-runtime`,
        store5,
    )

    kaptTest(`room-compiler`)

    testImplementation(
        project(Module.cryptoCommon),
        project(Module.cryptoAndroid),
        project(Module.accountData),
        project(Module.accountDomain),
        project(Module.keyDomain),
        `android-test-core`,
        `androidx-collection`,
        `coroutines-test`,
        junit,
        robolectric,
        `room-ktx`,
    )
}
