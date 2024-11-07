package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Set Primary Keys on first SSO login.")
@SchemaId("https://proton.me/android_core_auth_sso_setupKeys_total_v1.schema.json")
public data class LoginSsoSetupPrimaryKeysTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(result.toStatus())

    public constructor(status: SetupPrimaryKeyStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData(
        val status: SetupPrimaryKeyStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class SetupPrimaryKeyStatus {
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

        fun <R> Result<R>.toStatus(): SetupPrimaryKeyStatus =
            when (exceptionOrNull()) {
                null -> SetupPrimaryKeyStatus.success
                else -> toHttpApiStatus().toApiStatus()
            }

        private fun HttpApiStatus.toApiStatus(): SetupPrimaryKeyStatus = when (this) {
            HttpApiStatus.http1xx -> SetupPrimaryKeyStatus.http1xx
            HttpApiStatus.http2xx -> SetupPrimaryKeyStatus.http2xx
            HttpApiStatus.http3xx -> SetupPrimaryKeyStatus.http3xx
            HttpApiStatus.http4xx -> SetupPrimaryKeyStatus.http4xx
            HttpApiStatus.http5xx -> SetupPrimaryKeyStatus.http5xx
            HttpApiStatus.connectionError -> SetupPrimaryKeyStatus.connectionError
            HttpApiStatus.notConnected -> SetupPrimaryKeyStatus.notConnected
            HttpApiStatus.parseError -> SetupPrimaryKeyStatus.parseError
            HttpApiStatus.sslError -> SetupPrimaryKeyStatus.sslError
            HttpApiStatus.cancellation -> SetupPrimaryKeyStatus.cancellation
            HttpApiStatus.unknown -> SetupPrimaryKeyStatus.unknown
        }
    }
}
