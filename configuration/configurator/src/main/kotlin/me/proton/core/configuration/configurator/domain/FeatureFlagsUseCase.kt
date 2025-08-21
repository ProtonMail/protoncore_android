package me.proton.core.configuration.configurator.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.configuration.configurator.entity.FeatureFlagConfiguration
import kotlin.reflect.KClass
import kotlinx.serialization.encodeToString


typealias FeatureFlagsSet = Set<FeatureFlagsUseCase.FeatureFlagEntity>

open class FeatureFlagsUseCase(
    private val contentResolverConfigManager: ContentResolverConfigManager,
    private val featureFlagsDataStore: DataStore<Preferences>,
    internal val configClass: KClass<*>,
    featureFlagsSet: FeatureFlagsSet
) : FeatureFlagConfiguration {

    @Serializable
    data class FeatureFlagEntity(
        val name: String,
        val project: String,
        val unleashValue: Boolean,
        val configuratorValue: Boolean? = null,
        val description: String?,
        val type: String,
        val strategiesJson: String? = null,
        val stale: Boolean = false,
        val impressionData: Boolean = false
    ) {
        val effectiveValue: Boolean get() = configuratorValue ?: unleashValue
        val isOverridden: Boolean get() = configuratorValue != unleashValue
    }

    private var _configState = MutableStateFlow(featureFlagsSet)

    var configState: StateFlow<FeatureFlagsSet> = _configState.asStateFlow()

    suspend fun initializeWithPersistedFlagsFromDataStore() {
        val persistedFlags = loadFlagsFromDataStore()
        if (persistedFlags.isNotEmpty()) {
            _configState.value = persistedFlags
        }
    }

    private suspend fun loadFlagsFromDataStore(): Set<FeatureFlagEntity> {
        return featureFlagsDataStore.data.first().let { preferences ->
            val customFlagsJson = preferences[stringPreferencesKey("feature_flags_set")] ?: return emptySet()
            try {
                Json.decodeFromString<List<FeatureFlagEntity>>(customFlagsJson).toSet()
            } catch (e: Exception) {
                emptySet()
            }
        }
    }

    private suspend fun saveFlagsToDataStore() {
        val flags = _configState.value.toList()
        featureFlagsDataStore.edit { preferences ->
            preferences[stringPreferencesKey("feature_flags_set")] =
                Json.encodeToString(flags)
        }
    }

    suspend fun updateFeatureFlags(flags: Set<FeatureFlagEntity>) = runCatching {
        _configState.value = flags
        saveConfig()
    }

    suspend fun resetToUnleashState() = runCatching {
        _configState.value = _configState.value.resetToUnleash()
        saveConfig()
    }

    override suspend fun syncConfigWithContentResolver() {
        val mapToInsert = _configState.value.associate { it.name to it.effectiveValue }
        if (mapToInsert.isNotEmpty()) {
            contentResolverConfigManager.insertConfigFieldMapAtClassPath(
                mapToInsert,
                EnvironmentConfiguration::class
            )
        }
    }

    override suspend fun saveConfig() {
        saveFlagsToDataStore()

        val mapToInsert = _configState.value.associate { it.name to it.effectiveValue }
        if (mapToInsert.isNotEmpty()) {
            contentResolverConfigManager.insertConfigFieldMapAtClassPath(
                mapToInsert,
                EnvironmentConfiguration::class
            )
        }
    }

    override suspend fun updateConfigField(key: String, newValue: Boolean) {
        _configState.value = _configState.value.withUpdatedValues(key, newValue)
        saveConfig()
    }

    override suspend fun fetchConfigField(key: String) {
        _configState.value.firstOrNull { it.name == key }?.effectiveValue?.let {
            updateConfigField(
                key,
                it
            )
        }
    }

    override suspend fun getConfigField(key: String): FeatureFlagEntity? {
        return _configState.value.firstOrNull { it.name == key }
    }

    private fun FeatureFlagsSet.withUpdatedValues(key: String, newValue: Boolean): FeatureFlagsSet =
        map { if (it.name == key) it.copy(configuratorValue = newValue) else it }.toSet()

    private fun FeatureFlagsSet.resetToUnleash(): FeatureFlagsSet =
        map {
            if (it.type != CUSTOM_TYPE) {
                it.copy(configuratorValue = it.unleashValue)
            } else {
                it
            }
        }.toSet()
}

const val CUSTOM_TYPE = "custom"
