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

public data class EnvironmentConfiguration(
    private val stringProvider: KFunction1<String, String?>
) : ConfigContract {
    override val host: String = stringProvider(::host.name) ?: ""
    override val proxyToken: String = stringProvider(::proxyToken.name) ?: ""
    override val apiPrefix: String = stringProvider(::apiPrefix.name) ?: "api"
    override val apiHost: String = stringProvider(::apiHost.name) ?: "$apiPrefix.$host"
    override val baseUrl: String = stringProvider(::baseUrl.name) ?: "https://$apiHost"
    override val hv3Host: String = stringProvider(::hv3Host.name) ?: "verify.$host"
    override val hv3Url: String = stringProvider(::hv3Url.name) ?: "https://$hv3Host"

    val useDefaultPins: Boolean get() = host == "proton.me"

    public companion object {

        private const val DEFAULT_CONFIG_CLASS: String = "me.proton.core.configuration.EnvironmentConfigurationDefaults"

        public fun fromMap(configMap: Map<String, Any?>): EnvironmentConfiguration =
            EnvironmentConfiguration(configMap::configField)

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

public inline fun <reified T> Map<String, Any?>.configField(key: String): T = this[key].let {
    require((it is String? || it is Boolean?) && it is T) {
        "Unexpected value type for property: $key. " +
            "Expected String? or Boolean?. Found ${it?.javaClass?.name}."
    }
    it
}
