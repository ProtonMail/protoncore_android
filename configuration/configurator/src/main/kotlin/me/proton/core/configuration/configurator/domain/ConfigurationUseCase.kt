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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.configurator.entity.Configuration
import kotlin.reflect.KClass

typealias ConfigFieldSet = Set<ConfigurationUseCase.ConfigField>

open class ConfigurationUseCase(
    private val contentResolverConfigManager: ContentResolverConfigManager,
    private val configClass: KClass<*>,
    private val defaultConfigValueMapper: (ConfigFieldSet, Map<String, Any?>) -> ConfigFieldSet,
    private val supportedContractFieldSet: ConfigFieldSet,
) : Configuration {

    data class ConfigField(
        val name: String,
        val isAdvanced: Boolean = true,
        val isPreserved: Boolean = false,
        val value: Any? = "",
        val isSearchable: Boolean = false,
        val fetcher: (suspend (String) -> Any)? = null,
    )

    private val _configState = MutableStateFlow(supportedContractFieldSet)

    var configState: StateFlow<ConfigFieldSet> = _configState.asStateFlow()

    fun setDefaultConfigurationFields() {
        val newValueMap = _configState.value.filter { it.isPreserved }.associate { it.name to it.value }
        _configState.value = defaultConfigValueMapper(_configState.value, newValueMap).toSet()
    }

    override suspend fun fetchConfig() {
        val resolvedConfigMap = contentResolverConfigManager.queryAtClassPath(configClass)

        _configState.value =
            if (resolvedConfigMap == null) {
                supportedContractFieldSet
            } else {
                defaultConfigValueMapper(_configState.value, resolvedConfigMap)
            }
    }

    override suspend fun saveConfig(advanced: Boolean) {
        val stateToInsert = _configState.value.filter { if (advanced) true else !it.isAdvanced }
        val mapToInsert = stateToInsert.associate { it.name to it.value }
        contentResolverConfigManager.insertConfigFieldMapAtClassPath(mapToInsert, configClass)
    }

    override suspend fun updateConfigField(key: String, newValue: Any) {
        _configState.value = _configState.value.withUpdatedValues(key, newValue)
    }

    override suspend fun fetchConfigField(key: String) {
        updateConfigField(
            key,
            supportedContractFieldSet.firstOrNull { it.name == key }?.fetcher?.let { it(key) }.toString()
        )
    }

    private fun ConfigFieldSet.withUpdatedValues(key: String, newValue: Any): ConfigFieldSet =
        map { if (it.name == key) it.copy(value = newValue) else it }.toSet()
}
