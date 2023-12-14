package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.EmptyStatusLabels

@Serializable
@Schema(description = "Forced log-out after a failed token refresh.")
@SchemaId("https://proton.me/android_core_session_forcedLogout_total_v1.schema.json")
public data class SessionForcedLogoutTotalV1(
    override val Labels: EmptyStatusLabels,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    public constructor() : this(EmptyStatusLabels())
}
