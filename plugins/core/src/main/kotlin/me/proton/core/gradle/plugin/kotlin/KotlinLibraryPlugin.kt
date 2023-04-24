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

package me.proton.core.gradle.plugin.kotlin

import me.proton.core.gradle.plugin.BuildConventionPlugin
import me.proton.core.gradle.plugin.KotlinLibraryExtension
import me.proton.core.gradle.plugin.PluginIds
import me.proton.core.gradle.plugin.applyJavaConvention
import me.proton.core.gradle.plugin.applyKotlinConvention
import me.proton.core.gradle.plugin.createProtonExt
import me.proton.core.gradle.plugins.coverage.ProtonCoveragePlugin
import org.gradle.api.Project

public class KotlinLibraryPlugin : BuildConventionPlugin() {
    override fun apply(target: Project) {
        super.apply(target)

        target.pluginManager.apply(PluginIds.javaLibrary)
        target.pluginManager.apply(PluginIds.kotlinJvm)
        target.pluginManager.apply(ProtonCoveragePlugin::class.java)

        val ext = target.createProtonExt<KotlinLibraryExtension>()
        target.applyJavaConvention()
        target.applyKotlinConvention(ext)
    }
}
