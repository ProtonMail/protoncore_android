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
    minBranchCoveragePercentage.set(71)
}

publishOption.shouldBePublishedAsLib = true

protonDagger {
    workManagerHiltIntegration = true
}

dependencies {
    api(
        project(Module.networkData),
        project(Module.reportDomain),
        androidxWork("runtime"),
        `coroutines-core`,
        `javax-inject`,
    )

    implementation(
        project(Module.domain),
        project(Module.kotlinUtil),
        project(Module.networkDomain),

        `android-work-runtime`,
        guavaListenableFuture,
        `lifecycle-livedata-core`,
        `lifecycle-liveData`,
        serialization("core"),
        retrofit,
    )

    testImplementation(
        project(Module.kotlinTest),
        `android-arch-testing`,
        `android-work-testing`,
        `android-test-core`,
        `coroutines-test`,
        `hilt-android`,
        `hilt-android-testing`,
        junit,
        `kotlin-test`,
        mockk,
        robolectric,
        turbine
    )

    kaptTest(`hilt-android-compiler`)
}

dependencyAnalysis {
    issues {
        onUsedTransitiveDependencies {
            exclude("com.google.guava:guava")
        }
    }
}