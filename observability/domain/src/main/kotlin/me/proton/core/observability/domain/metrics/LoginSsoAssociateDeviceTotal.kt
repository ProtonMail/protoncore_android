package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.network.domain.ResponseCodes.AUTH_DEVICE_NOT_ACTIVE
import me.proton.core.network.domain.ResponseCodes.AUTH_DEVICE_NOT_FOUND
import me.proton.core.network.domain.ResponseCodes.AUTH_DEVICE_REJECTED
import me.proton.core.network.domain.ResponseCodes.AUTH_DEVICE_TOKEN_INVALID
import me.proton.core.network.domain.ResponseCodes.NOT_ALLOWED
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.hasProtonErrorCode
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Associate SSO auth device.")
@SchemaId("https://proton.me/android_core_auth_sso_associateDevice_total_v1.schema.json")
public data class LoginSsoAssociateDeviceTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(result.toStatus())

    public constructor(status: AssociateStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData(
        val status: AssociateStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class AssociateStatus {
        notActive,
        notFound,
        invalidToken,
        rejected,
        sessionAlreadyAssociated,
        success,

        http1xx,
        http2xx,
        http3xx,
        http4xx,
        http5xx,
        connectionError,
        notConnected,
        parseError,
        sslError,
        cancellation,
        unknown
    }

    internal companion object {

        fun <R> Result<R>.toStatus(): AssociateStatus = when (exceptionOrNull()) {
            null -> AssociateStatus.success
            else -> when {
                hasProtonErrorCode(AUTH_DEVICE_NOT_FOUND) -> AssociateStatus.notFound
                hasProtonErrorCode(AUTH_DEVICE_NOT_ACTIVE) -> AssociateStatus.notActive
                hasProtonErrorCode(AUTH_DEVICE_REJECTED) -> AssociateStatus.rejected
                hasProtonErrorCode(AUTH_DEVICE_TOKEN_INVALID) -> AssociateStatus.invalidToken
                hasProtonErrorCode(NOT_ALLOWED) -> AssociateStatus.sessionAlreadyAssociated
                else -> toHttpApiStatus().toApiStatus()
            }
        }

        private fun HttpApiStatus.toApiStatus(): AssociateStatus = when (this) {
            HttpApiStatus.http1xx -> AssociateStatus.http1xx
            HttpApiStatus.http2xx -> AssociateStatus.http2xx
            HttpApiStatus.http3xx -> AssociateStatus.http3xx
            HttpApiStatus.http4xx -> AssociateStatus.http4xx
            HttpApiStatus.http5xx -> AssociateStatus.http5xx
            HttpApiStatus.connectionError -> AssociateStatus.connectionError
            HttpApiStatus.notConnected -> AssociateStatus.notConnected
            HttpApiStatus.parseError -> AssociateStatus.parseError
            HttpApiStatus.sslError -> AssociateStatus.sslError
            HttpApiStatus.cancellation -> AssociateStatus.cancellation
            HttpApiStatus.unknown -> AssociateStatus.unknown
        }
    }
}
