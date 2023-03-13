package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.UnlockUserLabels
import me.proton.core.observability.domain.metrics.common.UnlockUserStatus

@Serializable
@Schema(description = "Unlocking the user just after converting External account to Internal.")
@SchemaId("https://proton.me/android_core_login_eaToIaUnlockUser_total_v1.schema.json")
public data class LoginEaToIaUnlockUserTotalV1(
    override val Labels: UnlockUserLabels,
    @Required override val Value: Long = 1
) : ObservabilityData() {
    public constructor(status: UnlockUserStatus) : this(UnlockUserLabels(status))
}
