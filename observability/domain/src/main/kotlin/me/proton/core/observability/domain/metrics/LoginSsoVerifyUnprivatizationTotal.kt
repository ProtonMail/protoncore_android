package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId

@Serializable
@Schema(description = "Verify unprivatization on first SSO login.")
@SchemaId("https://proton.me/android_core_auth_sso_verifyUnprivatization_total_v1.schema.json")
public data class LoginSsoVerifyUnprivatizationTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(result.toStatus())

    public constructor(status: VerifyStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData(
        val status: VerifyStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class VerifyStatus {
        success,
        failure,

        failurePublicAddressKeysError,
        failureUnprivatizeStateError,
        failureVerificationError
    }

    internal companion object {
        fun <R> Result<R>.toStatus(): VerifyStatus =
            when (exceptionOrNull()) {
                null -> VerifyStatus.success
                else -> VerifyStatus.failure
            }
    }
}
