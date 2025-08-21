package me.proton.core.configuration

import me.proton.core.configuration.entity.EnvironmentConfigFieldProvider
import me.proton.core.configuration.provider.MapConfigFieldProvider

public data class FeatureFlagsConfiguration(
    val configFieldProvider: EnvironmentConfigFieldProvider
) {
    public companion object {
        public fun fromMap(configMap: Map<String, Any?>): FeatureFlagsConfiguration =
            FeatureFlagsConfiguration(MapConfigFieldProvider(configMap))
    }
}

public fun FeatureFlagsConfiguration.getOverrideOrNull(key: String): Boolean? =
    runCatching { configFieldProvider.getBoolean(key) }.getOrNull()