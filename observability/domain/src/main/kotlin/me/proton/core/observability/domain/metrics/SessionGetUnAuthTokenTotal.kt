package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.HttpStatusLabels

@Serializable
@Schema(description = "Request an un-auth session token.")
@SchemaId("https://proton.me/android_core_session_getUnAuthToken_total_v2.schema.json")
public data class SessionGetUnAuthTokenTotal(
    override val Labels: HttpStatusLabels,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    public constructor(status: HttpApiStatus) : this(HttpStatusLabels(status))
}
