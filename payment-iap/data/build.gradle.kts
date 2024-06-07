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

plugins {
    protonAndroidLibrary
    protonDagger
}

protonCoverage {
    branchCoveragePercentage.set(63)
    lineCoveragePercentage.set(63)
}

protonDagger {
    workManagerHiltIntegration = true
}

publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.paymentiap.data"
}

dependencies {
    api(
        project(Module.paymentDomain),
        project(Module.paymentIapDomain)
    )
    implementation(
        project(Module.kotlinUtil),
        `android-work-runtime`,
        `coroutines-core`,
        dagger,
        googlePlayBilling,
        fragment,
        `javax-inject`,
        `lifecycle-runtime`
    )
    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        `kotlin-test`,
        `kotlin-test-junit`,
        mockk,
        turbine
    )
}