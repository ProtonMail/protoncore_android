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
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

publishOption.shouldBePublishedAsLib = true

android()

dependencies {
    implementation(
        project(Module.kotlinUtil),
        project(Module.network),
        project(Module.data),
        project(Module.dataRoom),
        project(Module.domain),
        project(Module.labelDomain),
        project(Module.userData),
        project(Module.eventManagerDomain),

        // Kotlin
        `serialization-json`,
        `coroutines-core`,

        // Other
        `android-work-runtime`,
        `hilt-android`,
        `hilt-androidx-workManager`,
        `okHttp-logging`,
        retrofit,
        `retrofit-kotlin-serialization`,
        `room-ktx`,
        store4,
    )

    kapt(
        `hilt-android-compiler`,
        `hilt-androidx-compiler`
    )

    kaptTest(`room-compiler`)

    testImplementation(
        project(Module.androidTest),
        project(Module.cryptoCommon),
        project(Module.cryptoAndroid),
        project(Module.accountData),
        project(Module.accountDomain),
        project(Module.userDomain),
        project(Module.keyDomain)
    )
}
