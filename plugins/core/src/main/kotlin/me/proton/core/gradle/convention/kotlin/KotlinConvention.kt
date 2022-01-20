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

package me.proton.core.gradle.convention.kotlin

import me.proton.core.gradle.JvmDefaults
import me.proton.core.gradle.convention.BuildConvention
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class KotlinConvention : BuildConvention<KotlinConventionSettings> {
    private val defaultCompilerArgs: List<String>
        get() = listOf(
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xopt-in=kotlin.time.ExperimentalTime"
        )

    override fun apply(target: Project, settings: KotlinConventionSettings) {
        target.tasks.withType<KotlinCompile> {
            applyConvention()
        }

        target.afterEvaluate {
            applyApiMode(target, settings)
        }
    }

    private fun KotlinCompile.applyConvention() {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + defaultCompilerArgs
            jvmTarget = JvmDefaults.jvmTarget.toString()
        }
    }

    private fun applyApiMode(target: Project, settings: KotlinConventionSettings) {
        if (settings.apiMode != ExplicitApiMode.Disabled) {
            target.tasks.withType<KotlinCompile> {
                // Workaround for https://youtrack.jetbrains.com/issue/KT-37652
                if (name.endsWith("TestKotlin")) return@withType

                kotlinOptions {
                    freeCompilerArgs = freeCompilerArgs + settings.apiMode.toCompilerArg()
                }
            }
        }
    }
}
