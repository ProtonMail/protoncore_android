package me.proton.core.eventmanager.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.IsCoreEventManagerEnabled
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import javax.inject.Inject

class IsCoreEventManagerEnabledImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureFlagManager: FeatureFlagManager
) : IsCoreEventManagerEnabled {

    override fun invoke(userId: UserId): Boolean {
        return isLocalEnabled() && !isRemoteDisabled(userId)
    }

    private fun isLocalEnabled(): Boolean {
        return context.resources.getBoolean(R.bool.core_feature_core_event_manager_enabled)
    }

    @OptIn(ExperimentalProtonFeatureFlag::class)
    private fun isRemoteDisabled(userId: UserId): Boolean {
        return featureFlagManager.getValue(userId, featureId)
    }

    companion object {
        val featureId = FeatureId("CoreEventManagerDisabled")
    }
}
