package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.LoginSsoChangePasswordTotal.ChangePasswordStatus
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Input Password on subsequent SSO login.")
@SchemaId("https://proton.me/android_core_auth_sso_inputPassword_total_v1.schema.json")
public data class LoginSsoInputPasswordTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(result.toStatus())

    public constructor(status: InputPasswordStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData(
        val status: InputPasswordStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class InputPasswordStatus {
        unlockSuccess,

        noKeySalt,
        noPrimaryKey,
        invalidPassphrase,

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

        fun <R> Result<R>.toStatus(): InputPasswordStatus =
            when (exceptionOrNull()) {
                null -> InputPasswordStatus.unlockSuccess
                else -> toHttpApiStatus().toApiStatus()
            }

        private fun HttpApiStatus.toApiStatus(): InputPasswordStatus = when (this) {
            HttpApiStatus.http1xx -> InputPasswordStatus.http1xx
            HttpApiStatus.http2xx -> InputPasswordStatus.http2xx
            HttpApiStatus.http3xx -> InputPasswordStatus.http3xx
            HttpApiStatus.http4xx -> InputPasswordStatus.http4xx
            HttpApiStatus.http5xx -> InputPasswordStatus.http5xx
            HttpApiStatus.connectionError -> InputPasswordStatus.connectionError
            HttpApiStatus.notConnected -> InputPasswordStatus.notConnected
            HttpApiStatus.parseError -> InputPasswordStatus.parseError
            HttpApiStatus.sslError -> InputPasswordStatus.sslError
            HttpApiStatus.cancellation -> InputPasswordStatus.cancellation
            HttpApiStatus.unknown -> InputPasswordStatus.unknown
        }
    }
}
