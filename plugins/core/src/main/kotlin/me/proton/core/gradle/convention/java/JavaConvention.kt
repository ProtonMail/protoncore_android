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

package me.proton.core.gradle.convention.java

import me.proton.core.gradle.convention.BuildConvention
import me.proton.core.gradle.plugin.CommonConfigurationExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

internal class JavaConvention : BuildConvention<Unit> {
    override fun apply(target: Project, settings: Unit) {
        val commonConfig = target.rootProject.extensions.getByType<CommonConfigurationExtension>()
        target.extensions.configure<JavaPluginExtension> {
            sourceCompatibility = commonConfig.jvmTarget.get()
            targetCompatibility = commonConfig.jvmTarget.get()
        }
    }
}
