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
    kotlin("kapt")
}

protonBuild {
    apiModeDisabled()
}

publishOption.shouldBePublishedAsLib = true

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
            }
        }
    }
}

dependencies {
    api(
        project(Module.accountData),
        project(Module.accountDomain),
        project(Module.challengeData),
        project(Module.contactData),
        project(Module.contactDomain),
        project(Module.cryptoCommon),
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.eventManagerData),
        project(Module.eventManagerDomain),
        project(Module.featureFlagData),
        project(Module.humanVerificationData),
        project(Module.keyData),
        project(Module.labelData),
        project(Module.mailSettingsData),
        project(Module.networkDomain),
        project(Module.paymentData),
        project(Module.pushData),
        project(Module.pushDomain),
        project(Module.userData),
        project(Module.userDomain),
        project(Module.userSettingsData),
        project(Module.observabilityData),
        project(Module.keyTransparencyData),
    )

    implementation(
        project(Module.cryptoAndroid),
        project(Module.keyDomain),
        project(Module.labelDomain),
        `androidx-collection`,
        `coroutines-core`,
        `room-ktx`
    )

    kapt(
        `room-compiler`
    )
}
