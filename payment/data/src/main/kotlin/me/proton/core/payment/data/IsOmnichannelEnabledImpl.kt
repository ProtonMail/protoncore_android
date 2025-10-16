package me.proton.core.payment.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.featureflag.data.IsFeatureFlagEnabledImpl
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.payment.data.R.bool.core_feature_omnichannel_client_enabled
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import javax.inject.Inject

private const val OMNICHANNEL_FEATURE_FLAG_IDENTIFIER: String = "OmnichannelClient"

public class IsOmnichannelEnabledImpl @Inject constructor(
    @ApplicationContext context: Context,
    featureFlagManager: FeatureFlagManager
) : IsOmnichannelEnabled, IsFeatureFlagEnabledImpl(
    context = context,
    featureFlagManager = featureFlagManager,
    featureId = FeatureId(OMNICHANNEL_FEATURE_FLAG_IDENTIFIER),
    localFlagId = core_feature_omnichannel_client_enabled
)
