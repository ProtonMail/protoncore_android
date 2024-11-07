package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Change Password on first SSO login.")
@SchemaId("https://proton.me/android_core_auth_sso_changePassword_total_v1.schema.json")
public data class LoginSsoChangePasswordTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(result.toStatus())

    public constructor(status: ChangePasswordStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData(
        val status: ChangePasswordStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ChangePasswordStatus {
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

        fun <R> Result<R>.toStatus(): ChangePasswordStatus =
            when (exceptionOrNull()) {
                null -> ChangePasswordStatus.success
                else -> toHttpApiStatus().toApiStatus()
            }

        private fun HttpApiStatus.toApiStatus(): ChangePasswordStatus = when (this) {
            HttpApiStatus.http1xx -> ChangePasswordStatus.http1xx
            HttpApiStatus.http2xx -> ChangePasswordStatus.http2xx
            HttpApiStatus.http3xx -> ChangePasswordStatus.http3xx
            HttpApiStatus.http4xx -> ChangePasswordStatus.http4xx
            HttpApiStatus.http5xx -> ChangePasswordStatus.http5xx
            HttpApiStatus.connectionError -> ChangePasswordStatus.connectionError
            HttpApiStatus.notConnected -> ChangePasswordStatus.notConnected
            HttpApiStatus.parseError -> ChangePasswordStatus.parseError
            HttpApiStatus.sslError -> ChangePasswordStatus.sslError
            HttpApiStatus.cancellation -> ChangePasswordStatus.cancellation
            HttpApiStatus.unknown -> ChangePasswordStatus.unknown
        }
    }
}
