package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.HttpStatusLabels

@Serializable
@Schema(description = "Fetching the domains when converting External account to Internal.")
@SchemaId("https://proton.me/android_core_login_eaToIafetchDomains_total_v1.schema.json")
public data class LoginEaToIaFetchDomainsTotalV1(
    override val Labels: HttpStatusLabels,
    @Required override val Value: Long = 1
) : ObservabilityData() {
    public constructor(status: HttpApiStatus) : this(HttpStatusLabels(status))
}
