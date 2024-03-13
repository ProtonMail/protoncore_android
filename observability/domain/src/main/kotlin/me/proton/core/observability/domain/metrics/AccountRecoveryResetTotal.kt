package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Reset password during account recovery process (state = Insecure).")
@SchemaId("https://proton.me/android_core_accountRecovery_reset_total_v1.schema.json")
public data class AccountRecoveryResetTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(result.toHttpApiStatus().toApiStatus())

    public constructor(status: ApiStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData constructor(
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
}

private fun HttpApiStatus.toApiStatus(): AccountRecoveryResetTotal.ApiStatus = when (this) {
    HttpApiStatus.http1xx -> AccountRecoveryResetTotal.ApiStatus.http1xx
    HttpApiStatus.http2xx -> AccountRecoveryResetTotal.ApiStatus.http2xx
    HttpApiStatus.http3xx -> AccountRecoveryResetTotal.ApiStatus.http3xx
    HttpApiStatus.http4xx -> AccountRecoveryResetTotal.ApiStatus.http4xx
    HttpApiStatus.http5xx -> AccountRecoveryResetTotal.ApiStatus.http5xx
    HttpApiStatus.connectionError -> AccountRecoveryResetTotal.ApiStatus.connectionError
    HttpApiStatus.notConnected -> AccountRecoveryResetTotal.ApiStatus.notConnected
    HttpApiStatus.parseError -> AccountRecoveryResetTotal.ApiStatus.parseError
    HttpApiStatus.sslError -> AccountRecoveryResetTotal.ApiStatus.sslError
    HttpApiStatus.cancellation -> AccountRecoveryResetTotal.ApiStatus.cancellation
    HttpApiStatus.unknown -> AccountRecoveryResetTotal.ApiStatus.unknown
}
