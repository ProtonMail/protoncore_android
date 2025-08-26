package me.proton.core.featureflag.domain

public fun interface FeatureFlagOverrider {
    public fun getOverrideOrNull(key: String): Boolean?
}
