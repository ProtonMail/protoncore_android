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

package me.proton.core.gradle.plugin.dagger

import me.proton.core.gradle.plugin.BuildConventionPlugin
import me.proton.core.gradle.plugin.DaggerExtension
import me.proton.core.gradle.plugin.PluginIds
import me.proton.core.gradle.plugin.applyDaggerConvention
import me.proton.core.gradle.plugin.createProtonExt
import org.gradle.api.Project

public class DaggerPlugin : BuildConventionPlugin() {

    override fun onApplyPlugins(target: Project) {
        target.pluginManager.apply(PluginIds.kapt)
        target.pluginManager.apply(PluginIds.hilt)
    }

    override fun onPluginsApplied(target: Project) {
        val ext = target.createProtonExt<DaggerExtension>("protonDagger")
        target.applyDaggerConvention(ext)
    }
}
