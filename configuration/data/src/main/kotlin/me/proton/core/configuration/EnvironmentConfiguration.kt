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

import me.proton.core.configuration.entity.ConfigContract
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty

private const val DEFAULT_CONFIG_CLASS: String = "me.proton.core.configuration.EnvironmentConfigurationDefaults"

public data class EnvironmentConfiguration(
    val stringProvider: KFunction1<String, Any?>
) : ConfigContract {
    override val host: String = getString(::host) ?: ""
    override val proxyToken: String = getString(::proxyToken) ?: ""
    override val apiPrefix: String = getString(::apiPrefix) ?: "api"
    override val apiHost: String = getString(::apiHost) ?: "$apiPrefix.$host"
    override val baseUrl: String = getString(::baseUrl) ?: "https://$apiHost"
    override val hv3Host: String = getString(::hv3Host) ?: "verify.$host"
    override val hv3Url: String = getString(::hv3Url) ?: "https://$hv3Host"
    override val useDefaultPins: Boolean = getString(::useDefaultPins) ?: (host == "proton.me")

    private fun <T> getString(propertyName: KProperty<Any>): T = stringProvider(propertyName.name) as T

    public companion object {

        public fun fromMap(configMap: Map<String, Any?>): EnvironmentConfiguration =
            EnvironmentConfiguration(configMap::get)

        public fun fromClass(className: String = DEFAULT_CONFIG_CLASS): EnvironmentConfiguration =
            fromMap(getConfigDataMapFromClass(className))

        private fun getConfigDataMapFromClass(className: String) = try {
            val defaultsClass = Class.forName(className)
            val instance = defaultsClass.newInstance()

            defaultsClass
                .declaredFields
                .associate { property ->
                    property.isAccessible = true
                    property.name to property.get(instance)
                }
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(
                "Class not found: $className. Make sure environment configuration gradle plugin is enabled!",
                e
            )
        }
    }
}
