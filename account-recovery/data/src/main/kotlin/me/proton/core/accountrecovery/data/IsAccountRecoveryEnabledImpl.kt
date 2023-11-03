package me.proton.core.accountrecovery.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.accountrecovery.domain.IsAccountRecoveryEnabled
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import javax.inject.Inject

public class IsAccountRecoveryEnabledImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureFlagManager: FeatureFlagManager
) : IsAccountRecoveryEnabled {

    override fun invoke(userId: UserId?): Boolean {
        return isLocalEnabled() && isRemoteEnabled(userId)
    }

    private fun isLocalEnabled(): Boolean {
        return context.resources.getBoolean(R.bool.core_feature_account_recovery_enabled)
    }

    @OptIn(ExperimentalProtonFeatureFlag::class)
    private fun isRemoteEnabled(userId: UserId?): Boolean {
        return featureFlagManager.getValue(userId, featureId)
    }

    internal companion object {
        val featureId = FeatureId("SignedInAccountRecovery")
    }
}
