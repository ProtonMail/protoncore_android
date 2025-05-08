/*
 * Copyright (c) 2025 Proton AG
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
import studio.forface.easygradle.dsl.`coroutines-test`
import studio.forface.easygradle.dsl.implementation
import studio.forface.easygradle.dsl.`kotlin-test`
import studio.forface.easygradle.dsl.mockk
import studio.forface.easygradle.dsl.testImplementation

plugins {
    protonAndroidLibrary
    protonDagger
    kotlin("plugin.serialization")
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.passvalidator.data"
}

dependencies {
    api(
        project(Module.authDomain),
        project(Module.domain),
        project(Module.featureFlagDomain),
        project(Module.networkData),
        project(Module.passValidatorDomain),
        project(Module.presentation),
        `coroutines-core`,
    )

    implementation(
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        cache4k,
        `serialization-core`,
        retrofit,
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        junit,
        `kotlin-test`,
        mockk,
        turbine,
    )
}
