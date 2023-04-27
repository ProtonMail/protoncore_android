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

package me.proton.core.gradle.plugins.coverage

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * The plugin should be applied on a root project.
 * It can be used to provide a common configuration for code coverage.
 * The configuration will be picked up by the submodules which use
 * the [ProtonCoveragePlugin].
 */
public class ProtonCoverageCommonConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) error("${this::class.simpleName} should be applied on the root project.")
        val ext = target.extensions.create<ProtonCoverageExtension>("protonCoverage")

        target.afterEvaluate {
            ext.finalizeValuesOnRead()
        }
    }
}
