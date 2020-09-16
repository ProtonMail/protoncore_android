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

import me.proton.core.util.gradle.*
import setup.setupPublishing
import setup.setupTests

/**
 * Registered tasks:
 * * `allTest`
 * * `detekt`
 * * `dokka`
 * * `multiModuleDetekt` [setupDetekt]
 * * `publishAll` [setupPublishing]
 */

buildscript {
    initVersions()

    repositories(repos)
    dependencies(classpathDependencies)
}

allprojects {
    repositories(repos)
}

setupKotlin(
    "-XXLanguage:+NewInference",
    "-Xuse-experimental=kotlin.Experimental",
    // Enables inline classes
    "-XXLanguage:+InlineClasses",
    // Enables experimental Coroutines from coroutines-test artifact, like `runBlockingTest`
    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
)
setupTests()
setupDetekt()
setupDokka()
setupPublishing()

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
