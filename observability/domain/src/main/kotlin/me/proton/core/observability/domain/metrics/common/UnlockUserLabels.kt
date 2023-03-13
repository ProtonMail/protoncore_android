package me.proton.core.observability.domain.metrics.common

import kotlinx.serialization.Serializable

@Serializable
public data class UnlockUserLabels constructor(val status: UnlockUserStatus)
