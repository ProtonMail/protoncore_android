package me.proton.core.plan.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.plan.domain.IsDynamicPlanAdjustedPriceEnabled
import javax.inject.Inject

class IsDynamicPlanAdjustedPriceEnabledImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureFlagManager: FeatureFlagManager
) : IsDynamicPlanAdjustedPriceEnabled {

    override fun invoke(userId: UserId?): Boolean {
        return isLocalEnabled() && isRemoteEnabled(userId)
    }

    override fun isLocalEnabled(): Boolean {
        return context.resources.getBoolean(R.bool.core_feature_dynamic_plan_adjusted_prices_enabled)
    }

    @OptIn(ExperimentalProtonFeatureFlag::class)
    override fun isRemoteEnabled(userId: UserId?): Boolean {
        return featureFlagManager.getValue(userId, featureId)
    }

    companion object {
        val featureId = FeatureId("DynamicPlanAdjustedPrice")
    }
}
