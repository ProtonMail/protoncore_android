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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class ProtonKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) = Unit
}

/**
 * Setup Kotlin for whole Project.
 * It will setup Kotlin compile options to sub-projects
 *
 * @param extraCompilerArgs
 *
 *
 * @author Davide Farella
 */
fun Project.kotlinCompilerArgs(vararg extraCompilerArgs: String) {

    // Configure sub-projects
    for (sub in subprojects) {

        // Options for Kotlin
        sub.tasks.withType<KotlinCompile> {
            // Ignore IDE errors
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + extraCompilerArgs
            }
        }
    }
}
