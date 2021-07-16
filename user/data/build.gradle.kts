/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
}

libVersion = Version(1, 1, 5)

android()

dependencies {
    implementation(
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        project(Module.networkData),
        project(Module.data),
        project(Module.domain),
        project(Module.userDomain),
        project(Module.cryptoCommon),

        // Features
        project(Module.key),
        project(Module.account),

        // Kotlin
        `kotlin-jdk7`,
        `serialization-json`,
        `coroutines-core`,

        // Other
        `okHttp-logging`,
        `retrofit`,
        `retrofit-kotlin-serialization`,
        `room-ktx`,
        `store4`
    )

    testImplementation(project(Module.androidTest))
    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        project(Module.domain),
        project(Module.auth),
        project(Module.account),
        project(Module.accountManager),
        project(Module.cryptoAndroid),
        project(Module.gopenpgp)
    )
}
