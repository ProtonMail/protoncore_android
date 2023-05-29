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

publishOption.shouldBePublishedAsLib = true

plugins {
    protonAndroidLibrary
    kotlin("plugin.serialization")
}

protonBuild {
    apiModeDisabled()
}

protonCoverage.disabled.set(true)

android {
    namespace = "me.proton.core.test.android.instrumented"
}

dependencies {
    api(
        project(Module.humanVerificationPresentation),
        project(Module.quark),
        espresso,
        recyclerview,
        `espresso-intents`,
        hamcrest,
        junit,
        `junit-ktx`,
        `serialization-core`,
        `room-testing`,
        `android-test-core-ktx`,
        `compose-ui`,
        `compose-ui-test`,
        `compose-ui-test-junit`
    )

    implementation(
        project(Module.accountManagerPresentation),
        project(Module.auth),
        project(Module.authPresentation),
        project(Module.cryptoValidatorPresentation),
        project(Module.humanVerificationDomain),
        project(Module.kotlinUtil),
        project(Module.paymentPresentation),
        project(Module.planPresentation),
        project(Module.presentation),
        project(Module.userSettings),
        `android-ktx`,
        `androidx-test-monitor`,
        appcompat,
        `compose-ui-text`,
        `coroutines-core`,
        `espresso-contrib`,
        `espresso-idling-resource`,
        `espresso-web`,
        `hilt-android-testing`,
        material,
        okhttp,
        `serialization-json`,
        uiautomator,
    )
}
