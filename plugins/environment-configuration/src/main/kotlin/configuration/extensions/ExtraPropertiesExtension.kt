/*
 * Copyright (c) 2022 Proton Technologies AG
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

package configuration.extensions

import configuration.EnvironmentConfig
import configuration.EnvironmentConfigSettings
import org.gradle.api.plugins.ExtraPropertiesExtension

private const val PROP_SUFFIX = "ProtonEnvironmentConfig"

fun ExtraPropertiesExtension.getEnvironmentConfigurationByName(
    name: String,
    fallbackSettings: EnvironmentConfig = EnvironmentConfig()
): EnvironmentConfig =
    if (properties.contains(name + PROP_SUFFIX))
        this[name + PROP_SUFFIX] as? EnvironmentConfigSettings ?: fallbackSettings
    else
        fallbackSettings

fun ExtraPropertiesExtension.setEnvironmentConfigurationByName(
    name: String,
    config: EnvironmentConfig
) {
    this[name + PROP_SUFFIX] = config
}
