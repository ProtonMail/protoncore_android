package me.proton.core.usersettings.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import javax.inject.Inject

class ObserveFeatureFlag @Inject constructor(
    private val featureFlagManager: FeatureFlagManager,
) {
    operator fun invoke(feature: me.proton.core.usersettings.domain.FeatureFlags): Flow<FeatureFlag> =
        featureFlagManager.observeOrDefault(
            userId = null,
            featureId = feature.id,
            default = FeatureFlag.default(feature.id.id, feature.default)
        ).onStart {
            featureFlagManager.prefetch(userId = null, setOf(feature.id))
        }
}
