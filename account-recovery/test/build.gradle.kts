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

import studio.forface.easygradle.dsl.android.retrofit
import studio.forface.easygradle.dsl.api
import studio.forface.easygradle.dsl.`coroutines-core`
import studio.forface.easygradle.dsl.implementation
import studio.forface.easygradle.dsl.`kotlin-test`
import studio.forface.easygradle.dsl.`kotlin-test-junit`

plugins {
    protonAndroidLibrary
}

protonCoverage.disabled.set(true)

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.accountrecovery.test"
}

dependencies {
    api(
        project(Module.accountManagerData),
        project(Module.accountRecoveryDomain),
        project(Module.authTest),
        project(Module.eventManagerDomain),
        project(Module.networkData),
        project(Module.notificationDomain),
        project(Module.quark),
        junit
    )

    implementation(
        project(Module.accountRecoveryPresentationCompose),
        project(Module.sentryUtil),
        `androidx-test-monitor`,
        `compose-ui-test-junit`,
        `coroutines-core`,
        fusion,
        `kotlin-test`,
        `kotlin-test-junit`,
        retrofit,
        uiautomator
    )
}
