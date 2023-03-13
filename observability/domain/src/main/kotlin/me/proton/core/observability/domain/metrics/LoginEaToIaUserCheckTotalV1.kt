package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.UserCheckLabels
import me.proton.core.observability.domain.metrics.common.UserCheckStatus

@Serializable
@Schema(description = "User checks just after converting External account to Internal.")
@SchemaId("https://proton.me/android_core_login_eaToIaUserCheck_total_v1.schema.json")
public data class LoginEaToIaUserCheckTotalV1(
    override val Labels: UserCheckLabels,
    @Required override val Value: Long = 1
) : ObservabilityData() {
    public constructor(status: UserCheckStatus) : this(UserCheckLabels(status))
}
