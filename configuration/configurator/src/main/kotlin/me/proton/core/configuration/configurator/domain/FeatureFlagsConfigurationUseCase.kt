package me.proton.core.configuration.configurator.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.EnvironmentConfiguration
import javax.inject.Inject

class FeatureFlagsConfigurationUseCase @Inject constructor(
    contentResolverConfigManager: ContentResolverConfigManager,
    featureFlagsDataStore: DataStore<Preferences>,
) : FeatureFlagsUseCase(
    contentResolverConfigManager = contentResolverConfigManager,
    featureFlagsDataStore = featureFlagsDataStore,
    configClass = EnvironmentConfiguration::class,
    featureFlagsSet = setOf(),
)
