package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId

@Serializable
@Schema(description = "Load Organization data for SSO.")
@SchemaId("https://proton.me/android_core_auth_sso_loadOrg_total_v1.schema.json")
public data class LoginSsoLoadOrganizationTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(result.toStatus())

    public constructor(status: LoadStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData(
        val status: LoadStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class LoadStatus {
        success,
        failure,
    }

    internal companion object {
        fun <R> Result<R>.toStatus(): LoadStatus =
            when (exceptionOrNull()) {
                null -> LoadStatus.success
                else -> LoadStatus.failure
            }
    }
}
