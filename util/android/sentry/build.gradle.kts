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
}

publishOption.shouldBePublishedAsLib = true

protonCoverage {
    branchCoveragePercentage.set(45)
    lineCoveragePercentage.set(64)
}

android {
    namespace = "me.proton.core.util.android.sentry"

    defaultConfig {
        buildConfigField(
            "String",
            "CORE_VERSION",
            computeVersionNameFromBranchName(CORE_RELEASE_BRANCH_PREFIX).toBuildConfigValue()
        )
    }
}

dependencies {
    implementation(
        project(Module.kotlinUtil),
        project(Module.networkData),
        project(Module.networkDomain),
        project(Module.eventManagerDomain),
        project(Module.accountManagerDomain),
        project(Module.deviceUtil),
        `hilt-android`,
    )

    api(
        `javax-inject`,
        sentry,
        `sentry-android-core`,
        timber
    )

    testImplementation(
        `kotlin-test`,
        mockk
    )
}
