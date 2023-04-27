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

package me.proton.core.gradle.convention.kotlin

import me.proton.core.gradle.convention.BuildConvention
import me.proton.core.gradle.plugin.CommonConfigurationExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
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
        val commonConfig = target.rootProject.extensions.getByType<CommonConfigurationExtension>()

        target.tasks.withType<KotlinCompile> {
            applyConvention(commonConfig)
        }

        target.afterEvaluate {
            applyApiMode(target, settings, commonConfig)
        }
    }

    private fun KotlinCompile.applyConvention(commonConfig: CommonConfigurationExtension) {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + defaultCompilerArgs
            jvmTarget = commonConfig.jvmTarget.get().toString()
        }
    }

    private fun applyApiMode(
        target: Project,
        settings: KotlinConventionSettings,
        commonConfig: CommonConfigurationExtension
    ) {
        val apiMode: ExplicitApiMode = when {
            settings.apiMode.isPresent -> settings.apiMode.get()
            commonConfig.apiMode.isPresent -> commonConfig.apiMode.get()
            else -> settings.apiMode.get()
        }
        if (apiMode != ExplicitApiMode.Disabled) {
            target.tasks.withType<KotlinCompile> {
                // Workaround for https://youtrack.jetbrains.com/issue/KT-37652
                if (name.endsWith("TestKotlin")) return@withType

                kotlinOptions {
                    freeCompilerArgs = freeCompilerArgs + apiMode.toCompilerArg()
                }
            }
        }
    }
}
