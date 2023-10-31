/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.network.domain.ResponseCodes.AUTH_SWITCH_TO_SRP
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.hasProtonErrorCode
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Obtaining the SSO challenge token.")
@SchemaId("https://proton.me/android_core_login_sso_obtainChallengeToken_total_v2.schema.json")
public data class LoginObtainSsoChallengeTokenTotal(
    override val Labels: StatusLabels,
    @Required override val Value: Long = 1,
) : ObservabilityData() {
    public constructor(status: Status) : this(StatusLabels(status))

    public constructor(result: Result<*>) : this(result.toStatus())

    @Serializable
    public data class StatusLabels constructor(
        @get:Schema(required = true)
        val status: Status
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class Status {
        http1xx,
        http2xx,
        http3xx,
        http4xx,
        http5xx,
        connectionError,
        notConnected,
        parseError,
        sslError,
        ssoDomainNotFound,
        cancellation,
        unknown
    }
}

private fun Result<*>.toStatus(): LoginObtainSsoChallengeTokenTotal.Status = when {
    hasProtonErrorCode(AUTH_SWITCH_TO_SRP) -> LoginObtainSsoChallengeTokenTotal.Status.ssoDomainNotFound
    else -> toHttpApiStatus().toStatus()
}

private fun HttpApiStatus.toStatus(): LoginObtainSsoChallengeTokenTotal.Status = when (this) {
    HttpApiStatus.http1xx -> LoginObtainSsoChallengeTokenTotal.Status.http1xx
    HttpApiStatus.http2xx -> LoginObtainSsoChallengeTokenTotal.Status.http2xx
    HttpApiStatus.http3xx -> LoginObtainSsoChallengeTokenTotal.Status.http3xx
    HttpApiStatus.http4xx -> LoginObtainSsoChallengeTokenTotal.Status.http4xx
    HttpApiStatus.http5xx -> LoginObtainSsoChallengeTokenTotal.Status.http5xx
    HttpApiStatus.connectionError -> LoginObtainSsoChallengeTokenTotal.Status.connectionError
    HttpApiStatus.notConnected -> LoginObtainSsoChallengeTokenTotal.Status.notConnected
    HttpApiStatus.parseError -> LoginObtainSsoChallengeTokenTotal.Status.parseError
    HttpApiStatus.sslError -> LoginObtainSsoChallengeTokenTotal.Status.sslError
    HttpApiStatus.cancellation -> LoginObtainSsoChallengeTokenTotal.Status.cancellation
    HttpApiStatus.unknown -> LoginObtainSsoChallengeTokenTotal.Status.unknown
}
