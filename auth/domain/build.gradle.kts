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


plugins {
    protonKotlinLibrary
}

proton {
    apiModeDisabled()
}

publishOption.shouldBePublishedAsLib = true

dependencies {

    api(`android-annotation`)

    implementation(

        project(Module.kotlinUtil),
        project(Module.domain),
        project(Module.networkDomain),
        project(Module.cryptoCommon),

        // Features
        project(Module.accountDomain),
        project(Module.accountManagerDomain),
        project(Module.userDomain),
        project(Module.keyDomain),
        project(Module.paymentDomain),
        project(Module.challengeDomain),

        // Kotlin
        `coroutines-core`,

        // Other
        `javax-inject`
    )

    testImplementation(project(Module.kotlinTest))
}
