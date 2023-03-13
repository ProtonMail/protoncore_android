package me.proton.core.observability.domain.metrics.common

import kotlinx.serialization.Serializable

@Serializable
public data class UserCheckLabels(val status: UserCheckStatus)
