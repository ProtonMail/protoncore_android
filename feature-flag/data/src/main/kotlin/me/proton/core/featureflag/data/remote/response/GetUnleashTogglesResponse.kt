package me.proton.core.featureflag.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.remote.resource.UnleashToggleResource
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope

@Serializable
public data class GetUnleashTogglesResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("toggles")
    val toggles: List<UnleashToggleResource>
)

internal fun UnleashToggleResource.toFeatureFlag(userId: UserId?) = FeatureFlag(
    userId = userId,
    featureId = FeatureId(name),
    scope = Scope.Unleash,
    defaultValue = false,
    value = true
)
