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

package me.proton.core.gradle.plugin.android

import me.proton.core.gradle.convention.android.AndroidConventionSettings
import me.proton.core.gradle.plugin.AndroidLibraryExtension
import me.proton.core.gradle.plugin.PluginIds
import me.proton.core.gradle.plugin.createProtonExt
import org.gradle.api.Project

public class AndroidLibraryPlugin : BaseAndroidPlugin<AndroidLibraryExtension>() {
    override val androidPluginId: String get() = PluginIds.androidLibrary

    override fun createConventionSettings(): AndroidConventionSettings =
        object : AndroidConventionSettings {
            override var vectorDrawablesSupport: Boolean = false
            override var viewBinding: Boolean = false
        }

    override fun createPluginExtension(target: Project): AndroidLibraryExtension = target.createProtonExt()
}
