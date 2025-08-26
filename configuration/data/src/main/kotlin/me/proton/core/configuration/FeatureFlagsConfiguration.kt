package me.proton.core.configuration

import me.proton.core.configuration.entity.EnvironmentConfigFieldProvider
import me.proton.core.configuration.provider.MapConfigFieldProvider
import me.proton.core.featureflag.domain.FeatureFlagOverrider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class FeatureFlagsConfiguration @Inject constructor(
    private val configFieldProvider: EnvironmentConfigFieldProvider
) : FeatureFlagOverrider {

    public companion object {
        public fun fromMap(configMap: Map<String, Any?>): FeatureFlagsConfiguration =
            FeatureFlagsConfiguration(MapConfigFieldProvider(configMap))
    }

    override fun getOverrideOrNull(key: String): Boolean? = runCatching {
        configFieldProvider.getBoolean(key)
    }.getOrNull()
}
