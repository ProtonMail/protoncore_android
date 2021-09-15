/*
 * Copyright (c) 2020 Proton Technologies AG
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

libVersion = Version(1, 15, 1)

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
}

android(
    minSdk = ProtonCore.minSdk
) {
    defaultConfig {
        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }
}

dependencies {
    // Test dependencies
    api(
        project(Module.androidTest).exclude(robolectric, mockk), `mockk-android`,
        project(Module.humanVerification),
        project(Module.auth),
        project(Module.presentation),
        project(Module.payment),
        project(Module.accountManagerPresentation),
        project(Module.kotlinUtil),
        project(Module.account),
        project(Module.domain),
        project(Module.data),
        project(Module.key),
        project(Module.user),
        project(Module.country),
        project(Module.plan),
        project(Module.mailSettings),
        project(Module.userSettings),

        // Android
        espresso,
        falcon,
        uiautomator,
        jsonsimple,
        `android-work-testing`,
        `android-test-runner`,
        `android-test-rules`,
        `espresso-contrib`,
        `espresso-intents`,
        `android-ktx`,
        `junit-ktx`,
        `serialization-json`,
        `okhttp-url-connection`,
        `room-testing`,
    )
}
