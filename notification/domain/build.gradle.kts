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

import studio.forface.easygradle.dsl.api
import studio.forface.easygradle.dsl.`coroutines-test`
import studio.forface.easygradle.dsl.`kotlin-test`
import studio.forface.easygradle.dsl.mockk
import studio.forface.easygradle.dsl.testImplementation

plugins {
    protonKotlinLibrary
    kotlin("plugin.serialization")
}

publishOption.shouldBePublishedAsLib = true

protonCoverage {
    minLineCoveragePercentage.set(88)
}

dependencies {
    api(
        project(Module.domain),
        project(Module.pushDomain)
    )

    testImplementation(
        project(Module.kotlinTest),
        `coroutines-test`,
        `kotlin-test`,
        mockk,
    )
}
