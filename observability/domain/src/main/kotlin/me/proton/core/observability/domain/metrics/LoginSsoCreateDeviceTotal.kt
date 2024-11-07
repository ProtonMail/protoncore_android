package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Create SSO auth device.")
@SchemaId("https://proton.me/android_core_auth_sso_createDevice_total_v1.schema.json")
public data class LoginSsoCreateDeviceTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(result.toHttpApiStatus().toApiStatus())

    public constructor(status: ApiStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData(
        val status: ApiStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ApiStatus {
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
        internal fun HttpApiStatus.toApiStatus(): ApiStatus = when (this) {
            HttpApiStatus.http1xx -> ApiStatus.http1xx
            HttpApiStatus.http2xx -> ApiStatus.http2xx
            HttpApiStatus.http3xx -> ApiStatus.http3xx
            HttpApiStatus.http4xx -> ApiStatus.http4xx
            HttpApiStatus.http5xx -> ApiStatus.http5xx
            HttpApiStatus.connectionError -> ApiStatus.connectionError
            HttpApiStatus.notConnected -> ApiStatus.notConnected
            HttpApiStatus.parseError -> ApiStatus.parseError
            HttpApiStatus.sslError -> ApiStatus.sslError
            HttpApiStatus.cancellation -> ApiStatus.cancellation
            HttpApiStatus.unknown -> ApiStatus.unknown
        }
    }
}
