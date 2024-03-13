package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Start an account recovery process.")
@SchemaId("https://proton.me/android_core_accountRecovery_start_total_v1.schema.json")
public data class AccountRecoveryStartTotal(
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

private fun HttpApiStatus.toApiStatus(): AccountRecoveryStartTotal.ApiStatus = when (this) {
    HttpApiStatus.http1xx -> AccountRecoveryStartTotal.ApiStatus.http1xx
    HttpApiStatus.http2xx -> AccountRecoveryStartTotal.ApiStatus.http2xx
    HttpApiStatus.http3xx -> AccountRecoveryStartTotal.ApiStatus.http3xx
    HttpApiStatus.http4xx -> AccountRecoveryStartTotal.ApiStatus.http4xx
    HttpApiStatus.http5xx -> AccountRecoveryStartTotal.ApiStatus.http5xx
    HttpApiStatus.connectionError -> AccountRecoveryStartTotal.ApiStatus.connectionError
    HttpApiStatus.notConnected -> AccountRecoveryStartTotal.ApiStatus.notConnected
    HttpApiStatus.parseError -> AccountRecoveryStartTotal.ApiStatus.parseError
    HttpApiStatus.sslError -> AccountRecoveryStartTotal.ApiStatus.sslError
    HttpApiStatus.cancellation -> AccountRecoveryStartTotal.ApiStatus.cancellation
    HttpApiStatus.unknown -> AccountRecoveryStartTotal.ApiStatus.unknown
}
