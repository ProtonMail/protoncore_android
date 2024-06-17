/*
 * Copyright (c) 2024 Proton Technologies AG
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
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNAUTHORIZED
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.ResponseCodes.PASSWORD_WRONG
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.ConfirmPasswordSubmissionTotal.Status
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.hasProtonErrorCode
import me.proton.core.observability.domain.metrics.common.isHttpError
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Submit the password confirmation to BE.")
@SchemaId("https://proton.me/android_core_confirmPassword_submission_total_v1.schema.json")
public class ConfirmPasswordSubmissionTotal(
    override val Labels: StatusLabels,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    public constructor(status: Status, type: SecondFactorProofType) : this(StatusLabels(status, type))
    public constructor(result: Result<*>, type: SecondFactorProofType) : this(result.toStatus(), type)

    @Serializable
    public data class StatusLabels(
        @get:Schema(required = true)
        val status: Status,
        @get:Schema(required = true)
        val type: SecondFactorProofType
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class Status {
        http1xx,
        http2xx,
        http3xx,
        http4xx,
        http401PasswordWrong,
        http422PasswordWrong,
        http5xx,
        connectionError,
        notConnected,
        parseError,
        sslError,
        cancellation,
        invalidServerAuthentication,
        unknown
    }

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class SecondFactorProofType {
        none,
        securityKey,
        totp
    }
}

private fun <R> Result<R>.toStatus(): Status = when {
    isHttpError(HTTP_UNAUTHORIZED) -> when {
        hasProtonErrorCode(PASSWORD_WRONG) -> Status.http401PasswordWrong
        else -> Status.http4xx
    }

    isHttpError(HTTP_UNPROCESSABLE) -> when {
        hasProtonErrorCode(PASSWORD_WRONG) -> Status.http422PasswordWrong
        else -> Status.http4xx
    }

    else -> toHttpApiStatus().toApiStatus()
}

private fun HttpApiStatus.toApiStatus(): Status = when (this) {
    HttpApiStatus.http1xx -> Status.http1xx
    HttpApiStatus.http2xx -> Status.http2xx
    HttpApiStatus.http3xx -> Status.http3xx
    HttpApiStatus.http4xx -> Status.http4xx
    HttpApiStatus.http5xx -> Status.http5xx
    HttpApiStatus.connectionError -> Status.connectionError
    HttpApiStatus.notConnected -> Status.notConnected
    HttpApiStatus.parseError -> Status.parseError
    HttpApiStatus.sslError -> Status.sslError
    HttpApiStatus.cancellation -> Status.cancellation
    HttpApiStatus.unknown -> Status.unknown
}
