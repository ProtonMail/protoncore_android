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
    protonAndroidLibrary
    protonDagger
    kotlin("plugin.serialization")
}

protonBuild {
    apiModeDisabled()
}

android {
    namespace = "me.proton.core.user.data"
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

publishOption.shouldBePublishedAsLib = true

dependencies {
    api(
        project(Module.authData),
        project(Module.challengeDomain),
        project(Module.cryptoCommon),
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.eventManagerDomain),
        project(Module.keyData),
        project(Module.keyDomain),
        project(Module.networkData),
        project(Module.userDomain),
        `javax-inject`,
        `serialization-core`,
        retrofit
    )

    coreLibraryDesugaring(`desugar-jdk-libs`)

    implementation(
        project(Module.accountData),
        project(Module.authDomain),
        project(Module.challengeData),
        project(Module.data),
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        
        // Kotlin
        `coroutines-core`,

        // Other
        `room-ktx`,
        store4,
        cache4k
    )

    androidTestImplementation(project(Module.androidTest)) {
        exclude(mockk) // We're including `mock-android` instead.
    }

    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        project(Module.auth),
        project(Module.accountManager),
        project(Module.accountManagerDataDb),
        project(Module.cryptoAndroid),
        project(Module.gopenpgp),
        project(Module.userSettings),
        project(Module.contact),
        project(Module.eventManager),
        project(Module.kotlinTest),
        `kotlin-test`,
        `mockk-android`,
        turbine
    )
}
