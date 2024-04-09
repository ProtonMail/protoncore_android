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

package me.proton.core.configuration

import android.os.Bundle
import me.proton.core.configuration.entity.ConfigContract
import me.proton.core.configuration.entity.EnvironmentConfigFieldProvider
import me.proton.core.configuration.provider.BundleConfigFieldProvider
import me.proton.core.configuration.provider.MapConfigFieldProvider
import me.proton.core.configuration.provider.StaticClassConfigFieldProvider

private const val DEFAULT_CONFIG_CLASS: String = "me.proton.core.configuration.EnvironmentConfigurationDefaults"

public data class EnvironmentConfiguration(
    val configFieldProvider: EnvironmentConfigFieldProvider
) : ConfigContract {
    override val host: String = configFieldProvider.getString(::host.name) ?: ""
    override val proxyToken: String = configFieldProvider.getString(::proxyToken.name) ?: ""
    override val apiPrefix: String = configFieldProvider.getString(::apiPrefix.name) ?: "api"
    override val apiHost: String = configFieldProvider.getString(::apiHost.name) ?: "$apiPrefix.$host"
    override val baseUrl: String = configFieldProvider.getString(::baseUrl.name) ?: "https://$apiHost"
    override val hv3Host: String = configFieldProvider.getString(::hv3Host.name) ?: "verify.$host"
    override val hv3Url: String = configFieldProvider.getString(::hv3Url.name) ?: "https://$hv3Host"
    override val useDefaultPins: Boolean = configFieldProvider.getBoolean(::useDefaultPins.name) ?: (host == "proton.me")

    public companion object {
        public fun fromMap(configMap: Map<String, Any?>): EnvironmentConfiguration =
            EnvironmentConfiguration(MapConfigFieldProvider(configMap))

        public fun fromClass(className: String = DEFAULT_CONFIG_CLASS): EnvironmentConfiguration =
            EnvironmentConfiguration(StaticClassConfigFieldProvider(className))

        public fun fromBundle(bundle: Bundle): EnvironmentConfiguration =
            EnvironmentConfiguration(BundleConfigFieldProvider(bundle))
    }
}
