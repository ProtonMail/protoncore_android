import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

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

plugins {
    protonAndroidLibrary
    protonDagger
    kotlin("plugin.serialization")
}

protonDagger {
    workManagerHiltIntegration = true
}

publishOption.shouldBePublishedAsLib = true

dependencies {
    api(
        project(Module.dataRoom),
        project(Module.eventManagerDomain),
        project(Module.pushDomain),
        `coroutines-core`
    )

    implementation(
        project(Module.domain),
        project(Module.network),
        project(Module.userData),
        project(Module.data),
        project(Module.kotlinUtil),
        `javax-inject`,
        retrofit,
        `serialization-json`,
        `room-ktx`,
        store4,
        `android-work-runtime`,
    )

    testImplementation(
        project(Module.androidTest),
        project(Module.accountData),
        project(Module.accountDomain),
        project(Module.cryptoAndroid),
        project(Module.cryptoCommon),
        project(Module.keyDomain),
        project(Module.userData),
        project(Module.userDomain),
        `android-work-testing`,
        `coroutines-test`,
        `hilt-android-testing`,
        `kotlin-test`,
        mockk
    )

    kaptTest(`hilt-android-compiler`)
    kaptTest(`room-compiler`)
}
