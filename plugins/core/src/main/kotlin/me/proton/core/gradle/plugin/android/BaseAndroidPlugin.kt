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
import me.proton.core.gradle.convention.kotlin.KotlinConventionSettings
import me.proton.core.gradle.plugin.BuildConventionPlugin
import me.proton.core.gradle.plugin.PluginIds
import me.proton.core.gradle.plugin.applyAndroidConvention
import me.proton.core.gradle.plugin.applyKotlinConvention
import org.gradle.api.Project

public abstract class BaseAndroidPlugin<E> : BuildConventionPlugin() where E : KotlinConventionSettings {
    protected abstract val androidPluginId: String
    protected abstract fun createConventionSettings(): AndroidConventionSettings
    protected abstract fun createPluginExtension(target: Project): E

    override fun apply(target: Project) {
        super.apply(target)
        val ext = createPluginExtension(target)

        target.pluginManager.apply(androidPluginId)
        target.pluginManager.apply(PluginIds.kotlinAndroid)

        target.applyAndroidConvention(createConventionSettings())
        target.applyKotlinConvention(ext)
    }
}
