package me.proton.core.plan.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.featureflag.data.IsFeatureFlagEnabledImpl
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.plan.domain.IsDynamicPlanAdjustedPriceEnabled
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage
import javax.inject.Inject

@ExcludeFromCoverage
class IsDynamicPlanAdjustedPriceEnabledImpl @Inject constructor(
    @ApplicationContext context: Context,
    featureFlagManager: FeatureFlagManager
) : IsDynamicPlanAdjustedPriceEnabled, IsFeatureFlagEnabledImpl(
    context,
    featureFlagManager,
    FeatureId("DynamicPlanAdjustedPrice"),
    R.bool.core_feature_dynamic_plan_adjusted_prices_enabled
)
