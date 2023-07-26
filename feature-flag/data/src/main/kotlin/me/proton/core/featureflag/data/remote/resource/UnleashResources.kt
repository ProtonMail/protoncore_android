package me.proton.core.featureflag.data.remote.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class UnleashToggleResource(
    @SerialName("name")
    val name: String,
    @SerialName("variant")
    val variant: UnleashVariantResource
)

@Serializable
public data class UnleashVariantResource(
    @SerialName("name")
    val name: String,
    @SerialName("enabled")
    val enabled: Boolean,
    @SerialName("payload")
    val payload: UnleashPayloadResource? = null
)

@Serializable
public data class UnleashPayloadResource(
    @SerialName("type")
    val type: String,
    @SerialName("value")
    val value: String
)
