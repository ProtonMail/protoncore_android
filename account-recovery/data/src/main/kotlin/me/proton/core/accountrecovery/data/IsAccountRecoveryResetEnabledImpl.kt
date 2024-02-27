package me.proton.core.accountrecovery.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.accountrecovery.domain.IsAccountRecoveryResetEnabled
import me.proton.core.featureflag.data.IsFeatureFlagEnabledImpl
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import javax.inject.Inject

public class IsAccountRecoveryResetEnabledImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureFlagManager: FeatureFlagManager
) : IsAccountRecoveryResetEnabled, IsFeatureFlagEnabledImpl(
    context = context,
    featureFlagManager = featureFlagManager,
    featureId = featureId,
    localFlagId = R.bool.core_feature_account_recovery_reset_enabled
) {
    internal companion object {
        val featureId = FeatureId("SignedInAccountRecoveryReset")
    }
}
