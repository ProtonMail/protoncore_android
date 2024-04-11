/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.configuration.configurator.domain

import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.configuration.configurator.entity.AppConfig
import me.proton.core.configuration.configurator.extension.getProxyToken
import me.proton.core.configuration.entity.ConfigContract
import me.proton.core.configuration.extension.primitiveFieldMap
import me.proton.core.test.quark.v2.QuarkCommand
import javax.inject.Inject

class EnvironmentConfigurationUseCase @Inject constructor(
    quark: QuarkCommand,
    contentResolverConfigManager: ContentResolverConfigManager,
    appConfig: AppConfig
) : ConfigurationUseCase(
    contentResolverConfigManager = contentResolverConfigManager,
    configClass = EnvironmentConfiguration::class,
    supportedContractFieldSet = setOf(
        ConfigField(
            ConfigContract::host.name,
            isAdvanced = false,
            isPreserved = true,
            value = defaultConfig.host,
            isSearchable = true
        ),
        ConfigField(ConfigContract::proxyToken.name, isAdvanced = false, isPreserved = true) {
            quark.baseUrl(appConfig.proxyUrl).getProxyToken() ?: error("Could not obtain proxy token")
        },
        ConfigField(ConfigContract::apiPrefix.name, isPreserved = true, value = defaultConfig.apiPrefix),
        ConfigField(ConfigContract::apiHost.name, value = defaultConfig.apiHost),
        ConfigField(ConfigContract::baseUrl.name, value = defaultConfig.baseUrl),
        ConfigField(ConfigContract::hv3Host.name, value = defaultConfig.hv3Host),
        ConfigField(ConfigContract::hv3Url.name, value = defaultConfig.hv3Url),
        ConfigField(ConfigContract::useDefaultPins.name, value = false),
    ),
    defaultConfigValueMapper = ::configFieldMapper
) {
    companion object {
        fun configFieldMapper(configFieldSet: ConfigFieldSet, configFieldMap: Map<String, Any?>): ConfigFieldSet {
            val config = EnvironmentConfiguration.fromMap(configFieldMap).primitiveFieldMap
            return configFieldSet.map { it.copy(value = config[it.name]) }.toSet()
        }

        val defaultConfig: EnvironmentConfiguration =
            EnvironmentConfiguration.fromMap(mapOf("host" to "proton.black"))
    }
}
